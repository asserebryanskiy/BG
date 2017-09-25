package badgegenerator;

import badgegenerator.fileloader.ExcelReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Singleton was chosen to store Excel values and not to repeat expensive operation of reading
 * from the hard drive twice.
 *
 * It also aids in preserving the application state to make it possible
 * to return to the screen without reloading it.
 */
public class ModelSingleton {
    private static ModelSingleton instance;

    private ExcelReader excelReader;
    private String srcPdfPath;
    private String srcXlsxPath;
    private boolean hasHeadings = false;
    private PDDocument document;
    private byte[] pdfImageByteArray;
    private boolean pdfEditorFirstLaunch = true;

    private ModelSingleton() {
    }

    public static ModelSingleton getInstance() {
        if(instance == null) instance = new ModelSingleton();
        return instance;
    }

    public void launchExcelReader() throws IOException{
        excelReader = new ExcelReader(srcXlsxPath, hasHeadings);
    }

    public void createImageFromPdf() throws IOException {
        document = PDDocument.load(new File(srcPdfPath));
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage bim = renderer.renderImageWithDPI(0, 300, ImageType.RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIOUtil.writeImage(bim, "png", out, 300);
        pdfImageByteArray = out.toByteArray();
    }

    public byte[] getPdfImageByteArray() {
        return pdfImageByteArray;
    }

    public double calculatePdfToImageRatio(double imageHeight) {
        double pdfHeight = document.getPage(0).getMediaBox().getHeight();
        return imageHeight / pdfHeight;
    }

    public void setSrcPdfPath(String srcPdfPath) {
        this.srcPdfPath = srcPdfPath;
    }

    public ExcelReader getExcelReader() {
        return excelReader;
    }

    public void setSrcXlsxPath(String srcXlsxPath) {
        this.srcXlsxPath = srcXlsxPath;
    }

    public void setHasHeadings(boolean hasHeadings) {
        this.hasHeadings = hasHeadings;
    }

    public String getSrcPdfPath() {
        return srcPdfPath;
    }

    public boolean getPdfEditorFirstLaunch() {
        return pdfEditorFirstLaunch;
    }

    public void setPdfEditorFirstLaunch(boolean pdfEditorFirstLaunch) {
        this.pdfEditorFirstLaunch = pdfEditorFirstLaunch;
    }

}
