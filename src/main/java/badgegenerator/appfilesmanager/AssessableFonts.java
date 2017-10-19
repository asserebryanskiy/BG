package badgegenerator.appfilesmanager;

import com.sun.javafx.PlatformUtil;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class is responsible for searching local fonts and displaying all fonts that it has found
 * and that are also supported by JavaFx.
 */
public class AssessableFonts {
    private static final Logger logger = Logger.getLogger(AssessableFonts.class.getSimpleName());
    private static List<String> assessableFxFonts;
    private static Map<String, File> allAssessableFonts;

    public static List<String> getAssessableFxFonts() {
        if(assessableFxFonts == null) {
            process();
        }
        return assessableFxFonts;
    }

    public static String getFontPath(String fontName) {
        if(allAssessableFonts == null) {
            process();
        }
        return allAssessableFonts.get(fontName.toLowerCase()).getAbsolutePath();
    }

    private static void process() {
        if(PlatformUtil.isMac()) {
            File fontsDirectory1 = new File("/Library/Fonts");
            File fontsDirectory2 = new File("/System/Library/Fonts");
            File fontsDirectory3 = new File(System.getProperty("user.home"),
                    "/Library/Fonts");

            allAssessableFonts = Stream.of(fontsDirectory1.listFiles(),
                    fontsDirectory2.listFiles(),
                    fontsDirectory3.listFiles())
                    .flatMap(Stream::of)
                    .filter(file -> !file.getName().endsWith(".dir")
                            && !file.getName().endsWith(".list")
                            && !file.getName().endsWith(".scale")
                            && !file.getName().contains(".DS_Store"))
                    .collect(Collectors.toMap(file -> {
                        String fileName = file.getName().toLowerCase();
                        return fileName.substring(0, fileName.length() - 4);
                    }, file -> file));
        } else {
            File fontsDir = new File(System.getenv("WINDIR"), "Fonts");
            try {
                allAssessableFonts = Arrays.stream(fontsDir.listFiles())
                        .collect(Collectors.toMap(file -> {
                            String fileName = file.getName().toLowerCase();
                            return fileName.substring(0, fileName.length() - 4);
                        }, file -> file));
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        String.format("Не удалось найти шрифты в папке%s", fontsDir.getAbsolutePath()));
                alert.show();
                e.printStackTrace();
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE,
                        String.format("Не удалось найти шрифты в папке%s", fontsDir.getAbsolutePath()),
                        e);
            }
        }

        assessableFxFonts = Font.getFontNames().stream()
                .filter(name -> allAssessableFonts.keySet().contains(name.toLowerCase()))
                .collect(Collectors.toList());
        assessableFxFonts.add("Helvetica");
    }
}
