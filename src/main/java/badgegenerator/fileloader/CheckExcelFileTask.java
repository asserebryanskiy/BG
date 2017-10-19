package badgegenerator.fileloader;

import badgegenerator.Main;
import badgegenerator.appfilesmanager.LoggerManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Checks if Excel file could be read properly
 */
public class CheckExcelFileTask extends Task {
    private ExcelReader excelReader;
    private static Logger logger = Logger.getLogger(CheckExcelFileTask.class.getSimpleName());

    CheckExcelFileTask(ExcelReader excelReader) {
        this.excelReader = excelReader;
    }

    @Override
    protected Boolean call() throws Exception {
        try {
            excelReader.processFile();
        } catch (Exception e) {
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Error", e);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Не удалось считать Excel файл:"
                        + System.lineSeparator()
                        + e.getMessage(),
                        ButtonType.OK);
                alert.show();
            });
            return false;
        }
        return true;
    }

    private void initializeLogger() throws IOException {
        File dir = new File(Main.getAppFilesDirPath()
                + "/logs/"
                + getClass().getSimpleName());
        if(!dir.exists()) dir.mkdirs();
        else {
            File[] files = dir.listFiles();
            if(files != null && files.length > 10) {
                files[0].delete();
            }
        }
        String fileName = dir.getAbsolutePath() +
                File.separator +
                LocalDateTime.now().toString() +
                ".log";
        FileHandler fh = new FileHandler(fileName);
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }
}
