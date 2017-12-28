package badgegenerator.pdfeditor;

import badgegenerator.appfilesmanager.SavesManager;
import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import badgegenerator.custompanes.IllegalFontSizeException;
import badgegenerator.custompanes.SingleLineField;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfFieldExtractor;
import badgegenerator.fxfieldssaver.FxFieldsSaver;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by andreyserebryanskiy on 19/10/2017.
 */
public class LayoutFromSavesTest extends ApplicationTest{
    private double initX;
    private double initX_WH;
    private String savePath;
    private ExcelReader excelReader;
    private Pane fieldsParent;

    @BeforeClass
    public static void beforeAll() throws Exception {
        InputStream lightStream = FieldsLayouterTest.class.getResourceAsStream("/fonts/CRC35.otf");
        Font.loadFont(lightStream, 13);
        lightStream.close();
    }

    @Before
    public void setUp() throws Exception {
        excelReader = mock(ExcelReader.class);
        when(excelReader.getLargestFields()).thenReturn(new String[]{"Short", "Shortestest words"});
        when(excelReader.getLongestWords()).thenReturn(new String[]{"Short", "Shortestest words"});
        when(excelReader.getHeadings()).thenReturn(new String[]{"Example", "Example1"});
        fieldsParent = new Pane();
        fieldsParent.setMaxSize(500, 340);
    }

    @Test
    public void ifNoSaveAndNoPdfFieldProvidedCreatesDefault() throws Exception {
        prepareSaves("LEFT");
        when(excelReader.getLargestFields())
                .thenReturn(new String[]{"Short", "Shortestest words", "Another"});
        when(excelReader.getLongestWords())
                .thenReturn(new String[]{"Short", "Shortestest words", "Another"});
        when(excelReader.getHeadings())
                .thenReturn(new String[]{"Example", "Example1", "Another"});

        PdfFieldExtractor extractor = prepareExtractor("/pdfs/threeFonts.pdf");
        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                savePath,
                extractor.getFields(), 1);

        assertThat(layouter.getFxFields().size(), is(3));
        assertThat("No message in the alert center",
                alertCenter.getNotifications().contains("Ни в сохранениях ни в pdf-документе не удалось найти параметры для поля \"Another\". Используются стандартные параметры: черный цвет, 13.0 размер шрифта, шрифт Circe Light."),
                is(true));
    }

    @Test
    public void ifCouldNotFindSaveWithProperColumnIdTakesFromPdf() throws Exception {
        prepareSaves("CENTER");
        when(excelReader.getLargestFields())
                .thenReturn(new String[]{"Short", "Shortestest words", "Должность"});
        when(excelReader.getLongestWords())
                .thenReturn(new String[]{"Short", "Shortestest words", "Должность"});
        when(excelReader.getHeadings())
                .thenReturn(new String[]{"Example", "Example1", "Должность"});

        PdfFieldExtractor extractor = prepareExtractor("/pdfs/threeFonts.pdf");
        AlertCenter alertCenter = new AlertCenter(new Pane());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent,
                alertCenter,
                excelReader,
                savePath,
                extractor.getFields(), 1);

        assertThat(layouter.getFxFields().size(), is(3));
        assertThat("No message in the alert center",
                alertCenter.getNotifications().contains("Не удалось найти сохранение для поля \"Должность\". Используются параметры из pdf-документа."),
                is(true));
    }

    @Test
    public void alignsCenterProperly() throws Exception {
        // Arrange
        prepareSaves("CENTER");

        // Act
        FieldsLayouter layouter = new FieldsLayouter(new Pane(),
                new AlertCenter(new Pane()),
                excelReader,
                savePath, new HashMap<>(), 1);
        FxField newField = layouter.getFxFields().get(0);
        FxField newFieldWH = layouter.getFxFields().get(1);
        double finalFieldCenterX = newField.getLayoutX() + newField.getPrefWidth() / 2;
        double finalFieldWHCenterX = newFieldWH.getLayoutX() + newFieldWH.getPrefWidth() / 2;

        // Assert
        assertThat("Single line", finalFieldCenterX, is(fieldsParent.getMaxWidth() / 2));
        assertThat("Multi line", finalFieldWHCenterX, is(fieldsParent.getMaxWidth() / 2));
    }

    @Test
    public void alignsRightProperly() throws Exception {
        // Arrange
        prepareSaves("RIGHT");

        // Act
        FieldsLayouter layouter = new FieldsLayouter(new Pane(),
                new AlertCenter(new Pane()),
                excelReader,
                savePath, new HashMap<>(), 1);
        FxField newField = layouter.getFxFields().get(0);
        FxField newFieldWH = layouter.getFxFields().get(1);
        double finalFieldRightX = newField.getLayoutX() + newField.getPrefWidth();
        double finalFieldWHRightX = newFieldWH.getLayoutX() + newFieldWH.getPrefWidth();

        // Assert
        assertThat("Single line", finalFieldRightX, is(fieldsParent.getMaxWidth()));
        assertThat("Multi line", finalFieldWHRightX, is(fieldsParent.getMaxWidth()));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        for (File file : SavesManager.getSavesFolder().listFiles()) {
            file.delete();
        }
    }

    private void prepareSaves(String alignment) throws IllegalFontSizeException {
        double parentWidth = fieldsParent.getMaxWidth();
        FxField field = new SingleLineField("Example", "Example",
                parentWidth);
        FxField fieldWithHyp = new FieldWithHyphenation("Example words", "Example1",
                parentWidth);
        List<FxField> fields = new ArrayList<>(2);
        fields.add(field);
        fields.add(fieldWithHyp);
        fields.forEach(f -> {
            f.setAlignment(alignment);
            f.setLayoutX(alignment.equals("CENTER") ? parentWidth / 2 - f.getPrefWidth() / 2 :
                    parentWidth - f.getPrefWidth());
        });

        String bundleName = "alignmentTest";
        FxFieldsSaver.createSave(fields, bundleName);
        savePath = SavesManager.getSavesFolder()
                + File.separator
                + bundleName;
    }

    private PdfFieldExtractor prepareExtractor(String pdfName) throws Exception {
        String pdfPath = Paths.get(getClass()
                .getResource(pdfName).toURI())
                .toFile()
                .getAbsolutePath();
        return new PdfFieldExtractor(pdfPath, new HashSet<>(Arrays.asList(excelReader.getHeadings())));
    }

    // to launch JavaFX environment
    @Override
    public void start(Stage stage) throws Exception {

    }
}