package badgegenerator.custompanes;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class ResizeableBorderTest extends TestBase{
    private static final String[] alignments = new String[]{"LEFT", "CENTER", "RIGHT"};
    private static final int OFFSET = 2;    // possible difference in double values
    private FieldWithHyphenation fieldWithHyp;
    private ResizeableBorder rightBorder;
    private ResizeableBorder leftBorder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        fieldWithHyp = (FieldWithHyphenation) fields.get(0);
        leftBorder = fieldWithHyp.getResizeableBorders().get(0);
        rightBorder = fieldWithHyp.getResizeableBorders().get(1);
        drag(leftBorder).dropBy(-100, 0);
        drag(rightBorder).dropBy(100,0);
    }

    @Test
    public void ifCenterAlignedAndMovedSlightlyNothingChangesRight() throws Exception {
        // Arrange
        drag(rightBorder).moveBy(-2, 0).release(MouseButton.PRIMARY);
        fieldWithHyp.setAlignment("CENTER");
        double initialWidth = fieldWithHyp.getPrefWidth();
        double delta = fieldWithHyp.computeStringWidth("word") / 2;
        double initialX = fieldWithHyp.getLayoutX();

        // Act
        drag(rightBorder).moveBy(delta, 0);
        double gotWidth = initialWidth + 2 * delta;
        assertThat("Didn't change width while dragging",
                fieldWithHyp.getPrefWidth() > gotWidth - OFFSET
                    && fieldWithHyp.getPrefWidth() < gotWidth + OFFSET,
                is(true));

        release(MouseButton.PRIMARY);
        assertThat("Didn't save width after mouse release", fieldWithHyp.getPrefWidth(),
                is(initialWidth));
        assertThat("Didn't save layoutX of field after release", fieldWithHyp.getLayoutX(),
                is(initialX));
    }

    @Test
    public void ifCenterAlignedAndMovedSlightlyNothingChangesLeft() throws Exception {
        // Arrange
        drag(rightBorder).moveBy(-2, 0).release(MouseButton.PRIMARY);
        fieldWithHyp.setAlignment("CENTER");
        double initialWidth = fieldWithHyp.getPrefWidth();
        double delta = fieldWithHyp.computeStringWidth("word") / 2;
        double initialX = fieldWithHyp.getLayoutX();

        // Act
        drag(leftBorder).moveBy(-delta, 0);
        double expected = initialWidth + 2 * delta;
        assertThat("Didn't change width while dragging",
                fieldWithHyp.getPrefWidth() > expected - OFFSET
                    && fieldWithHyp.getPrefWidth() < expected + OFFSET,
                is(true));

        release(MouseButton.PRIMARY);
        assertThat("Didn't save width after mouse release", fieldWithHyp.getPrefWidth(),
                is(initialWidth));
        assertThat("Didn't save layoutX of field after release", fieldWithHyp.getLayoutX(),
                is(initialX));
    }

    @Test
    public void rightBorderIsWorking() throws Exception {
        // Act
        drag(rightBorder).moveBy(-10, 0);

        // Assert
        assertThat(fieldWithHyp.getNumberOfLines(), is(2));
    }

    @Test
    public void leftBorderIsWorkingOnShrinking() throws Exception {
        // Act
        drag(leftBorder).moveBy(50, 0);

        // Assert
        assertThat(fieldWithHyp.getNumberOfLines(), is(4));
    }

    @Test
    public void leftBorderIsWorkingOnIncreasing() throws Exception {
        double delta = fieldWithHyp.getPrefWidth();
        for (String alignment : alignments) {
            // Arrange
            fieldWithHyp.setAlignment(alignment);
            drag(leftBorder).moveBy(delta, 0)
                    .release(MouseButton.PRIMARY);
            fieldWithHyp.setLayoutX(delta + 1);

            // Act
            drag(leftBorder).moveBy(-delta, 0);

            // Assert
            assertThat(alignment + " wrong number of lines",
                    fieldWithHyp.getNumberOfLines(), is(1));
        }
    }

    @Test
    public void rightBorderIsWorkingOnIncreasing() throws Exception {
        double delta = fieldWithHyp.getPrefWidth();
        for (String alignment : alignments) {
            // Arrange
            fieldWithHyp.setAlignment(alignment);
            drag(rightBorder).moveBy(-delta, 0)
                    .release(MouseButton.PRIMARY);

            // Act
            drag(rightBorder).moveBy(delta, 0);

            // Assert
            assertThat(alignment + " wrong number of lines",
                    fieldWithHyp.getNumberOfLines(), is(1));
        }
    }

    @Test
    public void onLeftAlignedFieldResizeToTheRightSavesX() throws Exception {
        double delta = fieldWithHyp.getPrefWidth();
        double initialX = fieldWithHyp.getLayoutX();
        // Arrange
        fieldWithHyp.setAlignment("LEFT");
        drag(rightBorder).moveBy(-delta, 0).release(MouseButton.PRIMARY);
        assertThat("Decrease", fieldWithHyp.getLayoutX(), is(initialX));

        // Act
        drag(rightBorder).moveBy(delta, 0);

        // Assert
        assertThat("Increase", fieldWithHyp.getLayoutX(), is(initialX));
    }

    @Test
    public void onRightAlignedFieldResizeToTheLeftSavesX() throws Exception {
        double newX = fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth()
                    - fieldWithHyp.computeStringWidth("Example");
        for (int i = 0; i < 20; i++) {
            // Arrange
            double delta = fieldWithHyp.getPrefWidth();
            double initialEndX = fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth();
            fieldWithHyp.setAlignment("RIGHT");
            System.out.println(newX);
//            System.out.println(fieldWithHyp.getPrefWidth());
            drag(leftBorder).moveBy(delta, 0).release(MouseButton.PRIMARY);
            sleep(2000);
            System.out.println(fieldWithHyp.getLayoutX());
            System.out.println("-------");
            assertThat("Decrease", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                    greaterThan(initialEndX - OFFSET));
            assertThat("Decrease", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                    lessThan(initialEndX + OFFSET));

            // Act
            drag(leftBorder).moveBy(-delta, 0);

            // Assert
            assertThat("Increase", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                    greaterThan(initialEndX - OFFSET));
            assertThat("Increase", fieldWithHyp.getLayoutX() + fieldWithHyp.getPrefWidth(),
                    lessThan(initialEndX + OFFSET));
        }
    }
}