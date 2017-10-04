package badgegenerator.custompanes;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.FileNotFoundException;

/**
 * An implementation of FxField used to layout single line text values.
 */
public class SingleLineField extends FxField {
    private Text text;

    public SingleLineField(String value,
                           int numberOfColumn,
                           double imageToPdfRatio,
                           double maxAllowableWidth,
                           double fontSize,
                           String fontPath) throws FileNotFoundException {
        super(numberOfColumn,
                imageToPdfRatio,
                maxAllowableWidth,
                fontPath,
                fontSize);
        text = new Text(value);
        getChildren().add(new VBox(text));
        text.setFont(font);
        setPrefWidth(computeStringWidth(value));
        setMaxHeight(computeMaxHeight());
    }

    @Override
    void setFontImpl(Font newFont) {
        text.setFont(newFont);
        setPrefWidth(getBoundsInLocal().getWidth());
    }

    @Override
    void setFontSizeImpl(double newFontSize) {
        if (newFontSize > 200
                || computeStringWidth(text.getText()) > maxAllowableWidth) {
            setFontSize(--newFontSize);
            return;
        }
        text.setFont(new Font(font.getName(), newFontSize));
        setPrefWidth(getBoundsInLocal().getWidth());
    }

    @Override
    void changeFieldsValues() {
        // displays information about clicked fxField
        if (fontSizeField != null) {
            fontSizeField.setText(String.valueOf((int) (getFontSize() / imageToPdfRatio)));
        }
        if (fontNameField != null) {
            fontNameField.setText(font.getName());
        }
        if (fontColorPicker != null) {
            fontColorPicker.setValue((Color) text.getFill());
        }
    }

    @Override
    void setTextFlowAligned(String alignment) {
    }

    @Override
    double computeMaxHeight() {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(font).getLineHeight();
    }

    @Override
    void setFillImpl(Color color) {
        text.setFill(color);
    }

    @Override
    void setAlignmentImpl(String alignment) {
        text.setTextAlignment(TextAlignment.valueOf(alignment));
    }
}
