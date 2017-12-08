package badgegenerator.appfilesmanager;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 08/12/2017.
 */
public class SavesManagerTest {
    @Test
    public void deletesExcessSavesIfNeeded() throws Exception {
        // Arrange
        File savesFolder = SavesManager.getSavesFolder();
        Arrays.stream(savesFolder.listFiles()).forEach(File::delete);
        for (int i = 0; i < 7; i++) {
            File file = new File(savesFolder, String.valueOf(i));
            file.mkdir();
        }

        // Act
        SavesManager.getSavesNames();

        // Assert
        assertThat(savesFolder.listFiles().length, is(5));

        // After
        Arrays.stream(savesFolder.listFiles()).forEach(File::delete);

    }
}