package badgegenerator.fileloader;

import com.sun.javafx.PlatformUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 06/12/2017.
 */
public class PdfFieldExtractorTest {
    @Test
    public void extractsProperFields() throws Exception {
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/extractionTest.pdf",
                "/excels/test.xlsx");

        Map<String, PdfField> result = extractor.getFields();

        if (PlatformUtil.isMac()) {
            assertThat(result.get("Имя").toString(),
                    is("Имя: x - 50.000000, y - 300.000000, color - 0-0-0, font - null, fontSize - 15.000000"));
            assertThat(result.get("Фамилия").toString(),
                    is("Фамилия: x - 50.000000, y - 270.000000, color - 0-0-0, font - null, fontSize - 15.000000"));
            assertThat(result.get("Должность").toString(),
                    is("Должность: x - 50.000000, y - 240.000000, color - 0-0-0, font - null, fontSize - 15.000000"));
        } else {
            assertThat(result.get("Имя").toString(),
                    is("Имя: x - 50,000000, y - 300,000000, color - 0-0-0, font - null, fontSize - 15,000000"));
            assertThat(result.get("Фамилия").toString(),
                    is("Фамилия: x - 50,000000, y - 270,000000, color - 0-0-0, font - null, fontSize - 15,000000"));
            assertThat(result.get("Должность").toString(),
                    is("Должность: x - 50,000000, y - 240,000000, color - 0-0-0, font - null, fontSize - 15,000000"));
        }
    }

    @Test
    public void extractsEvenWithBrokenEncoding() throws Exception {
        // Act
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/threeFonts.pdf",
                "/excels/test.xlsx");
        Map<String, PdfField> fields = extractor.getFields();

        // Assert
        assert fields.keySet().contains("Имя");
        assert fields.keySet().contains("Фамилия");
        assert fields.keySet().contains("Должность");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void excelWithNoHeadingIsNotAllowed() throws Exception {
        thrown.expect(WrongHeadingsException.class);
        thrown.expectMessage("Не удалось найти в pdf заголовки: Предприниматель, Георгиевский, Андрей");

        PdfFieldExtractor extractor = prepareExtractor("/pdfs/extractionTest.pdf",
                "/excels/noHeadings.xlsx");
    }

    @Test
    public void excelWithWrongHeadingIsNotAllowed() throws Exception {
        thrown.expect(WrongHeadingsException.class);
        thrown.expectMessage("Не удалось найти в pdf заголовок Позиция");

        PdfFieldExtractor extractor = prepareExtractor("/pdfs/extractionTest.pdf",
                "/excels/wrongHeading.xlsx");
    }

    private PdfFieldExtractor prepareExtractor(String pdfName, String excelName) throws Exception {
        Path excelPath = Paths.get(getClass()
                .getResource(excelName).toURI());
        ExcelReader excelReader = new ExcelReader(excelPath.toFile().getAbsolutePath());
        excelReader.processFile();
        String pdfPath = Paths.get(getClass()
                .getResource(pdfName).toURI())
                .toFile()
                .getAbsolutePath();
        return new PdfFieldExtractor(pdfPath, excelReader);
    }
}