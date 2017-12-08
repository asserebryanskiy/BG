package badgegenerator.pdfeditor;

import badgegenerator.custompanes.*;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Abstract class for NewFieldsLayouter and SavedFieldsLayouter.
 * Aids in complex process of FxFields, Guides, ResizeableBorders and DistanceViewers
 * layout.
 */
abstract class AbstractFieldsLayouter {
    private final List<Line> gridLines;
    private final Pane horizontalScaleBar;
    private final Pane verticalScaleBar;
    protected double imageToPdfRatio;
    private Pane fieldsParent;
    private String[] largestFields;
    private final String[] longestWords;
    private final String[] headings;
    private List<FxField> fxFields;

    double x;
    double y;
    Color color;
    double fontSize;
    String fontPath;
    String alignment;
    
    AbstractFieldsLayouter(Pane fieldsParent,
                           Pane verticalScaleBar,
                           Pane horizontalScaleBar,
                           List<Line> gridLines,
                           String[] largestFields,
                           String[] longestWords,
                           String[] headings,
                           double imageToPdfRatio) {
        this.fieldsParent = fieldsParent;
        this.verticalScaleBar = verticalScaleBar;
        this.horizontalScaleBar = horizontalScaleBar;
        this.gridLines = gridLines;
        this.largestFields = largestFields;
        this.longestWords = longestWords;
        this.headings = headings;
        this.imageToPdfRatio = imageToPdfRatio;
    }

