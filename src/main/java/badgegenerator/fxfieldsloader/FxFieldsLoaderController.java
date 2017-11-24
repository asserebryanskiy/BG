package badgegenerator.fxfieldsloader;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.LaunchPdfEditorTask;
import com.sun.javafx.tk.Toolkit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FxFieldsLoaderController implements Initializable{
    private final String PLACEHOLDER = "--Нет сохранения--";

    @FXML
    private StackPane root;
    @FXML
    private ListView<Text> savedFields;
    @FXML
    private Rectangle loaderBackground;
    @FXML
    private StackPane loaderScreen;
    @FXML
    private Text loaderMessage;
    @FXML
    private VBox btnBox;

    private ExcelReader excelReader;
    private String pdfPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final double longestBtnWidth = Toolkit.getToolkit().getFontLoader()
                .computeStringWidth(((Button) btnBox.getChildren().get(0)).getText(),
                        ((Button) btnBox.getChildren().get(0)).getFont()) + 40;
        btnBox.getChildren().forEach(btn -> ((Button) btn).setPrefWidth(longestBtnWidth));
    }

    public void setSavedFieldsNames(List<String> savedFieldsNames) {
//        ObservableList<Text> values = FXCollections.observableArrayList(savedFieldsNames);
        ObservableList<Text> values = savedFieldsNames.stream()
                .map(Text::new)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        if(savedFieldsNames.size() < 5) {
            for(int i = savedFieldsNames.size(); i < 5; i++) {
                Text text = new Text(PLACEHOLDER);
                text.setFill(Color.GRAY);
                values.add(text);
            }
        }
        savedFields.setItems(values);
    }

    public void handleLoadField(ActionEvent event) throws IOException {
        String saveName = savedFields.getSelectionModel().getSelectedItem().getText();
        if(saveName != null && !saveName.equals(PLACEHOLDER)) {
            File save = new File(SavesManager.getSavesFolder().getAbsolutePath()
                    + "/" + saveName);
            SavesManager.setCurrentSaveName(saveName);
            if(save.list().length - 1 ==
                    excelReader.getLargestFields().length) {
                // getSavesNames fields
                showProgressScreen(true);
                final Stage pdfRedactorWindow =
                        (Stage) ((Node) event.getSource()).getScene().getWindow();
                Task launchPdfEditorTask = new LaunchPdfEditorTask(excelReader,
                        pdfPath,
                        save.getAbsolutePath());
                loaderMessage.textProperty().bind(launchPdfEditorTask.messageProperty());
                launchPdfEditorTask.setOnSucceeded(event1 -> {
                    showProgressScreen(false);
                    if(launchPdfEditorTask.getValue() != null) {
                        pdfRedactorWindow.setScene(
                                new Scene((Parent) launchPdfEditorTask.getValue()));
                        pdfRedactorWindow.setResizable(false);
                        pdfRedactorWindow.setX(pdfRedactorWindow.getX() - 200);
                        pdfRedactorWindow.setY(pdfRedactorWindow.getY() - 100);
                        pdfRedactorWindow.show();
                        pdfRedactorWindow.centerOnScreen();
                    }
                });
                Thread thread = new Thread(launchPdfEditorTask);
                thread.setDaemon(true);
                thread.start();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Количество колонок в excel и количество сохраненных полей различается",
                        ButtonType.OK);
                alert.show();
            }
        }
    }

    private void showProgressScreen(boolean value) {
        loaderBackground.setWidth(root.getScene().getWidth());
        loaderBackground.setHeight(root.getScene().getHeight());
        loaderScreen.setVisible(value);
    }

    public void setExcelReader(ExcelReader excelReader) {
        this.excelReader = excelReader;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public void handleLoadNewFields(ActionEvent event) throws IOException {
        showProgressScreen(true);
        final Stage pdfRedactorWindow =
                (Stage) ((Node) event.getSource()).getScene().getWindow();
        Task launchPdfEditorTask = new LaunchPdfEditorTask(excelReader, pdfPath);
        loaderMessage.textProperty().bind(launchPdfEditorTask.messageProperty());
        launchPdfEditorTask.setOnSucceeded(event1 -> {
            showProgressScreen(false);
            if(launchPdfEditorTask.getValue() != null) {
                pdfRedactorWindow.setScene(
                        new Scene((Parent) launchPdfEditorTask.getValue()));
                pdfRedactorWindow.setResizable(false);
                pdfRedactorWindow.setX(pdfRedactorWindow.getX() - 200);
                pdfRedactorWindow.setY(pdfRedactorWindow.getY() - 100);
                pdfRedactorWindow.show();
            }
        });
        Thread thread = new Thread(launchPdfEditorTask);
        thread.setDaemon(true);
        thread.start();
    }
}
