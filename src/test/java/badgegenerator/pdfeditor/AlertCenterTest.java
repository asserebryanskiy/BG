package badgegenerator.pdfeditor;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by andreyserebryanskiy on 11/12/2017.
 */
public class AlertCenterTest extends ApplicationTest {

    @Test
    public void returnsProperNotifications() throws Exception {
        AlertCenter alertCenter = new AlertCenter(new Pane());
        for (int i = 0; i < 20; i++) {
            alertCenter.showNotification("Some text " + i);
        }

        for (int i = 19; i >= 0; i--) {
            assertThat(alertCenter.getNotifications().get(i), is("Some text " + i));
        }
    }


    // to launch JavaFX Environment
    @Override
    public void start(Stage stage) throws Exception {

    }
}