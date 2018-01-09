package badgegenerator.appfilesmanager;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AssessableFontsTest {
    private static Set<String> fontNames;
    @BeforeClass
    public static void setUp() throws Exception {
        fontNames = AssessableFonts.getFontsNames();
    }

    @Test
    public void arialIsAvailable() throws Exception {
        assertThat(fontNames.contains("Arial"), is(true));
    }

    @Test
    public void timesNewRomanIsAvailable() throws Exception {
        assertThat(fontNames.contains("Times New Roman"), is(true));
    }

    /*@Test
    public void helveticaIsAvailable() throws Exception {
        assertThat(fontNames.contains("Helvetica"), is(true));
    }*/

    @Test
    public void circeFontIsAvailable() throws Exception {
        assertThat(fontNames.contains("Circe Light"), is(true));
    }

    @Test
    public void containsPlumbRegularWithoutIllegalCharacter() throws Exception {
        assertThat(fontNames.contains("Plumb Regular"), is(true));
    }

    @Test
    public void splitsFontNameOnTwoWordsWithSpace() throws Exception {
        String[] wrongFontNames = new String[]{"Plumb-Regular",
                "Plumb_Regular", "Plumb+Regular", "Plumb-regular", "Plumb regular",
                "Plumb_regular", "Plumb+regular", "plumb regular"};
        for (String wrongFontName : wrongFontNames) {
            assertThat(wrongFontName, AssessableFonts.getProperFontName(wrongFontName),
                    is("Plumb Regular"));
        }
    }
}