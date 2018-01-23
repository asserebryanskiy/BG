package badgegenerator.pdfcreator;

import badgegenerator.appfilesmanager.LoggerManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceCmyk;
import com.itextpdf.kernel.color.DeviceGray;
import com.itextpdf.kernel.color.DeviceRgb;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import java.io.ByteArrayOutputStream;
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
    private String columnId;
    private boolean capitalized;

    public FxToPdfFieldAdapter(FxField fxField,
                               double imageToPdfRatio,
                               float pdfHeight) {
        x = (float) (fxField.getLayoutX() / imageToPdfRatio);
        y = (float) (pdfHeight - (fxField.getLayoutY() + getFontMetrics(fxField).getMaxAscent())
                / imageToPdfRatio);
        alignment = fxField.getAlignment();
        fontSize = (float) (fxField.getFontSize() / imageToPdfRatio);

        if(fxField.getFont().getName().equals("Circe Light")) {
            try {
                InputStream fontInputStream = getClass()
                        .getResourceAsStream("/fonts/CRC35.OTF");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int a;
                while((a = fontInputStream.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, a);
                }
                baos.flush();
                fontProgram = FontProgramFactory.createFont(baos.toByteArray());
            } catch (Exception e) {
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE, "Не удалось загрузить Circe Light", e);
                e.printStackTrace();
            }
        } else {
            try {
                fontProgram = FontProgramFactory.createFont(fxField.getFontPath());
            } catch (Exception e) {
                LoggerManager.initializeLogger(logger);
                logger.log(Level.SEVERE,
                        String.format("Не удалось загрузить шрифт из %s", fxField.getFontPath()),
                        e);
                e.printStackTrace();
            }
        }

        // color adaptation
        Color pdfColor = fxField.getPdfColor();
        if (!fxField.usePdfColor() || pdfColor == null) {
            javafx.scene.paint.Color fxColor = fxField.getFill();
            this.pdfColor = new DeviceRgb((float) fxColor.getRed(),
                    (float) fxColor.getGreen(),
                    (float) fxColor.getBlue());
        }
        // usePdfColor is true and pdfColor is not null
        else {
            System.out.println("Set pdf color");
            float[] colorValue = pdfColor.getColorValue();
            this.pdfColor = pdfColor instanceof DeviceCmyk ?
                    new DeviceCmyk(colorValue[0], colorValue[1], colorValue[2], colorValue[3]) :
                    pdfColor instanceof DeviceGray ? new DeviceGray(colorValue[0]) :
                    new DeviceRgb(colorValue[0], colorValue[1], colorValue[2]);
        }

        leading = (float) ((getFontMetrics(fxField).getMaxAscent()
                        + getFontMetrics(fxField).getMaxDescent()) / imageToPdfRatio);
        if (fxField instanceof FieldWithHyphenation) {
            numberOfLines = ((FieldWithHyphenation) fxField).getNumberOfLines();
        } else numberOfLines = 1;
        columnId = fxField.getColumnId();
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

    String getColumnId() {
        return columnId;
    }

    boolean isCapitalized() {
        return capitalized;
    }
}
