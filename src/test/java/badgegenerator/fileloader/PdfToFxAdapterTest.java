package badgegenerator.fileloader;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by andreyserebryanskiy on 06/12/2017.
 */
public class PdfToFxAdapterTest {
    @Test
    public void fontAdaptationTest() throws Exception {
        Path excelPath = Paths.get(getClass()
                .getResource("/excels/test.xlsx").toURI());
        ExcelReader excelReader = new ExcelReader(excelPath.toFile().getAbsolutePath()
        );
        excelReader.processFile();
        String pdfPath = Paths.get(getClass()
                .getResource("/pdfs/threeFonts.pdf").toURI())
                .toFile()
                .getAbsolutePath();
        PdfFieldExtractor extractor = new PdfFieldExtractor(pdfPath,
                new HashSet<>(Arrays.asList(excelReader.getHeadings())));

        extractor.getFields().values().forEach(f -> new PdfToFxAdapter(f, 1));
    }
}