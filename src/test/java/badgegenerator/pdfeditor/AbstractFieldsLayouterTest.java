package badgegenerator.pdfeditor;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AbstractFieldsLayouterTest extends ApplicationTest{
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

    @Override
    public void start(Stage stage) throws Exception {

    }
}