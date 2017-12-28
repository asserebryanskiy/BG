package badgegenerator.pdfeditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Created by andreyserebryanskiy on 11/12/2017.
 */
public class MockApp extends Application {

    private Pane alertBox;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new Pane();
        alertBox = new StackPane();
        root.getChildren().add(alertBox);
        primaryStage.setScene(new Scene(root, 200, 200));
        primaryStage.show();
    }

    public Pane getAlertBox() {
        return alertBox;
    }
}
