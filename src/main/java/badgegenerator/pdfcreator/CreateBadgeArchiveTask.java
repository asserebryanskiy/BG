package badgegenerator.pdfcreator;

import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.ExcelReader;
import javafx.concurrent.Task;

import java.util.Comparator;
import java.util.List;

/**
 * Task is designed to make time consuming creation of archive be done behind the UI
 */
public class CreateBadgeArchiveTask extends Task {
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
    protected Void call() throws Exception {
        fxFields.sort(Comparator.comparing(FxField::getLayoutY));
        BadgeCreator badgeCreator = new BadgeCreator(fxFields,
                pdfPath,
                excelReader.getValues(),
                imageToPdfRatio,
                compressFieldIfLineMissing);
        BadgeArchive badgeArchive = new BadgeArchive(targetDirectoryPath, badgeCreator);
        final int numberOfFiles = excelReader.getValues().length;
        for(int i = excelReader.getHasHeadings() ? 1 : 0;
            i < numberOfFiles; i++) {
            if(isCancelled()) break;
            badgeArchive.createBadgeEntry(i);
            updateProgress(i, numberOfFiles + 1);
            updateMessage(String.format("Готов %d файл из %d", i, numberOfFiles));
        }
        updateProgress(numberOfFiles, numberOfFiles + 1);
        updateMessage("Создаю общий бейдж");
        badgeArchive.createCommonBadge();
        badgeArchive.getOutputStream().close();
        return null;
    }
}
