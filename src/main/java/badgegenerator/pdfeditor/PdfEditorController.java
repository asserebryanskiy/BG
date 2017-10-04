package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fxfieldssaver.FxFieldsSaverController;
import badgegenerator.pdfcreator.CreateBadgeArchiveTask;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class PdfEditorController {

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
    private Rectangle horizontalScaleBack;

    @FXML
    private Rectangle verticalScaleBack;

    @FXML private Button leftAlignmentButton;
    @FXML private Button centerAlignmentButton;
    @FXML private Button rightAlignmentButton;

    private List<FxField> fxFields;
    private Font font;

    private double imageToPdfRatio;
    private Task createBadgesArchiveTask;
    private static FileChooser fileChooser = new FileChooser();
    private boolean compressFieldIfLineMissing = true;
    private String pdfPath;
    private ExcelReader excelReader;

    public void init() {
        init(null);
    }

    public void init(String savesPath) {
        AbstractFieldsLayouter layouter;
        if(savesPath != null) {
            layouter = new SavedFieldsLayouter(editingArea,
                    verticalScaleBar,
                    horizontalScaleBar,
                    excelReader.getLargestFields(),
                    excelReader.getLongestWords(),
                    imageToPdfRatio,
                    savesPath);
        } else {
            layouter = new NewFieldsLayouter(editingArea,
                    verticalScaleBar,
                    horizontalScaleBar,
                    excelReader.getLargestFields(),
                    excelReader.getLongestWords(),
                    imageToPdfRatio);
        }
        layouter.positionFields();
        layouter.addScaleMarks(verticalScaleBack, horizontalScaleBack);
        fxFields = layouter.getFxFields();
        fxFields.forEach(fxField -> {
            fxField.setFontNameField(fontNameField);
            fxField.setFontSizeField(fontSizeField);
            fxField.setFontColorPicker(fontColorPicker);
        });
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
        progressIndicatorBackground.setWidth(pdfRedactorRoot.getBoundsInParent().getWidth()
                + pdfRedactorRoot.getPadding().getLeft() * 2);
        progressIndicatorBackground.setHeight(pdfRedactorRoot.getBoundsInParent().getHeight()
                + pdfRedactorRoot.getPadding().getTop() * 2);
        font = fxFields.get(0).getFont();
        fontSizeField.setText(String.format("%d",
                (int) (fxFields.get(0).getFont().getSize() / imageToPdfRatio)));
        fontNameField.setText(fxFields.get(0).getFont().getName());
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
            FileInputStream fontInputStream = new FileInputStream(selectedFile.getAbsolutePath());
            Font newFont = Font.loadFont(fontInputStream,
                    Double.valueOf(fontSizeField.getText()) * imageToPdfRatio);
            fontNameField.setText(font.getName());
            fxFields.stream()
                    .filter(field -> field.isSelected)
                    .forEach(field -> {
                        field.setFont(newFont);
                        field.setFontPath(selectedFile.getAbsolutePath());
                    });
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleChangeFieldFontSize() {
        double newFontSize = Double.valueOf(fontSizeField.getText()) * imageToPdfRatio;
        fxFields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> field.setFontSize(newFontSize));
    }

    public void handleChangeFontColor() {
        fxFields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> field.setFill(fontColorPicker.getValue()));
    }

    public void handleSaveBadges(MouseEvent event) throws IOException, InterruptedException {
        FileChooser directoryChooser = new FileChooser();
        File selectedDirectory = directoryChooser.showSaveDialog(null);

        if(selectedDirectory != null) {
            showProgressScreen(true);
            createBadgesArchiveTask = new CreateBadgeArchiveTask(fxFields,
                    imageToPdfRatio,
                    excelReader,
                    pdfPath,
                    selectedDirectory.getAbsolutePath() + ".zip",
                    compressFieldIfLineMissing);
            createBadgesArchiveTask.setOnSucceeded(event1 -> {
                showProgressScreen(false);
                Alert doneAlert = new Alert(Alert.AlertType.INFORMATION);
                doneAlert.setContentText("Скачивание завершено");
                doneAlert.show();
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
        } else alignment = "RIGHT";

        fxFields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> field.setAlignment(alignment));
    }

    public void handleSetCompressFieldsTrue(ActionEvent event) {
        compressFieldIfLineMissing = true;
    }

    public void handleSetCompressFieldsFalse(ActionEvent event) {
        compressFieldIfLineMissing = false;
    }

    public void setPdfPreview(byte[] imageFromPdf, double height) {
        pdfPreview.setImage(new Image(new ByteArrayInputStream(imageFromPdf)));
        pdfPreview.setFitHeight(height);
        pdfPreview.setPreserveRatio(true);
        editingArea.setMaxHeight(pdfPreview.getFitHeight());
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

    public void handleSaveFields(ActionEvent event) throws IOException {
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
    }
}
