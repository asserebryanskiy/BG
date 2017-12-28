package badgegenerator.custompanes;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by andreyserebryanskiy on 11/11/2017.
 */
interface StyleableText {
    Font getFont();
    void setFont(Font font) throws IllegalFontSizeException;
    void setFont(String fontPath) throws IllegalFontSizeException;

    double getFontSize();
    void setFontSize(double newFontSize) throws IllegalFontSizeException;

    Color getFill();
    void setFill(Color color);

    String getAlignment();
    void setAlignment(String alignment);

    boolean isCapitalized();
    void setCapitalized(boolean value);
}
