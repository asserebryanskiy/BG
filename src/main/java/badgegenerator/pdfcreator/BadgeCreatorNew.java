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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BadgeCreatorNew {
    private static final String TEMP_FILE_FULL_NAME = Main.getAppFilesDirPath() + "/temp.pdf";
//    private static Logger logger = Logger.getLogger(BadgeCreator.class.getSimpleName());

    private final float OFFSET = 0.5f; // offset to nivilate float point effects
    private final List<FxField> fxFields;
    private final ArrayList<FxToPdfFieldAdapter> adapters;
    private final List<String> headings;
    private final String srcPdfPath;
    private float ratio;
    private String[][] participants;

    private float sumOfShifts;
    private int numberOfLines;
    private boolean compressFieldIfLineMissing;
    private byte[] commonPdfBytes;
    private PdfDocument commonPdf;
    private File commonPdfFile;

    public BadgeCreatorNew(List<FxField> fxFields,
                           String srcPdfPath,
                           String[][] participants,
                           String[] headings,
                           double imageToPdfRatio,
                           boolean compressFieldIfLineMissing) throws IOException {
        // in memory test
//        LoggerManager.initializeLogger(logger);
        this.fxFields = fxFields;
        this.srcPdfPath = srcPdfPath;
        PdfReader reader;
        try {
            reader = new PdfReader(srcPdfPath);
        } catch (IOException e) {
            throw new IOException("Не удалось найти исходный pdf с макетом");
        }
        PdfDocument srcPdf = new PdfDocument(reader);
        PdfPage srcPage = srcPdf.getFirstPage();
        Rectangle pageSize = srcPage.getPageSize();
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

        reader.close();
    }

    byte[] createBadgeInMemory(int i) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfReader reader;
        try {
            reader = new PdfReader(srcPdfPath);
        } catch (IOException e) {
            throw new IOException("Не удалось найти исходный pdf с макетом");
        }
        PdfDocument srcPdf = new PdfDocument(reader);
        PdfWriter writer = new PdfWriter(out);
        PdfDocument newPdf = new PdfDocument(writer);
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
        out.close();
        reader.close();
        writer.flush();
        numberOfLines = 0;

        byte[] newBadgeBytes = out.toByteArray();
        writeToCommonFile(newBadgeBytes);

        return newBadgeBytes;
    }

    private void writeToCommonFile(byte[] newBytes) throws IOException {
        // write to commonPdf if there is enough space available
        if (true) {
            // 1. create temp pdfDoc from newBytes

            // 2. copy page from it to commonPdf

            // 3. close temp
        }
        // if there is not enough space
        // 1.




        // if there is enough heap space write to byte array
        // if not write all accumulated bytes to file and start new byte array
        System.out.printf("Before : %d Mb%n", Runtime.getRuntime().totalMemory() / 1_000_000);
        if (newBytes.length * 2 > Runtime.getRuntime().freeMemory()) {
            System.out.println("Wrote to file");
            File tempFile = new File(TEMP_FILE_FULL_NAME);
            PdfDocument temp;
            PdfWriter writer;
            if (tempFile.exists()) {
                writer = new PdfWriter(tempFile);
                temp = new PdfDocument(new PdfReader(tempFile), writer);
            } else {
                writer = new PdfWriter(new FileOutputStream(TEMP_FILE_FULL_NAME));
                temp = new PdfDocument(writer);
            }

            temp.close();
        }

        ByteArrayOutputStream commonPdfOutStream = new ByteArrayOutputStream();
        if (commonPdfBytes == null) {
            commonPdf = new PdfDocument(new PdfWriter(commonPdfOutStream));
        } else {
            commonPdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(commonPdfBytes)),
                    new PdfWriter(commonPdfOutStream));
        }

        PdfDocument temp = new PdfDocument(new PdfReader(new ByteArrayInputStream(newBytes)));
        temp.copyPagesTo(1,1, commonPdf);
        temp.close();
        System.out.printf("In time : %d Mb%n", Runtime.getRuntime().totalMemory() / 1_000_000);
        commonPdf.close();
        commonPdfOutStream.close();
        commonPdfBytes = commonPdfOutStream.toByteArray();
        System.out.printf("After : %d Mb%n", Runtime.getRuntime().totalMemory() / 1_000_000);
        System.out.println("----------");
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
    byte[] getCommonBadgeBytes() throws IOException {
        return commonPdfBytes;
    }
}
