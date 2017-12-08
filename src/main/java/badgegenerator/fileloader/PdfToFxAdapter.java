package badgegenerator.fileloader;

import badgegenerator.appfilesmanager.AssessableFonts;
import com.itextpdf.kernel.font.PdfFont;
import javafx.scene.paint.Color;

/**
 * Created by andreyserebryanskiy on 06/12/2017.
 */
public class PdfToFxAdapter {
    private final String columnId;
    private final double x;
    private final double y;
    private final Color color;
    private final double fontSize;
    private final String fontName;
    private final String fontPath;
    private String alignment;

    public PdfToFxAdapter(PdfField field, double imageToPdfRatio) {
        columnId = field.getName();
        x = field.getX() * imageToPdfRatio;
        y = (field.getPdfHeight() - field.getY()) * imageToPdfRatio;
        float[] colorValue = field.getColor().getColorValue();
        color = Color.color(colorValue[0], colorValue[1], colorValue[2]);
        PdfFont pdfFont = field.getFont();
        fontName = getFontName(pdfFont.getFontProgram().toString());
        fontPath = AssessableFonts.getFontPath(fontName);
        fontSize = field.getFontSize() * imageToPdfRatio;
        alignment = field.getAlignment();
    }

    private String getFontName(String s) {
        StringBuilder builder = new StringBuilder();
        int styleIndex = s.indexOf('-');
        if (styleIndex == -1) {
            return s.substring(s.indexOf('+') + 1);
        }
        String name  = s.substring(s.indexOf('+') + 1, styleIndex);
        String style = s.substring(styleIndex + 1);
        return builder.append(name).append(" ").append(style).toString();
    }

    public String getColumnId() {
        return columnId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public double getFontSize() {
        return fontSize;
    }

    public String getFontName() {
        return fontName;
    }

    public String getFontPath() {
        return fontPath;
    }

    public String getAlignment() {
        return alignment;
    }
}
