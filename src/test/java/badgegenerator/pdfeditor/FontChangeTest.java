package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import javafx.scene.paint.Color;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 10/10/2017.
 */
public class FontChangeTest {
    private String fontPath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/freeset.ttf";
    private FieldWithHyphenation fieldWithHyp;
    private SingleLineField field;
    private List<FxField> fields;

    @Before
    public void setUp() throws Exception {
        fieldWithHyp = new FieldWithHyphenation("Example words", 0, 200);
        field = new SingleLineField("Example", 1, 200);
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
        // Act
        fields.forEach(f -> f.setFont(fontPath));

        // Assert
        assertThat("SingleLine", field.getPrefWidth(), is(field.computeStringWidth("Example")));
        assertThat("With hyphenation", fieldWithHyp.getPrefWidth(),
                is(fieldWithHyp.computeStringWidth("Example words")));
    }

    @Test
    public void ifNewFontGreaterThanMaxAllowableWidthItHyphenates() throws Exception {
        // Act
        fieldWithHyp.setFontSize(30);

        // Assert
        assertThat(fieldWithHyp.getNumberOfLines(), is(2));
    }

    @Test
    public void prefSizeIncreasesOnFontSizeIncrease() throws Exception {
        // Act
        fields.forEach(f -> f.setFontSize(20));

        // Assert
        assertThat("SingleLine", field.getPrefWidth(), is(field.computeStringWidth("Example")));
        assertThat("With hyphenation", fieldWithHyp.getPrefWidth(),
                is(fieldWithHyp.computeStringWidth("Example words")));
    }

    @Test
    public void prefSizeDecreasesOnFontSizeDecrease() throws Exception {
        // Act
        fields.forEach(f -> f.setFontSize(10));

        // Assert
        assertThat("SingleLine", field.getPrefWidth(), is(field.computeStringWidth("Example")));
        assertThat("With hyphenation", fieldWithHyp.getPrefWidth(),
                is(fieldWithHyp.computeStringWidth("Example words")));
    }

    @Test
    public void fontSizeCouldNotBeSetGreaterThanMax() throws Exception {
        // Act
        fields.forEach(f -> {
            f.setFontSize(60);
        });

        // Assert
        assertThat("SingleLine", field.getFontSize(), is(51.0));
        assertThat("With hyphenation", fieldWithHyp.getFontSize(), is(51.0));
    }



    @Test
    public void colorPreservesOnFontChange() throws Exception {
        // Arrange
        fields.forEach(f -> f.setFill(Color.RED));

        // Act
        fields.forEach(f -> f.setFont(fontPath));

        // Assert
        assertThat("SingleLine", field.getFill(), is(Color.RED));
        assertThat("Multi line", fieldWithHyp.getFill(), is(Color.RED));
    }

    @Test
    public void colorPreservesOnFontSizeChange() throws Exception {
        // Arrange
        fields.forEach(f -> f.setFill(Color.RED));

        // Act
        fields.forEach(f -> f.setFontSize(20));

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

        // Act
        fields.forEach(f -> f.setFont(fontPath));

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
        fields.forEach(f -> f.setFontSize(20));

        // Assert
        assertThat("Single line", field.getLayoutX() + field.getPrefWidth(),
                is(fieldRightX));
        assertThat("Multi line", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                is(fieldWithHypRightX));
    }
}
