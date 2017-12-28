package badgegenerator.pdfeditor;

import badgegenerator.appfilesmanager.AssessableFonts;
import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.IllegalFontSizeException;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfField;
import badgegenerator.fxfieldssaver.FxFieldsSaverController;
import badgegenerator.helppopup.HelpPopUp;
import badgegenerator.pdfcreator.CreateBadgeArchiveTask;
import com.sun.javafx.PlatformUtil;
import com.sun.javafx.tk.Toolkit;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PdfEditorController {
    private static Logger logger = Logger.getLogger(PdfEditorController.class.getSimpleName());

    @FXML
    private Button btnBrowseFont;

    @FXML
    private GridPane pdfRedactorRoot;

    @FXML
    private ImageView pdfPreview;

    @FXML
    private StackPane editingArea;

    @FXML
    private Pane verticalScaleBar;

    @FXML
    private Pane horizontalScaleBar;

    @FXML
    private TextField fontNameField;

    @FXML
    private TextField fontSizeField;

    @FXML
    private ColorPicker fontColorPicker;

    @FXML
    private Rectangle progressIndicatorBackground;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button cancelButton;

    @FXML
    private VBox progressIndicatorBox;

    @FXML
    private Label progressStatusLabel;

    @FXML
    private CheckBox capsLockCheckBox;

    @FXML
    private Button saveFieldsBtn;

    @FXML private Button leftAlignmentButton;
    @FXML private Button centerAlignmentButton;
    @FXML private Button rightAlignmentButton;

    @FXML
    private MenuBar menuBar;

    @FXML private CheckMenuItem visualizeGridCheckMenuItem;
    @FXML private CheckMenuItem alignFieldsCheckMenuItem;

    @FXML private RadioButton bindingYesButton;
    @FXML private RadioButton bindingNoButton;
    @FXML private CheckMenuItem bindingCheckMenuItem;

    @FXML private StackPane alertPane;

    private List<FxField> fxFields;

    private double imageToPdfRatio;
    private Task createBadgesArchiveTask;
    private static FileChooser fileChooser = new FileChooser();
    private BooleanProperty compressFieldIfLineMissing
            = new SimpleBooleanProperty(true);
    private String pdfPath;
    private ExcelReader excelReader;
    private List<HelpPopUp> helpPopUps = new ArrayList<>();
    private List<Button> alignmentButtons;
    private List<Line> gridLines = new ArrayList<>();
    private AlertCenter alertCenter;

    public void init(String savesPath, Map<String, PdfField> fields) {
        alertCenter = new AlertCenter(alertPane);
        FieldsLayouter layouter = new FieldsLayouter(editingArea,
                alertCenter,
                excelReader,
                savesPath,
                fields,
                imageToPdfRatio);
        fxFields = layouter.getFxFields();

        ScaleMarks.addTo(editingArea, verticalScaleBar, horizontalScaleBar,
                imageToPdfRatio, fxFields, gridLines);

        // adds possibility to remove selection
        pdfRedactorRoot.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (fxFields.stream()
                    .anyMatch(field -> event.getSceneX() >= field.getMouseX()
                            && event.getSceneX() <= field.getMouseX() + field.getPrefWidth()
                            && event.getSceneY() <= field.getMouseY()
                            && event.getSceneY() >= field.getMouseY() - field.getMaxHeight())) {
                return;
            }
            fxFields.forEach(f -> f.setSelected(false));
        });

        // sets default values
        SVGPath leftAlignmentSvg = new SVGPath();
        SVGPath centerAlignmentSvg = new SVGPath();
        SVGPath rightAlignmentSvg = new SVGPath();
        leftAlignmentSvg.setContent("M17.986,16.001c0,0.274-0.223,0.499-0.5,0.499H0.5c-0.275,0-0.5-0.225-0.5-0.499  V14c0-0.275,0.225-0.5,0.5-0.5h16.986c0.277,0,0.5,0.225,0.5,0.5V16.001z M21.986,8c0-0.275-0.223-0.5-0.5-0.5H0.5  C0.225,7.5,0,7.725,0,8v2.001C0,10.275,0.225,10.5,0.5,10.5h20.986c0.277,0,0.5-0.225,0.5-0.499V8z M13.986,2  c0-0.275-0.223-0.5-0.5-0.5H0.5C0.225,1.5,0,1.725,0,2v2.001C0,4.275,0.225,4.5,0.5,4.5h12.986c0.277,0,0.5-0.225,0.5-0.499V2z   M24,20c0-0.275-0.225-0.5-0.5-0.5h-23C0.225,19.5,0,19.725,0,20v2.001C0,22.275,0.225,22.5,0.5,22.5h23  c0.275,0,0.5-0.225,0.5-0.499V20z");
        centerAlignmentSvg.setContent("M4,14c0-0.275,0.225-0.5,0.5-0.5h15c0.275,0,0.5,0.225,0.5,0.5v2.001  c0,0.274-0.225,0.499-0.5,0.499h-15c-0.275,0-0.5-0.225-0.5-0.499V14z M0,10.001C0,10.275,0.225,10.5,0.5,10.5h23  c0.275,0,0.5-0.225,0.5-0.499V8c0-0.275-0.225-0.5-0.5-0.5h-23C0.225,7.5,0,7.725,0,8V10.001z M4,4.001C4,4.275,4.225,4.5,4.5,4.5  h15c0.275,0,0.5-0.225,0.5-0.499V2c0-0.275-0.225-0.5-0.5-0.5h-15C4.225,1.5,4,1.725,4,2V4.001z M0,22.001  C0,22.275,0.225,22.5,0.5,22.5h23c0.275,0,0.5-0.225,0.5-0.499V20c0-0.275-0.225-0.5-0.5-0.5h-23C0.225,19.5,0,19.725,0,20V22.001z");
        rightAlignmentSvg.setContent("M6.014,14c0-0.274,0.223-0.5,0.5-0.5H23.5c0.275,0,0.5,0.225,0.5,0.5v2.001  c0,0.274-0.225,0.499-0.5,0.499H6.514c-0.277,0-0.5-0.225-0.5-0.499V14z M2.014,10.001c0,0.274,0.223,0.499,0.5,0.499H23.5  c0.275,0,0.5-0.225,0.5-0.499V8c0-0.274-0.225-0.5-0.5-0.5H2.514c-0.277,0-0.5,0.225-0.5,0.5V10.001z M10.014,4.001  c0,0.274,0.223,0.499,0.5,0.499H23.5c0.275,0,0.5-0.225,0.5-0.499V2c0-0.274-0.225-0.5-0.5-0.5H10.514c-0.277,0-0.5,0.225-0.5,0.5  V4.001z M0,22.001C0,22.275,0.225,22.5,0.5,22.5H23.5c0.275,0,0.5-0.225,0.5-0.499V20c0-0.274-0.225-0.5-0.5-0.5H0.5  C0.225,19.5,0,19.725,0,20V22.001z");
        leftAlignmentButton.setGraphic(leftAlignmentSvg);
        centerAlignmentButton.setGraphic(centerAlignmentSvg);
        rightAlignmentButton.setGraphic(rightAlignmentSvg);
        alignmentButtons = new ArrayList<>(3);
        alignmentButtons.add(leftAlignmentButton);
        alignmentButtons.add(centerAlignmentButton);
        alignmentButtons.add(rightAlignmentButton);
        alignmentButtons.forEach(btn -> ((SVGPath)btn.getGraphic()).setFill(Color.WHITE));
        btnBrowseFont.setPrefWidth(Toolkit.getToolkit().getFontLoader()
                .computeStringWidth(btnBrowseFont.getText(), btnBrowseFont.getFont()) + 40);
        fontSizeField.setText(String.format("%d",
                (int) (fxFields.get(0).getFont().getSize() / imageToPdfRatio)));
        fontNameField.setText(fxFields.get(0).getFont().getName());
        fontNameField.setEditable(true);
        Set<String> fontsNames = AssessableFonts.getFontsNames();
