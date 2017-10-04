package badgegenerator.fileloader;

import badgegenerator.Main;
import badgegenerator.fxfieldsloader.FxFieldsLoaderController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FileLoaderController implements Initializable{
    @FXML
    private Button btnBrowseXlsx;
    @FXML
    private Button btnBrowsePdf;
    @FXML
    private Button btnLoadFields;
    @FXML
    private Button btnCreateNewFields;
    @FXML
    private Rectangle progressIndicatorBackground;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TextField xlsxField;
    @FXML
    private TextField pdfField;
    @FXML
    private CheckBox hasHeadingsCheckBox;
    @FXML
    private Text xlsxNotLoadedLabel;
    @FXML
    private Text pdfNotLoadedLabel;

    private String pdfPath;
    private String xlsxPath;
    private boolean hasHeadings;
    private ExcelReader excelReader;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(btnBrowsePdf);
        buttons.add(btnBrowseXlsx);
        buttons.add(btnCreateNewFields);
        buttons.add(btnLoadFields);
        buttons.forEach(btn -> btn.setMinWidth(
                Main.computeStringWidth(btn.getText(), btn.getFont())));
    }

    public void handleBrowseXlsx(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XLSX files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            xlsxField.setText(selectedFile.getName());
            xlsxPath = selectedFile.getAbsolutePath();
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleBrowsePdf(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            pdfField.setText(selectedFile.getName());
            pdfPath = selectedFile.getAbsolutePath();
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleProceed(MouseEvent event) throws IOException {
        if(checkIfFieldsAreFilled()) {
            showProgressScreen(true);
            final Stage pdfRedactorWindow = (Stage) ((Node) event.getSource()).getScene().getWindow();

            excelReader = new ExcelReader(xlsxPath, hasHeadings);
            Task checkExcelFileTask = new CheckExcelFileTask(excelReader);
            checkExcelFileTask.setOnSucceeded(e -> {
                if((boolean) checkExcelFileTask.getValue()) {
                    Task launchPdfEditorTask =
                            new LaunchPdfEditorTask(excelReader, pdfPath);
                    launchPdfEditorTask.setOnSucceeded(event1 -> {
                        showProgressScreen(false);
                        pdfRedactorWindow.setScene(
                                new Scene((Parent) launchPdfEditorTask.getValue()));
                        pdfRedactorWindow.setResizable(false);
                        pdfRedactorWindow.setX(pdfRedactorWindow.getX() - 200);
                        pdfRedactorWindow.setY(pdfRedactorWindow.getY() - 100);
                        pdfRedactorWindow.show();
                    });
                    launchPdfEditorTask.setOnFailed(c -> {
                        showProgressScreen(false);
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Не удалось создать поля.");
                        alert.show();
                    });
                    Thread thread = new Thread(launchPdfEditorTask);
                    thread.setDaemon(true);
                    thread.start();
                } else {
                    showProgressScreen(false);
                }
            });

            Thread thread = new Thread(checkExcelFileTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void handleOpenSavedFieldsScreen(ActionEvent event) {
        if(checkIfFieldsAreFilled()) {
            showProgressScreen(true);

            excelReader = new ExcelReader(xlsxPath, hasHeadings);
            Task checkExcelFileTask = new CheckExcelFileTask(excelReader);
            checkExcelFileTask.setOnSucceeded(e -> {
                if((boolean) checkExcelFileTask.getValue()) {
                    /*File savedFilesDirectory = new File(getClass()
                            .getResource("/savedFields").getFile());*/
                    List<String> savesNames = SavesLoader.getSavesNames();
                    if(savesNames == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Ошибка при чтении файлов сохранения");
                                alert.show();
                        return;
                    }
                    if(savesNames.size() != 0) {
                        final Stage savedFieldsWindow =
                                (Stage) ((Node) event.getSource()).getScene().getWindow();
                        FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("/fxml/FxFieldsLoader.fxml"));
                        Parent root;
                        try {
                            root = loader.load();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            try {
                                e1.printStackTrace(new PrintStream(
                                        new File(SavesLoader
                                                .getSavesFolder().getParentFile()
                                                .getAbsolutePath() + "log.txt")));
                            } catch (FileNotFoundException e2) {
                                e2.printStackTrace();
                            }
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    String.format("Не удалось загрузить сохраненные поля%n%s",
                                    e.toString()));
                            alert.show();
                            showProgressScreen(false);
                            return;
                        }
                        FxFieldsLoaderController controller = loader.getController();
                        controller.setSavedFieldsNames(savesNames);
                        controller.setExcelReader(excelReader);
                        controller.setPdfPath(pdfPath);
                        savedFieldsWindow.setScene(new Scene(root));
                        savedFieldsWindow.setResizable(false);
                        savedFieldsWindow.show();
                    } else {
                        showProgressScreen(false);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                "Нет сохраненных полей",
                                ButtonType.OK);
                        alert.show();
                    }
                } else {
                    showProgressScreen(false);
                }
            });

            Thread thread = new Thread(checkExcelFileTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private boolean checkIfFieldsAreFilled() {
        boolean fieldsAreFilled = true;
        if(pdfField.getText().isEmpty()) {
            fieldsAreFilled = false;
            pdfNotLoadedLabel.setVisible(true);
        } else pdfNotLoadedLabel.setVisible(false);
        if(xlsxField.getText().isEmpty()){
            fieldsAreFilled = false;
            xlsxNotLoadedLabel.setVisible(true);
        } else xlsxNotLoadedLabel.setVisible(false);
        return fieldsAreFilled;
    }

    public void handleSetHasHeadings() {
        hasHeadings = hasHeadingsCheckBox.isSelected();
    }

    private void showProgressScreen(boolean value) {
        progressIndicator.setVisible(value);
        progressIndicatorBackground.setVisible(value);
    }
}
