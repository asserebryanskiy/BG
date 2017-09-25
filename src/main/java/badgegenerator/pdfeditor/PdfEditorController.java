package badgegenerator.pdfeditor;

import badgegenerator.ModelSingleton;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;


public class PdfEditorController implements Initializable {

    @FXML
    private GridPane pdfRedactorRoot;

    @FXML
    private ImageView pdfPreview;

    @FXML
    private StackPane editingArea;

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

    @FXML private Button leftAlignmentButton;
    @FXML private Button centerAlignmentButton;
    @FXML private Button rightAlignmentButton;

    private static final List<Field> fields = new ArrayList<>();
    private Font font;
    private static Line verticalGuide;

    private double imageToPdfRatio;
    private Task createBadgesArchiveTask;
    private static FileChooser fileChooser = new FileChooser();
    private boolean compressFieldIfLineMissing = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // displays the png instance of loaded pdf document
        //ToDo: add Back button to return to loading documents page
        boolean firstLaunch = ModelSingleton.getInstance().getPdfEditorFirstLaunch();
        if(firstLaunch) {
            ModelSingleton.getInstance().setPdfEditorFirstLaunch(false);
            try {
                ModelSingleton.getInstance().createImageFromPdf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Image image = new Image(
                new ByteArrayInputStream(ModelSingleton.getInstance().getPdfImageByteArray()));

        pdfPreview.setImage(image);
        pdfPreview.setPreserveRatio(true);

        imageToPdfRatio = ModelSingleton.getInstance()
                .calculatePdfToImageRatio(pdfPreview.getFitHeight());

        // fix the height of pdf editing area
        editingArea.setMaxHeight(pdfPreview.getFitHeight());


        if(firstLaunch) {
            // places largest fields on the pdf-document image
            addFields();
            // adds fieldGuides on the screen to help in fields manipulations
            addVerticalGuides();
        }
        else {
//            fontPath = ModelSingleton.getInstance().getFontPath();
            editingArea.getChildren().addAll(fields);
            editingArea.getChildren().addAll(Field.getGuides());
            editingArea.getChildren().add(verticalGuide);
            fields.stream()
                    .filter(field -> field.mayHasHyphenation)
                    .forEach(field -> editingArea.getChildren()
                            .addAll(field.getResizeableBorders()));
        }
        // adds possibility to remove selection
        pdfRedactorRoot.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (fields.stream()
                    .anyMatch(field -> event.getSceneX() >= field.getMouseX()
                            && event.getSceneX() <= field.getMouseX() + field.getPrefWidth()
                            && event.getSceneY() <= field.getMouseY()
                            && event.getSceneY() >= field.getMouseY() - field.getHeight())) {
                return;
            }
            fields.forEach(f -> f.setSelected(false));
        });

