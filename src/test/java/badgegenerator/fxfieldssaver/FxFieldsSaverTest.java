package badgegenerator.fxfieldssaver;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.SingleLineField;
import com.sun.javafx.PlatformUtil;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
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
    private static File directory;
    @Before
    public void setUp() throws IOException {
        FxField field1 = new SingleLineField("Example", 0, 100);
        String fontPath = getClass().getResource("/freeset.ttf").getPath();
        if(PlatformUtil.isWindows()) fontPath = fontPath.substring(1);
        FxField field2 = new FieldWithHyphenation("Example words", 1, 120);
        field2.setFont(fontPath);
        List<FxField> fields = new ArrayList<>();
        fields.add(field1);
        fields.add(field2);

        FxFieldsSaver.createSave(fields, "test");
        directory = new File(SavesManager.getSavesFolder().getAbsolutePath()
                + File.separator
                + "test");
    }

    @Test
    public void saveExists() throws Exception {
        assertThat(directory.exists(), is(true));
    }

    @Test
    public void saveContainsFxfFiles() throws Exception {
        assertThat(Arrays.stream(directory.list())
                .filter(name -> name.endsWith(".fxf"))
                .count(), is(2L));
    }

    @Test
    public void saveContainsFont() throws Exception {
        // Arrange
        File fontFile = new File(String.format("%s%s%s%s%s",
                directory.getAbsolutePath(),
                File.separator,
                "fonts",
                File.separator,
                "freeset.ttf"));

        // Assert
        assertThat(fontFile.exists(), is(true));
    }

    @AfterClass
    public static void afterAllTests() throws Exception {
        delete(directory.toPath());
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
