package badgegenerator;

import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FileLoader.fxml"));
        primaryStage.setTitle("Генератор бейджей");
        primaryStage.getIcons().add(new Image(getClass()
                .getResourceAsStream("/images/BG_icon.png")));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static double computeStringWidth(String str, Font font) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(str, font);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
