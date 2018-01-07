package badgegenerator.fileloader;

import badgegenerator.Main;
import badgegenerator.appfilesmanager.AssessableFonts;
import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.fxfieldsloader.FxFieldsLoaderController;
import badgegenerator.helppopup.HelpPopUp;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileLoaderController implements Initializable{
    private static Logger logger = Logger.getLogger(FileLoaderController.class.getSimpleName());

    @FXML
    private StackPane root;
    @FXML
    private Button btnBrowseXlsx;
    @FXML
    private Button btnBrowsePdf;
    @FXML
    private Button btnBrowseEmptyPdf;
    @FXML
    private Button btnLoadFields;
    @FXML
    private Button btnCreateNewFields;
    @FXML
    private Rectangle progressIndicatorBackground;
    @FXML
    private StackPane loadingScreen;
    @FXML
    private Text loaderMessage;
    @FXML
    private TextField excelFileField;
    @FXML
    private TextField pdfField;
    @FXML
    private TextField emptyPdfField;
    @FXML
    private Text excelNotLoadedLabel;
    @FXML
    private Text emptyPdfNotLoadedLabel;
    @FXML
    private Text pdfNotLoadedLabel;
    private String pdfPath;
    private String excelFilePath;
    private ExcelReader excelReader;
    private HelpPopUp helpPopUp;
    private Thread loadFontsThread;
    private String emptyPdfPath;
    private File lastFolder;            // last opened with fileChooser directory

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(btnBrowsePdf);
        buttons.add(btnBrowseXlsx);
        buttons.add(btnCreateNewFields);
        buttons.add(btnLoadFields);
        buttons.add(btnBrowseEmptyPdf);
        buttons.forEach(btn -> btn.setMinWidth(Main
                .computeStringWidth(btn.getText(), btn.getFont()) + 10));
        Task<Void> loadFontsTasks = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                AssessableFonts.process();
                return null;
            }
        };
        loadFontsThread = new Thread(loadFontsTasks);
        loadFontsThread.setDaemon(true);
        loadFontsThread.start();
    }

    public void handleBrowseExcel() {
        FileChooser fileChooser = new FileChooser();
        if (lastFolder != null) fileChooser.setInitialDirectory(lastFolder);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel files", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            excelFileField.setText(selectedFile.getName());
            excelFilePath = selectedFile.getAbsolutePath();
            lastFolder = selectedFile.getParentFile();
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void browseEmptyPdf() {
        FileChooser fileChooser = new FileChooser();
        if (lastFolder != null) fileChooser.setInitialDirectory(lastFolder);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            emptyPdfField.setText(selectedFile.getName());
            emptyPdfPath = selectedFile.getAbsolutePath();
            lastFolder = selectedFile.getParentFile();
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleBrowsePdf() {
        FileChooser fileChooser = new FileChooser();
        if (lastFolder != null) fileChooser.setInitialDirectory(lastFolder);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            pdfField.setText(selectedFile.getName());
            pdfPath = selectedFile.getAbsolutePath();
            lastFolder = selectedFile.getParentFile();
        } else {
            System.out.println("File is incorrect");
        }

    }

    public void handleProceed(MouseEvent event) throws IOException {
        if(checkIfFieldsAreFilled()) {
            showProgressScreen(true);
            final Stage pdfRedactorWindow = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if(loadFontsThread.isAlive()) {
                loaderMessage.setText("Загружаю шрифты");
                try {
                    loadFontsThread.join();
                } catch (InterruptedException e) {
                    LoggerManager.initializeLogger(logger);
                    logger.log(Level.SEVERE, e.toString(), e);
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Не удалось загрузить шрифты");
                    alert.show();
                    e.printStackTrace();
                }
            }

            excelReader = new ExcelReader(excelFilePath);
            Task checkExcelFileTask = new CheckExcelFileTask(excelReader);
            loaderMessage.textProperty().bind(checkExcelFileTask.messageProperty());
            checkExcelFileTask.setOnSucceeded(e -> {
                if((boolean) checkExcelFileTask.getValue()) {
                    Task launchPdfEditorTask = new LaunchPdfEditorTask(excelReader, pdfPath, emptyPdfPath);
                    loaderMessage.textProperty().unbind();
                    loaderMessage.textProperty().bind(launchPdfEditorTask.messageProperty());
                    launchPdfEditorTask.setOnSucceeded(event1 -> {
                        showProgressScreen(false);
                        Parent parent = (Parent) launchPdfEditorTask.getValue();
                        if (parent != null) {
                            pdfRedactorWindow.setScene(new Scene(parent));
                            pdfRedactorWindow.setResizable(false);
                            pdfRedactorWindow.show();
                            pdfRedactorWindow.centerOnScreen();
                        }
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

            excelReader = new ExcelReader(excelFilePath);
            Task checkExcelFileTask = new CheckExcelFileTask(excelReader);
            loaderMessage.textProperty().bind(checkExcelFileTask.messageProperty());
            checkExcelFileTask.setOnSucceeded(e -> {
                if((boolean) checkExcelFileTask.getValue()) {
                    loaderMessage.textProperty().unbind();
                    loaderMessage.setText("Загружаю сохранения");
                    List<String> savesNames = SavesManager.getSavesNames();
                    if(savesNames == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Ошибка при чтении файлов сохранения");
                        alert.show();
                        LoggerManager.initializeLogger(logger);
                        logger.log(Level.WARNING, "Ошибка при чтении файлов сохранения");
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
                            LoggerManager.initializeLogger(logger);
                            logger.log(Level.SEVERE,
                                    "Ошибка при загрузке при загрузке FxFieldsLoader.fxml",
                                    e1);
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
                        controller.setEmptyPdfPath(emptyPdfPath);
                        savedFieldsWindow.setScene(new Scene(root));
                        savedFieldsWindow.setResizable(false);
                        savedFieldsWindow.show();
                        savedFieldsWindow.centerOnScreen();
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
        if(emptyPdfField.getText().isEmpty()) {
            fieldsAreFilled = false;
            emptyPdfNotLoadedLabel.setVisible(true);
        } else emptyPdfNotLoadedLabel.setVisible(false);
        if(excelFileField.getText().isEmpty()){
            fieldsAreFilled = false;
            excelNotLoadedLabel.setVisible(true);
        } else excelNotLoadedLabel.setVisible(false);
        return fieldsAreFilled;
    }

    private void showProgressScreen(boolean value) {
        progressIndicatorBackground.setWidth(root.getScene().getWidth());
        progressIndicatorBackground.setHeight(root.getScene().getHeight());
        loadingScreen.setVisible(value);
    }

    public void handleChangeColor(MouseEvent event) {
        ((SVGPath)((Pane) event.getSource()).getChildren().get(0)).setFill(Color.GRAY);
    }

    public void handleResetColor(MouseEvent event) {
        ((SVGPath)((Pane) event.getSource()).getChildren().get(0)).setFill(Color.BLACK);
    }

    public void handleShowHelpBox(MouseEvent event) throws IOException {
        if(helpPopUp != null && helpPopUp.isShowing()) return;
        helpPopUp = new HelpPopUp((Node) event.getSource());
        helpPopUp.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
    }
}
