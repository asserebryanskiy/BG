package badgegenerator.pdfeditor;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BadgeCreator {

    private final List<Field> fields;
    private float ratio;
    private String[][] participants;

    private PdfDocument srcPdf;
    private PdfDocument commonPdf;
    private ByteArrayOutputStream commonPdfOutStream;
    private Rectangle pageSize;

    private float sumOfShifts;
    private int numberOfLines;
    private boolean compressFieldIfLineMissing;

    public BadgeCreator(List<Field> fields,
                        String srcPdfPath,
                        String[][] participants,
                        double imageToPdfRatio,
                        boolean compressFieldIfLineMissing) throws IOException {
        // in memory test
        this.fields = fields;
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
            Field field = fields.get(j);
            String value = participantRow[field.getNumberOfColumn()];

            PdfFont font;
            if(field.getFontPath() == null) {
                InputStream fontInputStream = getClass()
                        .getResourceAsStream("/fonts/Helvetica.otf");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int a;
                while((a = fontInputStream.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, a);
                }
                baos.flush();
                font = PdfFontFactory.createFont(baos.toByteArray(),
                        PdfEncodings.IDENTITY_H, true);
            } else {
                font = PdfFontFactory.createFont(field.getFontPath(),
                        PdfEncodings.IDENTITY_H, true);
            }

            String alignment = field.getTextFlow().getTextAlignment().name();
            float fontSize = (float) ((field.getFont().getSize()) / ratio);
            float yCoordinate = transform(field.getLayoutY() / ratio)
                    - getFontMetrics(field).getMaxAscent() / ratio;
            if(compressFieldIfLineMissing && sumOfMissingLines > 0) {
                yCoordinate += sumOfMissingLines;
            }

            pdfCanvas.beginText()
                    .setFontAndSize(font, fontSize)
                    .setColor(new DeviceRgb(field.getRed(),
                            field.getGreen(),
                            field.getBlue()), true)
                    .setLeading(getFontMetrics(field).getMaxAscent() / ratio
                            + getFontMetrics(field).getMaxDescent() / ratio)
                    .moveText(field.getLayoutX() / ratio, yCoordinate);

            numberOfLines = 0;
            sumOfShifts = 0;
            float maxWidth = (float) (field.getPrefWidth() / ratio);
            if(field.mayHasHyphenation) {
                String[] words = value.split("\\s");
                StringBuilder line = new StringBuilder();
                line.append(words[0]);
                float spaceWidth = (float) field.computeStringWidth(" ") / ratio;
                float lineWidth;
                for (int index = 1; index < words.length; index++) {
                    String word = words[index];
                    float wordWidth = (float) field.computeStringWidth(word) / ratio;
                    lineWidth = (float) field.computeStringWidth(line.toString()) / ratio;
                    if (lineWidth + spaceWidth + wordWidth <= maxWidth) {
                        line.append(" ").append(word);
                    } else {
                        showTextAligned(alignment, pdfCanvas, line.toString(), lineWidth, maxWidth);
                        line.delete(0, line.length());
                        line.append(word);
                    }
                }
                if(line.length() != 0) {
                    lineWidth = (float) field.computeStringWidth(line.toString()) / ratio;
                    showTextAligned(alignment, pdfCanvas, line.toString(), lineWidth, maxWidth);
                }
            } else {
                float lineWidth = (float) field.computeStringWidth(value) / ratio;
                showTextAligned(alignment, pdfCanvas, value, lineWidth, maxWidth);
            }

            pdfCanvas.endText();
            if(compressFieldIfLineMissing) {
                sumOfMissingLines += (field.getNumberOfLines() - numberOfLines)
                        * ((getFontMetrics(field).getMaxAscent()
                        + getFontMetrics(field).getMaxDescent()) / ratio);
            }
        }

        newPdf.close();
        PdfDocument temp = new PdfDocument(new PdfReader(
                new ByteArrayInputStream(out.toByteArray())));
        temp.copyPagesTo(1,1,commonPdf);
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


    private FontMetrics getFontMetrics(Field field) {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(field.getFont());
    }

    /**
     * Is used to transform javaFx field coordinates (which originate from upper-left corner)
     * to pdf coordinates (which originate from lower-left corner).
     * */
    private float transform(double coordinate) {
        return (float) (pageSize.getHeight() - coordinate);
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
