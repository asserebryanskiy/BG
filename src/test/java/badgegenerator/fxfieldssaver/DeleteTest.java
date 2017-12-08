package badgegenerator.fxfieldssaver;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeleteTest extends FxFieldsSaverTestBase {
    @Test
    public void onDeleteClickFilesAreDeleted() throws Exception {
        clickOn("deleteTest");
        clickOn("#deleteBtn");
        assertThat(deleteTest.exists(), is(false));
    }

    @Test
    public void onDeleteFieldBecomesReadyToSave() throws Exception {
        // Arrange
        clickOn("#0");
        clickOn("#deleteBtn");
        Button saveBtn = find("#saveBtn");

        // Act
        clickOn("#0");
        type(KeyCode.I);
        clickOn(saveBtn);

        // Assert
        assertThat(window(saveBtn).isShowing(), is(false));

        // tearDown
        Button okBtn = (Button) ((ButtonBar) window(0).getScene().getRoot()
                .getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof ButtonBar)
                .findFirst()
                .get()).getButtons().get(0);

        clickOn(okBtn);
    }
}
