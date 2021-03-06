package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.IllegalFontSizeException;
import badgegenerator.custompanes.SingleLineField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.testfx.framework.junit.ApplicationTest;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 10/10/2017.
 */
public class FontChangeTest extends ApplicationTest{
    private FieldWithHyphenation fieldWithHyp;
    private SingleLineField field;
    private List<FxField> fields;

    @Before
    public void setUp() throws Exception {
        fieldWithHyp = new FieldWithHyphenation("Example words", "Example", 200);
        field = new SingleLineField("Example", "Example1", 200);
        fields = new ArrayList<>();
        fields.add(fieldWithHyp);
        fields.add(field);
    }

    @After
    public void tearDown() throws Exception {
        fields.clear();
    }

    @Test
    public void prefSizeChangesOnFontChange() throws Exception {
        // Arrange
        URL fontUrl = getClass().getResource("/fonts/freeset.ttf");
        String fontPath = Paths.get(fontUrl.toURI()).toFile().getAbsolutePath();

        // Act
        for (FxField f : fields) {
            f.setFont(fontPath);
        }

        // Assert
        assertThat("SingleLine", field.getPrefWidth(), is(field.computeStringWidth("Example")));
        assertThat("With hyphenation", fieldWithHyp.getPrefWidth(),
                is(fieldWithHyp.computeStringWidth("Example words")));
    }

    @Test
    public void ifNewFontGreaterThanMaxAllowableWidthItHyphenates() throws Exception {
        // Act
        fieldWithHyp.setFontSize(40);

        // Assert
        assertThat(fieldWithHyp.getNumberOfLines(), is(2));
    }

    @Test
    public void prefSizeIncreasesOnFontSizeIncrease() throws Exception {
        // Act
        fields.forEach(f -> {
            try {
                f.setFontSize(20);
            } catch (IllegalFontSizeException ignored) {
            }
        });

        // Assert
        assertThat("SingleLine", field.getPrefWidth(), is(field.computeStringWidth("Example")));
        assertThat("With hyphenation", fieldWithHyp.getPrefWidth(),
                is(fieldWithHyp.computeStringWidth("Example words")));
    }

    @Test
    public void prefSizeDecreasesOnFontSizeDecrease() throws Exception {
        // Act
        fields.forEach(f -> {
            try {
                f.setFontSize(10);
            } catch (IllegalFontSizeException e) {
                e.printStackTrace();
            }
        });

        // Assert
        assertThat("SingleLine", field.getPrefWidth(), is(field.computeStringWidth("Example")));
        assertThat("With hyphenation", fieldWithHyp.getPrefWidth(),
                is(fieldWithHyp.computeStringWidth("Example words")));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fontSizeCouldNotBeSetGreaterThanMax() throws Exception {
        thrown.expect(IllegalFontSizeException.class);

        // Act
        for (FxField f : fields) {
            f.setFontSize(60);
        }
    }



    @Test
    public void colorPreservesOnFontChange() throws Exception {
        // Arrange
        fields.forEach(f -> f.setFill(Color.RED));
        URL fontUrl = getClass().getResource("/fonts/freeset.ttf");
        String fontPath = Paths.get(fontUrl.toURI()).toFile().getAbsolutePath();


        // Act
        for (FxField f : fields) {
            f.setFont(fontPath);
        }

        // Assert
        assertThat("SingleLine", field.getFill(), is(Color.RED));
        assertThat("Multi line", fieldWithHyp.getFill(), is(Color.RED));
    }

    @Test
    public void colorPreservesOnFontSizeChange() throws Exception {
        // Arrange
        fields.forEach(f -> f.setFill(Color.RED));

        // Act
        fields.forEach(f -> {
            try {
                f.setFontSize(20);
            } catch (IllegalFontSizeException e) {
                e.printStackTrace();
            }
        });

        // Assert
        assertThat("SingleLine", field.getFill(), is(Color.RED));
        assertThat("Multi line", fieldWithHyp.getFill(), is(Color.RED));
    }

    @Test
    public void alignedFieldsSavesAlignmentOnFontChange() throws Exception {
        // Arrange
        fields.forEach(f -> f.setAlignment("RIGHT"));
        double fieldRightX = field.getPrefWidth();
        double fieldWithHypRightX = fieldWithHyp.getPrefWidth();
        URL fontUrl = getClass().getResource("/fonts/freeset.ttf");
        String fontPath = Paths.get(fontUrl.toURI()).toFile().getAbsolutePath();


        // Act
        for (FxField f : fields) {
            f.setFont(fontPath);
        }

        // Assert
        assertThat("Single line", field.getLayoutX() + field.getPrefWidth(),
                is(fieldRightX));
        assertThat("Multi line", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                is(fieldWithHypRightX));
    }

    @Test
    public void alignedFieldsSavesAlignmentOnFontSizeChange() throws Exception {
        // Arrange
        fields.forEach(f -> f.setAlignment("RIGHT"));
        double fieldRightX = field.getPrefWidth();
        double fieldWithHypRightX = fieldWithHyp.getPrefWidth();

        // Act
        fields.forEach(f -> {
            try {
                f.setFontSize(20);
            } catch (IllegalFontSizeException e) {
                e.printStackTrace();
            }
        });

        // Assert
        assertThat("Single line", field.getLayoutX() + field.getPrefWidth(),
                is(fieldRightX));
        assertThat("Multi line", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                is(fieldWithHypRightX));
    }

    @Override
    public void start(Stage stage) throws Exception {
    }
}
