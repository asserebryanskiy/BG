package badgegenerator.pdfcreator;

import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Is used to transform javaFx field parameters to pdf ones.
 * Includes:
 *  - calculating of new x and y positions, according to imageToPdfRatio
 *  - axes transformation (from upper-left corner in Fx to lower-left in pdf)
 *  - creating PdfFont from javaFX Font and Color respectively
 *  - calculating proper leading between lines.
 * */
public class FxToPdfFieldAdapter {
    private float x;
    private float y;
    private String alignment;
    private float fontSize;
    private PdfFont font;
    private Color pdfColor;
    private float leading;
    private int numberOfLines;

    public FxToPdfFieldAdapter(FxField fxField,
                               double imageToPdfRatio,
                               float pdfHeight) throws IOException {
        x = (float) (fxField.getLayoutX() / imageToPdfRatio);
        y = (float) (pdfHeight - (fxField.getLayoutY() + getFontMetrics(fxField).getMaxAscent())
                / imageToPdfRatio);
        alignment = fxField.getAlignment();
        fontSize = (float) (fxField.getFontSize() / imageToPdfRatio);

        if(fxField.getFontPath() == null) {
            InputStream fontInputStream = getClass()
                    .getResourceAsStream("/fonts/Helvetica.otf");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int a;
            while((a = fontInputStream.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, a);
            }
            baos.flush();
            font = PdfFontFactory.createFont(baos.toByteArray(),
                    PdfEncodings.IDENTITY_H, true);
        } else {
            font = PdfFontFactory.createFont(fxField.getFontPath(),
                    PdfEncodings.IDENTITY_H, true);
        }

        javafx.scene.paint.Color fxColor = fxField.getFill();
        pdfColor = new DeviceRgb((int) (fxColor.getRed() * 255),
                (int) (fxColor.getGreen() * 255),
                (int) (fxColor.getBlue() * 255));

        leading = (float) ((getFontMetrics(fxField).getMaxAscent()
                        + getFontMetrics(fxField).getMaxDescent()) / imageToPdfRatio);
        if (fxField instanceof FieldWithHyphenation) {
            numberOfLines = ((FieldWithHyphenation) fxField).getNumberOfLines();
        } else numberOfLines = 1;
    }

    private FontMetrics getFontMetrics(FxField fxField) {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(fxField.getFont());
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getAlignment() {
        return alignment;
    }

    public float getFontSize() {
        return fontSize;
    }

    public PdfFont getFont() {
        return font;
    }

    public Color getPdfColor() {
        return pdfColor;
    }

    public float getLeading() {
        return leading;
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }
}
