package badgegenerator.appfilesmanager;

import badgegenerator.Main;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class that is used to access FxFields saves located outside of the jar file.
 * Saves folder is put in the same directory as jar file.
 */
public class SavesManager {
    private static Logger logger = Logger.getLogger(SavesManager.class.getSimpleName());

    private static File savesFolder;
    private static String currentSaveName;

    public static List<String> getSavesNames() {
        File savesFolder = getSavesFolder();
        if(savesFolder != null && savesFolder.listFiles() != null) {
            File[] saves = savesFolder.listFiles();
            if (saves.length > 5) {
                Arrays.sort(saves, Comparator.comparing(File::lastModified));
                for (int i = 5; i < saves.length; i++)
                    saves[i].delete();
            }
            return Arrays.stream(saves)
                    .map(File::getName)
                    .filter(fileName -> !fileName.startsWith("."))
                    .collect(Collectors.toList());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Не удалось найти папку с сохранениями");
            alert.show();
            logger.log(Level.WARNING, "Не удалось найти папку с сохранениями");
            return new ArrayList<>(0);
        }
    }

    public static File getSavesFolder() {
        if(savesFolder == null) {
           savesFolder = new File(Main.getAppFilesDirPath()
                   + File.separator
                   + "saves");
           if(!savesFolder.exists()) savesFolder.mkdir();
        }
        return savesFolder;
    }

    public static String getCurrentSaveName() {
        if(currentSaveName != null) return currentSaveName;
        else return "";
    }

    public static void setCurrentSaveName(String currentSaveName) {
        SavesManager.currentSaveName = currentSaveName;
    }
}