//        TextFields.bindAutoCompletion(fontNameField, fontsNames);
        AutoCompletionTextFieldBinding<String> actb =
                new AutoCompletionTextFieldBinding<>(fontNameField,
                        SuggestionProvider.create(fontsNames));
        actb.setOnAutoCompleted(event -> handleChangeFont());
        fontNameField.setFocusTraversable(true);
        fxFields.forEach(fxField -> {
            fxField.setFontNameField(fontNameField);
            fxField.setFontSizeField(fontSizeField);
            fxField.setFontColorPicker(fontColorPicker);
            fxField.setAlignmentButtons(alignmentButtons);
            fxField.setCapsLockCheckBox(capsLockCheckBox);
        });
        saveFieldsBtn.autosize();
        menuBar.setUseSystemMenuBar(true);
        bindingCheckMenuItem.selectedProperty()
                .bindBidirectional(bindingYesButton.selectedProperty());
        bindingCheckMenuItem.selectedProperty()
                .addListener((observable, oldValue, newValue) ->
                        bindingNoButton.setSelected(!newValue));
        bindingNoButton.selectedProperty()
                .addListener((observable, oldValue, newValue) ->
                        bindingCheckMenuItem.setSelected(!newValue));
        compressFieldIfLineMissing.bind(bindingYesButton.selectedProperty());
        visualizeGridCheckMenuItem.selectedProperty()
                .addListener((o, oldVal, newVal) ->
                        alignFieldsCheckMenuItem.setDisable(!newVal));
        GridPane grid = (GridPane) horizontalScaleBar.getParent();
        grid.getRowConstraints().get(0).setMaxHeight(horizontalScaleBar
                .getBoundsInLocal().getHeight());

        // is done after screen show because size of alertPane is recomputed
        pdfRedactorRoot.boundsInParentProperty().addListener(((observable, oldValue, newValue) -> {
            alertPane.setPrefWidth(newValue.getWidth());
            if (alertCenter.hasNotifications()) alertCenter.flagLast();
        }));
    }

    public void handleChangeFont() {
        String fontName = fontNameField.getText();
        if (Font.getFontNames().contains(fontName)) {
            fxFields.stream()
                    .filter(f -> f.isSelected)
                    .forEach(field -> {
                        Font font = new Font(fontName, field.getFontSize());
                        try {
                            field.setFont(font);
                        } catch (IllegalFontSizeException e) {
                            double previousFontSize = field.getFontSize();
                            field.setMaxFontSize();
                            String message = ErrorMessages.tooBigOldFontSize(field, previousFontSize,
                                    imageToPdfRatio, excelReader);
                            alertCenter.showNotification(message, true);
                        }
                    });
        } else {
            fxFields.stream()
                    .filter(f -> f.isSelected)
                    .forEach(field -> {
                        FileInputStream fis;
                        try {
                            fis = new FileInputStream(AssessableFonts
                                    .getFontPath(fontName));
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    "Не удалось установить шрифт " + fontName);
                            alert.show();
                            LoggerManager.initializeLogger(logger);
                            logger.log(Level.SEVERE, "Ошибка при загрузке шрифта " + fontName, e);
                            e.printStackTrace();
                            return;
                        }
                        Font font = Font.loadFont(fis, field.getFontSize());
                        try {
                            field.setFont(font);
                        } catch (IllegalFontSizeException e) {
                            double previousFontSize = field.getFontSize();
                            field.setMaxFontSize();
                            String message = ErrorMessages.tooBigOldFontSize(field, previousFontSize,
                                    imageToPdfRatio, excelReader);
                            alertCenter.showNotification(message, true);
                        }
                    });
        }
    }

    public void handleBrowseFont() throws InterruptedException, IOException {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TTF files", "*.ttf"),
                new FileChooser.ExtensionFilter("AFM files", "*.afm"),
                new FileChooser.ExtensionFilter("OTF files", "*.otf"),
                new FileChooser.ExtensionFilter("PFM files", "*.pfm"),
                new FileChooser.ExtensionFilter("TTC files", "*.ttc"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            fxFields.stream()
                    .filter(field -> field.isSelected)
                    .forEach(field -> {
                        try {
                            field.setFont(selectedFile.getAbsolutePath());
                        } catch (IllegalFontSizeException e) {
                            double previousFontSize = field.getFontSize();
                            field.setMaxFontSize();
                            String message = ErrorMessages.tooBigOldFontSize(field, previousFontSize,
                                    imageToPdfRatio, excelReader);
                            alertCenter.showNotification(message, true);
                        }
                    });
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleChangeFieldFontSize() {
        double newFontSize = Double.valueOf(fontSizeField.getText()) * imageToPdfRatio;
        fxFields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> {
                    try {
                        field.setFontSize(newFontSize);
                    } catch (IllegalFontSizeException e) {
                        String message = ErrorMessages.tooBigFontSize(field, newFontSize,
                                imageToPdfRatio, excelReader);
                        alertCenter.showNotification(message, true);
                        field.setMaxFontSize();
                    }
                });
    }

    public void handleChangeFontColor() {
        fxFields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> field.setFill(fontColorPicker.getValue()));
    }

    public void handleSaveBadges() throws IOException, InterruptedException {
        FileChooser directoryChooser = new FileChooser();
        File selectedDirectory = directoryChooser.showSaveDialog(null);

        if(selectedDirectory != null) {
            showProgressScreen(true);
            createBadgesArchiveTask = new CreateBadgeArchiveTask(fxFields,
                    imageToPdfRatio,
                    excelReader,
                    pdfPath,
                    selectedDirectory.getAbsolutePath() + ".zip",
                    compressFieldIfLineMissing.get());
            createBadgesArchiveTask.setOnSucceeded(event1 -> {
                if (createBadgesArchiveTask.getValue().equals(true)) {
                    showProgressScreen(false);
//                    Stage doneAlert = new Stage();

                    ButtonType yesBtn = new ButtonType("Да", ButtonBar.ButtonData.YES);
                    ButtonType noBtn = new ButtonType("Нет", ButtonBar.ButtonData.NO);
                    Alert doneAlert = new Alert(Alert.AlertType.INFORMATION,
                            "Скачивание завершено!"
                                    + System.lineSeparator()
                                    + System.lineSeparator()
                                    + "Открыть папку с архивом?",
                            noBtn, yesBtn);
                    Optional<ButtonType> result = doneAlert.showAndWait();
                    if (result.isPresent() && result.get() == yesBtn) {
                        String path = selectedDirectory.getAbsolutePath();
                        String parentPath = path.substring(0, path.lastIndexOf(File.separator));
                        try {
                            if(PlatformUtil.isMac()) {
                                Runtime.getRuntime().exec("open " + parentPath);
                            } else {
                                Runtime.getRuntime().exec("explorer.exe /select, "
                                        + parentPath
                                        + path.substring(path.lastIndexOf(File.separator))
                                        + ".zip");
                            }
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    "Не удалось открыть папку с архивом");
                            alert.show();
                            LoggerManager.initializeLogger(logger);
                            logger.log(Level.SEVERE, "Ошибка при открытии папки с архивом", e);
                            e.printStackTrace();
                        }
                        /*if (Desktop.isDesktopSupported()) {
                            try {
                                String path = selectedDirectory.getAbsolutePath();
                                Desktop.getDesktop().open(
                                        new File(path.substring(0, path.lastIndexOf(File.separator))));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }*/
                    }
                } else {
                    showProgressScreen(false);
                }
            });
            createBadgesArchiveTask.setOnCancelled(e -> showProgressScreen(false));
            progressIndicator.progressProperty().bind(createBadgesArchiveTask.progressProperty());
            progressStatusLabel.textProperty().bind(createBadgesArchiveTask.messageProperty());
            Thread thread = new Thread(createBadgesArchiveTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void showProgressScreen(boolean value) {
        progressIndicatorBackground.setWidth(pdfRedactorRoot.getScene().getWidth());
        progressIndicatorBackground.setHeight(pdfRedactorRoot.getScene().getHeight());
        progressIndicatorBox.setVisible(value);
        progressIndicatorBackground.setVisible(value);
        progressIndicator.setVisible(value);
        cancelButton.setVisible(value);
    }

    public void handleCancelBadgeCreation() {
        createBadgesArchiveTask.cancel();
    }

    public void handleSetAlignment(ActionEvent event) {
        String alignment;
        if(event.getSource().toString().contains("left")) {
            alignment = "LEFT";
        } else if(event.getSource().toString().contains("center")) {
            alignment = "CENTER";
        } else {
            alignment = "RIGHT";
        }
        alignmentButtons.forEach(btn -> {
            if(btn.getId().contains(alignment.toLowerCase())) {
                ((SVGPath)btn.getGraphic()).setFill(Color.BLACK);
            } else {
                ((SVGPath)btn.getGraphic()).setFill(Color.GRAY);
            }
        });

        fxFields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> field.setAlignment(alignment));
    }

    public void handleSaveFields() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/FxFieldsSaver.fxml"));
        Parent root = loader.load();
        FxFieldsSaverController controller = loader.getController();
        Stage saveFieldsWindow = new Stage();
        controller.init(fxFields, saveFieldsWindow);
        saveFieldsWindow.setScene(new Scene(root));
        saveFieldsWindow.setTitle("Сохранить поля");
        saveFieldsWindow.initModality(Modality.APPLICATION_MODAL);
        saveFieldsWindow.toFront();
        saveFieldsWindow.show();
        saveFieldsWindow.centerOnScreen();
    }

    public void handleChangeColor(MouseEvent event) {
        ((SVGPath)((Pane) event.getSource()).getChildren().get(0)).setFill(Color.GRAY);
    }

    public void handleResetColor(MouseEvent event) {
        ((SVGPath)((Pane) event.getSource()).getChildren().get(0)).setFill(Color.BLACK);
    }

    public void setCapitalized() {
        fxFields.stream().filter(field -> field.isSelected)
                .forEach(field -> field.setCapitalized(capsLockCheckBox.isSelected()));
    }

    public void handleBack(ActionEvent event) throws IOException {
        FxField.getGuides().clear();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FileLoader.fxml"));
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public void handleShowHelpBox(MouseEvent event) throws IOException {
        Node source = (Node) event.getSource();
        String sourceId = source.getId();
        if(helpPopUps.stream()
                .anyMatch(popUp -> popUp.getParentsNodeId().equals(sourceId))) return;
        HelpPopUp helpPopUp = new HelpPopUp(source);
        helpPopUp.show(source, event.getScreenX(), event.getScreenY());
        helpPopUps.add(helpPopUp);
        helpPopUp.setOnHidden(e -> helpPopUps.remove(helpPopUp));
    }

    public void handleShowGridLines(ActionEvent event) {
        CheckMenuItem menuItem = (CheckMenuItem) event.getSource();
        gridLines.forEach(line -> line.setVisible(menuItem.isSelected()));
    }

    public void setPdfPreview(byte[] imageFromPdf, double height) {
        pdfPreview.setImage(new Image(new ByteArrayInputStream(imageFromPdf)));
        pdfPreview.setFitHeight(height);
        pdfPreview.setPreserveRatio(true);
        editingArea.setMaxHeight(pdfPreview.getFitHeight());
        editingArea.setMaxWidth(pdfPreview.getBoundsInParent().getWidth());
    }

    public void setImageToPdfRatio(double imageToPdfRatio) {
        this.imageToPdfRatio = imageToPdfRatio;
    }
    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public void setExcelReader(ExcelReader excelReader) {
        this.excelReader = excelReader;
    }

    public void handleSetAlignFields() {
        fxFields.forEach(field ->
                field.setAlignFieldWithGrid(alignFieldsCheckMenuItem.isSelected()));
    }

    public void handleMakeBold() {
        FxField firstSelected = fxFields.stream()
                .filter(f -> f.isSelected).findFirst().orElse(null);
        if (firstSelected != null) {
            if (firstSelected.getFont().getName().contains("Bold")) {
                fxFields.stream()
                        .filter(f -> f.isSelected)
                        .forEach(field -> {
                            try {
                                field.setFont(Font.font(field.getFont().getFamily(),
                                        FontWeight.NORMAL, field.getFontSize()));
                            } catch (IllegalFontSizeException e) {
                                e.printStackTrace();
                                double previousFontSize = field.getFontSize();
                                String message = ErrorMessages.fontStyleError(field, previousFontSize,
                                        "Стандартный", imageToPdfRatio, excelReader);
                                alertCenter.showNotification(message, true);
                                field.setMaxFontSize();
                            }
                        });
            } else {
                fxFields.stream()
                        .filter(f -> f.isSelected)
                        .forEach(field -> {
                            try {
                                field.setFont(Font.font(field.getFont().getFamily(),
                                        FontWeight.BOLD, field.getFontSize()));
                            } catch (IllegalFontSizeException e) {
                                e.printStackTrace();
                                double previousFontSize = field.getFontSize();
                                String message = ErrorMessages.fontStyleError(field, previousFontSize,
                                        "Жирный", imageToPdfRatio, excelReader);
                                alertCenter.showNotification(message, true);
                                field.setMaxFontSize();
                            }
                        });
            }
        }
    }

    public void handleMakeItalic() {
        FxField firstSelected = fxFields.stream()
                .filter(f -> f.isSelected).findFirst().orElse(null);
        if (firstSelected != null) {
            if (firstSelected.getFont().getName().contains("Italic")) {
                fxFields.stream()
                        .filter(f -> f.isSelected)
                        .forEach(field -> {
                            try {
                                field.setFont(Font.font(field.getFont().getFamily(),
                                        FontPosture.REGULAR, field.getFontSize()));
                            } catch (IllegalFontSizeException e) {
                                e.printStackTrace();
                                double previousFontSize = field.getFontSize();
                                String message = ErrorMessages.fontStyleError(field, previousFontSize,
                                        "Стандартный", imageToPdfRatio, excelReader);
                                alertCenter.showNotification(message, true);
                                field.setMaxFontSize();
                            }
                        });
            } else {
                fxFields.stream()
                        .filter(f -> f.isSelected)
                        .forEach(field -> {
                            try {
                                field.setFont(Font.font(field.getFont().getFamily(),
                                        FontPosture.ITALIC, field.getFontSize()));
                            } catch (IllegalFontSizeException e) {
                                e.printStackTrace();
                                double previousFontSize = field.getFontSize();
                                String message = ErrorMessages.fontStyleError(field, previousFontSize,
                                        "Курсив", imageToPdfRatio, excelReader);
                                alertCenter.showNotification(message, true);
                                field.setMaxFontSize();
                            }
                        });
            }
        }
    }

/*
    public void handleShowAnimation(MouseEvent event) {
        HelpAnimation animation = new HelpAnimation();
        helpBox.getChildren().removeIf(node -> node instanceof HelpAnimation);
        helpBox.getChildren().add(animation);
        closeHelpBoxPane.setManaged(false);
        closeHelpBoxPane.setLayoutX(200);
        closeHelpBoxPane.setPrefSize(12,12);
        closeHelpBoxPane.setMaxHeight(12);
        closeHelpBoxPane.setMaxWidth(12);
        helpBox.setVisible(true);
        if(((Node)event.getSource()).getId().contains("yes")) {
            animation.playWithMotion();
        } else {
            animation.play();
        }
    }*/

}
