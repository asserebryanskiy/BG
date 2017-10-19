package badgegenerator.custompanes;

import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DistanceViewerTest extends ApplicationTest{

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

    @Override
    public void start(Stage stage) throws Exception {

    }
}