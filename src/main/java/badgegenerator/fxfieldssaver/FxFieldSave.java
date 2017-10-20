package badgegenerator.fxfieldssaver;

import badgegenerator.custompanes.FxField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.Serializable;

/**
 * Special class to implement FxField save for future use.
 */
public class FxFieldSave implements Serializable{
    private static final long serialVersionUID = 2903588029587616670L;
    private double x;
    private double y;
    private double width;
    private double red;
    private double green;
    private double blue;
    private double fontSize;
    private String fontName;
    private String fontPath;
    private String alignment;
    private int numberOfColumn;
    private Font font;
    private boolean capitalized;

    public FxFieldSave(FxField field) {
        x = field.getLayoutX();
        y = field.getLayoutY();
        width = field.getPrefWidth();
        fontSize = field.getFontSize();
        Color color = field.getFill();
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
        fontName = field.getFont().getName();
        alignment = field.getAlignment();
        numberOfColumn = field.getNumberOfColumn();
        capitalized = field.isCapitalized();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRed() {
        return red;
    }

    public double getGreen() {
        return green;
    }

    public double getBlue() {
        return blue;
    }

    public double getFontSize() {
        return fontSize;
    }

    public String getFontPath() {
        return fontPath;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public int getNumberOfColumn() {
        return numberOfColumn;
    }

    public String getAlignment() {
        return alignment;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Font getFont() {
        return font;
    }

    public double getWidth() {
        return width;
    }

    public boolean isCapitalized() {
        return capitalized;
    }
}
