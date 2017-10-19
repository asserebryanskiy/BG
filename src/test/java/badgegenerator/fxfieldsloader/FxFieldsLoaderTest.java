package badgegenerator.fxfieldsloader;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import badgegenerator.fxfieldssaver.FxFieldSave;
import badgegenerator.fxfieldssaver.FxFieldsSaver;
import javafx.scene.paint.Color;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class FxFieldsLoaderTest {
    private static final double initialX = 20;
    private static final Color initialColor = Color.RED;
    private static final String fontPath = FxFieldsLoaderTest.class
            .getResource(File.separator + "freeset.ttf").getFile();
    private static final double initialFontSize = 20;
    private static final double initialY = 20;
    private static final String initialAlignment = "CENTER";

    private static FxFieldSave loadedSave;
    private static FxFieldSave loadedSaveWithHyp;

    @BeforeClass
    public static void beforeAllTests() throws Exception {
        FxField field = new SingleLineField("Example", 0, 200);
        FxField fieldWithHyp = new FieldWithHyphenation("Example words", 1, 200);
        List<FxField> fields = new ArrayList<>(2);
        fields.add(field);
        fields.add(fieldWithHyp);
        fields.forEach(f -> {
            f.setLayoutX(initialX);
            f.setLayoutY(initialY);
            f.setAlignment("LEFT");
            f.setFontSize(initialFontSize);
            f.setFont(fontPath);
            f.setFill(initialColor);
            f.setAlignment(initialAlignment);
        });
        FxFieldsSaver.createSave(fields, "test");

        List<FxFieldSave> saves = FxFieldsLoader.load(SavesManager.getSavesFolder()
                + File.separator
                + "test");
        loadedSave = saves.get(0);
        loadedSaveWithHyp = saves.get(1);
    }

    @AfterClass
    public static void afterAllTests() throws Exception {
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
        assertThat(loadedSave.getFontSize(), is(initialFontSize));
        assertThat(loadedSaveWithHyp.getFontSize(), is(initialFontSize));
    }

    @Test
    public void colorPreservesAfterSaveLoad() throws Exception {
        // Arrange
        Color color1 = new Color(loadedSave.getRed(),
                loadedSave.getGreen(),
                loadedSave.getBlue(),
                1);
        Color color2 = new Color(loadedSaveWithHyp.getRed(),
                loadedSaveWithHyp.getGreen(),
                loadedSaveWithHyp.getBlue(),
                1);

        // Assert
        assertThat(color1, is(initialColor));
        assertThat(color2, is(initialColor));
    }

    @Test
    public void xPreservesAfterSaveLoad() throws Exception {
        assertThat(loadedSave.getX(), is(initialX));
        assertThat(loadedSaveWithHyp.getX(), is(initialX));
    }

    @Test
    public void yPreservesAfterSaveLoad() throws Exception {
        assertThat(loadedSave.getY(), is(initialY));
        assertThat(loadedSaveWithHyp.getY(), is(initialY));
    }

    @Test
    public void fontPathPreservesAfterSaveLoad() throws Exception {
        // Arrange
        String expectedPath = SavesManager.getSavesFolder().getAbsolutePath()
                + File.separator
                + "test"
                + File.separator
                + "fonts"
                + File.separator
                + "FreeSet.ttf";

        // Assert
        assertThat(loadedSave.getFontPath(), is(expectedPath));
        assertThat(loadedSaveWithHyp.getFontPath(), is(expectedPath));
    }

    @Test
    public void alignmentPreservesAfterSaveLoad() throws Exception {
        assertThat(loadedSave.getAlignment(), is(initialAlignment));
        assertThat(loadedSaveWithHyp.getAlignment(), is(initialAlignment));
    }
}