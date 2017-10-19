package badgegenerator.fxfieldssaver;

import badgegenerator.NodeNotFoundException;
import badgegenerator.appfilesmanager.SavesManager;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public class FxFieldsSaverTestBase extends ApplicationTest {
    private static boolean headlessMode = false;
    static File savesDir = SavesManager.getSavesFolder();
    static File deleteTest;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FxFieldsSaverMock app = new FxFieldsSaverMock();
        app.start(primaryStage);
    }

    @BeforeClass
    public static void setUpHeadlessMode() throws Exception {
        if(headlessMode) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
        if (savesDir.listFiles() != null) {
            Arrays.stream(savesDir.listFiles())
                    .map(File::getAbsolutePath)
                    .forEach(FxFieldsSaverTestBase::delete);
        }
        File save1 = new File(savesDir, "test1");
        File save2 = new File(savesDir, "test2");

        deleteTest = new File(savesDir, "deleteTest");
        save1.mkdir();
        save2.mkdir();
        deleteTest.mkdir();
        File file = new File(deleteTest, "test.fxf");
        FileOutputStream os = new FileOutputStream(file);
        os.write(1);
        os.close();
    }

    @AfterClass
    public static void afterAllTests() throws Exception {
        Arrays.stream(savesDir.listFiles())
                .map(File::getAbsolutePath)
                .forEach(FxFieldsSaverTestBase::delete);
    }

    @After
    public void afterEachTest() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    public <T extends Node> T find(final String query) throws Exception {
        return (T) lookup(query).tryQuery().orElseThrow(NodeNotFoundException::new);
    }

    static void delete(String dirPath) {
        Path directory = Paths.get(dirPath);
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
