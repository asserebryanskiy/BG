package badgegenerator;

import badgegenerator.appfilesmanager.HelpMessages;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfFieldExtractor;
import badgegenerator.pdfeditor.PdfEditorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by andreyserebryanskiy on 11/09/2017.
 */
public class PdfEditorMock extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // load fonts
        InputStream boldStream = getClass().getResourceAsStream("/fonts/CIRCE-BOLD.otf");
        Font.loadFont(boldStream, 13);
        boldStream.close();
        InputStream lightStream = getClass().getResourceAsStream("/fonts/CRC35.otf");
        Font.loadFont(lightStream, 13);
        lightStream.close();

        String excelPath = getResourcePath("/excels/newYear.xlsx");
        ExcelReader excelReader = new ExcelReader(excelPath);
        excelReader.processFile();
        String fullPdfPath = getResourcePath("/pdfs/newYear.pdf");
        String emptyPdfPath = getResourcePath("/pdfs/newYearEmpty.pdf");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PdfEditor.fxml"));
        Parent root = loader.load();
        PdfEditorController controller = loader.getController();
        PDDocument pdf = PDDocument.load(new File(emptyPdfPath));
        HelpMessages.load();
        String savesPath = null;
//        String savesPath = SavesManager.getSavesFolder().listFiles()[0].getAbsolutePath();

        PdfFieldExtractor extractor = new PdfFieldExtractor(fullPdfPath,
                new HashSet<>(Arrays.asList(excelReader.getHeadings())));
        double imageHeight = 500;
        float pdfHeight = pdf.getPage(0).getMediaBox().getHeight();
        controller.setImageToPdfRatio(imageHeight / pdfHeight);
        controller.setPdfPreview(createImageFromPdf(pdf), imageHeight);
        controller.setPdfPath(emptyPdfPath);
        controller.setExcelReader(excelReader);
        controller.init(savesPath, extractor.getFields());

        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    private String getResourcePath(String name) throws URISyntaxException {
        return Paths.get(getClass()
                .getResource(name).toURI())
                .toFile().getAbsolutePath();
    }

    private byte[] createImageFromPdf(PDDocument pdf) throws IOException {
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
