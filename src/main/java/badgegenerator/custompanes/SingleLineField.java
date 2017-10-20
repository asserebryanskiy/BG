package badgegenerator.custompanes;

import com.sun.javafx.tk.Toolkit;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * An implementation of FxField used to layout single line text values.
 */
public class SingleLineField extends FxField {
    private final String originalValue;
    private Text text;

    public SingleLineField(String value,
                           int numberOfColumn,
                           double imageToPdfRatio,
                           double maxAllowableWidth,
                           double fontSize,
                           String fontPath) {
        super(numberOfColumn,
                imageToPdfRatio,
                maxAllowableWidth,
                fontPath,
                fontSize);
        originalValue = value;
        text = new Text(value);
        text.setTextOrigin(VPos.TOP);
        getChildren().add(text);
        text.setFont(font);
        setPrefWidth(computeStringWidth(value));
        setMaxHeight(computeMaxHeight());
    }

    public SingleLineField(String value, int id, double maxAllowableWidth) {
        this(value, id, 1, maxAllowableWidth, 13, null);
    }

    @Override
    void setFontImpl() {
        text.setFont(font);
        setPrefWidth(computeStringWidth(text.getText()));
    }

    @Override
    String getLongestWord() {
        return text.getText();
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @Override
    void setFontSizeImpl(double newFontSize) {
        text.setFont(font);
        setPrefWidth(computeStringWidth(text.getText()));
    }

    @Override
    void changeFieldsValues() {
        // displays information about clicked fxField
        if (fontSizeField != null) {
            fontSizeField.setText(String.valueOf((int) (getFontSize() / imageToPdfRatio)));
        }
        if (fontNameField != null) {
            fontNameField.setText(font.getName());
            fontColorPicker.requestFocus();
        }
        if (fontColorPicker != null) {
            fontColorPicker.setValue((Color) text.getFill());
        }
        if(alignmentButtons != null) {
            alignmentButtons.forEach(btn -> {
                if(btn.getId().contains(getAlignment().toLowerCase())) {
                    ((SVGPath)btn.getGraphic()).setFill(Color.BLACK);
                } else {
                    ((SVGPath)btn.getGraphic()).setFill(Color.GRAY);
                }
            });
        }
        if(capsLockCheckBox != null) {
            capsLockCheckBox.setSelected(isCapitalized());
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

    @Override
    void setCapitalizedImpl(boolean value) {
        text.setText(value ? originalValue.toUpperCase() : originalValue);
        setPrefWidth(computeStringWidth(text.getText()));
    }
}
