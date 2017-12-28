package badgegenerator.appfilesmanager;

import com.sun.javafx.PlatformUtil;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class is used to load help messages. Then it provides API
 * to get messages by id of node requesting message.
 */
public class HelpMessages {
    private static Logger logger = Logger.getLogger(HelpMessages.class.getSimpleName());
    private static Map<String, String> helpMessages;

    public static void load() {
        helpMessages = new HashMap<>();
        try(Scanner scanner = new Scanner(
                HelpMessages.class
                        .getResourceAsStream("/helpMessages.txt"), "UTF8")) {
            while(scanner.hasNext()) {
                String line = scanner.nextLine();
                String key = line.substring(0, line.indexOf("|"));
                String value = line.substring(line.indexOf("|") + 1, line.length());
                if(helpMessages.containsKey(key)) {
                    helpMessages.merge(key, value, String::concat);
                } else helpMessages.put(key, value);
            }
            String fontHelpMessage = PlatformUtil.isMac() ? "Программа ищет шрифты в папках:%n"
                    + " - /System/Library/Fonts%n"
                    + " - /Library/Fonts%n"
                    + " - User/Library/Fonts%n"
                    : String.format("Программа ищет шрифты в папке %s%n",
                    new File(System.getenv("WINDIR"), "Fonts").getAbsolutePath());
            helpMessages.put("fontsHelpIcon", fontHelpMessage);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Не удалось загрузить текст подсказок");
            alert.show();
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static String getMessage(String nodeId) {
        return String.format(helpMessages.get(nodeId));
    }
}
