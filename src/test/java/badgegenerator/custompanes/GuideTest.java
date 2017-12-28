package badgegenerator.custompanes;

import javafx.scene.shape.Line;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 11/09/2017.
 */
public class GuideTest extends TestBase {
    @Test
    public void leftGuideIsWorking() throws Exception {
        // Arrange
        Guide leftGuide = FxField.getGuides().stream()
                .filter(guide -> guide.getGuideId() == field.getColumnId().hashCode()
                        && guide.getPosition().equals(Position.LEFT))
                .findFirst()
                .get();

        // Act
        drag(fieldWithHyp).moveTo(targetFieldPos.getMinX() + fieldWithHyp.getPrefWidth() / 2 - 3,
                draggedFieldPos.getMinY());

        // Assert
        assertThat("Wrong position", fieldWithHyp.getLayoutX(), is(field.getLayoutX()));
        assertThat("Guide is not showing", leftGuide.isVisible(), is(true));
    }

    @Test
    public void horizontalGuideIsWorking() throws Exception {
        // Arrange
        Line horizontalGuide = app.getHorizontalGuide();
        double centerY = bounds(horizontalGuide).query().getMinY() + 0.5;

        // Act
        drag(fieldWithHyp).moveTo(targetFieldPos.getMinX(), centerY - 3);

        // Assert
        assertThat("Wrong position", (int) bounds(fieldWithHyp).query().getMinY(),
                is((int) (centerY - fieldWithHyp.getMaxHeight() / 2)));
        assertThat("Guide is not showing", horizontalGuide.isVisible(), is(true));
    }

    @Test
    public void centerGuideIsWorking() throws Exception {
        // Arrange
        Line centralGuide = app.getVerticalGuide();
        double centerX = bounds(centralGuide).query().getMinX() + 0.5;

        // Act
        drag(fieldWithHyp).moveTo(centerX - 3, draggedFieldPos.getMinY());

        // Assert
        assertThat("Wrong position", (int) bounds(fieldWithHyp).query().getMinX(),
                is((int) (centerX - fieldWithHyp.getPrefWidth() / 2)));
        assertThat("Guide is not showing", centralGuide.isVisible(), is(true));
    }

    @Test
    public void rightGuideIsWorking() throws Exception {
        // Arrange
        field.setLayoutX(fieldWithHyp.computeStringWidth(fieldWithHyp.getText() + 10));
        targetFieldPos = bounds(field).query();
        Guide rightGuide = FxField.getGuides().stream()
                .filter(guide -> guide.getGuideId() == field.getColumnId().hashCode()
                        && guide.getPosition().equals(Position.RIGHT))
                .findFirst()
                .get();

        // Act
        drag(fieldWithHyp).moveTo(targetFieldPos.getMaxX() + fieldWithHyp.getPrefWidth() / 2
                        - fieldWithHyp.getPrefWidth() - 3,
                draggedFieldPos.getMinY());

        // Assert
        assertThat("Wrong position", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth()
                , is(field.getLayoutX() + field.getPrefWidth()));
//        assertThat("Guide is not showing", rightGuide.isVisible(), is(true));
    }
}
