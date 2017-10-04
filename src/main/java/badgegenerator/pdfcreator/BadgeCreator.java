package badgegenerator.pdfcreator;

import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class BadgeCreator {

    private final List<FxField> fxFields;
    private float ratio;
    private String[][] participants;

    private PdfDocument srcPdf;
    private PdfDocument commonPdf;
    private ByteArrayOutputStream commonPdfOutStream;
    private Rectangle pageSize;

    private float sumOfShifts;
    private int numberOfLines;
    private boolean compressFieldIfLineMissing;

    public BadgeCreator(List<FxField> fxFields,
                        String srcPdfPath,
                        String[][] participants,
                        double imageToPdfRatio,
                        boolean compressFieldIfLineMissing) throws IOException {
        // in memory test
        this.fxFields = fxFields;
        srcPdf = new PdfDocument(new PdfReader(srcPdfPath));
        PdfPage srcPage = srcPdf.getFirstPage();
        pageSize = srcPage.getPageSize();
        this.participants = participants;
        ratio = (float) imageToPdfRatio;
        this.compressFieldIfLineMissing = compressFieldIfLineMissing;

        commonPdfOutStream = new ByteArrayOutputStream();
        commonPdf = new PdfDocument(new PdfWriter(commonPdfOutStream));
    }

    public byte[] createBadgeInMemory(int i) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument newPdf = new PdfDocument(new PdfWriter(out));
        srcPdf.copyPagesTo(1,1,newPdf);
        PdfPage page = newPdf.getFirstPage();
        PdfCanvas pdfCanvas = new PdfCanvas(page);

        String[] participantRow = participants[i];
        int sumOfMissingLines = 0;
        for (int j = 0; j < participantRow.length; j++) {
            FxField fxField = fxFields.get(j);
            FxToPdfFieldAdapter adapter = new FxToPdfFieldAdapter(fxFields.get(j),
                    ratio, pageSize.getHeight());
            String value = participantRow[fxFields.get(j).getNumberOfColumn()];
            String alignment = adapter.getAlignment();
            float yCoordinate = adapter.getY();
            if(compressFieldIfLineMissing && sumOfMissingLines > 0) {
                yCoordinate += sumOfMissingLines;
            }

            pdfCanvas.beginText()
                    .setFontAndSize(adapter.getFont(), adapter.getFontSize())
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
                    if (lineWidth + spaceWidth + wordWidth <= maxWidth) {
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
        PdfDocument temp = new PdfDocument(new PdfReader(
                new ByteArrayInputStream(out.toByteArray())));
        temp.copyPagesTo(1,1,commonPdf);
        temp.close();
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
    public byte[] createCommonBadge() {
        commonPdf.close();
        return commonPdfOutStream.toByteArray();
    }
}
