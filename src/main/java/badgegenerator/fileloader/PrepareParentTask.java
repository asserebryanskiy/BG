package badgegenerator.fileloader;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Launches next stage from fxml file;
 */
public class PrepareParentTask extends Task {
    private final String fxmlFilePath;

    public PrepareParentTask(String fxmlFilePath) {
        this.fxmlFilePath = fxmlFilePath;
    }

    @Override
    protected Parent call() throws Exception {
        return FXMLLoader.load(getClass().getResource(fxmlFilePath));
    }
}
