package badgegenerator.pdfcreator;

import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.ExcelReader;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task is designed to make time consuming creation of archive be done behind the UI
 */
public class CreateBadgeArchiveTask extends Task {
    private static Logger logger = Logger.getLogger(CreateBadgeArchiveTask.class.getSimpleName());

    private final String targetDirectoryPath;
    private final List<FxField> fxFields;
    private final double imageToPdfRatio;
    private final ExcelReader excelReader;
    private final boolean compressFieldIfLineMissing;
    private final String pdfPath;

    public CreateBadgeArchiveTask(List<FxField> fxFields,
                           double imageToPdfRatio,
                           ExcelReader excelReader,
                           String pdfPath,
                           String targetDirectoryPath,
                           boolean compressFieldIfLineMissing) {
        this.fxFields = fxFields;
        this.imageToPdfRatio = imageToPdfRatio;
        this.excelReader = excelReader;
        this.pdfPath = pdfPath;
        this.targetDirectoryPath = targetDirectoryPath;
        this.compressFieldIfLineMissing = compressFieldIfLineMissing;
    }

    @Override
    protected Boolean call() throws Exception {
        try {
            // is done to provide fields bindings functionality
            fxFields.sort(Comparator.comparing(FxField::getLayoutY));
            BadgeCreator badgeCreator = new BadgeCreator(fxFields,
                    pdfPath,
                    excelReader.getValues(),
                    excelReader.getHeadings(),
                    imageToPdfRatio,
                    compressFieldIfLineMissing);
            BadgeArchive badgeArchive = new BadgeArchive(targetDirectoryPath, badgeCreator);
            final int numberOfFiles = excelReader.getValues().length;
            for(int i = 0; i < numberOfFiles; i++) {
                if(isCancelled()) break;
                badgeArchive.createBadgeEntry(i);
                updateProgress(i, numberOfFiles + 1);
                updateMessage(String.format("Готов %d файл из %d", i, numberOfFiles));
            }
            updateProgress(numberOfFiles, numberOfFiles + 1);
            updateMessage("Создаю общий файл");
            badgeArchive.createCommonBadge();
            badgeArchive.getOutputStream().close();
            return true;
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    String.format("Ошибка при создании бейджей%n%s", e.getMessage()));
            alert.show();
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Error : " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }
}
