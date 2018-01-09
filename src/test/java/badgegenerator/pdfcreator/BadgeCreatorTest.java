package badgegenerator.pdfcreator;

import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfField;
import badgegenerator.fileloader.PdfFieldExtractor;
import badgegenerator.pdfeditor.AlertCenter;
import badgegenerator.pdfeditor.FieldsLayouter;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by andreyserebryanskiy on 14/12/2017.
 */
public class BadgeCreatorTest extends ApplicationTest {
    ExcelReader excelReader;
    BadgeCreator bc;
    List<FxField> fxFields;

    @Test
    public void savesXOnLeftAligned() throws Exception {
        String pdfName = "/pdfs/threeFonts.pdf";
        String excelName = "/excels/test.xlsx";
        String emptyPdfPath = "/pdfs/empty.pdf";
        prepare(pdfName, excelName, emptyPdfPath);

        int numberOfBadges = excelReader.getValues().length;
        for (int i = 0; i < numberOfBadges; i++) {
            Collection<PdfField> gotPdfFields = prepareForTest(i);
            gotPdfFields.stream()
                    .mapToDouble(PdfField::getX)
                    .forEach(x -> assertThat(x, is(fxFields.get(0).getLayoutX())));
        }
    }

    @Test
    public void savesXOnRightAligned() throws Exception {
        String pdfName = "/pdfs/hShiftRight.pdf";
        String excelName = "/excels/test.xlsx";
        String emptyPdfPath = "/pdfs/empty.pdf";
        prepare(pdfName, excelName, emptyPdfPath);

        int numberOfBadges = excelReader.getValues().length;
        int endX = (int) (fxFields.get(0).getLayoutX() + fxFields.get(0).getPrefWidth());
        for (int i = 0; i < numberOfBadges; i++) {
            Collection<PdfField> gotPdfFields = prepareForTest(i);
            gotPdfFields.stream()
                    .mapToDouble(f -> f.getX() + f.getWidth())
                    .forEach(x -> assertThat((int) x, is(endX)));
        }
    }

    @Test
    public void savesXOnCenterAligned() throws Exception {
        String[][] TEST_FILES = new String[][]{
                {"/pdfs/newYear.pdf", "/excels/newYear.xlsx", "/pdfs/newYearEmpty.pdf"},
                {"/pdfs/sts.pdf", "/excels/sts.xlsx", "/pdfs/stsEmpty.pdf"},
                {"/pdfs/hShiftCenter.pdf", "/excels/test.xlsx", "/pdfs/empty.pdf"}
        };
        for (String[] current : TEST_FILES) {
            prepare(current[0], current[1], current[2]);

            int numberOfBadges = excelReader.getValues().length;
            int centerX = (int) (fxFields.get(0).getLayoutX() + fxFields.get(0).getPrefWidth() / 2);
            for (int i = 0; i < numberOfBadges; i++) {
                Collection<PdfField> gotPdfFields = prepareForTest(i);
                gotPdfFields.stream()
                        .mapToDouble(f -> f.getX() + f.getWidth() / 2)
                        .forEach(x -> assertThat(current[0], (int) x, is(centerX)));
            }
        }
    }

    @Test
    public void savesColor() throws Exception {
        String[][] TEST_FILES = new String[][]{
                {"/pdfs/newYear.pdf", "/excels/newYear.xlsx", "/pdfs/newYearEmpty.pdf"},
                {"/pdfs/sts.pdf", "/excels/sts.xlsx", "/pdfs/stsEmpty.pdf"}
        };
        for (String[] current : TEST_FILES) {
            prepare(current[0], current[1], current[2]);

            int numberOfBadges = excelReader.getValues().length;
            float[] colorValue = fxFields.get(0).getPdfColor().getColorValue();
            for (int i = 0; i < numberOfBadges; i++) {
                Collection<PdfField> gotPdfFields = prepareForTest(i);
                gotPdfFields.stream()
                        .map(PdfField::getColor)
                        .forEach(color -> assertThat(color.getColorValue(), is(colorValue)));
            }
        }
    }

    @NotNull
    private Collection<PdfField> prepareForTest(int i) throws IOException {
        PdfDocument newPdf = new PdfDocument(new PdfReader(
                new ByteArrayInputStream(bc.createBadgeInMemory(i))));
        Set<String> fieldValues = new HashSet<>(Arrays.asList(excelReader.getValues()[i]));
        fieldValues.removeIf(String::isEmpty);
        PdfFieldExtractor newExtractor = new PdfFieldExtractor(newPdf, fieldValues);
        return newExtractor.getFields().values();
    }

    private void prepare(String pdfName, String excelName, String emptyPdfPath) throws IOException, URISyntaxException {
        InputStream lightStream = getClass().getResourceAsStream("/fonts/CRC35.OTF");
        Font.loadFont(lightStream, 13);
        lightStream.close();
        String pdfPath = getPath(pdfName);
        String excelPath = getPath(excelName);
        excelReader = new ExcelReader(excelPath);
        excelReader.processFile();
        HashSet<String> headings = new HashSet<>(Arrays.asList(excelReader.getHeadings()));
        PdfFieldExtractor extractor = new PdfFieldExtractor(pdfPath, headings);
        PdfDocument pdf = new PdfDocument(new PdfReader(pdfPath));
        Rectangle pageSize = pdf.getFirstPage().getPageSize();
        Pane fieldsParent = new Pane();
        fieldsParent.setMaxSize(pageSize.getWidth(), pageSize.getHeight());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent, new AlertCenter(new Pane()),
                excelReader, null, extractor.getFields(), 1);
        bc = new BadgeCreator(layouter.getFxFields(), emptyPdfPath,
                excelReader.getValues(), excelReader.getHeadings(), 1, true);
        fxFields = layouter.getFxFields();
    }

    @Test
    public void ifCapitalizedInOriginalWillBeCapitalizedInFinal() throws Exception {

    }

    private String getPath(String name) throws URISyntaxException {
        return Paths.get(getClass()
                .getResource(name).toURI())
                .toFile()
                .getAbsolutePath();
    }

    // used only to launch JavaFX environment and thus is empty
    @Override
    public void start(Stage stage) throws Exception {

    }
}