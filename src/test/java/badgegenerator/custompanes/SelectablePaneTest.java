package badgegenerator.custompanes;

import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SelectablePaneTest extends TestBase {
    @Test
    public void multipleFieldsCouldBeSelected() throws Exception {
        press(KeyCode.CONTROL);
        clickOn(field);
        clickOn(fieldWithHyp);

        // Assert
        assertThat("First is not selected", field.isSelected, is(true));
        assertThat("Second is not selected", fieldWithHyp.isSelected, is(true));
    }
}