        // binds fields to fields' values
        fields.forEach(field -> {
            field.setFontNameField(fontNameField);
            field.setFontSizeField(fontSizeField);
            field.setFontColorPicker(fontColorPicker);
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
        font = fields.get(0).getFont();
        fontSizeField.setText(String.format("%d",
                (int) (fields.get(0).getFont().getSize() * imageToPdfRatio)));
        fontNameField.setText(fields.get(0).getFont().getName());
    }

    private void addFields() {
        // chooses largest strings from list of all fields for every field
        String[] fieldsValues = ModelSingleton.getInstance()
                .getExcelReader().getLargestFields();
        String[] longestWords = ModelSingleton.getInstance()
                .getExcelReader().getLongestWords();

        final double yCoordinate = editingArea.getBoundsInLocal().getHeight() / 2
                - (fieldsValues.length * 20 / 2);

        IntStream.range(0, fieldsValues.length).forEach(i -> {
            Field field = new Field(fieldsValues[i],
                    longestWords[i],
                    i,
                    editingArea.getBoundsInLocal().getWidth() - 48,
                    imageToPdfRatio);

            field.getTextFlow().setTextAlignment(TextAlignment.CENTER);
            field.setLayoutX(editingArea.getBoundsInLocal().getWidth() / 2
                    - field.getPrefWidth() / 2);
            field.setLayoutY(yCoordinate + i * 20);

            field.setManaged(false);
            editingArea.getChildren().add(field);
            fields.add(field);

            try {
                field.addGuide(new Guide(field, Position.LEFT));
                field.addGuide(new Guide(field, Position.RIGHT));
            } catch (NoParentFoundException | NoIdFoundException e) {
                e.printStackTrace();
            }
            if(field.mayHasHyphenation) {
                ResizeableBorder leftBorder =
                        new ResizeableBorder(field, Position.LEFT);
                ResizeableBorder rightBorder =
                        new ResizeableBorder(field, Position.RIGHT);
                editingArea.getChildren().addAll(leftBorder, rightBorder);
                leftBorder.setManaged(false);
                rightBorder.setManaged(false);
            }
        });
    }

    private void addVerticalGuides() {
        double endY = editingArea.getBoundsInLocal().getHeight() - 1;
        Field.getGuides().forEach(guide -> {
            editingArea.getChildren().add(guide);
            guide.setManaged(false);
            guide.setVisible(false);
        });

        verticalGuide = new Line(editingArea.getBoundsInLocal().getWidth() / 2,
                0,
                editingArea.getBoundsInLocal().getWidth() / 2,
                endY);
        verticalGuide.setVisible(false);
        editingArea.getChildren().add(verticalGuide);
        Field.setVerticalGuide(verticalGuide);
    }

    public void handleBrowseFont() throws InterruptedException, IOException {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TTF files", "*.ttf"),
                new FileChooser.ExtensionFilter("AFM files", "*.afm"),
                new FileChooser.ExtensionFilter("PFM files", "*.pfm"),
                new FileChooser.ExtensionFilter("PFM files", "*.pfm"),
                new FileChooser.ExtensionFilter("TTC files", "*.ttc"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            FileInputStream fontInputStream = new FileInputStream(selectedFile.getAbsolutePath());
            Font newFont = Font.loadFont(fontInputStream,
                    Double.valueOf(fontSizeField.getText()) * imageToPdfRatio);
            fontNameField.setText(font.getName());
            fields.stream()
                    .filter(field -> field.isSelected)
                    .forEach(field -> {
                        setFontAligned(field, newFont, field.getFontSize());
                        field.setFontPath(selectedFile.getAbsolutePath());
                    });
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleChangeFieldFontSize() {
        double newFontSize;
        try {
            newFontSize = Double.valueOf(fontSizeField.getText()) * imageToPdfRatio;
        } catch (RuntimeException re) {
            return;
        }
        fields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> setFontAligned(field, field.getFont(), newFontSize));
    }

    private void setFontAligned(Field field, Font newFont, double fontSize) {
        double x = field.getLayoutX();
        double oldWidth = field.getPrefWidth();
        Paint color = field.getLines().get(0).getFill();
        field.setFieldFont(newFont, fontSize);
        field.getLines().forEach(line -> line.setFill(color));
        switch (field.getTextFlow().getTextAlignment().name()) {
            case("RIGHT"):
                field.setLayoutX(x + oldWidth - field.getPrefWidth());
                break;
            case("CENTER"):
                field.setLayoutX(x + oldWidth / 2 - field.getPrefWidth() / 2);
        }
    }

    public void handleChangeFontColor() {
        fields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> field.getLines().forEach(line ->
                        line.setFill(fontColorPicker.getValue())));
    }

    public void handleSaveBadges(MouseEvent event) throws IOException, InterruptedException {
        FileChooser directoryChooser = new FileChooser();
        File selectedDirectory = directoryChooser.showSaveDialog(null);

        if(selectedDirectory != null) {
            showProgressScreen(true);
            createBadgesArchiveTask = new CreateBadgeArchiveTask(fields,
                    imageToPdfRatio,
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

    private void showProgressScreen(boolean show) {
        progressIndicatorBox.setVisible(show);
        progressIndicatorBackground.setVisible(show);
        progressIndicator.setVisible(show);
        cancelButton.setVisible(show);
    }

    public static FileChooser getFileChooser() {
        return fileChooser;
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

        fields.stream()
                .filter(field -> field.isSelected)
                .forEach(field -> field.getTextFlow()
                        .setTextAlignment(TextAlignment.valueOf(alignment)));
    }

    public void handleSetCompressFieldsTrue(ActionEvent event) {
        compressFieldIfLineMissing = true;
    }

    public void handleSetCompressFieldsFalse(ActionEvent event) {
        compressFieldIfLineMissing = false;
    }
}
