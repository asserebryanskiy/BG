package badgegenerator.fxfieldssaver;

import badgegenerator.Main;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeleteTest extends FxFieldsSaverTestBase {
    @BeforeClass
    public static void beforeAllTests() throws Exception {
        InputStream lightStream = Main.class.getResourceAsStream("/fonts/CRC35.otf");
        Font.loadFont(lightStream, 13);
        lightStream.close();
    }

    @Test
    public void onDeleteClickFilesAreDeleted() throws Exception {
        clickOn("deleteTest");
        clickOn("#deleteBtn");
        assertThat(deleteTest.exists(), is(false));
    }

    @Test
    public void onDeleteFieldBecomesReadyToSave() throws Exception {
        // Arrange
        Node node = find("test1");
        clickOn(node);
        clickOn("#deleteBtn");
        Button saveBtn = find("#saveBtn");

        // Act
        clickOn(node);
        type(KeyCode.I);
        clickOn(saveBtn);

        // Assert
        assertThat(window(saveBtn).isShowing(), is(false));

        // tearDown
        Button okBtn = (Button) ((ButtonBar) window(0).getScene().getRoot()
                .getChildrenUnmodifiable().stream()
                .filter(n -> n instanceof ButtonBar)
                .findFirst()
                .get()).getButtons().get(0);

        clickOn(okBtn);
    }
}