    void positionFields() {
        fxFields = new ArrayList<>(largestFields.length);
        IntStream.range(0, largestFields.length)
                .forEach(i -> {
                    FxField fxField;
                    setFieldFontAndSize(headings[i]);
                    if(largestFields[i].length() > longestWords[i].length()) {
                        fxField = new FieldWithHyphenation(largestFields[i],
                                longestWords[i],
                                headings[i],
                                imageToPdfRatio,
                                fieldsParent.getMaxWidth(),
                                fontPath,
                                fontSize);
                    } else {
                        fxField = new SingleLineField(largestFields[i],
                                headings[i],
                                imageToPdfRatio,
                                fieldsParent.getMaxWidth(),
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
                    fxField.setFill(color);
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
        Line verticalGuide = new Line(fieldsParent.getMaxWidth() / 2,
                1,
                fieldsParent.getMaxWidth() / 2,
                fieldsParent.getBoundsInLocal().getHeight() - 1);
        verticalGuide.setVisible(false);
        Line horizontalGuide = new Line(1,
                fieldsParent.getBoundsInLocal().getHeight() / 2,
                fieldsParent.getMaxWidth() - 1,
                fieldsParent.getBoundsInLocal().getHeight() / 2);
        horizontalGuide.setManaged(false);
        horizontalGuide.setVisible(false);
        fieldsParent.getChildren().addAll(verticalGuide, horizontalGuide);
        FxField.setVerticalGuide(verticalGuide);
        FxField.setHorizontalGuide(horizontalGuide);
        moveFieldsInBounds();
    }

    private void moveFieldsInBounds() {
        fxFields.forEach(field -> {
            switch (field.getAlignment()) {
                case ("RIGHT"): {
                    if (field.getLayoutX() < 0) {
                        double rightX = field.getLayoutX() + field.getPrefWidth();
                        if (field instanceof FieldWithHyphenation
                                && field.getMinWidth() < rightX) {
                            FieldWithHyphenation fieldWH = (FieldWithHyphenation) field;
                            fieldWH.setPrefWidth(rightX);
                            fieldWH.computeHyphenation();
                            fieldWH.setLayoutX(rightX - fieldWH.getPrefWidth());
                        } else {
                            while (field.getPrefWidth() >= rightX)
                                field.setFontSize(field.getFontSize() - 1);
                        }
                    }
                    break;
                }case ("CENTER"): {
                    if (field.getLayoutX() < 0) {
                        double centerX = fieldsParent.getMaxHeight() / 2;
                        if (field instanceof FieldWithHyphenation
                                && field.getMinWidth() < fieldsParent.getMaxWidth()) {
                            FieldWithHyphenation fieldWH = (FieldWithHyphenation) field;
                            fieldWH.setPrefWidth(fieldsParent.getMaxWidth());
                            fieldWH.computeHyphenation();
                            fieldWH.setLayoutX(centerX - fieldWH.getPrefWidth() / 2);
                        } else {
                            while (field.getPrefWidth() >= fieldsParent.getMaxWidth()) {
                                System.out.println(field.getFontSize());
                                field.setFontSize(field.getFontSize() - 1);
                            }
                        }
                    }
                    break;
                } default: {
                    if (field.getLayoutX() + field.getPrefWidth() > fieldsParent.getMaxWidth()) {
                        if (field instanceof FieldWithHyphenation
                                && field.getLayoutX() + field.getMinWidth()
                                < fieldsParent.getMaxWidth() - field.getLayoutX()) {
                            FieldWithHyphenation fieldWH = (FieldWithHyphenation) field;
                            fieldWH.setPrefWidth(fieldsParent.getMaxWidth() - field.getLayoutX());
                            fieldWH.computeHyphenation();
                        } else {
                            while (field.getPrefWidth() >= fieldsParent.getMaxWidth() - field.getLayoutX())
                                field.setFontSize(field.getFontSize() - 1);
                        }
                    }
                }
            }
        });

        // move down field if upper one run into it
        fxFields.sort(Comparator.comparingDouble(Node::getLayoutY));
        int shift = 0;
        final int INTENT = 10;
        double lastY = fxFields.get(0).getLayoutY() + fxFields.get(0).getMaxHeight();
        for (int i = 1; i < fxFields.size(); i++) {
            FxField field = fxFields.get(i);
            if (lastY < field.getLayoutY())
                shift += lastY - field.getLayoutY() + INTENT;
            if (field.getLayoutY() + field.getMaxHeight() + shift < fieldsParent.getMaxHeight()) {
                field.setLayoutY(field.getLayoutY() + shift);
            } else break;
            lastY = field.getLayoutY() + field.getMaxHeight();
        }
    }

    void addScaleMarks() {
        float textHeight = Toolkit.getToolkit().getFontLoader().getFontMetrics(new Font(8))
                .getLineHeight();
        float textWidth = Toolkit.getToolkit().getFontLoader()
                .computeStringWidth("999", new Font(8));
        double parentHeight = fieldsParent.getBoundsInLocal().getHeight();
        double parentWidth = fieldsParent.getMaxWidth();

        double bigStep = Math.ceil(parentHeight / imageToPdfRatio / 200) * 10 * imageToPdfRatio;
        double smallStep = bigStep / 5;

        for(double i = 0; i <= parentHeight; i += bigStep) {
            Text text = new Text(String.valueOf((int) Math.round(i / imageToPdfRatio)));
            text.setFont(new Font(8));
            text.setLayoutY(i + textHeight);
            text.setManaged(false);
            Line line = new Line(1, i, textWidth + 10, i);
            line.setManaged(false);
            Line gridLine = new Line(1, i, parentWidth - 1, i);
            addGridLine(gridLine);
            if(fxFields != null) fxFields.forEach(field -> {
                field.addHorizontalGridLine(gridLine);
                field.setHGridLineStep(bigStep);
            });
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
        for(double i = 0; i <= parentWidth; i += bigStep) {
            Text text = new Text(String.valueOf((int) Math.round(i / imageToPdfRatio)));
            text.setFont(new Font(8));
            text.setLayoutY(textHeight);
            text.setLayoutX(i + 5);
            text.setManaged(false);
            Line line = new Line(i, 1, i, textWidth + 10);
            line.setManaged(false);
            Line gridLine = new Line(i, 1, i, parentHeight - 1);
            addGridLine(gridLine);
            if(fxFields != null) fxFields.forEach(field -> {
                field.addVerticalGridLine(gridLine);
                field.setVGridLineStep(bigStep);
            });
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

    private void addGridLine(Line gridLine) {
        gridLine.setStrokeWidth(0.1);
        gridLine.setFill(Color.DARKGRAY);
        gridLine.setManaged(false);
        gridLine.setVisible(false);
        gridLines.add(gridLine);
        fieldsParent.getChildren().add(gridLine);
    }

    List<FxField> getFxFields() {
        return fxFields;
    }

    protected abstract void setFieldFontAndSize(String columnId);

    protected abstract void setFieldsParameters(FxField fxField);
}
