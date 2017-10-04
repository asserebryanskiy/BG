package badgegenerator.fileloader;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Checks if Excel file could be read properly
 */
public class CheckExcelFileTask extends Task {
    private ExcelReader excelReader;

    CheckExcelFileTask(ExcelReader excelReader) {
        this.excelReader = excelReader;
    }

    @Override
    protected Boolean call() throws Exception {
        try {
            excelReader.processFile();
        } catch (NullPointerException npe) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Не удалось считать Excel файл",
                        ButtonType.OK);
                alert.show();
            });
            return false;
        }
        return true;
    }
}
