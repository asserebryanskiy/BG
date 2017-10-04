package badgegenerator.custompanes;

import javafx.scene.shape.Line;

/**
 * Guides are used to position objects on the screen.
 * Currently only vertical guides are available.
 */
public class Guide extends Line {
    public Guide(badgegenerator.custompanes.FxField fxField,
                 Position position) throws NoParentFoundException, NoIdFoundException {
        super();
        setManaged(false);
        setVisible(false);
        if (fxField.getParent() == null) {
            throw new NoParentFoundException("Could be used only with fxField that has Parent");
        } else if (fxField.getId() == null) {
            throw new NoIdFoundException("Could be used only with fxField that has ID");
        } else {
            setStartY(0);
            setEndY(fxField.getParent().getBoundsInLocal().getHeight());
            if(position.equals(Position.RIGHT)) {
                setId(String.format("%sEndGuide", fxField.getId()));
                setStartX(fxField.getLayoutX() + fxField.getPrefWidth());
                setEndX(fxField.getLayoutX() + fxField.getPrefWidth());
                startXProperty()
                        .bind(fxField.layoutXProperty().add(fxField.prefWidthProperty()));
                endXProperty()
                        .bind(fxField.layoutXProperty().add(fxField.prefWidthProperty()));
            } else {
                setId(String.format("%sStartGuide", fxField.getId()));
                setStartX(fxField.getLayoutX());
                setEndX(fxField.getLayoutX());
                startXProperty().bind(fxField.layoutXProperty());
                endXProperty().bind(fxField.layoutXProperty());
            }
        }
    }
}
