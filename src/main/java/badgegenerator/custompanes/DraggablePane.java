package badgegenerator.custompanes;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;


/**
 * Draggable pane could be dropped around its fieldsParent.
 */
public abstract class DraggablePane extends SelectablePane {
    private double mouseX;
    private double mouseY;
    private double fieldX;
    private double fieldY;
    private double deltaX;
    private double deltaY;
    private double leftLayoutBorder;
    private double rightLayoutBorder;
    private double topLayoutBorder;
    private double bottomLayoutBorder;

    DraggablePane() {
        super();
        makeDraggable();
        setManaged(false);
        setCursor(Cursor.OPEN_HAND);
    }

    private void makeDraggable() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            setCursor(Cursor.CLOSED_HAND);
            calculateLayoutBorders();
            changeFieldsValues();
            // point, where mouse clicked
            mouseX = event.getSceneX();
            // fieldX - center of field
            fieldX = getLayoutX() + getPrefWidth() / 2;
            // deltaX - distance between click and center of field
            deltaX = fieldX - mouseX;
            mouseY = event.getSceneY();
            fieldY = getLayoutY() + getMaxHeight() / 2;
            deltaY = fieldY - mouseY;
        });
        addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double newX = event.getSceneX() + deltaX - getPrefWidth() / 2;
            double newY = event.getSceneY() + deltaY - getMaxHeight() / 2;
//            double newX = getLayoutX() + event.getSceneX() + deltaX - fieldX;
//            double newY = getLayoutY() + event.getSceneY() + deltaY - fieldY;
            if (notInsideParent(newX, newY)) return;
            newX = checkIfIntersectVerticalGuides(newX);
            newY = checkIfIntersectHorizontalGuides(newY);

            setLayoutX(newX);
            setLayoutY(newY);
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            fieldX = getLayoutX() + getPrefWidth() / 2;
            fieldY = getLayoutY() + getMaxHeight() / 2;
        });
        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            setCursor(Cursor.OPEN_HAND);
            makeGuidesInvisible();
        });
    }

    /**
     * If some fields (e.g. fontNameField, colorField) are bound to DraggablePane,
     * than in the moment DraggablePane is clicked the information on those field
     * is refreshed according to DraggablePane parameters.
     * */
    abstract void changeFieldsValues();
    abstract double checkIfIntersectVerticalGuides(double newX);
    abstract double checkIfIntersectHorizontalGuides(double newY);
    abstract void makeGuidesInvisible();

    private void calculateLayoutBorders() {
        leftLayoutBorder = 0;
        rightLayoutBorder = getParent().getBoundsInLocal().getWidth();
        topLayoutBorder = 0;
        bottomLayoutBorder = getParent().getBoundsInLocal().getHeight();
    }

    private boolean notInsideParent(double newX, double newY) {
        if (newX < leftLayoutBorder) {
            setLayoutX(leftLayoutBorder);
            return true;
        } else if (newX + getPrefWidth() > rightLayoutBorder) {
            setLayoutX(rightLayoutBorder - getPrefWidth());
            return true;
        }
        if (newY < topLayoutBorder) {
            setLayoutY(topLayoutBorder);
            return true;
        } else if (newY + getMaxHeight() > bottomLayoutBorder) {
            setLayoutY(bottomLayoutBorder - getMaxHeight());
            return true;
        }
        return false;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }
}
