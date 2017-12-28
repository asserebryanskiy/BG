package badgegenerator.helppopup;

import badgegenerator.appfilesmanager.HelpMessages;
import badgegenerator.appfilesmanager.LoggerManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Popup;
import javafx.stage.Screen;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HelpPopUp has custom design.
 */
public class HelpPopUp extends Popup{
    private static final Logger logger = Logger.getLogger(HelpPopUp.class.getSimpleName());
    private String parentsNodeId;

    public HelpPopUp(Node node) {
        parentsNodeId = node.getId();
        String message = HelpMessages.getMessage(parentsNodeId);
        preparePopup(message);
    }

    public HelpPopUp(String message) {
        preparePopup(message);
    }

    private void preparePopup(String message) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HelpPopUp.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Не удалось отобразить окно с подсказкой");
            alert.show();
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Ошибка при загрузке HelpPopUp.fxml", e);
            return;
        }
        HelpPopUpController controller = loader.getController();
        controller.setContent(message);

        getContent().add(root);

        setOnShown(event -> {
            HelpPopUp source = (HelpPopUp) event.getSource();
            double screenX = source.getX() + source.getWidth();
            double screenRightBorder = Screen.getPrimary().getBounds().getMaxX();
            if(screenX + 100 > screenRightBorder) setX(screenX - 2 * getWidth());
        });
    }

    public String getParentsNodeId() {
        return parentsNodeId;
    }
}
