package badgegenerator.fileloader;

import badgegenerator.appfilesmanager.AssessableFonts;
import badgegenerator.appfilesmanager.LoggerManager;
import com.itextpdf.kernel.color.DeviceCmyk;
import com.itextpdf.kernel.color.DeviceGray;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by andreyserebryanskiy on 06/12/2017.
 */
public class PdfToFxAdapter {
    private static Logger logger = Logger.getLogger(PdfToFxAdapter.class.getSimpleName());

    private final String columnId;      // name of column in excel
    private final double x;             // x-coord in JavaFX environment
    private final double y;             // y-coord in JavaFX environment
    private Color color;          // javafx.color
    private com.itextpdf.kernel.color.
            Color pdfColor;
    private final double fontSize;      // fontSize in JavaFX
    private final String fontName;      // name of the font in the form it could be found on local machine
    private final String fontPath;      // path to the font
    private final boolean capitalized;  // should all letters of this field be capitalized
    private final String alignment;     // alignment (left/center/right) of the field

    public PdfToFxAdapter(PdfField field, double imageToPdfRatio) {
        columnId = field.getName();
        x = field.getX() * imageToPdfRatio;
        // we subtract from pdfHeight, because in pdf y-coord is computed from bottom
        // and in JavaFX from top
        y = (field.getPdfHeight() - field.getY()) * imageToPdfRatio;
        setColor(field);
        PdfFont pdfFont = field.getFont();
        fontName = getFontName(pdfFont.getFontProgram().toString());
        fontPath = AssessableFonts.getFontPath(fontName);
        fontSize = field.getFontSize() * imageToPdfRatio;
        capitalized = field.isCapitalized();
        alignment = field.getAlignment();
    }

    private void setColor(PdfField field) {
        // try-catch is done to prevent unexpected behaviour from PdfColor
        // which happens to be very different depending on how that pdf was created.
        try {
            pdfColor = field.getColor();
            float[] colorValue;
            if (pdfColor instanceof DeviceGray) {
                float gray = pdfColor.getColorValue()[0];
                color = Color.color(gray, gray, gray);
            } else if (pdfColor instanceof DeviceCmyk) {
                DeviceRgb rgb = com.itextpdf.kernel.color.Color
                        .convertCmykToRgb((DeviceCmyk) field.getColor());
                colorValue = rgb.getColorValue();
                color = Color.color(colorValue[0], colorValue[1], colorValue[2]);
            } else {
                colorValue = pdfColor.getColorValue();
                color = Color.color(colorValue[0], colorValue[1], colorValue[2]);
            }
        } catch (Exception e) {
            LoggerManager.initializeLogger(logger);
            logger.log(Level.INFO, pdfColor.toString());
            logger.log(Level.SEVERE, "error processing color", e);
            color = null;
        }
    }

    // retrieves human-readable font name from pdfFontName
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

    public boolean isCapitalized() {
        return capitalized;
    }

    public com.itextpdf.kernel.color.Color getPdfColor() {
        return pdfColor;
    }
}
