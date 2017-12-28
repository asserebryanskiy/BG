package badgegenerator.fileloader;

import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.pdfeditor.PdfEditorController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launches next stage from fxml file;
 */
public class LaunchPdfEditorTask extends Task {
    private static Logger logger = Logger.getLogger(LaunchPdfEditorTask.class.getSimpleName());

    private final ExcelReader excelReader;
    private final String pdfPath;
    private final String fxFieldsPath;
    private final String emptyPdfPath;

    public LaunchPdfEditorTask(ExcelReader excelReader,
                               String pdfPath,
                               String emptyPdfPath)  {
        this(excelReader, pdfPath, emptyPdfPath, null);
    }

    public LaunchPdfEditorTask(ExcelReader excelReader,
                               String pdfPath,
                               String emptyPdfPath,
                               String fxFieldsPath)  {
        this.excelReader = excelReader;
        this.pdfPath = pdfPath;
        this.emptyPdfPath = emptyPdfPath;
        this.fxFieldsPath = fxFieldsPath;
    }

    @Override
    protected Parent call()  {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PdfEditor.fxml"));
        Parent root;
        try {
            PdfFieldExtractor extractor = new PdfFieldExtractor(pdfPath,
                    new HashSet<>(Arrays.asList(excelReader.getHeadings())));;
            root = loader.load();
            PdfEditorController controller = loader.getController();
            updateMessage("Загружаю pdf");
            PDDocument pdf = PDDocument.load(new File(emptyPdfPath));
            double imageHeight = 500;
            float pdfHeight = pdf.getPage(0).getMediaBox().getHeight();
            controller.setImageToPdfRatio(imageHeight / pdfHeight);
            controller.setPdfPreview(createImageFromPdf(pdf), imageHeight);
            controller.setPdfPath(emptyPdfPath);
            controller.setExcelReader(excelReader);
            controller.init(fxFieldsPath, extractor.getFields());
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Не удалось загрузить окно редактирования pdf",
                        ButtonType.OK);
                alert.show();
            });
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Ошибка при загрузке PdfEditor.fxml", e);
            e.printStackTrace();
            return null;
        }

        return root;
    }

    private byte[] createImageFromPdf(PDDocument pdf) throws IOException {
        PDFRenderer renderer = new PDFRenderer(pdf);
        BufferedImage bim = renderer.renderImageWithDPI(0, 300, ImageType.RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIOUtil.writeImage(bim, "png", out, 300);
        return out.toByteArray();
    }
}
