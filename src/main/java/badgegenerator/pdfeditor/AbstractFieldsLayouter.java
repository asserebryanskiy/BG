package badgegenerator.pdfeditor;

import badgegenerator.custompanes.*;
import badgegenerator.fxfieldssaver.FxFieldSave;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Abstract class for NewFieldsLayouter and SavedFieldsLayouter.
 * Aids in complex process of FxFields, Guides, ResizeableBorders and DistanceViewers
 * layout.
 */
abstract class AbstractFieldsLayouter {
    private Pane horizontalScaleBar;
    private Pane verticalScaleBar;
    protected double imageToPdfRatio;
    List<FxFieldSave> saves;
    Pane fieldsParent;
    String[] largestFields;
    private String[] longestWords;
    private List<FxField> fxFields;

    double x;
    double y;
    double red;
    double green;
    double blue;
    double fontSize;
    String fontPath;
    String alignment;
    
    AbstractFieldsLayouter(Pane fieldsParent,
                           Pane verticalScaleBar,
                           Pane horizontalScaleBar,
                           String[] largestFields,
                           String[] longestWords,
                           double imageToPdfRatio) {
        this.fieldsParent = fieldsParent;
        this.verticalScaleBar = verticalScaleBar;
        this.horizontalScaleBar = horizontalScaleBar;
        this.largestFields = largestFields;
        this.longestWords = longestWords;
        this.imageToPdfRatio = imageToPdfRatio;
    }

    void positionFields() {
        fxFields = new ArrayList<>(largestFields.length);
        IntStream.range(0, largestFields.length)
                .forEach(i -> {
                    FxField fxField;
                    setFieldFontAndSize(i);
                    try {
                        if(largestFields[i].length() > longestWords[i].length()) {
                            fxField = new FieldWithHyphenation(largestFields[i],
                                    longestWords[i],
                                    i,
                                    imageToPdfRatio,
                                    fieldsParent.getBoundsInLocal().getWidth() - 48,
                                    fontPath,
                                    fontSize);
                        } else {
                            fxField = new SingleLineField(largestFields[i],
                                    i,
                                    imageToPdfRatio,
                                    fieldsParent.getBoundsInLocal().getWidth() - 48,
                                    fontSize,
                                    fontPath);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Не удалось найти файл сохранения");
                        alert.show();
                        return;
                    }
                    setFieldsParameters(fxField, i);
                    if(fxField instanceof FieldWithHyphenation) {
                        ((FieldWithHyphenation) fxField).addResizeableBorder(
                                new ResizeableBorder(((FieldWithHyphenation) fxField),
                                        Position.LEFT));
                        ((FieldWithHyphenation) fxField).addResizeableBorder(
                                new ResizeableBorder(((FieldWithHyphenation) fxField),
                                        Position.RIGHT));
                        fieldsParent.getChildren().addAll(
                                ((FieldWithHyphenation) fxField).getResizeableBorders());
                    }
                    fxField.setLayoutX(x);
                    fxField.setLayoutY(y);
                    fxField.setFill(Color.color(red,green,blue));
                    fxField.setAlignment(alignment);
                    fieldsParent.getChildren().add(fxField);
                    try {
                        fxField.addGuide(new Guide(fxField, Position.LEFT));
                        fxField.addGuide(new Guide(fxField, Position.RIGHT));
                    } catch (NoParentFoundException | NoIdFoundException e) {
                        e.printStackTrace();
                    }
                    horizontalScaleBar.getChildren().add(
                            new DistanceViewer(fxField, Orientation.HORIZONTAL));
                    verticalScaleBar.getChildren().add(
                            new DistanceViewer(fxField, Orientation.VERTICAL));
                    /*fieldsParent.getChildren().addAll(
                            new DistanceViewer(fxField, Orientation.VERTICAL),
                            new DistanceViewer(fxField, Orientation.HORIZONTAL));*/
                    fxFields.add(fxField);
                });
        fieldsParent.getChildren().addAll(FxField.getGuides());
        Line verticalGuide = new Line(fieldsParent.getBoundsInLocal().getWidth() / 2,
                0,
                fieldsParent.getBoundsInLocal().getWidth() / 2,
                fieldsParent.getBoundsInLocal().getHeight() - 1);
        verticalGuide.setVisible(false);
        fieldsParent.getChildren().add(verticalGuide);
        FxField.setVerticalGuide(verticalGuide);
    }

    void addScaleMarks(Rectangle verticalScaleBack, Rectangle horizontalScaleBack) {
        double parentHeight = fieldsParent.getBoundsInLocal().getHeight();
        double oneMarkHeight = Toolkit.getToolkit().getFontLoader()
                .getFontMetrics(new Font(8)).getLineHeight();
        int maxNumberOfMarksV = (int) (parentHeight / oneMarkHeight);
        double vStep = Math.ceil(parentHeight * imageToPdfRatio / maxNumberOfMarksV / 10) * 10;
        vStep /= imageToPdfRatio;
        // VERTICAL
        for(double i = 0; i <= parentHeight; i += vStep) {
            VBox pane = new VBox();
            Text text = new Text(String.format("%.0f", i * imageToPdfRatio));
            pane.setLayoutX(0);
            pane.setLayoutY(parentHeight - i);
            text.setFont(new Font(8));
            Line line = new Line(0, i, 25, i);
            pane.setManaged(false);
            pane.setManaged(false);
            pane.getChildren().addAll(line, text);
            verticalScaleBar.getChildren().add(pane);
        }
        verticalScaleBack.setHeight(verticalScaleBar.getBoundsInLocal().getHeight() + 10);

        double parentWidth = fieldsParent.getBoundsInLocal().getWidth();
        double oneMarkWidth = Toolkit.getToolkit().getFontLoader()
                .computeStringWidth("100 ", new Font(8));
        int maxNumberOfMarksH = (int) (parentWidth / oneMarkWidth);
        double hStep = Math.ceil(parentWidth * imageToPdfRatio / maxNumberOfMarksH / 10) * 10;
        hStep /= imageToPdfRatio;
        // HORIZONTAL
        for(double i = 0; i <= parentWidth - oneMarkWidth; i += hStep) {
            HBox pane = new HBox();
            Text text = new Text(String.format("%.0f ", i * imageToPdfRatio));
            pane.setLayoutY(0);
            pane.setLayoutX(i);
            text.setFont(new Font(8));
            Line line = new Line(i, 1, i, 16);
            pane.setManaged(false);
            pane.setManaged(false);
            pane.getChildren().addAll(line, text);
            horizontalScaleBar.getChildren().add(pane);
        }
        horizontalScaleBack.setWidth(parentWidth);
    }

    List<FxField> getFxFields() {
        return fxFields;
    }

    protected abstract void setFieldFontAndSize(int i);

    protected abstract void setFieldsParameters(FxField fxField, int i);
}
