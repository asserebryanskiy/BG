package badgegenerator.fxfieldssaver;

import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FxField;
import com.sun.javafx.tk.Toolkit;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class FxFieldsSaverController {
    private static final Logger logger = Logger.getLogger(FxFieldsSaverController.class.getSimpleName());

    private static final Color DARK_COLOR = Color.rgb(62, 41, 54);
    private final String PLACEHOLDER = "--Нет сохранения--";

    @FXML
    private VBox savesBox;
    @FXML
    private VBox btnBox;

    private File srcDirectory;
    private List<FxField> fields;
    private Stage stage;
    private int lastClickedItemIndex;
    private List<String> saveNames;
    private List<Integer> saveIndices = new ArrayList<>();

    public void init(List<FxField> fields, Stage stage) {
        this.fields = fields;
        this.stage = stage;
        srcDirectory = SavesManager.getSavesFolder();
        saveNames = SavesManager.getSavesNames();
        if(saveNames.size() > 0) {
            IntStream.range(0, saveNames.size())
                    .forEach(i -> {
                        TextField textField = (TextField) savesBox.getChildren().get(i);
                        textField.setText(saveNames.get(i));
                        textField.setId(String.valueOf(i));
                        textField.setFocusTraversable(false);
                        textField.setEditable(false);
                        saveIndices.add(i);
                    });
        }
        if(saveIndices.size() < 5) {
            for(int i = saveIndices.size(); i < 5; i++) {
                TextField textField = (TextField) savesBox.getChildren().get(i);
                textField.setPromptText(PLACEHOLDER);
                textField.setId(String.valueOf(i));
            }
            lastClickedItemIndex = saveIndices.size();
        }
        DropShadow shadow = new DropShadow();
        shadow.setColor(DARK_COLOR);
        shadow.setSpread(1);
        shadow.setRadius(0);
        shadow.setOffsetX(5);
        shadow.setOffsetY(0);
        savesBox.getChildren().forEach(node -> node.setOnMousePressed(event -> {
            lastClickedItemIndex = Integer.parseInt(node.getId());
            savesBox.getChildren().forEach(n -> n.setEffect(null));
            node.setEffect(shadow);
        }));
        final double longestBtnWidth = Toolkit.getToolkit().getFontLoader()
                .computeStringWidth(((Button) btnBox.getChildren().get(0)).getText(),
                        ((Button) btnBox.getChildren().get(0)).getFont()) + 40;
        btnBox.getChildren().forEach(btn -> ((Button) btn).setPrefWidth(longestBtnWidth));
    }

    public void handleSaveBtn() throws IOException {
        TextField clickedField = (TextField) savesBox.getChildren()
                .get(lastClickedItemIndex);
        if(saveIndices.contains(lastClickedItemIndex)) {
            return;
        } else if (saveNames.contains(clickedField.getText())) {
            clickedField.setText("");
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Сохранение с таким именем уже существует.");
            alert.show();
            return;
        } else if(clickedField.getText().startsWith(".")) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Имя сохранения не может начинаться с точки.");
            alert.show();
            return;
        } else if(clickedField.getText().contains("/")
                || clickedField.getText().contains("\\")) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Имя сохранения не может содержать\"/\" и \"\\\".");
            alert.show();
            return;
        }
        else if(clickedField.getText().isEmpty()) return;

        FxFieldsSaver.createSave(fields, clickedField.getText());
        stage.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Настройки сохранены");
        alert.show();
    }

    public void handleDeleteSave()  {
        TextField clickedField = (TextField) savesBox.getChildren()
                .get(lastClickedItemIndex);
        if (!saveIndices.contains(lastClickedItemIndex)) return;
        else if(SavesManager.getCurrentSaveName().equals(clickedField.getText())) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Нельзя удалить текущее сохранение.");
            alert.show();
            return;
        }
        saveNames.remove(clickedField.getText());
        try {
            delete(srcDirectory.getAbsolutePath()
                    + File.separator
                    + clickedField.getText());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Не удалось удалить сохранение");
            alert.show();
            LoggerManager.initializeLogger(logger);
            logger.log(Level.SEVERE, "Ошибка при удалении сохранения", e);
        }
        clickedField.setText("");
        clickedField.setPromptText(PLACEHOLDER);
        clickedField.setEditable(true);
        saveIndices.remove(new Integer(lastClickedItemIndex));
    }

    public void handleCancel() {
        stage.close();
    }

    private void delete(String dirPath) throws IOException {
        Path directory = Paths.get(dirPath);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
