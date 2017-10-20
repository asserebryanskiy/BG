package badgegenerator.fxfieldsloader;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import badgegenerator.fxfieldssaver.FxFieldSave;
import badgegenerator.fxfieldssaver.FxFieldsSaver;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 02/10/2017.
 */
public class FxFieldsLoaderTest extends ApplicationTest{
    private List<FxField> fields;

    @Before
    public void setUp() throws Exception {
        FxField field = new SingleLineField("Example", 0, 200);
        FxField fieldWithHyp = new FieldWithHyphenation("Example words", 1, 200);
        fields = new ArrayList<>(2);
        fields.add(field);
        fields.add(fieldWithHyp);
    }

    @After
    public void tearDown() throws Exception {
        delete(Paths.get(SavesManager.getSavesFolder().getAbsolutePath()
                + File.separator
                + "test"));
    }

    private static void delete(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    public void fontSizeSavesAfterSavesLoad() throws Exception {
        // Arrange
        double fontSize = 20;
        fields.forEach(f -> f.setFontSize(fontSize));

        // Act
        FxFieldsSaver.createSave(fields, "test");
        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        FxFieldSave loadedSave = saves.get(0);
        FxFieldSave loadedSaveWithHyp = saves.get(1);

        // Assert
        assertThat(loadedSave.getFontSize(), is(fontSize));
        assertThat(loadedSaveWithHyp.getFontSize(), is(fontSize));
    }

    @Test
    public void capitalizationSavesAfterSavesLoad() throws Exception {
        // Arrange
        fields.forEach(f -> f.setCapitalized(true));

        // Act
        FxFieldsSaver.createSave(fields, "test");
        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        FxFieldSave loadedSave = saves.get(0);
        FxFieldSave loadedSaveWithHyp = saves.get(1);

        // Assert
        assertThat(loadedSave.isCapitalized(), is(true));
        assertThat(loadedSaveWithHyp.isCapitalized(), is(true));
    }

    @Test
    public void colorPreservesAfterSaveLoad() throws Exception {
        // Arrange
        Color color = Color.RED;
        fields.forEach(f -> f.setFill(color));

        // Act
        FxFieldsSaver.createSave(fields, "test");
        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        FxFieldSave loadedSave = saves.get(0);
        FxFieldSave loadedSaveWithHyp = saves.get(1);
        Color color1 = new Color(loadedSave.getRed(),
                loadedSave.getGreen(),
                loadedSave.getBlue(),
                1);
        Color color2 = new Color(loadedSaveWithHyp.getRed(),
                loadedSaveWithHyp.getGreen(),
                loadedSaveWithHyp.getBlue(),
                1);

        // Assert
        assertThat(color1, is(color));
        assertThat(color2, is(color));
    }

    @Test
    public void xPreservesAfterSaveLoad() throws Exception {
        // Arrange
        double initialX = 30;
        fields.forEach(f -> f.setLayoutX(initialX));

        // Act
        FxFieldsSaver.createSave(fields, "test");
        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        FxFieldSave loadedSave = saves.get(0);
        FxFieldSave loadedSaveWithHyp = saves.get(1);

        // Assert
        assertThat(loadedSave.getX(), is(initialX));
        assertThat(loadedSaveWithHyp.getX(), is(initialX));
    }

    @Test
    public void yPreservesAfterSaveLoad() throws Exception {
        // Arrange
        double initialY = 30;
        fields.forEach(f -> f.setLayoutY(initialY));

        // Act
        FxFieldsSaver.createSave(fields, "test");
        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        FxFieldSave loadedSave = saves.get(0);
        FxFieldSave loadedSaveWithHyp = saves.get(1);

        // Assert
        assertThat(loadedSave.getY(), is(initialY));
        assertThat(loadedSaveWithHyp.getY(), is(initialY));
    }

    @Test
    public void fontPathPreservesAfterSaveLoad() throws Exception {
        // Arrange
        String path = getClass().getResource(  "/freeset.ttf").getFile();
        fields.forEach(f -> f.setFont(path));

        // Act
        FxFieldsSaver.createSave(fields, "test");
        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        FxFieldSave loadedSave = saves.get(0);
        FxFieldSave loadedSaveWithHyp = saves.get(1);
        String expectedPath = SavesManager.getSavesFolder().getAbsolutePath()
                + "/test/fonts/FreeSet.ttf";

        // Assert
        assertThat(loadedSave.getFontPath(), is(expectedPath));
        assertThat(loadedSaveWithHyp.getFontPath(), is(expectedPath));
    }

    @Test
    public void alignmentPreservesAfterSaveLoad() throws Exception {
        // Arrange
        String initialAlignment = "CENTER";
        fields.forEach(f -> f.setAlignment(initialAlignment));

        // Act
        FxFieldsSaver.createSave(fields, "test");
        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        FxFieldSave loadedSave = saves.get(0);
        FxFieldSave loadedSaveWithHyp = saves.get(1);

        // Assert
        assertThat(loadedSave.getAlignment(), is(initialAlignment));
        assertThat(loadedSaveWithHyp.getAlignment(), is(initialAlignment));
    }

    @Override
    public void start(Stage stage) throws Exception {
    }
}