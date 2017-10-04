package badgegenerator.custompanes;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 * Borders, that could be added to the field with possible hyphenation. \
 * Provide possibility to change its width and consequently wrapping property.
 */
public class ResizeableBorder extends StackPane{
    private SVGPath resizeSvg;
    private double borderX;
    private double deltaX;

    // Pane structure
    public ResizeableBorder(FieldWithHyphenation fxField,
                            Position value) {
        super();
        setManaged(false);
        setAlignment(Pos.TOP_LEFT);
        Rectangle base = new Rectangle(24, fxField.getMaxHeight());
        layoutYProperty().bind(fxField.layoutYProperty());
        maxHeightProperty().bind(fxField.maxHeightProperty());
        base.heightProperty().bind(fxField.maxHeightProperty());
        setMaxWidth(base.getWidth());

        resizeSvg = new SVGPath();
        resizeSvg.setContent("M6 11v-4l-6 5 6 5v-4h12v4l6-5-6-5v4z");
        resizeSvg.setManaged(false);
        resizeSvg.setVisible(false);
        resizeSvg.setLayoutY(getHeight() / 2 - resizeSvg.getBoundsInLocal().getHeight() / 2);

        // mouse hover effects
        fxField.setOnMouseEntered(event -> fxField.getResizeableBorders().stream()
                .map(ResizeableBorder::getResizeSvg)
                .forEach(svg -> {
                    svg.setVisible(true);
                }));
        fxField.setOnMouseExited(event -> fxField.getResizeableBorders().stream()
                .map(ResizeableBorder::getResizeSvg)
                .forEach(svg -> svg.setVisible(false)));
        setOnMouseEntered(event -> resizeSvg.setVisible(true));
        setOnMouseExited(event -> resizeSvg.setVisible(false));
        resizeSvg.setId(String.format("%sResizeSvg%s", fxField.getId(), value.name()));

        setOnMousePressed(event -> {
            borderX = getLayoutX() + getPrefWidth() / 2;
            deltaX = borderX - event.getSceneX();
        });

        // dragging property
        if(value.name().equals("RIGHT")) {
            layoutXProperty().bind(fxField.layoutXProperty().add(fxField.prefWidthProperty()));
            setOnMouseDragged(event -> {
                resizeSvg.setVisible(true);
                final double newX = event.getSceneX() + deltaX - getMaxWidth() / 2;
                if(newX < fxField.getLayoutX() + fxField.getMinWidth()
                        || newX >= getParent().getBoundsInLocal().getWidth()
                        || (fxField.getNumberOfLines() == 1
                        && newX > fxField.getLayoutX() + fxField.getPrefWidth())) return;

                fxField.setPrefWidth(newX - fxField.getLayoutX());
                fxField.computeHyphenation();
                fxField.setTextFlowAligned(fxField.getAlignment());
                borderX = getLayoutX() + getPrefWidth() / 2;
            });
        } else { // LEFT
            layoutXProperty().bind(fxField.layoutXProperty().subtract(base.getWidth()));
            setOnMouseDragged(event -> {
                resizeSvg.setVisible(true);
                final double newX = event.getSceneX() + deltaX - getPrefWidth() / 2;
                if(newX <= 0
                        || newX > fxField.getLayoutX() + fxField.getPrefWidth() - fxField.getMinWidth()
                        || (fxField.getNumberOfLines() == 1 && newX < fxField.getLayoutX())) {
                    return;
                }
                double oldWidth = fxField.getPrefWidth();
                fxField.setPrefWidth(fxField.getLayoutX() + oldWidth - newX);
                fxField.computeHyphenation();
                fxField.setLayoutX(fxField.getLayoutX() + oldWidth - fxField.getPrefWidth());
                fxField.setTextFlowAligned(fxField.getAlignment());
                borderX = getLayoutX() + getPrefWidth() / 2;
            });
        }
        setCursor(Cursor.H_RESIZE);
        base.setOpacity(0);

        getChildren().addAll(base, resizeSvg);
    }


    public SVGPath getResizeSvg() {
        return resizeSvg;
    }
}
