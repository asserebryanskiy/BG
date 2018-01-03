package badgegenerator.fileloader;

import com.sun.javafx.PlatformUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 06/12/2017.
 */
public class PdfFieldExtractorTest {
    @Test
    public void ifFieldIsCapitalizedInPdfItIsRetrievedCapitalized() throws Exception {
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/capitalized.pdf",
                "/excels/capitalized.xlsx");

        Map<String, PdfField> result = extractor.getFields();

        assertThat(result.get("Имя").isCapitalized(), is(false));
        assertThat(result.get("ФАМИЛИЯ").isCapitalized(), is(true));
        assertThat(result.get("ДОЛЖНОСТЬ В КОМПАНИИ").isCapitalized(), is(true));
    }

    @Test
    public void extractsProperlyBadlyCenterAlignedFields() throws Exception {
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/newYear.pdf",
                "/excels/newYear.xlsx");

        assertThat(extractor.getFields().get("ИМЯ").getAlignment(), is("CENTER"));
    }

    @Test
    public void properlyExtractsCenterAlignedFields() throws Exception {
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/hShiftCenter.pdf",
                "/excels/test.xlsx");

        assertThat(extractor.getFields().get("Имя").getAlignment(), is("CENTER"));
    }

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
    public void extractsBrokenWordsComplete() throws Exception {
        // Arrange
        Set<String> words = new HashSet<>();
        words.add("Имя");
        words.add("Фамилия");
        words.add("Должность");
        words.add("г. Томск");
        words.add("III Конференция поставщиков химической продукции СИБУР");
        words.add("28-29 сентября 2017 г.");
        String pdfPath = Paths.get(getClass()
                .getResource("/pdfs/threeFonts.pdf").toURI())
                .toFile()
                .getAbsolutePath();
        words = words.stream().map(str -> {
            StringBuilder builder = new StringBuilder();
            String[] wordsArray = str.split("\\s");
            if (wordsArray.length == 1) return str.trim();
            for (String word : wordsArray) {
                builder.append(word);
            }
            return builder.toString().trim();
        }).collect(Collectors.toSet());

        // Act
        PdfFieldExtractor extractor = new PdfFieldExtractor(pdfPath, words);

        // Assert
        Set<String> finalWords = words;
        extractor.getFields().keySet().forEach(word -> {
            assertThat(word, finalWords.contains(word), is(true));
        });

    }

    @Test
    public void extractsBrokenWordsCompleteSimple() throws Exception {
        // Act
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/newYear.pdf",
                "/excels/wrongEncoding.xlsx");

        // Assert
        Map<String, PdfField> fields = extractor.getFields();
        assertThat("ИМЯ", fields.get("ИМЯ"), notNullValue());
        assertThat("ФАМИЛИЯ", fields.get("ФАМИЛИЯ"), notNullValue());
        assertThat("Компания", fields.get("Компания"), notNullValue());
    }

    @Test
    public void extractsEvenWithBrokenEncoding() throws Exception {
        // Act
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/multiWordsHeading.pdf",
                "/excels/multiWordsHeading.xlsx");
        Map<String, PdfField> fields = extractor.getFields();

        // Assert
        assertThat("Имя", fields.keySet().contains("Имя"), is(true));
        assertThat("Фамилия", fields.keySet().contains("Фамилия"), is(true));
        assertThat("Должность в компании", fields.keySet().contains("Должность в компании"),
                is(true));
    }

    @Test
    public void absentFontIsExtractedProperly() throws Exception {
        // Act
        PdfFieldExtractor extractor = prepareExtractor("/pdfs/threeFonts.pdf",
                "/excels/test.xlsx");
        Map<String, PdfField> fields = extractor.getFields();
        Map<String, PdfToFxAdapter> adapters = new HashMap<>(fields.size());
        fields.keySet().forEach(name ->
                adapters.put(name, new PdfToFxAdapter(fields.get(name), 1)));

        // Assert
        assertThat(adapters.get("Имя").getFontName(), is("Circe Light"));
        assertThat(adapters.get("Фамилия").getFontName(), is("FreeSet"));
        assertThat(adapters.get("Должность").getFontName(), is("Helvetica Regular"));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PdfFieldExtractor prepareExtractor(String pdfName, String excelName) throws Exception {
        Path excelPath = Paths.get(getClass()
                .getResource(excelName).toURI());
        ExcelReader excelReader = new ExcelReader(excelPath.toFile().getAbsolutePath());
        excelReader.processFile();
        String pdfPath = Paths.get(getClass()
                .getResource(pdfName).toURI())
                .toFile()
                .getAbsolutePath();
        return new PdfFieldExtractor(pdfPath, new HashSet<>(Arrays.asList(excelReader.getHeadings())));
    }
}