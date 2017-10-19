package badgegenerator.pdfeditor;

import javafx.scene.layout.Pane;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AbstractFieldsLayouterTest {
    @Test
    public void fxFieldsAreCreated() throws Exception {
        NewFieldsLayouter layouter = new NewFieldsLayouter(new Pane(),
                new Pane(),
                new Pane(),
                new String[]{"Example"},
                new String[]{"Example"},
                1);
        layouter.positionFields();

        assertThat(layouter.getFxFields(), notNullValue());
    }
}