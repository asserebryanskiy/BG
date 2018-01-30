package badgegenerator.pdfcreator;

import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.ExcelReader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task is designed to make time consuming creation of archive be done behind the UI
 */
public class CreateBadgeArchiveTask extends Task {
    private static Logger logger = Logger.getLogger(CreateBadgeArchiveTask.class.getSimpleName());

    private final String archiveFullPath;
    private final List<FxField> fxFields;
    private final double imageToPdfRatio;
    private final ExcelReader excelReader;
    private final boolean compressFieldIfLineMissing;
    private final String pdfPath;

    public CreateBadgeArchiveTask(List<FxField> fxFields,
                           double imageToPdfRatio,
                           ExcelReader excelReader,
                           String pdfPath,
                           String archiveFullPath,
                           boolean compressFieldIfLineMissing) throws NotEnoughSpaceException {
        // assert that there is enough space fro creating an archive
        long estimatedArchiveSize = new File(pdfPath).length() * excelReader.getValues().length * 2;
        long freeSpace = new File(archiveFullPath).getParentFile().getFreeSpace();
        if (freeSpace < estimatedArchiveSize)
            throw new NotEnoughSpaceException(estimatedArchiveSize, freeSpace);

        this.fxFields = fxFields;
        this.imageToPdfRatio = imageToPdfRatio;
        this.excelReader = excelReader;
        this.pdfPath = pdfPath;
        this.archiveFullPath = archiveFullPath;
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
            BadgeArchive badgeArchive = new BadgeArchive(archiveFullPath, badgeCreator);
            final int numberOfFiles = excelReader.getValues().length;
            for(int i = 0; i < numberOfFiles; i++) {
                if(isCancelled()) break;
                badgeArchive.createBadgeEntry(i);
                // * 2 because firstly all badges are created and
                // than one common file for all badges is created
                updateProgress(i, numberOfFiles * 2 + 1);
                updateMessage(String.format("Готов %d файл из %d", i, numberOfFiles));
            }
            badgeArchive.progressCreatingCommonFileProperty().addListener((obs, old, newV) -> {
                updateProgress(0.5 + newV.doubleValue() / 2, 1);
            });
            updateMessage("Создаю общий файл");
            badgeArchive.createCommonBadge();
            badgeArchive.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Error : " + e.getMessage(), e);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Ошибка при создании бейджей");
                alert.show();
            });
            return false;
        }
    }
}
