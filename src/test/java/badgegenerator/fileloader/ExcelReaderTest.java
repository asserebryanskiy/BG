package badgegenerator.fileloader;


import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExcelReaderTest {
    private ExcelReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new ExcelReader(getClass().getResource("/excels/test.xlsx").getFile()
        );
        reader.processFile();
    }

    @Test
    public void readerProperlyCountsColumns() throws Exception {
        assertThat(reader.getLargestFields().length, is(3));
    }

    @Test
    public void readerProperlyRetrievesValues() throws Exception {
        assertThat("Beloborodskiy in cell 3-1", reader.getValues()[3][1],
                is("Белобородский"));
        assertThat("Empty cell at 2-2", reader.getValues()[2][2], is(""));
        assertThat("Headings", reader.getHeadings()[2], is("Должность"));
    }

    @Test
    public void longestFieldsAreRetrieved() throws Exception {
        assertThat(reader.getLargestFields(),
                is(new String[]{"Андрей", "Белобородский", "Заместитель главы правления по вопросам транспорта"}));
    }

    @Test
    public void longestWordsAreRetrieved() throws Exception {
        assertThat(reader.getLongestWords(),
                is(new String[]{"Андрей", "Белобородский", "Предприниматель"}));
    }
}