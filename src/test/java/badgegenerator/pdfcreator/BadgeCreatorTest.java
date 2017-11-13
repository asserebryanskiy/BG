package badgegenerator.pdfcreator;

import badgegenerator.custompanes.FieldWithHyphenation;
import badgegenerator.custompanes.FxField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreyserebryanskiy on 10/11/2017.
 */
public class BadgeCreatorTest {
    @Test
    public void twoWordsFieldDoesNotHyphenateOccasionally() throws Exception {
        String content = "Two words";
        List<FxField> fields = new ArrayList<>(1);
        FxField field = new FieldWithHyphenation(content, 0, 100);
        field.setLayoutX(30);
//        field.setAlignment("LEFT");
        field.setPrefWidth(field.computeStringWidth(content));
        fields.add(field);
        String pdfPath = getClass().getResource("/example.pdf").getPath();
        String[][] words = new String[][]{{content}};
        BadgeCreator bg = new BadgeCreator(fields, pdfPath, words,
                1, true);

        // Act
        byte[] pdf = bg.createBadgeInMemory(0);
        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdf));
        PdfWriter writer = new PdfWriter(
                new FileOutputStream("src/test/testResources/test.pdf"));
        PdfDocument doc = new PdfDocument(reader, writer);
        doc.close();

        //
    }
}