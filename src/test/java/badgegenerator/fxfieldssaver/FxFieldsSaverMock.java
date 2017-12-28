package badgegenerator.fxfieldssaver;

import badgegenerator.Main;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreyserebryanskiy on 02/10/2017.
 */
public class FxFieldsSaverMock extends Application{
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FxField field1 = new SingleLineField("Example",
                "Example", 1,
                100);
        FxField field2 = new FieldWithHyphenation("Example words",
                "Example", "Example2", 1,
                120);
        List<FxField> fields = new ArrayList<>();
        fields.add(field1);
        fields.add(field2);
        FXMLLoader loader = new FXMLLoader(Main.class
                .getResource("/fxml/FxFieldsSaver.fxml"));
        Parent root = loader.load();
        FxFieldsSaverController controller = loader.getController();
        controller.init(fields, primaryStage);

        primaryStage.setTitle("testRoot");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}