package badgegenerator.custompanes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 12/12/2017.
 */
public class FxFieldTest {
    @Test
    public void getValuesOutOfRangeReturnsProperValue() throws Exception {
        FxField field = new SingleLineField("Example", "Example", 100);
        String[] query = new String[]{"Длинное-длинное-слово", "короткое", "слово"};

        // Act
        List<String> outOfRange = field.getValuesOutOfRange(13, Arrays.asList(query));

        // Assert
        assertThat(outOfRange.get(0), is("Длинное-длинное-слово"));
    }

    @Test
    public void getValuesOutOfRangeReturnsProperValueIfSeveralOutput() throws Exception {
        FxField field = new SingleLineField("Example", "Example", 100);
        List<String> query = Arrays.asList("Почти Короткое", "короткое", "слово");
        query = new ArrayList<>(query);
        for (int i = 0; i < 20; i++) {
            query.add("Длинное-длинное-слово " + i);
        }

        // Act
        List<String> outOfRange = field.getValuesOutOfRange(13, query);

        // Assert
        for (int i = 19; i >= 0; i--) {
            assertThat(outOfRange.get(i), is("Длинное-длинное-слово " + i));
        }
    }

    @Test
    public void getValuesOutOfRangeReturnProperOutputOnMultiWordsField() throws Exception {
        FxField field = new SingleLineField("Word", "Word", 100);
        List<String> query = Arrays.asList("Wordword word", "a b wordf", "a b c d f");
        query = new ArrayList<>(query);

        // Act
        List<String> outOfRange = field
                .getValuesOutOfRange(13, query, field.computeStringWidth("word"));

        // Assert
        assertThat(outOfRange.toString(), is("[Wordword word, a b wordf]"));
    }
}