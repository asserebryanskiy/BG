package badgegenerator.fxfieldsloader;

import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.fxfieldssaver.FxFieldSave;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class is used to construct FxFieldSave objects out of saves (.fxf files and saved fonts).
 */
public class FxFieldsLoader {
    private static Logger logger = Logger.getLogger(FxFieldsLoader.class.getSimpleName());

    private FxFieldsLoader() {
    }

    public static Map<String, FxFieldSave> load(String savePath) {
        File directory = new File(savePath);

        return Arrays.stream(directory.list())
                .filter(name -> name.contains(".fxf"))
                .map(fileName -> {
                    try {
                        FileInputStream fis =
                                new FileInputStream(String.format("%s%s%s",
                                        savePath, File.separator, fileName));
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        FxFieldSave fieldFile = (FxFieldSave) ois.readObject();
                        ois.close();
                        fis.close();
                        if(!fieldFile.getFontName().equals("Circe Light")) {
                            String fontPath = searchForFont(savePath,
                                    fieldFile.getFontName());
                            fieldFile.setFontPath(fontPath);
                        }
                        return fieldFile;
                    } catch (Exception e) {
                        LoggerManager.initializeLogger(logger);
                        logger.log(Level.SEVERE, "Ошибка при загрузке сохранения", e);
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toMap(FxFieldSave::getColumnId, fieldFile -> fieldFile));
    }

    private static String searchForFont(String savePath, String fontName) throws Exception {
        File directory = new File(savePath
                + File.separator
                + "fonts");
        return Arrays.stream(directory.list())
                .filter(fileName ->
                        fileName.substring(0, fileName.length() - 4).equals(fontName))
                .map(fileName -> String.format("%s%s%s%s%s",
                        savePath, File.separator, "fonts", File.separator, fileName))
                .findFirst()
                .orElseThrow(Exception::new);
    }
}
