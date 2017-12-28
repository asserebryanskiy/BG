package badgegenerator.fileloader;


import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExcelReaderTest {

    @Test
    public void readsAlsoNumericCells() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass()
                .getResource("/excels/wrongCellFormat.xlsx").getFile());
        // Act
        reader.processFile();

        // Assert
        assertThat(reader.getColumn("Позиция").toString(), is("[1, 2, 3, 4]"));
    }

    @Test
    public void readsAlsoCellsWithFormulas() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass()
                .getResource("/excels/wrongCellFormat.xlsx").getFile());
        // Act
        reader.processFile();

        // Assert
        assertThat(reader.getColumn("Команда").toString(), is("[10, 20, 30, 40]"));
    }

    @Test
    public void deletesRedundantEmptyRows() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass()
                .getResource("/excels/redundantRows.xlsx").getFile());
        reader.processFile();

        // Act
        String[][] result = reader.getValues();

        // Assert
        assertThat(result.length, is(5));
    }

    @Test
    public void getColumnReturnsProperValues() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass().getResource("/excels/test.xlsx").getFile());
        reader.processFile();

        // Act
        List<String> result = reader.getColumn("Фамилия");

        // Assert
        assertThat(result.toString(), is("[Георгиевский, Сергеев, Белобородский, Беловодов]"));
    }

    @Test
    public void readerProperlyCountsColumns() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass().getResource("/excels/test.xlsx").getFile());
        reader.processFile();

        assertThat(reader.getLargestFields().length, is(3));
    }

    @Test
    public void readerProperlyRetrievesValues() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass().getResource("/excels/test.xlsx").getFile());
        reader.processFile();

        assertThat("Beloborodskiy in cell 2-1", reader.getValues()[2][1],
                is("Белобородский"));
        assertThat("Empty cell at 1-2", reader.getValues()[1][2], is(""));
        assertThat("Headings", reader.getHeadings()[2], is("Должность"));
    }

    @Test
    public void longestFieldsAreRetrieved() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass().getResource("/excels/test.xlsx").getFile());
        reader.processFile();

        assertThat(reader.getLargestFields(),
                is(new String[]{"Андрей", "Белобородский", "Заместитель главы правления по вопросам транспорта"}));
    }

    @Test
    public void longestWordsAreRetrieved() throws Exception {
        // Arrange
        ExcelReader reader = new ExcelReader(getClass().getResource("/excels/test.xlsx").getFile());
        reader.processFile();

        assertThat(reader.getLongestWords(),
                is(new String[]{"Андрей", "Белобородский", "Предприниматель"}));
    }
}