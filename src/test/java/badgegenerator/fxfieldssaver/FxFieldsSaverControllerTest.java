package badgegenerator.fxfieldssaver;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.junit.After;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FxFieldsSaverControllerTest extends FxFieldsSaverTestBase{
    @After
    public void tearDown() throws Exception {
        if(listTargetWindows().size() > 1) {
            Button okBtn = (Button) ((ButtonBar) window(1).getScene().getRoot().getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof ButtonBar)
                    .findFirst()
                    .get()).getButtons().get(0);

            clickOn(okBtn);
        }
    }

    @Test
    public void saveWithEmptyNameIsNotAllowed() throws Exception {
        // Act
        clickOn("#3");
        clickOn("#saveBtn");

        // Assert
        assertThat(window(0).isShowing(), is(true));
//        List<Window> windows = listTargetWindows();
//        windows.forEach(System.out::println);
    }

    @Test
    public void warningIsShownIfNameStartsWithDote() throws Exception {
        // Act
        clickOn("#3");
        type(KeyCode.PERIOD, KeyCode.I);
        clickOn("#saveBtn");

        // Assert
        getDialogText();
        assertThat(getDialogText(), is("Имя сохранения не может начинаться с точки."));
    }

    private String getDialogText() {
        return window(1).getScene().getRoot().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof Label)
                .map(label -> ((Label) label).getText())
                .findFirst()
                .orElse("Error occurred");
    }

    @Test
    public void warningIsShownIfNameContainsIllegalSymbols() throws Exception {
        // Act
        clickOn("#3");
        type(KeyCode.I, KeyCode.SLASH);
        clickOn("#saveBtn");

        // Assert
        getDialogText();
        assertThat(getDialogText(), is("Имя сохранения не может содержать\"/\" и \"\\\"."));
    }

    @Test
    public void warningIsShownIfNameIsOccupied() throws Exception {
        // Act
        clickOn("#3");
        type(KeyCode.T, KeyCode.E, KeyCode.S, KeyCode.T, KeyCode.DIGIT2);
        clickOn("#saveBtn");

        // Assert
        getDialogText();
        assertThat(getDialogText(), is("Сохранение с таким именем уже существует."));
    }

    @Test
    public void allSavesAreShown() throws Exception {
        // Arrange
        VBox box = find("#savesBox");
        String[] expectedNames = new String[]{"deleteTest", "test1", "test2", "", ""};

        // Act
        Set<String> gainedNames = box.getChildren().stream()
                .map(node -> ((TextField) node).getText())
                .collect(Collectors.toSet());

        // Assert
        for (String name : expectedNames) {
            assertThat(name, gainedNames.contains(name), is(true));
        }
    }
}