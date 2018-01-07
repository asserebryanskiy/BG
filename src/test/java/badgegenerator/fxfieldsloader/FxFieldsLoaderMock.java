package badgegenerator.fxfieldsloader;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.fileloader.ExcelReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;

public class FxFieldsLoaderMock extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        InputStream lightStream = getClass().getResourceAsStream("/fonts/CRC35.OTF");
        Font.loadFont(lightStream, 13);
        lightStream.close();
        ExcelReader excelReader =
                new ExcelReader("/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/excels/test.xlsx");
        excelReader.processFile();
        String pdfPath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/pdfs/threeFonts.pdf";
        String emptyPdfPath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/pdfs/empty.pdf";
        /*File savedFilesDirectory = new File(Main.class
                .getResource("/savedFields").getFile());
        String[] names = savedFilesDirectory.list();*/
        List<String> savesNames = SavesManager.getSavesNames();
        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/FxFieldsLoader.fxml"));
        Parent root = loader.load();
        FxFieldsLoaderController controller = loader.getController();
        controller.setSavedFieldsNames(savesNames);
        controller.setExcelReader(excelReader);
        controller.setPdfPath(pdfPath);
        controller.setEmptyPdfPath(emptyPdfPath);
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}