package badgegenerator.fileloader;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PdfField collect all necessary information for further
 * creation of badges.
 *
 * Created by andreyserebryanskiy on 05/12/2017.
 */
public class PdfField {
    // obligatory fields
    private final String name;
    private final float x;
    private final float y;
    private final float pdfHeight;

    private Color color = Color.BLACK;
    private PdfFont font;
    private float fontSize = 12;
    private String alignment;

    public PdfField(String name, float x, float y, float pdfHeight) throws IOException {
        this.name = name;
        this.x = x;
        this.y = y;
        this.pdfHeight = pdfHeight;
        createDefaultFont();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFont(PdfFont font) {
        this.font = font;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public PdfFont getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    @Override
    public String toString() {
        float[] colorValue = color.getColorValue();
        return String.format("%s: x - %f, y - %f, color - %.0f-%.0f-%.0f, font - %s, fontSize - %f",
                name, x, y, colorValue[0], colorValue[1], colorValue[2],
                font.getFontProgram().getFontNames().getCidFontName(), fontSize);
    }

    private void createDefaultFont() throws IOException {
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
    }

    public float getPdfHeight() {
        return pdfHeight;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public String getAlignment() {
        return alignment;
    }
}
