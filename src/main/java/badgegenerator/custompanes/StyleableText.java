package badgegenerator.custompanes;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by andreyserebryanskiy on 11/11/2017.
 */
interface StyleableText {
    Font getFont();
    void setFont(Font font);
    void setFont(String fontPath);

    double getFontSize();
    void setFontSize(double newFontSize);

    Color getFill();
    void setFill(Color color);

    String getAlignment();
    void setAlignment(String alignment);

    boolean isCapitalized();
    void setCapitalized(boolean value);

    boolean isBold();
    void setBold(boolean value);

    boolean isItalic();
    void setItalic(boolean value);
}
