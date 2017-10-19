package badgegenerator.appfilesmanager;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AssessableFontsTest {
    private static List<String> fontNames;
    @BeforeClass
    public static void setUp() throws Exception {
        fontNames = AssessableFonts.getAssessableFxFonts();
    }

    @Test
    public void arialIsAvailable() throws Exception {
        assertThat(fontNames.contains("Arial"), is(true));
    }

    @Test
    public void timesNewRomanIsAvailable() throws Exception {
        assertThat(fontNames.contains("Times New Roman"), is(true));
    }

    @Test
    public void helveticaIsAvailable() throws Exception {
        assertThat(fontNames.contains("Helvetica"), is(true));
    }
}