package badgegenerator.pdfeditor;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.junit.Test;

/**
 * Created by andreyserebryanskiy on 11/11/2017.
 */
public class PdfEditorControllerTest {
    @Test
    public void makesFieldBoldIfThereIsNoBoldFont() throws Exception {
        // Impact
        Font.getFamilies().stream().filter(n -> n.contains("Impact"))
                .forEach(System.out::println);

        Text text = new Text("example");
        text.setFont(Font.font("Impact", FontWeight.BOLD, 13));

    }
}