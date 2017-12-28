package badgegenerator.custompanes;

import badgegenerator.helppopup.HelpPopUp;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 * Borders, that could be added to the field with possible hyphenation.
 * Provide possibility to change its width and consequently wrapping property.
 *
 * If field is aligned CENTER than changes fxFields width on both sides.
 */
public class ResizeableBorder extends StackPane{
    private static final int COUNTS_TILL_TOOLTIP = 100;  // value of counter on which a popup with information
                                                         // about impossibility of further resize will show

    private final FieldWithHyphenation fxField;
    private final boolean positionLeft; // is this ResizeableBorder placed at the start of FxField?

    private int counter;                // number of tries to resize FxField more than allowed
    private double maxAllowableWidth;   // width of the parent
    private SVGPath resizeSvg;          // H-Resize icon
    private double deltaX;              // distance between center of ResizeableBorder
                                        // and the point where mouse clicked
    private double oldX;                // previous position of this ResizeableBorder
    private HelpPopUp popup;            // popup that is shown if user is trying
                                        // to resize FxField more than allowed

    // Pane structure
    public ResizeableBorder(FieldWithHyphenation fxField,
                            Position position) {
        super();
        this.fxField = fxField;
        positionLeft = position.equals(Position.LEFT);
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
                .forEach(svg -> svg.setVisible(true)));
        fxField.setOnMouseExited(event -> fxField.getResizeableBorders().stream()
                .map(ResizeableBorder::getResizeSvg)
                .forEach(svg -> svg.setVisible(false)));
        /*// DEBUG //
        setOnMouseEntered(event -> fxField.getResizeableBorders().stream()
                .map(ResizeableBorder::getResizeSvg)
                .forEach(svg -> svg.setVisible(true)));
        // DEBUG //*/
        setOnMouseEntered(event -> resizeSvg.setVisible(true));
        setOnMouseExited(event -> resizeSvg.setVisible(false));
        setId(String.format("%sResizeableBorder%s", fxField.getId(), position.name()));

        // on mouse click update borderX and deltaX values
        setOnMousePressed(event -> {
            deltaX = getLayoutX() + getMaxWidth() / 2 - event.getSceneX();
            if (positionLeft) oldX = getLayoutX() + getMaxWidth();
            else              oldX = getLayoutX();
            maxAllowableWidth = getParent().getBoundsInLocal().getWidth();
        });

        // dragging property
        if(!positionLeft) { // Position.RIGHT
            layoutXProperty().bind(fxField.layoutXProperty().add(fxField.prefWidthProperty()));
            setOnMouseDragged((MouseEvent event) -> {
                // set visible
                if (fxField.getAlignment().equals("CENTER")) fxField.getResizeableBorders()
                        .forEach(b -> b.getResizeSvg().setVisible(true));
                else resizeSvg.setVisible(true);

                // save initial field parameters
                double fieldStartX     = fxField.getLayoutX();
                double fieldStartWidth = fxField.getPrefWidth();

                // compute precise new layoutX of ResizeableBorder and delta with old one
                double newX = event.getSceneX() + deltaX - getMaxWidth() / 2;
                double delta = newX - oldX;

                // if user is trying to resize FxField to less than minWidth value show popup with information
                if (newX < fieldStartX + fxField.getMinWidth()) {
                    counter++;
                    if (counter > COUNTS_TILL_TOOLTIP) {
                        if (popup != null && popup.isShowing()) return;
                        popup = new HelpPopUp(prepareMessage());
                        popup.show(this, event.getSceneX(), event.getScreenY());
                    }
                    return;
                }
                else counter = 0;
                // if out of screen or if FxField is already one-line return
                if (newX < fieldStartX + fxField.getMinWidth()
                        || newX >= maxAllowableWidth
                        || (fxField.getNumberOfLines() == 1
                        && newX > fieldStartX + fxField.getPrefWidth())) return;

                // if FxField is aligned center, it changes width in both directions
                if (fxField.getAlignment().equals("CENTER")) {
                    processCenterAligned(fieldStartX, fieldStartWidth, delta);
                }
                // if not aligned CENTER
                else {
                    double newWidth = fxField.getPrefWidth() + delta;
                    fxField.setPrefWidth(newWidth);
                    if (fxField.couldBeHyphenated()) {
                        fxField.computeHyphenation();
                        fxField.setPrefWidth(newWidth);
                        // if new prefWidth is smaller than minWidth arrange
                        // text properly inside pane
                        fxField.setTextFlowAligned();
                    }
                }
                oldX = newX;
            });
        }
        // Position.LEFT
        else {
            layoutXProperty().bind(fxField.layoutXProperty().subtract(base.getWidth()));
            setOnMouseDragged(event -> {
                // set visible
                if (fxField.getAlignment().equals("CENTER")) fxField.getResizeableBorders()
                        .forEach(b -> b.getResizeSvg().setVisible(true));
                else resizeSvg.setVisible(true);

                // save initial field parameters
                double fieldStartX     = fxField.getLayoutX();
                double fieldStartWidth = fxField.getPrefWidth();

                // compute precise new layoutX of ResizeableBorder and delta with old one
                double newX = event.getSceneX() + deltaX + getMaxWidth() / 2;
                double delta = oldX - newX;

                // if user is trying to resize FxField to less than minWidth value show popup with information
                if (newX > fieldStartX + fieldStartWidth - fxField.getMinWidth()) {
                    counter++;
                    if (counter > COUNTS_TILL_TOOLTIP) {
                        if (popup != null && popup.isShowing()) return;
                        popup = new HelpPopUp(prepareMessage());
                        popup.show(this, event.getSceneX(), event.getScreenY());
                    }
                    return;
                }
                else counter = 0;

                // if out of screen or if FxField is already one-line return
                if(newX <= 0
                        || newX > fieldStartX + fieldStartWidth - fxField.getMinWidth()
                        || (fxField.getNumberOfLines() == 1 && newX < fieldStartX)) {
                    return;
                }
                if (fxField.getAlignment().equals("CENTER")) {
                    processCenterAligned(fieldStartX, fieldStartWidth, delta);
                }
                // if not aligned CENTER
                else {
                    double newWidth = fxField.getPrefWidth() + delta;
                    fxField.setPrefWidth(newWidth);
                    if (fxField.couldBeHyphenated()) {
                        fxField.computeHyphenation();
                        // we need to set pref width again because
                        // after hyphenation computation it was updated
                        fxField.setPrefWidth(newWidth);
                        // if size is increasing
                        if (delta > 0) fxField.setTextFlowPos(0);
                        else           fxField.setTextFlowPos(newWidth
                                            - fxField.getLongestLineWidth());
                    } else {
                        fxField.setTextFlowPos(Math.max(0, fxField.getTextFlowPos() + delta));
                    }
                    fxField.setLayoutX(fieldStartX - delta);
                }
                oldX = newX;
            });
        }

