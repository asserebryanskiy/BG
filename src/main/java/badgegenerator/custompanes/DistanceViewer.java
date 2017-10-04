package badgegenerator.custompanes;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * DistanceViewer is added with fxField to Stage and displays fxField's current location.
 */
public class DistanceViewer extends Pane {
    private Text text;
    public DistanceViewer(FxField fxField, Orientation orientation) {
        setManaged(false);
        setVisible(false);
        text = new Text();
        Line line = new Line();
        line.setStrokeWidth(1.5);
//        getChildren().addAll(line, text);
        text.setFont(new Font(10));
        text.setVisible(true);
        fxField.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->
                setVisible(true));
        fxField.addEventHandler(MouseEvent.MOUSE_RELEASED, event ->
                setVisible(false));
        if(orientation.name().equals("VERTICAL")) {
            text.setText(String.valueOf((int) ((500 - fxField.getLayoutY())
                    * fxField.getImageToPdfRatio())));
            setLayoutX(20);
            line.setStartX(0);
            line.setEndX(25);
            line.setStartY(0);
            line.setEndY(0);
            layoutYProperty().bind(fxField.layoutYProperty());
            layoutYProperty().addListener((observable, oldValue, newValue) ->
                    text.setText(String.format("%.0f", (500 - newValue.doubleValue())
                                                    * fxField.getImageToPdfRatio())));
            GridPane grid = new GridPane();
            grid.addColumn(0, line, text);
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setHalignment(HPos.RIGHT);
            grid.getColumnConstraints().add(constraints);
            getChildren().add(grid);
        } else {
            text.setText(String.valueOf((int) (fxField.getLayoutX()
                    * fxField.getImageToPdfRatio())));
            setLayoutY(10);
            line.setStartX(0);
            line.setEndX(0);
            line.setStartY(0);
            line.setEndY(16);
            layoutXProperty().bind(fxField.layoutXProperty());
            fxField.layoutXProperty().addListener(((observable, oldValue, newValue) ->
                    text.setText(String.valueOf((int) (newValue.doubleValue()
                            * fxField.getImageToPdfRatio())))));
            GridPane grid = new GridPane();
            grid.addRow(0, line, text);
            RowConstraints constraints = new RowConstraints();
            constraints.setValignment(VPos.BASELINE);
            grid.getRowConstraints().add(constraints);
            getChildren().add(grid);
        }
    }

    public String getText() {
        return text.getText();
    }
}
