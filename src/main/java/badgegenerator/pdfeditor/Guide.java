package badgegenerator.pdfeditor;

import javafx.scene.shape.Line;

/**
 * Guides are used to position objects on the screen.
 * Currently only vertical guides are available.
 */
public class Guide extends Line {
    public Guide(Field field, Position position) throws NoParentFoundException, NoIdFoundException {
        super();
        if (field.getParent() == null) {
            throw new NoParentFoundException("Could be used only with field that has Parent");
        } else if (field.getId() == null) {
            throw new NoIdFoundException("Could be used only with field that has ID");
        } else {
            setStartY(0);
            setEndY(field.getParent().getBoundsInLocal().getHeight());
            if(position.equals(Position.RIGHT)) {
                setId(String.format("%sEndGuide", field.getId()));
                setStartX(field.getLayoutX() + field.getPrefWidth());
                setEndX(field.getLayoutX() + field.getPrefWidth());
                startXProperty()
                        .bind(field.layoutXProperty().add(field.prefWidthProperty()));
                endXProperty()
                        .bind(field.layoutXProperty().add(field.prefWidthProperty()));
            } else {
                setId(String.format("%sStartGuide", field.getId()));
                setStartX(field.getLayoutX());
                setEndX(field.getLayoutX());
                startXProperty().bind(field.layoutXProperty());
                endXProperty().bind(field.layoutXProperty());
            }
        }
    }
}
