package badgegenerator.pdfcreator;

import badgegenerator.Util;
import badgegenerator.fileloader.ExcelReader;
import javafx.concurrent.Task;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by andreyserebryanskiy on 30/01/2018.
 */
public class CreateBadgeArchiveTaskTest {

    @Test
    public void whenCalledWithNotEnoughSpaceExceptionIsThrown() throws Exception {
        String pdfPath = Util.getPath("/pdfs/raifFull.pdf");
        String dirPath = System.getProperty("user.home") + "/Desktop";
        long freeSpace = new File(dirPath).getFreeSpace();
        long fileSize = new File(pdfPath).length();
        ExcelReader excelReader = Mockito.mock(ExcelReader.class);
        int numberOfFiles = (int) (freeSpace / (fileSize * 2) + 1);
        when(excelReader.getValues()).thenReturn(new String[numberOfFiles][0]);

        try {
            Task task = new CreateBadgeArchiveTask(null, 0,
                    excelReader, pdfPath, dirPath + "/archive.zip",
                    true);
        } catch (NotEnoughSpaceException e) {
            assertThat((double) e.getAvailableSpace(), closeTo(freeSpace, 200000));
            assertThat(e.getQuerySpace(), is(fileSize * 2 * numberOfFiles));
        }



    }
}