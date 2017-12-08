package badgegenerator.pdfeditor;

import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfFieldExtractor;
import badgegenerator.fileloader.WrongHeadingsException;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AbstractFieldsLayouterTest extends ApplicationTest{

    private Pane fieldsParent;

    @Before
    public void setUp() throws Exception {
        fieldsParent = new Pane();
        fieldsParent.setMaxSize(500, 340);
    }

    @Test
    public void movesFieldsAlignedRightProperly() throws Exception {
        ExcelReader excelReader = prepareExcelReader();
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftRight.pdf");

        AbstractFieldsLayouter layouter = new NewFieldsLayouter(fieldsParent,
                new Pane(),
                new Pane(),
                new ArrayList<>(),
                excelReader.getLargestFields(),
                excelReader.getLongestWords(),
                excelReader.getHeadings(),
                1,
                extractor.getFields());
        layouter.positionFields();

        layouter.getFxFields().forEach(f ->
                assertThat(f.getLayoutX() > 0, is(true)));
    }

    @Test
    public void movesFieldsAlignedCenterProperly() throws Exception {
        ExcelReader excelReader = prepareExcelReader();
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftCenter.pdf");

        AbstractFieldsLayouter layouter = new NewFieldsLayouter(fieldsParent,
                new Pane(),
                new Pane(),
                new ArrayList<>(),
                excelReader.getLargestFields(),
                excelReader.getLongestWords(),
                excelReader.getHeadings(),
                1,
                extractor.getFields());
        layouter.positionFields();

        layouter.getFxFields().forEach(f -> {
                    assertThat("left "  + f.getColumnId(), f.getLayoutX() > 0, is(true));
                    assertThat("right " + f.getColumnId(),
                            f.getLayoutX() + f.getPrefWidth() < fieldsParent.getMaxWidth(),
                            is(true));
                });

    }

    private ExcelReader prepareExcelReader() throws URISyntaxException, IOException {
        Path excelPath = Paths.get(getClass()
                .getResource("/excels/test.xlsx").toURI());
        ExcelReader excelReader = new ExcelReader(excelPath.toFile().getAbsolutePath());
        excelReader.processFile();
        return excelReader;
    }

    @Test
    public void fxFieldsAreCreated() throws Exception {
        ExcelReader excelReader = prepareExcelReader();
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/extractionTest.pdf");

        NewFieldsLayouter layouter = new NewFieldsLayouter(fieldsParent,
                new Pane(),
                new Pane(),
                new ArrayList<>(),
                excelReader.getLargestFields(),
                excelReader.getLongestWords(),
                excelReader.getHeadings(),
                1,
                extractor.getFields());
        layouter.positionFields();

        assertThat(layouter.getFxFields(), notNullValue());
    }

    private PdfFieldExtractor prepareExtractor(ExcelReader excelReader, String pdfName)
            throws URISyntaxException, IOException, WrongHeadingsException {
        String pdfPath = Paths.get(getClass()
                .getResource(pdfName).toURI())
                .toFile()
                .getAbsolutePath();
        PdfDocument pdf = new PdfDocument(new PdfReader(pdfPath));
        Rectangle pageSize = pdf.getFirstPage().getPageSize();
        fieldsParent.setMaxSize(pageSize.getWidth(), pageSize.getHeight());
        return new PdfFieldExtractor(pdfPath, excelReader);
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}