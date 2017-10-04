package badgegenerator.fxfieldssaver;

import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.SavesLoader;
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

/**
 * Class is used to create folders with serialized fields and their fonts.
 */
public class FxFieldsSaver {
    private final String bundleName;
    private List<FxField> fields;
    private String bundlePath;

    public FxFieldsSaver(List<FxField> fields,
                         String bundleName) {
        this.fields = fields;
        this.bundleName = bundleName;
        bundlePath = SavesLoader.getSavesFolder().getAbsolutePath()
                + "/" + bundleName;
    }

    public void createSave() {
        File directory = new File(bundlePath);
        directory.mkdir();
        fields.forEach(field -> {
            FxFieldSave fieldFile = new FxFieldSave(field);
            String filePath = String.format("%s/%s.fxf",
                    directory.getAbsolutePath(),
                    fieldFile.getNumberOfColumn());
            ObjectOutputStream oos;
            try {
                FileOutputStream fos = new FileOutputStream(filePath);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(fieldFile);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        String.format("Не удалось сохранить поле%n%s", e.toString()));
                alert.show();
                e.printStackTrace();
            }
        });

        File fontsDirectory = new File(directory.getAbsolutePath() + "/fonts");
        fontsDirectory.mkdir();
        Map<String, FxField> uniqueFontNames = new HashMap<>();
        for(FxField field : fields) {
            String fontName = field.getFont().getName();
            if(!fontName.equals("Helvetica")
                    && !uniqueFontNames.containsKey(fontName)) {
                uniqueFontNames.put(fontName, field);
            }
        }
        uniqueFontNames.keySet().forEach(key -> {
            String fontPath = uniqueFontNames.get(key).getFontPath();
            Path from = Paths.get(fontPath);
            Path to = Paths.get(String.format("%s/%s.%s",
                    fontsDirectory.getAbsolutePath(),
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
                e.printStackTrace();
            }
        });
    }
}
