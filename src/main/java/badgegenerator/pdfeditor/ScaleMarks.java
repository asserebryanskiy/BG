package badgegenerator.pdfeditor;

import badgegenerator.custompanes.DistanceViewer;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.Orientation;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Created by andreyserebryanskiy on 11/12/2017.
 */
public class ScaleMarks {
    static void addTo(Pane fieldsParent,
                      Pane verticalScaleBar,
                      Pane horizontalScaleBar,
                      double imageToPdfRatio,
                      List<FxField> fxFields,
                      List<Line> gridLines) {
        fxFields.forEach(f -> {
            horizontalScaleBar.getChildren().add(
                    new DistanceViewer(f, Orientation.HORIZONTAL));
            verticalScaleBar.getChildren().add(
                    new DistanceViewer(f, Orientation.VERTICAL));
        });
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
            addGridLine(gridLine, gridLines, fieldsParent);
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
            addGridLine(gridLine, gridLines, fieldsParent);
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

    private static void addGridLine(Line gridLine, List<Line> gridLines, Pane fieldsParent) {
        gridLine.setStrokeWidth(0.1);
        gridLine.setFill(Color.DARKGRAY);
        gridLine.setManaged(false);
        gridLine.setVisible(false);
        gridLines.add(gridLine);
        fieldsParent.getChildren().add(gridLine);
    }
}
