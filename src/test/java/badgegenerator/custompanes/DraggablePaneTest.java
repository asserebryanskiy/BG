package badgegenerator.custompanes;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DraggablePaneTest extends TestBase {
    @Test
    public void fieldWithHypCouldNotBeDraggedOutOfTopBorder() throws Exception {
        // Act
        drag(fieldWithHyp).moveBy(0, -200);

        // Assert
        assertThat(fieldWithHyp.getLayoutY(), is(0.0));
    }

    @Test
    public void fieldWithHypCouldNotBeDraggedOutOfLeftBorder() throws Exception {
        // Act
        drag(fieldWithHyp).moveBy(-200, 0);

        // Assert
        assertThat(fieldWithHyp.getLayoutX(), is(0.0));
    }

    @Test
    public void fieldWithHypCouldNotBeDraggedOutOfRightBorder() throws Exception {
        // Act
        drag(fieldWithHyp).moveBy(200, 0);

        // Assert
        assertThat(fieldWithHyp.getLayoutX(),
                is(300 - fieldWithHyp.getPrefWidth()));
    }

    @Test
    public void fieldWithHypCouldNotBeDraggedOutOfBottomBorder() throws Exception {
        // Act
        drag(fieldWithHyp).moveBy(0, 300);

        // Assert
        assertThat(fieldWithHyp.getLayoutY(), is(300 - fieldWithHyp.getMaxHeight()));
    }

    @Test
    public void fieldCouldNotBeDraggedOutOfTopBorder() throws Exception {
        // Act
        drag(field).moveBy(0, -200);

        // Assert
        assertThat(field.getLayoutY(), is(0.0));
    }

    @Test
    public void fieldCouldNotBeDraggedOutOfLeftBorder() throws Exception {
        // Act
        drag(field).moveBy(-200, 0);

        // Assert
        assertThat(field.getLayoutX(), is(0.0));
    }

    @Test
    public void fieldCouldNotBeDraggedOutOfRightBorder() throws Exception {
        // Act
        drag(field).moveBy(200, 0);

        // Assert
        assertThat(field.getLayoutX(),
                is(300 - field.getPrefWidth()));
    }

    /*@Test
    public void fieldCouldNotBeDraggedOutOfBottomBorder() throws Exception {
        // Act
        drag(field).moveBy(0, 300);

        // Assert
        assertThat(field.getLayoutY(), is(300 - field.getMaxHeight()));
    }*/
}
