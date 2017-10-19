package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CapitalizationTest {
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
    public void whenCapitalizedAllLettersAreCapital() throws Exception {
        // Act
        fields.forEach(f -> f.setCapitalized(true));

        // Assert
        assertThat("Single line", field.getText(), is("EXAMPLE"));
        assertThat("Multi line", fieldWithHyp.getText(), is("EXAMPLE WORDS"));
    }

    @Test
    public void whenDecapitalizedAllLettersReturnOriginalState() throws Exception {
        // Arrange
        fields.forEach(f -> f.setCapitalized(true));

        // Act
        fields.forEach(f -> f.setCapitalized(false));

        // Assert
        assertThat("Single line", field.getText(), is("Example"));
        assertThat("Multi line", fieldWithHyp.getText(), is("Example words"));
    }

    @Test
    public void whenCapitalizedWidthIncreases() throws Exception {
        // Act
        field.setCapitalized(true);

        // Assert
        assertThat("Single line", field.getPrefWidth(), is(field.computeStringWidth("EXAMPLE")));
    }

    @Test
    public void whenDecapitalizedWidthDecreases() throws Exception {
        // Arrange
        field.setCapitalized(true);

        // Act
        field.setCapitalized(false);

        // Assert
        assertThat("Single line", field.getPrefWidth(), is(field.computeStringWidth("Example")));
    }

    @Test
    public void whenCapitalizedHyphenationIsImplemented() throws Exception {
        // Act
        fieldWithHyp.setCapitalized(true);

        // Assert
        assertThat(fieldWithHyp.getNumberOfLines(), is(2));
    }

    @Test
    public void whenCapitalizedFieldSavesAlignment() throws Exception {
        // Arrange
        fields.forEach(f -> f.setAlignment("RIGHT"));
        double fieldRightX = field.getPrefWidth();
        double fieldWithHypRightX = fieldWithHyp.getPrefWidth();

        // Act
        fields.forEach(f -> f.setCapitalized(true));

        // Assert
        assertThat("Single line", field.getLayoutX() + field.getPrefWidth(),
                is(fieldRightX));
        assertThat("Multi line", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                is(fieldWithHypRightX));
    }
}