        // after mouse released update prefWidth of FxField, make borders invisible
        setOnMouseReleased(e -> {
            double llw = fxField.getLongestLineWidth(); // longest line width
            double minWidth = fxField.getMinWidth();
            if (fxField.getAlignment().equals("CENTER") && fxField.getTextFlowPos() > 0) {
                double delta = fxField.getTextFlowPos();
                if (llw < minWidth) {
                    fxField.setTextFlowPos((minWidth - llw) / 2);
                    fxField.setPrefWidth(minWidth);
                } else {
                    fxField.setTextFlowPos(0);
                    fxField.setPrefWidth(Math.max(fxField.getPrefWidth() - 2 * delta, minWidth));
                    fxField.setLayoutX(fxField.getLayoutX() + delta);
                }
            } else {
                if (positionLeft) fxField.setLayoutX(fxField.getLayoutX()
                        + (fxField.getPrefWidth() - Math.max(llw, minWidth)));
                fxField.setPrefWidth(Math.max(llw, minWidth));
                fxField.setTextFlowAligned();
            }
            if (fxField.getAlignment().equals("CENTER")) fxField.getResizeableBorders()
                    .forEach(b -> b.getResizeSvg().setVisible(false));
            else resizeSvg.setVisible(false);

            counter = 0;
        });

        // set parameters of ResizeableBorder
        setCursor(Cursor.H_RESIZE);
        base.setOpacity(0);
        getChildren().addAll(base, resizeSvg);
    }

    private String prepareMessage() {
        return String.format("Поле \"%s\" не может быть более уменьшенно, " +
                "так как тогда в границы поля не поместится значение \"%s\".%n",
                fxField.getText(), fxField.getLongestWord());
    }

    private void processCenterAligned(double fieldStartX, double fieldStartWidth, double delta) {
        // 2 because we increment in the beginning and in the end of the field
        double newWidth = fxField.getPrefWidth() + 2 * delta;
        fxField.setPrefWidth(newWidth); // apply new width
        fxField.setLayoutX(fxField.getLayoutX() - delta); // align center again
        // if could be hyphenated hyphenate and then change textFlow position
        if (fxField.couldBeHyphenated()) {
            fxField.computeHyphenation();
            if (delta > 0) fxField.setTextFlowPos(0);
            else fxField.setTextFlowPos((newWidth - fxField.getLongestLineWidth()) / 2);
            fxField.setPrefWidth(newWidth);
            fxField.setLayoutX(fieldStartX + (fieldStartWidth - fxField.getPrefWidth()) / 2);
        } else {
            fxField.setTextFlowPos(fxField.getTextFlowPos() + delta);
        }
    }

    // helper method that is used only inside this class to get
    // svg of the opposite to this ResizeableBorder of some FxField
    private SVGPath getResizeSvg() {
        return resizeSvg;
    }
}
