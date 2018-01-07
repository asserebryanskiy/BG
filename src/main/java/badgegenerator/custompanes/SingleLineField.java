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
    private final String originalValue;  // value with which field was initialized
    private Text text;                   // javaFx Text that holds String value

    public SingleLineField(String value,
                           String columnId,
                           double imageToPdfRatio,
                           double maxAllowableWidth) {
        super(columnId,
                imageToPdfRatio,
                maxAllowableWidth);
        originalValue = value;
        text = new Text(value);
        text.setTextOrigin(VPos.TOP);
        getChildren().add(text);
        text.setFont(font);
        setPrefWidth(computeStringWidth(value));
        setMaxHeight(computeMaxHeight());
    }

    public SingleLineField(String value, String id, double maxAllowableWidth) {
        this(value, id, 1, maxAllowableWidth);
    }

    @Override
    void setFontImpl() {
        changeFont();
    }

    private void changeFont() {
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
        changeFont();
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
                    ((SVGPath)btn.getGraphic()).setFill(PRESSED_COLOR);
                } else {
                    ((SVGPath)btn.getGraphic()).setFill(Color.WHITE);
                }
            });
        }
        if(capsLockCheckBox != null) {
            capsLockCheckBox.setSelected(isCapitalized());
        }
        if (usePdfColorMenuItem != null) {
            usePdfColorMenuItem.setSelected(usePdfColor() && getPdfColor() != null);
            usePdfColorMenuItem.setDisable(getPdfColor() == null);
        }
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
