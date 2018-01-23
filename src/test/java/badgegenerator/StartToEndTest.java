package badgegenerator;

import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfFieldExtractor;
import badgegenerator.pdfcreator.CreateBadgeArchiveTask;
import badgegenerator.pdfeditor.AlertCenter;
import badgegenerator.pdfeditor.FieldsLayouter;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StartToEndTest extends ApplicationTest{
    private File archive;

    @Test
    public void newYearBadges() throws Exception {
        String pdfName = "/pdfs/newYear.pdf";
        String excelName = "/excels/newYear.xlsx";
        test(pdfName, excelName);
    }

    @Test
    public void stsBadges() throws Exception {
        String pdfName = "/pdfs/sts.pdf";
        String excelName = "/excels/sts.xlsx";
        test(pdfName, excelName);
    }

    @Test
    public void siburBadges() throws Exception {
        String pdfName = "/pdfs/threeFonts.pdf";
        String excelName = "/excels/test.xlsx";
        test(pdfName, excelName);
    }

    @Test
    public void capitalizedBadges() throws Exception {
        String pdfName = "/pdfs/capitalized.pdf";
        String excelName = "/excels/capitalized.xlsx";
        test(pdfName, excelName);
    }

    @Test
    public void grayColorBadges() throws Exception {
        String pdfName = "/pdfs/grayColor.pdf";
        String excelName = "/excels/grayColor.xlsx";
        test(pdfName, excelName);
    }

    @After
    public void tearDown() throws Exception {
        File dir = archive.getParentFile();
        archive.delete();
        dir.delete();
    }

    private void test(String pdfName, String excelName) throws IOException, URISyntaxException, InterruptedException {
        InputStream lightStream = getClass().getResourceAsStream("/fonts/CRC35.OTF");
        Font.loadFont(lightStream, 13);
        lightStream.close();

        String pdfPath = getPath(pdfName);
        String excelPath = getPath(excelName);
        ExcelReader excelReader = new ExcelReader(excelPath);
        excelReader.processFile();
        PdfFieldExtractor extractor = new PdfFieldExtractor(pdfPath,
                new HashSet<>(Arrays.asList(excelReader.getHeadings())));
        PdfDocument pdf = new PdfDocument(new PdfReader(pdfPath));
        Rectangle pageSize = pdf.getFirstPage().getPageSize();
        Pane fieldsParent = new Pane();
        fieldsParent.setMaxSize(pageSize.getWidth(), pageSize.getHeight());
        FieldsLayouter layouter = new FieldsLayouter(fieldsParent, new AlertCenter(new Pane()),
                excelReader, null, extractor.getFields(), 1);

        File tempDir = new File(new File(pdfPath).getParentFile().getParentFile(), "temp");
        tempDir.mkdir();
        archive = new File(tempDir, "test.zip");
        String archivePath = archive.getAbsolutePath();
        CreateBadgeArchiveTask task = new CreateBadgeArchiveTask(layouter.getFxFields(),
                1, excelReader, pdfPath, archivePath, true);
        // assert that number of files is equal to excelReader.getValues().length + 1
        // assert that archive exists

        Thread testThread = new Thread(task);
        testThread.start();
        if (testThread.isAlive()) testThread.join();
        assertThat("Archive doesn't exist", archive.exists(), is(true));
        List<String> files = new ArrayList<>(excelReader.getValues().length + 1);
        ZipInputStream is = new ZipInputStream(new FileInputStream(archivePath));
        ZipEntry entry = is.getNextEntry();
        while (entry != null) {
            System.out.println(entry.getName());
            files.add(entry.getName());
            entry = is.getNextEntry();
        }
        assertThat(files.size(), is(excelReader.getValues().length + 1));
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
