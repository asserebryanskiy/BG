package badgegenerator.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UtilTest {
    @Test
    public void properlyReplacesCamelTwoCasedWords() {
        assertThat(Util.retrieveWordsFromCamelCase("CamelCase"), is("Camel Case"));
    }

    @Test
    public void properlyReplacesCamelThreeCasedWords() {
        assertThat(Util.retrieveWordsFromCamelCase("CamelCaseThree"),
                is("Camel Case Three"));
    }

    @Test
    public void retrieveFromCamelCase_IfPassedOneWordReturnsIt() {
        assertThat(Util.retrieveWordsFromCamelCase("Camelcase"), is("Camelcase"));
    }

    @Test
    public void retrieveFromCamelCase_IfPassedEmptyStringReturnsIt() {
        assertThat(Util.retrieveWordsFromCamelCase(""), is(""));
    }
}