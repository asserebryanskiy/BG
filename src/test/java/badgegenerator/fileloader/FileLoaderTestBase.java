package badgegenerator.fileloader;

import badgegenerator.NodeNotFoundException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Base class to run before and after each test case.
 * Launches new application before and clears up stage after.
 */
public class FileLoaderTestBase extends ApplicationTest {
    private static boolean headlessMode = false;

    @BeforeClass
    public static void setUpHeadlessMode() throws Exception {
        if(headlessMode) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FileLoader.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    @After
    public void afterEachTest() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @Test
    public void couldNotProceedWithoutPdf() throws Exception {
        Text label = find("#pdfNotLoadedLabel");

        clickOn("#btnCreateNewFields");

        assertThat(label.isVisible(), is(true));
    }

    @Test
    public void couldNotProceedWithoutXlsx() throws Exception {
        Text label = find("#excelNotLoadedLabel");

        clickOn("#btnCreateNewFields");

        assertThat(label.isVisible(), is(true));
    }

    public <T extends Node> T find(final String query) throws Exception {
        return (T) lookup(query).tryQuery().orElseThrow(NodeNotFoundException::new);
//                .stream()
//                .findFirst()
//                .orElseThrow(NodeNotFoundException::new);
    }
}
