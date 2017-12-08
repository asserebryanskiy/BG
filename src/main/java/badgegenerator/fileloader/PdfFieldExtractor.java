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
    private final Set<String> fieldNames;

    public PdfFieldExtractor(String pdfPath, ExcelReader excelReader)
            throws IOException, WrongHeadingsException {
        fields = new HashMap<>(excelReader.getHeadings().length);
        fieldNames = new HashSet<>(Arrays.asList(excelReader.getHeadings()));
        PdfDocument pdf = new PdfDocument(new PdfReader(pdfPath));
        pdfHeight = pdf.getFirstPage().getPageSize().getHeight();
        widths  = new ArrayList<>(fieldNames.size());
        xCoords = new ArrayList<>(fieldNames.size());

        Rectangle rect = pdf.getFirstPage().getMediaBox();

        ContentFilter contentFilter = new ContentFilter(rect);
        FilteredEventListener listener = new FilteredEventListener();
        listener.attachEventListener(new LocationTextExtractionStrategy(), contentFilter);
        new PdfCanvasProcessor(listener).processPageContent(pdf.getFirstPage());
        if (!fieldNames.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Не удалось найти в pdf ");
            int size = fieldNames.size();
            if (size == 1) errorMessage.append("заголовок ");
            else           errorMessage.append("заголовки: ");
            for (String fieldName : fieldNames)
                errorMessage.append(fieldName).append(", ");
            errorMessage.delete(errorMessage.length() - 2, errorMessage.length());
            throw new WrongHeadingsException(errorMessage.toString());
        }

        pdf.close();
        computeAlignment();
    }

    public Map<String, PdfField> getFields() {
        return fields;
    }

    private void computeAlignment() {
        final float OFFSET = 3;  // acceptable difference in pixels
        int centerCounter = 0;
        int rightCounter = 0;
        for (int i = 1; i < fields.size(); i++) {
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
        if      (rightCounter == target)  alignment = "RIGHT";
        else if (centerCounter == target) alignment = "CENTER";
        else                              alignment = "LEFT";

        fields.values().forEach(f -> f.setAlignment(alignment));
    }

    private class ContentFilter extends TextRegionEventFilter {
        ContentFilter(Rectangle filterRect) {
            super(filterRect);
        }

        @Override
        public boolean accept(IEventData data, EventType type) {
            if (type.equals(EventType.RENDER_TEXT)) {
                TextRenderInfo renderInfo = (TextRenderInfo) data;
                String text = renderInfo.getText();
                if (text != null) {
                    if (fieldNames.isEmpty()) return false;
                    for (String fieldName : fieldNames) {
                        if (fieldName.length() == text.length()
                                && !fieldName.equals(text))
                            text = checkForWrongEncoding(text, fieldName);
                    }
                    if (fieldNames.contains(text)) {
                        float x = renderInfo.getBaseline().getStartPoint().get(0);
                        float y = renderInfo.getBaseline().getStartPoint().get(1);
                        xCoords.add(x);
                        widths.add(renderInfo.getUnscaledWidth());
                        PdfField field;
                        try {
                            field = new PdfField(text, x, y, pdfHeight);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                        field.setColor(renderInfo.getFillColor());
                        field.setFont(renderInfo.getFont());
                        field.setFontSize(renderInfo.getFontSize());
                        fields.put(text, field);
                        fieldNames.remove(text);
                    }
                    /*char[] chars = text.toCharArray();
                    char c = chars[0];
                    // if encoding is broken for some reason
                    if (!fieldNames.isEmpty() && c > 0x00c0 && c < 0x02b0) {
                        StringBuilder builder = new StringBuilder(text.length());
                        final int DIFFERENCE = 848; // got by experiments
                        for (char aChar : chars) {
                            builder.append(String.valueOf((char) (aChar + DIFFERENCE)));
                        }
                        text = builder.toString();
                    }*/
                }
                return false;
            }
            return false;
        }

        private String checkForWrongEncoding(String text, String fieldName) {
            char[] chars = text.toCharArray();
            char c = chars[0];
            if (c > 0x00c0 && c < 0x02b0) {
                int shift = fieldName.charAt(0) - c;
                for (int i = 1; i < chars.length; i++) {
                    c = chars[i];
                    int newShift = fieldName.charAt(i) - c;
                    if (newShift != shift) return text;
                    shift = newShift;
                }
                return fieldName;
            } else return text;
        }
    }

    /*private void deleteFields(PdfDocument pdf, Set<String> fieldNames) {
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
