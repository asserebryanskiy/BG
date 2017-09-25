package badgegenerator.pdfeditor;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Borders, that could be added to the field with possible hyphenation. \
 * Provide possibility to change its width and consequently wrapping property.
 */
public class ResizeableBorder extends StackPane{
    private SVGPath resizeSvg;
    private double borderX;
    private double deltaX;

    // Pane structure
    public ResizeableBorder(Field field,
                            Position value) {
        super();
        setAlignment(Pos.TOP_LEFT);
        Rectangle base = new Rectangle(24, field.getPrefHeight());
        layoutYProperty().bind(field.layoutYProperty());
        maxHeightProperty().bind(field.prefHeightProperty());
        base.heightProperty().bind(field.prefHeightProperty());
        setMaxWidth(base.getWidth());

        resizeSvg = new SVGPath();
        resizeSvg.setContent("M6 11v-4l-6 5 6 5v-4h12v4l6-5-6-5v4z");
        resizeSvg.setManaged(false);
        resizeSvg.setVisible(false);
        resizeSvg.setLayoutY(getHeight() / 2 - resizeSvg.getBoundsInLocal().getHeight() / 2);

        // mouse hover effects
        field.setOnMouseEntered(event -> field.getResizeableBorders().stream()
                .map(ResizeableBorder::getResizeSvg)
                .forEach(svg -> {
                    svg.setVisible(true);
                }));
        field.setOnMouseExited(event -> field.getResizeableBorders().stream()
                .map(ResizeableBorder::getResizeSvg)
                .forEach(svg -> svg.setVisible(false)));
        setOnMouseEntered(event -> resizeSvg.setVisible(true));
        setOnMouseExited(event -> resizeSvg.setVisible(false));
        resizeSvg.setId(String.format("%sResizeSvg%s", field.getId(), value.name()));

        setOnMousePressed(event -> {
            borderX = getLayoutX() + getPrefWidth() / 2;
            deltaX = borderX - event.getSceneX();
        });

        // dragging property
        if(value.name().equals("RIGHT")) {
            layoutXProperty().bind(field.layoutXProperty().add(field.prefWidthProperty()));
            setOnMouseDragged(event -> {
                resizeSvg.setVisible(true);
                final double newX = event.getSceneX() + deltaX - getMaxWidth() / 2;
                if(newX < field.getLayoutX() + field.getMinWidth()
                        || newX >= getParent().getBoundsInLocal().getWidth()
                        || (field.getNumberOfLines() == 1
                        && newX > field.getLayoutX() + field.getPrefWidth())) return;

                field.setPrefWidth(newX - field.getLayoutX());
                field.computeHyphenation();
                field.setTextFlowAligned();
                borderX = getLayoutX() + getPrefWidth() / 2;
//                deltaX = borderX - event.getSceneX();
            });
        } else { // LEFT
            layoutXProperty().bind(field.layoutXProperty().subtract(base.getWidth()));
            setOnMouseDragged(event -> {
                resizeSvg.setVisible(true);
                final double newX = event.getSceneX() + deltaX - getPrefWidth() / 2;
                if(newX <= 0
                        || newX > field.getLayoutX() + field.getPrefWidth() - field.getMinWidth()
                        || (field.getNumberOfLines() == 1 && newX < field.getLayoutX())) {
                    return;
                }
                double oldWidth = field.getPrefWidth();
                field.setPrefWidth(field.getLayoutX() + field.getPrefWidth() - newX);
                field.computeHyphenation();
                field.setLayoutX(field.getLayoutX() + oldWidth - field.getPrefWidth());
                field.setTextFlowAligned();
                borderX = getLayoutX() + getPrefWidth() / 2;
            });
        }
        setCursor(Cursor.H_RESIZE);
        base.setOpacity(0);

        getChildren().addAll(base, resizeSvg);

        field.addResizeableBorder(this);
    }


    public SVGPath getResizeSvg() {
        return resizeSvg;
    }
}
