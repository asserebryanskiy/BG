package badgegenerator.fileloader;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.TextRegionEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Extracts fields from provided pdf by searching for excelReader headings
 * and saving parameters of containing them text (color, font size, font,
 * position on page).
 *
 * Created by andreyserebryanskiy on 06/12/2017.
 */
public class PdfFieldExtractor {
    private final Map<String, PdfField> fields;
    private final float pdfHeight;
    private final List<Float> widths;
    private final List<Float> xCoords;
    private final Set<String> trimmedFieldNames;
    private final Set<String> originalFieldNames;

    public PdfFieldExtractor(String pdfPath, Set<String> fieldNames)
            throws IOException {
        // initialize fields
        originalFieldNames = fieldNames;
        trimmedFieldNames = fieldNames.stream()
                .map(this::getRidOfSpaces)
                .collect(Collectors.toSet());
        fields = new HashMap<>(fieldNames.size());
        PdfDocument pdf = new PdfDocument(new PdfReader(pdfPath));
        pdfHeight = pdf.getFirstPage().getPageSize().getHeight();
        widths = new ArrayList<>(fieldNames.size());
        xCoords = new ArrayList<>(fieldNames.size());

        // process pdf
        Rectangle rect = pdf.getFirstPage().getMediaBox();
        ContentFilter contentFilter = new ContentFilter(rect);
        FilteredEventListener listener = new FilteredEventListener();
        listener.attachEventListener(new LocationTextExtractionStrategy(), contentFilter);
        new PdfCanvasProcessor(listener).processPageContent(pdf.getFirstPage());

        /*// builder error message if not all headings from excel were found in pdf
        if (!this.trimmedFieldNames.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Не удалось найти в pdf ");
            int size = this.trimmedFieldNames.size();
            if (size == 1) errorMessage.append("заголовок ");
            else           errorMessage.append("заголовки: ");
            for (String fieldName : trimmedFieldNames) {
                fieldName = getOriginalText(fieldName);
                errorMessage.append(fieldName).append(", ");
            }
            errorMessage.delete(errorMessage.length() - 2, errorMessage.length());
            throw new WrongHeadingsException(errorMessage.toString());
        }*/

        pdf.close();
        computeAlignment();
    }

    public Map<String, PdfField> getFields() {
        return fields;
    }

    /**********************************
     PRIVATE HELPER METHODS AND CLASSES
     *********************************/

    private String getRidOfSpaces(String str) {
        StringBuilder builder = new StringBuilder();
        String[] words = str.split("\\s");
        if (words.length == 1) return str.trim();
        for (String word : words) {
            builder.append(word);
        }
        return builder.toString().trim();
    }

    private void computeAlignment() {
        final float OFFSET = 1;  // acceptable difference in pixels
        int centerCounter = 0;
        int rightCounter = 0;
        int leftCounter = 0;
        for (int i = 1; i < fields.size(); i++) {
            float lxi = xCoords.get(i);                         // left x coord of i-th field
            float lxp = xCoords.get(i - 1);                         // left x coord of (i-1)th field
            if (lxi > lxp - OFFSET && lxi < lxp + OFFSET)
                leftCounter++;

            float rxi = xCoords.get(i) + widths.get(i);         // right x coord of i-th field
            float rxp = xCoords.get(i - 1) + widths.get(i - 1); // right x coord of (i-1)th field
            if (rxi > rxp - OFFSET && rxi < rxp + OFFSET)
                rightCounter++;

            float cxi = xCoords.get(i) + widths.get(i) / 2;         // center x coord of i-th field
            float cxp = xCoords.get(i - 1) + widths.get(i - 1) / 2; // center x coord of (i-1)th field
            if (cxi > cxp - OFFSET && cxi < cxp + OFFSET)
                centerCounter++;
        }

        int target = fields.size() - 1;
        String alignment;
        if      (rightCounter  == target) alignment = "RIGHT";
        else if (centerCounter == target) alignment = "CENTER";
        else if (leftCounter   == target) alignment = "LEFT";
        else {
            if (leftCounter >= centerCounter && leftCounter >= rightCounter) {
                alignment = "LEFT";
            } else if (centerCounter > rightCounter) {
                alignment = "CENTER";
            } else {
                alignment = "RIGHT";
            }
        }

        fields.values().forEach(f -> f.setAlignment(alignment));
    }

