package badgegenerator.fxfieldssaver;

import badgegenerator.Main;
import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.IllegalFontSizeException;
import badgegenerator.custompanes.SingleLineField;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 02/10/2017.
 */
public class FxFieldsSaverTest extends ApplicationTest{
    private static File saveDir;

    @BeforeClass
    public static void beforeAllTests() throws Exception {
        InputStream lightStream = Main.class.getResourceAsStream("/fonts/CRC35.otf");
        Font.loadFont(lightStream, 13);
        lightStream.close();
    }

    @Before
    public void setUp() throws IOException, URISyntaxException, IllegalFontSizeException {
        FxField field1 = new SingleLineField("Example", "Example", 100);
        String fontPath = Paths.get(getClass()
                .getResource("/fonts/freeset.ttf").toURI())
                .toFile()
                .getAbsolutePath();
        FxField field2 = new FieldWithHyphenation("Example words", "Example2", 120);
        field2.setFont(fontPath);
        List<FxField> fields = new ArrayList<>();
        fields.add(field1);
        fields.add(field2);

        FxFieldsSaver.createSave(fields, "test");
        saveDir = new File(SavesManager.getSavesFolder().getAbsolutePath()
                + File.separator
                + "test");
    }

    @Test
    public void saveExists() throws Exception {
        assertThat(saveDir.exists(), is(true));
    }

    @Test
    public void saveContainsFxfFiles() throws Exception {
        assertThat(Arrays.stream(saveDir.list())
                .filter(name -> name.endsWith(".fxf"))
                .count(), is(2L));
    }

    @Test
    public void saveContainsFont() throws Exception {
        // Arrange
        File fontFile = new File(String.format("%s%s%s%s%s",
                saveDir.getAbsolutePath(),
                File.separator,
                "fonts",
                File.separator,
                "FreeSet.ttf"));

        // Assert
        assertThat(fontFile.exists(), is(true));
    }

    @AfterClass
    public static void afterAllTests() throws Exception {
        delete(saveDir.toPath());
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

    @Override
    public void start(Stage stage) throws Exception {

    }
}
