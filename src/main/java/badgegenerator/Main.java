package badgegenerator;

import badgegenerator.appfilesmanager.HelpMessages;
import com.sun.javafx.PlatformUtil;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main extends Application {
    private static String appFilesDirPath;

    @Override
    public void start(Stage primaryStage) throws Exception{
        // load fonts
        InputStream boldStream = getClass().getResourceAsStream("/fonts/CIRCE-BOLD.OTF");
        Font.loadFont(boldStream, 13);
        boldStream.close();
        InputStream lightStream = getClass().getResourceAsStream("/fonts/CRC35.OTF");
        Font.loadFont(lightStream, 13);
        lightStream.close();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FileLoader.fxml"));
        primaryStage.setTitle("Генератор бейджей");
        primaryStage.getIcons().add(new Image(getClass()
                .getResourceAsStream("/images/BG_icon.png")));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        HelpMessages.load();
        primaryStage.setOnCloseRequest(event ->
                Arrays.stream(Logger.getGlobal().getHandlers()).forEach(Handler::close));
    }

    public static double computeStringWidth(String str, Font font) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(str, font);
    }

    public static String getAppFilesDirPath() {
        if(appFilesDirPath == null) {
            computeAppFilesDirPath();
        }
        return appFilesDirPath;
    }

    private static void computeAppFilesDirPath() {
        StringBuilder builder = new StringBuilder();
        builder.append(System.getProperty("user.home"));
        if(PlatformUtil.isWindows()) {
            builder.append("\\AppData\\BG");
        } else if(PlatformUtil.isMac()) {
            builder.append("/Documents/BG");
        }
        appFilesDirPath = builder.toString();
        File appFilesDir = new File(appFilesDirPath);
        if(!appFilesDir.exists()) {
            appFilesDir.mkdir();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
