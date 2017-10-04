package badgegenerator.fileloader;

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

/**
 * Launches next stage from fxml file;
 */
public class LaunchPdfEditorTask extends Task {
    private final String fxmlFilePath = "/fxml/PdfEditor.fxml";
    private final ExcelReader excelReader;
    private final String pdfPath;
    private final String fxFieldsPath;
    private PDDocument pdf;

    public LaunchPdfEditorTask(ExcelReader excelReader, String pdfPath) {
        this.excelReader = excelReader;
        this.pdfPath = pdfPath;
        fxFieldsPath = null;
    }

    public LaunchPdfEditorTask(ExcelReader excelReader, String pdfPath, String fxFieldsPath) {
        this.excelReader = excelReader;
        this.pdfPath = pdfPath;
        this.fxFieldsPath = fxFieldsPath;
    }

    @Override
    protected Parent call()  {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
        Parent root;
        try {
            root = loader.load();
            PdfEditorController controller = loader.getController();
            pdf = PDDocument.load(new File(pdfPath));

            double imageHeight = 500;
            double pdfHeight = pdf.getPage(0).getMediaBox().getHeight();
            controller.setImageToPdfRatio(imageHeight / pdfHeight);
            controller.setPdfPreview(createImageFromPdf(), imageHeight);
            controller.setPdfPath(pdfPath);
            controller.setExcelReader(excelReader);
            if(fxFieldsPath != null) {
                controller.init(fxFieldsPath);
            } else {
                controller.init();
            }
        } catch (Exception e) {
//        } catch (IOException | NullPointerException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Не удалось загрузить окно редактирования pdf",
                        ButtonType.OK);
                alert.show();
                e.printStackTrace();
            });
            return null;
        }

        return root;
    }

    private byte[] createImageFromPdf() throws IOException {
        PDFRenderer renderer = new PDFRenderer(pdf);
        BufferedImage bim = renderer.renderImageWithDPI(0, 300, ImageType.RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIOUtil.writeImage(bim, "png", out, 300);
        return out.toByteArray();
    }
}
