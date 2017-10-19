package badgegenerator.custompanes;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DistanceViewerTest {
    @Test
    public void yChangeChangesText() throws Exception {
        FxField field = new SingleLineField("Example",
                0, 1, 100,
                13, null);
        DistanceViewer viewer = new DistanceViewer(field, Orientation.VERTICAL);

        // Act
        field.setLayoutY(100);

        // Assert
        assertThat(viewer.getText(), is("100"));
    }
}