package badgegenerator.fileloader;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WrongExcelsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void excelWithMoreThan10ColumnsNotAllowed() throws Exception {
        thrown.expect(IOException.class);
        thrown.expectMessage("Больше 10 столбцов в таблице");

        ExcelReader reader = new ExcelReader(getClass().getResource("/wrongExcel2.xls").getFile(),
                false);
        reader.processFile();
    }

    @Test
    public void excelWithMoreThan2000RowsNotAllowed() throws Exception {
        thrown.expect(IOException.class);
        thrown.expectMessage("Список участников должен начинаться с первого ряда");

        ExcelReader reader = new ExcelReader(getClass().getResource("/wrongExcel1.xls").getFile(),
                false);
        reader.processFile();
    }

    @Test
    public void emptyExcelIsNotAllowed() throws Exception {
        thrown.expect(IOException.class);
        thrown.expectMessage("Загруженный файл пустой");

        ExcelReader reader = new ExcelReader(getClass().getResource("/emptyExcel.xls").getFile(),
                false);
        reader.processFile();
    }

    @Test
    public void activeCellLocatesProperly() throws Exception {
        // Arrange
        Workbook book = new XSSFWorkbook(getClass()
                .getResourceAsStream("/wrongExcel3.xlsx"));
        Sheet sheet = book.getSheetAt(0);

        // Assert
        assertThat(sheet.getFirstRowNum(), is(2));
    }
}
