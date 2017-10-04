package badgegenerator.custompanes;

import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * Created by andreyserebryanskiy on 28/09/2017.
 */
public abstract class SelectablePane extends Pane{
    public boolean isSelected;

    public SelectablePane() {
        super();
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if(event.isControlDown()) setSelected(true);
            else {
                for (Node node : getParent().getChildrenUnmodifiable()) {
                    if(node instanceof SelectablePane) {
                        ((SelectablePane) node).setSelected(false);
                    }
                }
                setSelected(true);
            }
        });
    }

    public void setSelected(boolean value) {
        isSelected = value;
        setEffect(isSelected ? new Glow(1) : null);
    }
}
