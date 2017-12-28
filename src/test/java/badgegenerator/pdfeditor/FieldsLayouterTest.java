package badgegenerator.pdfeditor;

import badgegenerator.Main;
import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfFieldExtractor;
import badgegenerator.fileloader.WrongHeadingsException;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldsLayouterTest extends ApplicationTest{

    private Pane fieldsParent;
    private static double width;
    private static double height;

    @BeforeClass
    public static void beforeAll() throws Exception {
        InputStream lightStream = FieldsLayouterTest.class.getResourceAsStream("/fonts/CRC35.otf");
        Font.loadFont(lightStream, 13);
        lightStream.close();
        String pdfPath = Paths.get(FieldsLayouterTest.class
                .getResource("/pdfs/threeFonts.pdf").toURI())
                .toFile()
                .getAbsolutePath();
        PdfDocument pdf = new PdfDocument(new PdfReader(pdfPath));
        width = pdf.getFirstPage().getPageSize().getWidth();
        height = pdf.getFirstPage().getPageSize().getHeight();
    }

    @Before
    public void setUp() throws Exception {
        fieldsParent = new Pane();
        fieldsParent.setMaxSize(width, height);
    }

    @Test
    public void movesFieldsAlignedRightProperly() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/test.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftRight.pdf");

        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                new AlertCenter(new Pane()),
                excelReader,
                null,
                extractor.getFields(), 1);

        layouter.getFxFields().forEach(f ->
                assertThat(f.getLayoutX() > 0, is(true)));
    }

    @Test
    public void ifCenterAlignedFieldNotFoundItIsAddedProperly() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/multiWordsHeading.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftCenter.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);
        FxField target = layouter.getFxFields().stream()
                .filter(f -> f.getColumnId().equals("Должность в компании"))
                .findAny().get();

        assertThat((int) target.getLayoutX(),
                is((int) (fieldsParent.getMaxWidth() / 2 - target.getPrefWidth() / 2)));
        assertThat(target.getLayoutX(), greaterThan(0.0));
        assertThat("No notification", alertCenter.getNotifications().contains("Не удалось найти в pdf поле \"Должность в компании\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light."),
                is(true));
    }

    @Test
    public void ifNoFieldsAreFoundInPdfAllValuesArePutInTheCenter() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/multiWordsHeading.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/empty.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);

        layouter.getFxFields().forEach(f -> {
            assertThat(f.getLayoutX(), is(width / 2 - f.getPrefWidth() / 2));
        });
        assertThat(alertCenter.getNotifications().toString(), is("[Не удалось найти в pdf поле \"Имя\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light., Не удалось найти в pdf поле \"Фамилия\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light., Не удалось найти в pdf поле \"Должность в компании\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light.]"));
    }

    @Test
    public void ifRightAlignedFieldNotFoundItIsAddedProperly() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/multiWordsHeading.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftRight.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);
        FxField first = layouter.getFxFields().get(0);
        double rightX = first.getLayoutX() + first.getPrefWidth();
        FxField target = layouter.getFxFields().stream()
                .filter(f -> f.getColumnId().equals("Должность в компании"))
                .findAny().get();

        assertThat(target.getLayoutX() + target.getPrefWidth(), is(rightX));
        assertThat(alertCenter.getNotifications().contains("Не удалось найти в pdf поле \"Должность в компании\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light."),
                is(true));
    }

    @Test
    public void movesFieldsAlignedCenterProperly() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/test.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftCenter.pdf");

        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                new AlertCenter(new Pane()),
                excelReader,
                null,
                extractor.getFields(), 1);

        layouter.getFxFields().forEach(f -> {
                    assertThat("left "  + f.getColumnId(), f.getLayoutX() > 0, is(true));
                    assertThat("right " + f.getColumnId(),
                            f.getLayoutX() + f.getPrefWidth() < fieldsParent.getMaxWidth(),
                            is(true));
                });
    }

    @Test
    public void fxFieldsAreCreated() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/test.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/extractionTest.pdf");

        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                new AlertCenter(new Pane()),
                excelReader,
                null,
                extractor.getFields(), 1);

        assertThat(layouter.getFxFields(), notNullValue());
    }

    @Test
    public void ifFontSizeWasDecreasedWhileCreationLAlertIsAdded() throws Exception {
        ExcelReader excelReader = mock(ExcelReader.class);
        when(excelReader.getLargestFields()).thenReturn(new String[]{"Андрей",
                "Ужасно-длинная-фамилия-которая-точно-не-поместиться", "Предприниматель"});
        when(excelReader.getLongestWords()).thenReturn(new String[]{"Андрей",
                "Ужасно-длинная-фамилия-которая-точно-не-поместиться", "Предприниматель"});
        when(excelReader.getColumn("Фамилия")).thenReturn(Arrays.asList("Андрей",
                "Ужасно-длинная-фамилия-которая-точно-не-поместиться", "Предприниматель"));
        when(excelReader.getHeadings()).thenReturn(new String[]{"Имя", "Фамилия", "Должность"});
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/extractionTest.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);

        assertThat(alertCenter.getNotifications().contains("Размер шрифта 15.0, установленный раннее, слишком большой для значения \"Ужасно-длинная-фамилия-которая-точно-не-поместиться\".\nДлина текста будет больше, чем ширина pdf.\nУстановлен максимально возможный размер шрифта: 11.0"),
                is(true));
    }

    @Test
    public void ifSomePdfFieldIsMissingReplacesItWithDefaultOne() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/test.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/multiWordsHeading.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);

        assertThat("Wrong number of fields", layouter.getFxFields().size(), is(3));
        assertThat("No message in the lert center",
                alertCenter.getNotifications().contains("Не удалось найти в pdf поле \"Должность\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light."),
                is(true));
        final double expectedX = layouter.getFxFields().get(0).getLayoutX();
        layouter.getFxFields().forEach(f -> {
            assertThat(f.getColumnId() + " alignment", f.getAlignment(), is("LEFT"));
            assertThat(f.getColumnId() + " x", f.getLayoutX(), is(expectedX));
        });
    }

    @Test
    public void ifRightAlignedFieldIsMissingReplacesIt() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/multiWordsHeading.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftRight.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);

        assertThat("Wrong number of fields", layouter.getFxFields().size(), is(3));
        assertThat("No message in the alert center",
                alertCenter.getNotifications().contains("Не удалось найти в pdf поле \"Должность в компании\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light."),
                is(true));
        FxField first = layouter.getFxFields().get(0);
        final double expectedEndX = first.getLayoutX() + first.getPrefWidth();
        layouter.getFxFields().forEach(f -> {
            assertThat(f.getColumnId() + " alignment", f.getAlignment(), is("RIGHT"));
            assertThat(f.getColumnId() + " end x", f.getLayoutX() + f.getPrefWidth(),
                    is(expectedEndX));
        });
    }

    @Test
    public void ifCenterAlignedFieldIsMissingReplacesIt() throws Exception {
        ExcelReader excelReader = prepareExcelReader("/excels/multiWordsHeading.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/hShiftCenter.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);

        assertThat("Wrong number of fields", layouter.getFxFields().size(), is(3));
        assertThat("No message in the alert center",
                alertCenter.getNotifications().contains("Не удалось найти в pdf поле \"Должность в компании\". Для него установлены стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light."),
                is(true));
        FxField first = layouter.getFxFields().get(0);
        final double expectedCenterX = first.getLayoutX() + first.getPrefWidth() / 2;
        layouter.getFxFields().forEach(f -> {
            assertThat(f.getColumnId() + " alignment", f.getAlignment(), is("CENTER"));
            assertThat(f.getColumnId() + " end x", f.getLayoutX() + f.getPrefWidth() / 2,
                    is(expectedCenterX));
        });
    }

    @Test
    public void absentFontsAreReplacedWithCirce() throws Exception {
        // mock Helvetica absence, check if it is replaced with Circe Light
        // Arrange
        Font.loadFont(Main.class.getResourceAsStream("/fonts/CRC35.OTF"), 13);
        ExcelReader excelReader = prepareExcelReader("/excels/test.xlsx");
        PdfFieldExtractor extractor = prepareExtractor(excelReader, "/pdfs/threeFonts.pdf");

        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                null,
                extractor.getFields(), 1);

        layouter.getFxFields().forEach(f -> {
            switch (f.getColumnId()) {
                case "Имя": assertThat("Имя", f.getFont().getName(), is("Circe Light"));
                    break;
                case "Фамилия": assertThat("Фамилия", f.getFont().getName(), is("FreeSet"));
                    break;
                case "Должность": assertThat("Должность", f.getFont().getName(), is("Circe Light"));
                    break;
            }
        });
    }

    /**************
     HELPER METHODS
     *************/

    private ExcelReader prepareExcelReader(String filePath) throws URISyntaxException, IOException {
        Path excelPath = Paths.get(getClass()
                .getResource(filePath).toURI());
        ExcelReader excelReader = new ExcelReader(excelPath.toFile().getAbsolutePath());
        excelReader.processFile();
        return excelReader;
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
        return new PdfFieldExtractor(pdfPath, new HashSet<>(Arrays.asList(excelReader.getHeadings())));
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}