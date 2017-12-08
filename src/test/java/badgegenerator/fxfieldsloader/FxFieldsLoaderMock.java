package badgegenerator.fxfieldsloader;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.fileloader.ExcelReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class FxFieldsLoaderMock extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        ExcelReader excelReader = new ExcelReader("/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/test.xlsx"
        );
        String pdfPath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/empty.pdf";
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
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}