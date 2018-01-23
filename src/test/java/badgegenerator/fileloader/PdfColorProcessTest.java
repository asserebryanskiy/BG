package badgegenerator.fileloader;

import com.itextpdf.kernel.color.DeviceGray;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.sun.tools.javac.util.List;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by andreyserebryanskiy on 23/01/2018.
 */
public class PdfColorProcessTest {
    @Test
    public void rgbColorTest() throws Exception {
        com.itextpdf.kernel.color.Color pdfColor = new DeviceRgb(100, 100, 100);
        Color result = test(pdfColor);
        assertThat(result.toString(), is(Color.rgb(100,100,100).toString()));
    }

    @Test
    public void grayColorTest() throws Exception {
        com.itextpdf.kernel.color.Color pdfColor = new DeviceGray(100f/255f);
        Color result = test(pdfColor);
        assertThat(result.toString(), is(Color.rgb(100,100,100).toString()));
    }

    @Test
    public void cmykTest() throws Exception {
        com.itextpdf.kernel.color.Color pdfColor = com.itextpdf.kernel.color.Color
                .convertRgbToCmyk(new DeviceRgb(100,100,100));
        Color result = test(pdfColor);
        assertThat(result.toString(), is(Color.rgb(100,100,100).toString()));
    }

    private Color test(com.itextpdf.kernel.color.Color pdfColor) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(os));
        Document doc = new Document(pdf);
        Paragraph p = new Paragraph("Test");
        p.setFontColor(pdfColor);
        doc.add(p);
        pdf.close();
        os.flush();

        PdfFieldExtractor extractor = new PdfFieldExtractor(
                new PdfDocument(new PdfReader(new ByteArrayInputStream(os.toByteArray()))),
                new HashSet<>(List.of("Test")));
        PdfToFxAdapter adapter = new PdfToFxAdapter(extractor.getFields().get("Test"), 1);
        return adapter.getColor();
    }
}