    private class ContentFilter extends TextRegionEventFilter {
        private List<String> accumulated;

        ContentFilter(Rectangle filterRect) {
            super(filterRect);
            accumulated = new ArrayList<>();
        }

        @Override
        public boolean accept(IEventData data, EventType type) {
            if (type.equals(EventType.RENDER_TEXT)) {
                TextRenderInfo renderInfo = (TextRenderInfo) data;
                String text = renderInfo.getText();
                if (text != null) {
                    if (trimmedFieldNames.isEmpty()) return false;
                    text = getRidOfSpaces(text);
                    text = checkForWrongEncoding(text);
//                    System.out.println(text);
                    if (trimmedFieldNames.contains(text)) {
                        addToFields(renderInfo, text);
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (String anAccumulated1 : accumulated) {
                            builder.append(anAccumulated1);
                        }
                        builder.append(text);
                        builder = checkForWrongEncoding(builder);
                        if (trimmedFieldNames.contains(builder.toString())) {
                            addToFields(renderInfo, builder.toString());
                            return false;
                        }
                        for (String anAccumulated : accumulated) {
                            builder.delete(0, anAccumulated.length());
                            builder = checkForWrongEncoding(builder);
                            if (trimmedFieldNames.contains(builder.toString())) {
                                addToFields(renderInfo, builder.toString());
                                return false;
                            }
                        }
                        accumulated.add(text);
                    }
                }
                return false;
            }
            return false;
        }

        private void addToFields(TextRenderInfo renderInfo, String text) {
            float x = renderInfo.getBaseline().getStartPoint().get(0);
            float y = renderInfo.getBaseline().getStartPoint().get(1);
            xCoords.add(x);
            widths.add(renderInfo.getUnscaledWidth());
            String columnId = getOriginalText(text);
            PdfField field;
            try {
                field = new PdfField(columnId, x, y, pdfHeight);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            field.setColor(renderInfo.getFillColor());
            field.setFont(renderInfo.getFont());
            field.setFontSize(renderInfo.getFontSize());
            // check if field should be capitalized
            if (columnId.toUpperCase().equals(columnId)) field.setCapitalized();
            fields.put(columnId, field);
            trimmedFieldNames.remove(text);
            accumulated.clear();
        }

        private String checkForWrongEncoding(String text) {
            for (String fieldName : trimmedFieldNames) {
                if (fieldName.length() == text.length()
                        && !fieldName.equals(text))
                    text = checkForWrongEncoding(text, fieldName);
            }
            return text;
        }

        private String checkForWrongEncoding(String text, String fieldName) {
            char[] chars = text.toCharArray();
            char c = chars[0];
            // boundaries were gained by experiments
            if (c > 0x00c0 && c < 0x02b0) {
                int shift = fieldName.charAt(0) - c;
                for (int i = 1; i < chars.length; i++) {
                    c = chars[i];
                    int newShift = fieldName.charAt(i) - c;
                    if (newShift != shift) return text;
                    shift = newShift;
                }
                return fieldName;
            }
            return text;
        }

        private StringBuilder checkForWrongEncoding(StringBuilder builder) {
            String initialText = builder.toString();
            String finalText = null;
            for (String fieldName : trimmedFieldNames) {
                if (fieldName.length() == builder.length()
                        && !fieldName.equals(initialText))
                    finalText = checkForWrongEncoding(initialText, fieldName);
            }
            if (finalText != null) builder.delete(0, builder.length()).append(finalText);
            return builder;
        }

    }

    // returns to original state (with spaces and proper case)
    private String getOriginalText(String text) {
        return originalFieldNames.stream()
                .filter(n -> getRidOfSpaces(n).equals(text))
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }

    /*private void deleteFields(PdfDocument pdf, Set<String> trimmedFieldNames) {
            PdfDictionary dict = pdf.getFirstPage().getPdfObject();
            PdfObject object = dict.get(PdfName.Contents);
            if (object instanceof PdfStream) {
                PdfStream stream = (PdfStream) object;
                byte[] data = stream.getBytes();
                try {
                    stream.setData(new String(data)
                            .replace("Имя", "")
                            .getBytes("UTF-8"));
                    System.out.println("deleted");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }*/
}
