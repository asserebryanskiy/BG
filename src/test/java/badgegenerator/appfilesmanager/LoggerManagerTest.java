package badgegenerator.appfilesmanager;

import badgegenerator.Main;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LoggerManagerTest {
    private static Logger logger = Logger.getLogger(LoggerManagerTest.class.getSimpleName());
    private String dirPath;

//    @PdfTest
//    public void loggerCreatesFiles() throws Exception {
//        // Arrange
//        LoggerManager.initializeLogger(logger);
//        dirPath = Main.getAppFilesDirPath() + "/logs/LoggerManagerTest";
//
//        // Act
//        logger.log(new LogRecord(Level.INFO, "Example message"));
//
//        // Assert
//        assertThat(new File(dirPath).list().length, is(2));
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        File[] files = new File(dirPath).listFiles();
//        for (File file : files) {
//            file.delete();
//        }
//    }
}