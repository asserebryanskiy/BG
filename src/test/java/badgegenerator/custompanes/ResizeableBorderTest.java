package badgegenerator.custompanes;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResizeableBorderTest extends TestBase{
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
    public void rightBorderIsWorking() throws Exception {
        // Act
        drag(rightBorder).moveBy(-10, 0);

        // Assert
        assertThat(fieldWithHyp.getNumberOfLines(), is(2));
    }

    @Test
    public void leftBorderIsWorking() throws Exception {
        // Act
        drag(leftBorder).moveBy(50, 0);

        // Assert
        assertThat(fieldWithHyp.getNumberOfLines(), is(2));
    }
}