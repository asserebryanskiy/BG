package badgegenerator.appfilesmanager;

import badgegenerator.Main;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * LoggerManager initializes loggers and provides them with files
 * to write in special folder on local machine.
 */
public class LoggerManager {
    public static void initializeLogger(Logger logger) {
        if(logger.getHandlers().length > 0) return;
        File dir = new File(Main.getAppFilesDirPath()
                + "/logs/"
                + logger.getName());
        if(!dir.exists()) dir.mkdirs();
        else {
            File[] files = dir.listFiles();
            if(files != null && files.length > 10) {
                for(int i = 0; i < files.length - 10; i++) {
                    files[i].delete();
                }
            }
        }
        String builder = dir.getAbsolutePath() +
                File.separator +
                String.format("%tF_%tH-%tM",
                        LocalDate.now(),
                        LocalTime.now(),
                        LocalTime.now()) +
                ".log";
        FileHandler fh;
        try {
            fh = new FileHandler(builder);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    String.format("Ошибка при инициализации логера%s%n%s",
                            logger.getName(), e.toString()));
            alert.show();
            return;
        }
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }
}
