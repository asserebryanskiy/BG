package badgegenerator.fxfieldsloader;

import badgegenerator.fxfieldssaver.FxFieldSave;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class is used to construct FxFieldSave objects out of saves (.fxf files and saved fonts).
 */
public class FxFieldsLoader {
    private FxFieldsLoader() {
    }

    public static List<FxFieldSave> load(String savePath) {
        File directory = new File(savePath);

        return Arrays.stream(directory.list())
                .filter(name -> name.contains(".fxf"))
                .map(fileName -> {
                    try {
                        FileInputStream fis =
                                new FileInputStream(String.format("%s/%s",
                                        savePath, fileName));
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        FxFieldSave fieldFile = (FxFieldSave) ois.readObject();
                        if(!fieldFile.getFontName().equals("Helvetica")) {
                            String fontPath = searchForFont(savePath,
                                    fieldFile.getFontName());
                            fieldFile.setFontPath(fontPath);
                            FileInputStream fontInStream = new FileInputStream(fontPath);
                            fieldFile.setFont(Font.loadFont(fontInStream, 13));
                        }
                        return fieldFile;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    private static String searchForFont(String savePath, String fontName) throws Exception {
        File directory = new File(savePath + "/fonts");
        return Arrays.stream(directory.list())
                .filter(fileName ->
                        fileName.substring(0, fileName.length() - 4).equals(fontName))
                .map(fileName -> savePath + "/fonts/" + fileName)
                .findFirst()
                .orElseThrow(Exception::new);
    }
}
