package badgegenerator.pdfeditor;

import badgegenerator.ModelSingleton;
import javafx.concurrent.Task;

import java.util.Comparator;
import java.util.List;

/**
 * Task is designed to make time consuming creation of archive be done behind the UI
 */
public class CreateBadgeArchiveTask extends Task {
    private final String targetDirectoryPath;
    private final List<Field> fields;
    private final double imageToPdfRatio;
    private final boolean compressFieldIfLineMissing;

    CreateBadgeArchiveTask(List<Field> fields,
                           double imageToPdfRatio,
                           String targetDirectoryPath,
                           boolean compressFieldIfLineMissing) {
        this.fields = fields;
        this.imageToPdfRatio = imageToPdfRatio;
        this.targetDirectoryPath = targetDirectoryPath;
        this.compressFieldIfLineMissing = compressFieldIfLineMissing;
    }

    @Override
    protected Void call() throws Exception {
        fields.forEach(Field::calculateRgbColor);
        fields.sort(Comparator.comparing(Field::getLayoutY));
        BadgeCreator badgeCreator = new BadgeCreator(fields,
                ModelSingleton.getInstance().getSrcPdfPath(),
                ModelSingleton.getInstance().getExcelReader().getValues(),
                imageToPdfRatio,
                compressFieldIfLineMissing);
        BadgeArchive badgeArchive = new BadgeArchive(targetDirectoryPath, badgeCreator);
        final int numberOfFiles = ModelSingleton.getInstance()
                .getExcelReader().getValues().length;
        for(int i = ModelSingleton.getInstance().getExcelReader().getHasHeadings() ? 1 : 0;
            i < numberOfFiles; i++) {
            if(isCancelled()) break;
            updateProgress(i, numberOfFiles + 1);
            updateMessage(String.format("Готов %d файл из %d", i, numberOfFiles));
            badgeArchive.createBadgeEntry(i);
        }
        updateProgress(numberOfFiles, numberOfFiles + 1);
        updateMessage("Создаю общий бейдж");
        badgeArchive.createCommonBadge();
        badgeArchive.getOutputStream().close();
        return null;
    }
}
