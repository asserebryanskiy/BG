package badgegenerator.pdfcreator;

import badgegenerator.appfilesmanager.AssessableFonts;
import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Is used to transform javaFx field parameters to pdf ones.
 * Includes:
 *  - calculating of new x and y positions, according to imageToPdfRatio
 *  - axes transformation (from upper-left corner in Fx to lower-left in pdf)
 *  - creating PdfFont from javaFX Font and Color respectively
 *  - calculating proper leading between lines.
 * */
public class FxToPdfFieldAdapter {
    private static Logger logger = Logger.getLogger(FxToPdfFieldAdapter.class.getSimpleName());

    private float x;
    private float y;
    private String alignment;
    private float fontSize;
    private FontProgram fontProgram;
    private Color pdfColor;
    private float leading;
    private int numberOfLines;
    private int numberOfColumn;
    private boolean capitalized;

    public FxToPdfFieldAdapter(FxField fxField,
                               double imageToPdfRatio,
                               float pdfHeight) {
        x = (float) (fxField.getLayoutX() / imageToPdfRatio);
        y = (float) (pdfHeight - (fxField.getLayoutY() + getFontMetrics(fxField).getMaxAscent())
                / imageToPdfRatio);
        alignment = fxField.getAlignment();
        fontSize = (float) (fxField.getFontSize() / imageToPdfRatio);

        if(fxField.getFont().getName().equals("Helvetica")) {
            InputStream fontInputStream = getClass()
                    .getResourceAsStream("/fonts/Helvetica.otf");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int a;
            try {
                while((a = fontInputStream.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, a);
                }
                baos.flush();
                fontProgram = FontProgramFactory.createFont(baos.toByteArray());
            } catch (IOException e) {
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE, "Не удалось загрузить Helvetica", e);
                e.printStackTrace();
            }
        } else if(fxField.getFontPath() == null) {
            String fontPath = AssessableFonts.getFontPath(fxField.getFont().getName());
            try {
                fontProgram = FontProgramFactory.createFont(fontPath);
            } catch (IOException e) {
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE,
                        String.format("Не удалось загрузить шрифт из %s", fontPath),
                        e);
                e.printStackTrace();
            }
        } else {
            try {
                fontProgram = FontProgramFactory.createFont(fxField.getFontPath());
            } catch (IOException e) {
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE,
                        String.format("Не удалось загрузить шрифт из %s", fxField.getFontPath()),
                        e);
                e.printStackTrace();
            }
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
        numberOfColumn = fxField.getNumberOfColumn();
        capitalized = fxField.isCapitalized();
    }

    private FontMetrics getFontMetrics(FxField fxField) {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(fxField.getFont());
    }

    float getX() {
        return x;
    }

    float getY() {
        return y;
    }

    public String getAlignment() {
        return alignment;
    }

    float getFontSize() {
        return fontSize;
    }

    FontProgram getFontProgram() {
        return fontProgram;
    }

    Color getPdfColor() {
        return pdfColor;
    }

    float getLeading() {
        return leading;
    }

    int getNumberOfLines() {
        return numberOfLines;
    }

    int getNumberOfColumn() {
        return numberOfColumn;
    }

    boolean isCapitalized() {
        return capitalized;
    }
}
