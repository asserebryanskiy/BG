package badgegenerator.appfilesmanager;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 20/10/2017.
 */
public class HelpMessagesTest {
    @Test
    public void helpMessageIsLoaded() throws Exception {
        // Arrange
        HelpMessages.load();

        // Act
        String message = HelpMessages.getMessage("ctrlHelpIcon");

        // Assert
        assertThat(message, is("Зажав клавишу Ctrl можно выделить сразу несколько полей."
                + System.lineSeparator()));
    }
}