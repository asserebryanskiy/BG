package badgegenerator.pdfeditor;

import badgegenerator.custompanes.*;
import badgegenerator.fxfieldssaver.FxFieldSave;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
                    setFieldsParameters(fxField);
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
                    fxFields.add(fxField);
                });
        fieldsParent.getChildren().addAll(FxField.getGuides());
        Line verticalGuide = new Line(fieldsParent.getBoundsInLocal().getWidth() / 2,
                1,
                fieldsParent.getBoundsInLocal().getWidth() / 2,
                fieldsParent.getBoundsInLocal().getHeight() - 1);
        verticalGuide.setVisible(false);
        Line horizontalGuide = new Line(1,
                fieldsParent.getBoundsInLocal().getHeight() / 2,
                fieldsParent.getBoundsInLocal().getWidth() - 1,
                fieldsParent.getBoundsInLocal().getHeight() / 2);
        horizontalGuide.setManaged(false);
        horizontalGuide.setVisible(false);
        fieldsParent.getChildren().addAll(verticalGuide, horizontalGuide);
        FxField.setVerticalGuide(verticalGuide);
        FxField.setHorizontalGuide(horizontalGuide);
    }

    void addScaleMarks() {
        float textHeight = Toolkit.getToolkit().getFontLoader().getFontMetrics(new Font(8))
                .getLineHeight();
        float textWidth = Toolkit.getToolkit().getFontLoader()
                .computeStringWidth("999", new Font(8));
        double parentHeight = fieldsParent.getBoundsInLocal().getHeight();

        // pdf - 419, ratio - 1.19
        double bigStep = Math.ceil(parentHeight / imageToPdfRatio / 200) * 10 * imageToPdfRatio;
        double smallStep = bigStep / 5;

        for(double i = 0; i <= parentHeight; i += bigStep) {
            Text text = new Text(String.valueOf((int) Math.round(i / imageToPdfRatio)));
            text.setFont(new Font(8));
            text.setLayoutY(i + textHeight);
            text.setManaged(false);
            Line line = new Line(0, i, textWidth + 10, i);
            line.setManaged(false);
            for(double j = smallStep; j < bigStep; j += smallStep) {
                double y = i + j;
                if(y > parentHeight) break;
                Line smallMark = new Line(textWidth + 5, y, textWidth + 10, y);
                smallMark.setManaged(false);
                verticalScaleBar.getChildren().add(smallMark);
            }
            verticalScaleBar.getChildren().addAll(text, line);
        }
        Rectangle vBack = new Rectangle(textWidth + 10, parentHeight,
                Color.color(0,0,0,0));
        verticalScaleBar.getChildren().add(vBack);

        // HORIZONTAL
        double parentWidth = fieldsParent.getBoundsInLocal().getWidth();
        for(double i = 0; i <= parentWidth; i += bigStep) {
            Text text = new Text(String.valueOf((int) Math.round(i / imageToPdfRatio)));
            text.setFont(new Font(8));
            text.setLayoutY(textHeight);
            text.setLayoutX(i + 5);
            text.setManaged(false);
            Line line = new Line(i, 1, i, textWidth + 10);
            line.setManaged(false);
            for(double j = smallStep; j < bigStep; j += smallStep) {
                double x = i + j;
                if(x > parentWidth) break;
                Line smallMark = new Line(x, textWidth + 10, x, textWidth + 5);
                smallMark.setManaged(false);
                horizontalScaleBar.getChildren().add(smallMark);
            }
            horizontalScaleBar.getChildren().addAll(text, line);
        }
        Rectangle hBack = new Rectangle(parentWidth, textWidth + 10,
                Color.color(0,0,0,0));
        horizontalScaleBar.getChildren().add(hBack);
    }

    List<FxField> getFxFields() {
        return fxFields;
    }

    protected abstract void setFieldFontAndSize(int i);

    protected abstract void setFieldsParameters(FxField fxField);
}
