package badgegenerator.fxfieldssaver;

import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FxField;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class is used to create folders with serialized fields and their fonts.
 */
public class FxFieldsSaver {
    private static Logger logger = Logger.getLogger(FxFieldsSaver.class.getSimpleName());

    private FxFieldsSaver() {
    }

    public static void createSave(List<FxField> fields, String bundleName) {
        File saveDir = new File(SavesManager.getSavesFolder().getAbsolutePath()
                + File.separator
                + bundleName);
        saveDir.mkdir();
        fields.forEach(field -> {
            FxFieldSave fieldFile = new FxFieldSave(field);
            String filePath = String.format("%s%s%s.fxf",
                    saveDir.getAbsolutePath(),
                    File.separator,
                    fieldFile.getColumnId());
            ObjectOutputStream oos;
            try(FileOutputStream fos = new FileOutputStream(filePath)) {
                oos = new ObjectOutputStream(fos);
                oos.writeObject(fieldFile);
                oos.close();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Не удалось сохранить поле");
                alert.show();
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE,
                        String.format("Ошибка при сохранении поля%d", field.getColumnId()),
                        e);
            }
        });

        File fontsDirectory = new File(saveDir.getAbsolutePath()
                + File.separator
                + "fonts");
        fontsDirectory.mkdir();
        Map<String, FxField> uniqueFontNames = new HashMap<>();
        for(FxField field : fields) {
            String fontName = field.getFont().getName();
            if(!fontName.equals("Circe Light")
                    && !uniqueFontNames.containsKey(fontName)) {
                uniqueFontNames.put(fontName, field);
            }
        }
        uniqueFontNames.keySet().forEach(key -> {
            String fontPath = uniqueFontNames.get(key).getFontPath();
            Path from = Paths.get(fontPath);
            Path to = Paths.get(String.format("%s%s%s.%s",
                    fontsDirectory.getAbsolutePath(),
                    File.separator,
                    key,
                    fontPath.substring(fontPath.length() - 3, fontPath.length())));
            try {
                Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Failed saving font file");
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        String.format("Не удалось сохранить файл шрифта%n%s",
                                e.toString()));
                alert.show();
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE,
                        String.format("Ошибка при сохранении шрифта в %s", fontPath),
                        e);
            }
        });
    }
}
