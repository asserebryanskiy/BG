package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import com.itextpdf.kernel.color.DeviceRgb;
import javafx.scene.paint.Color;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by andreyserebryanskiy on 04/01/2018.
 */
public class ColorChangeTest {
    @Test
    public void afterSettingFillUsePdfColorIsFalse() throws Exception {
        FxField field = new SingleLineField("Example", "Example", 100);
        field.setPdfColor(new DeviceRgb());
        assertThat("Before", field.usePdfColor(), is(true));

        // Act
        field.setFill(Color.RED);

        // Assert
        assertThat("After", field.usePdfColor(), is(false));
    }

    @Test
    public void afterSettingUsePdfColorToTrueFillChangesToPdfColor() throws Exception {
        FxField field = new SingleLineField("Example", "Example", 100);
        DeviceRgb pdfColor = new DeviceRgb();
        field.setPdfColor(pdfColor);
        field.setFill(Color.RED);

        // Act
        field.setUsePdfColor(true);

        // Assert
        assertThat("Red", pdfColor.getColorValue()[0], is((float) field.getFill().getRed()));
        assertThat("Green", pdfColor.getColorValue()[1], is((float) field.getFill().getGreen()));
        assertThat("Blue", pdfColor.getColorValue()[2], is((float) field.getFill().getBlue()));
    }

    @Test
    public void ifPdfColorIsNullSettingUsePdfColorToTrueWillNotInfluence() throws Exception {
        FxField field = new SingleLineField("Example", "Example", 100);
        Color color = Color.RED;
        field.setFill(color);

        // Act
        field.setUsePdfColor(true);

        // Assert
        assertThat("Wrong color", field.getFill(), is(color));
        assertThat("Wrong usePdfColor value", field.usePdfColor(), is(false));
    }
}
