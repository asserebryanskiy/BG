package badgegenerator.fileloader;

import badgegenerator.appfilesmanager.AssessableFonts;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * Created by andreyserebryanskiy on 06/12/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AssessableFonts.class)
public class PdfToFxAdapterTest {
    @BeforeClass
    public static void beforeAllTests() throws Exception {
        PowerMockito.mockStatic(AssessableFonts.class);
        when(AssessableFonts.getFontPath(any())).thenReturn(null);
    }

    @Test
    public void ifLoadedFontNameEndsWithMT_suffixIsRemoved() throws URISyntaxException, IOException {
        String fieldName = "№";
        String pdfPath = Paths.get(getClass()
                .getResource("/pdfs/raifFull.pdf").toURI())
                .toFile()
                .getAbsolutePath();
        PdfFieldExtractor extractor = new PdfFieldExtractor(pdfPath,
                new HashSet<>(Collections.singletonList(fieldName)));

        PdfField pdfField = extractor.getFields().get(fieldName);

        assertThat(new PdfToFxAdapter(pdfField, 1).getFontName(), is("Arial Bold"));
    }
}