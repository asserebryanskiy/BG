package badgegenerator;

import badgegenerator.appfilesmanager.HelpMessages;
import badgegenerator.appfilesmanager.LoggerManager;
import com.sun.javafx.PlatformUtil;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static Logger logger = Logger.getLogger(Main.class.getSimpleName());
    private static String appFilesDirPath;

    @Override
    public void start(Stage primaryStage) {
        // load fonts
        String bold  = "/fonts/CIRCE-BOLD.OTF";
        String light = "/fonts/CRC35.OTF";
        try {
            loadFont(bold);
            loadFont(light);
        } catch (IOException e) {
            e.printStackTrace();
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Error loading fonts", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось загрузить шрифты Circe." +
                    "\nИспользуется стандартный системный шрифт.");
            alert.show();
        }

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/FileLoader.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Error loading FileLoader.fxml", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось запустить программу.");
            alert.show();
        }
        assert root != null;
        primaryStage.setTitle("Генератор бейджей");
        primaryStage.getIcons().add(new Image(getClass()
                .getResourceAsStream("/images/BG_icon.png")));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        HelpMessages.load();
        primaryStage.setOnCloseRequest(event ->
                Arrays.stream(Logger.getGlobal().getHandlers()).forEach(Handler::close));
    }

    private void loadFont(String fontName) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(fontName)) {
            Font.loadFont(stream, 13);
        }
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
