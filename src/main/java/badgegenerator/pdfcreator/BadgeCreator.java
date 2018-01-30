package badgegenerator.pdfcreator;

import badgegenerator.Main;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BadgeCreator {
    // temporal file that is created to avoid storing large files (>3 Gb) in memory
    private static final String TEMP_FILE_FULL_NAME = Main.getAppFilesDirPath() + "/temp.pdf";
    // offset to nivilate float point effects
    private final float OFFSET = 0.5f;

    private final List<FxField> fxFields;
    private final ArrayList<FxToPdfFieldAdapter> adapters;
    private final List<String> headings;
    private float ratio;
    private String[][] participants;

    private PdfDocument srcPdf;     // src empty pdf with background
    private PdfDocument commonPdf;  // pdf file where all badges arae stored
    private FileOutputStream commonPdfOutStream;
    private Rectangle pageSize;     // size of src pdf

    private float sumOfShifts;      // used to memorize space between two subsequent lines
    private int numberOfLines;      // number of line in particular badge
    // specifies, if lower lines should be layouted upper, if some line upper is missing
    private boolean compressFieldIfLineMissing;

    public BadgeCreator(List<FxField> fxFields,
                 String srcPdfPath,
                 String[][] participants,
                 String[] headings,
                 double imageToPdfRatio,
                 boolean compressFieldIfLineMissing) throws IOException {
        this.fxFields = fxFields;
        try {
            srcPdf = new PdfDocument(new PdfReader(srcPdfPath));
        } catch (IOException e) {
            throw new IOException("Не удалось найти исходный pdf с макетом");
        }
        PdfPage srcPage = srcPdf.getFirstPage();
        pageSize = srcPage.getPageSize();
        this.adapters = new ArrayList<>(fxFields.size());
        fxFields.forEach(field -> adapters.add(
                new FxToPdfFieldAdapter(field, imageToPdfRatio, pageSize.getHeight())));
        if(adapters.stream().anyMatch(a -> a.getFontProgram() == null)) {
            throw new IOException("Не удалось загрузить файл шрифта");
        }
        this.participants = participants;
        this.headings = Arrays.asList(headings);
        ratio = (float) imageToPdfRatio;
        this.compressFieldIfLineMissing = compressFieldIfLineMissing;

        commonPdfOutStream = new FileOutputStream(TEMP_FILE_FULL_NAME);
        commonPdf = new PdfDocument(new PdfWriter(commonPdfOutStream));
    }

    byte[] createBadgeInMemory(int i) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument newPdf = new PdfDocument(new PdfWriter(out));
        srcPdf.copyPagesTo(1,1, newPdf);
        PdfPage page = newPdf.getFirstPage();
        PdfCanvas pdfCanvas = new PdfCanvas(page);

        String[] participantRow = participants[i];
        int sumOfMissingLines = 0;
        for (int j = 0; j < participantRow.length; j++) {
            FxField fxField = fxFields.get(j);
            FxToPdfFieldAdapter adapter = adapters.get(j);
            String value = adapter.isCapitalized() ?
                    participantRow[headings.indexOf(adapter.getColumnId())].toUpperCase() :
                    participantRow[headings.indexOf(adapter.getColumnId())];
            String alignment = adapter.getAlignment();
            float yCoordinate = adapter.getY();
            if(compressFieldIfLineMissing && sumOfMissingLines > 0) {
                yCoordinate += sumOfMissingLines;
            }
            PdfFont font = PdfFontFactory
                    .createFont(adapter.getFontProgram(), PdfEncodings.IDENTITY_H, true);

            pdfCanvas.beginText()
                    .setFontAndSize(font, adapter.getFontSize())
                    .setColor(adapter.getPdfColor(), true)
                    .setLeading(adapter.getLeading())
                    .moveText(adapter.getX(), yCoordinate);

            numberOfLines = 0;
            sumOfShifts = 0;
            float maxWidth = (float) (fxField.getPrefWidth() / ratio);
            // ToDo: create HyphenationComputer interface to combine javaFX and Pdf realization.
            if(fxField instanceof FieldWithHyphenation) {
                String[] words = value.split("\\s");
                StringBuilder line = new StringBuilder();
                line.append(words[0]);
                float spaceWidth = (float) fxField.computeStringWidth(" ") / ratio;
                float lineWidth;
                for (int index = 1; index < words.length; index++) {
                    String word = words[index];
                    float wordWidth = (float) fxField.computeStringWidth(word) / ratio;
                    lineWidth = (float) fxField.computeStringWidth(line.toString()) / ratio;
                    float newLineWidth = lineWidth + spaceWidth + wordWidth;
                    if (newLineWidth < maxWidth + OFFSET) {
                        line.append(" ").append(word);
                    } else {
                        showTextAligned(alignment, pdfCanvas, line.toString(), lineWidth, maxWidth);
                        line.delete(0, line.length());
                        line.append(word);
                    }
                }
                if(line.length() != 0) {
                    lineWidth = (float) fxField.computeStringWidth(line.toString()) / ratio;
                    showTextAligned(alignment, pdfCanvas, line.toString(), lineWidth, maxWidth);
                }
            } else {
                float lineWidth = (float) fxField.computeStringWidth(value) / ratio;
                showTextAligned(alignment, pdfCanvas, value, lineWidth, maxWidth);
            }

            pdfCanvas.endText();
            if(compressFieldIfLineMissing) {
                sumOfMissingLines += (adapter.getNumberOfLines() - numberOfLines)
                        * adapter.getLeading();
            }
        }

        newPdf.close();

        // copy page to commonPdf
        PdfDocument temp = new PdfDocument(new PdfReader(
                new ByteArrayInputStream(out.toByteArray())));
        temp.copyPagesTo(1,1,commonPdf);
        // to avoid filling the Java heap with used objects
        commonPdf.getPage(commonPdf.getNumberOfPages()).flush(true);
        commonPdf.flushCopiedObjects(temp);
        temp.close();
        out.close();

        numberOfLines = 0;
        return out.toByteArray();
    }

    private void showTextAligned(String alignment,
                                  PdfCanvas pdfCanvas,
                                  String line,
                                  float lineWidth,
                                  float maxWidth) {
        switch (alignment) {
            case "CENTER": {
                float shift = (maxWidth - lineWidth) / 2 - sumOfShifts;
                pdfCanvas.moveText(shift, 0);
                if(numberOfLines == 0) pdfCanvas.showText(line);
                else pdfCanvas.newlineShowText(line);
                numberOfLines++;
                sumOfShifts += shift;
                break;
            }
            case "RIGHT": {
                float shift = maxWidth - lineWidth - sumOfShifts;
                pdfCanvas.moveText(shift, 0);
                if(numberOfLines == 0) pdfCanvas.showText(line);
                else pdfCanvas.newlineShowText(line);
                numberOfLines++;
                sumOfShifts += shift;
                break;
            }
            default:
                pdfCanvas.moveText(0, 0);
                if(numberOfLines == 0) pdfCanvas.showText(line);
                else pdfCanvas.newlineShowText(line);
                numberOfLines++;
                break;
        }
    }

    /**
     * Creates one pdf, containing all other badges.
     *
     * Should be called only after all badges were created.
     * */
     String getCommonFilePath() throws IOException {
        commonPdf.close();
        commonPdfOutStream.close();

        return TEMP_FILE_FULL_NAME;
    }
}
