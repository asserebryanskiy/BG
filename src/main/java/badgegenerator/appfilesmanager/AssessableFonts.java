package badgegenerator.appfilesmanager;

import com.sun.javafx.PlatformUtil;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    private static Map<String, String> allAssessableFonts;

    public static Set<String> getFontsNames() {
        if(allAssessableFonts == null) {
            process();
        }
        return allAssessableFonts.keySet();
    }

    public static String getFontPath(String fontName) {
        if(allAssessableFonts == null) {
            process();
        }
        String fontPath = allAssessableFonts.get(fontName);
        // is needed because sime fonts has awkward names!
        // e.g. Plumb-Regular instead of Plumb Regular
        if (fontPath == null) {
            fontPath = allAssessableFonts.keySet().stream()
                    .filter(name -> name.equals(getJoinedWithDelimiter(fontName, "-"))
                            || name.equals(getJoinedWithDelimiter(fontName, "_"))
                            || name.equals(getJoinedWithDelimiter(fontName, "+")))
                    .findAny()
                    .orElse(null);
        }
        return fontPath;
    }

    private static String getJoinedWithDelimiter(String fontName, String delimiter) {
        String[] words = fontName.split("\\s");
        return Arrays.stream(words).collect(Collectors.joining(delimiter));
    }

    public synchronized static void process() {
        Stream<File> stream;
        if(PlatformUtil.isMac()) {
            File fontsDirectory1 = new File("/Library/Fonts");
            File fontsDirectory2 = new File("/System/Library/Fonts");
            File fontsDirectory3 = new File(System.getProperty("user.home"),
                    "/Library/Fonts");
            stream = Stream.of(fontsDirectory1.listFiles(),
                    fontsDirectory2.listFiles(),
                    fontsDirectory3.listFiles())
                    .flatMap(Stream::of)
                    .filter(file -> !file.getName().endsWith(".dir")
                            && !file.getName().endsWith(".list")
                            && !file.getName().endsWith(".scale")
                            && !file.getName().contains(".DS_Store"));
        } else {
            File fontsDir = new File(System.getenv("WINDIR"), "Fonts");
            try {
                stream = Arrays.stream(fontsDir.listFiles());
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        String.format("Не удалось найти шрифты в папке%s", fontsDir.getAbsolutePath()));
                alert.show();
                e.printStackTrace();
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE,
                        String.format("Не удалось найти шрифты в папке%s", fontsDir.getAbsolutePath()),
                        e);
                return;
            }
        }
        allAssessableFonts = new HashMap<>();
        stream.forEach(file -> {
            try {
                String fileName = file.getName().toLowerCase();
                if(fileName.endsWith(".ttf")
                        || fileName.endsWith(".afm")
                        || fileName.endsWith(".otf")
                        || fileName.endsWith(".pfm")
                        || fileName.endsWith(".ttc")) {
                    Font font = Font.loadFont(new FileInputStream(file), 10);
                    String fontName = font.getName();
                    allAssessableFonts.putIfAbsent(fontName, file.getAbsolutePath());
                }
            } catch (Exception ignored) {
            }
        });

        allAssessableFonts.putIfAbsent("Circe Light", null);
    }
}
