package badgegenerator.fileloader;

import badgegenerator.Main;

import java.io.File;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that is used to access FxFields saves located outside of the jar file.
 * Saves folder is put in the same directory as jar file.
 */
public class SavesLoader {
    private static File savesFolder;
    private static String currentSaveName;

    public static List<String> getSavesNames() {
        return Arrays.stream(getSavesFolder().list())
                .filter(fileName -> !fileName.startsWith("."))
                .collect(Collectors.toList());
    }

    public static File getSavesFolder() {
        if(savesFolder == null) {
            CodeSource src = Main.class.getProtectionDomain().getCodeSource();
            String jarPath = src.getLocation().getPath();
            File file = new File(jarPath);
            String savesPath = file.getParentFile().getPath() + "/saves";
            savesFolder = new File(savesPath);
            if(!savesFolder.exists()) savesFolder.mkdir();
        }
        return savesFolder;
    }

    public static String getCurrentSaveName() {
        if(currentSaveName != null) return currentSaveName;
        else return "";
    }

    public static void setCurrentSaveName(String currentSaveName) {
        SavesLoader.currentSaveName = currentSaveName;
    }
}
