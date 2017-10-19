package badgegenerator;

import badgegenerator.appfilesmanager.HelpMessages;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.pdfeditor.PdfEditorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by andreyserebryanskiy on 11/09/2017.
 */
public class PdfEditorMock extends Application {
    private PDDocument pdf;

    @Override
    public void start(Stage primaryStage) throws Exception {
        ExcelReader excelReader = new ExcelReader("/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/test.xlsx",
                true);
        excelReader.processFile();
        String pdfPath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/example.pdf";
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PdfEditor.fxml"));
        Parent root = loader.load();
        PdfEditorController controller = loader.getController();
        pdf = PDDocument.load(new File(pdfPath));
        HelpMessages.load();

        double imageHeight = 500;
        double pdfHeight = pdf.getPage(0).getMediaBox().getHeight();
        controller.setImageToPdfRatio(imageHeight / pdfHeight);
        controller.setPdfPreview(createImageFromPdf(), imageHeight);
        controller.setPdfPath(pdfPath);
        controller.setExcelReader(excelReader);
        controller.init();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private byte[] createImageFromPdf() throws IOException {
        PDFRenderer renderer = new PDFRenderer(pdf);
        BufferedImage bim = renderer.renderImageWithDPI(0, 300, ImageType.RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIOUtil.writeImage(bim, "png", out, 300);
        return out.toByteArray();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
