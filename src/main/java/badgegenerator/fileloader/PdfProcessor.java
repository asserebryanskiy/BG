package badgegenerator.fileloader;

import com.itextpdf.kernel.pdf.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates an InputStream for further usage in pdf creation (BadgeCreator class).
 * Moreover, it deletes original fields from the pdf.
 *
 * Created by andreyserebryanskiy on 07/12/2017.
 */
public class PdfProcessor {
    private final InputStream pdfStream;
    public PdfProcessor(String pdfPath, String[] headings) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfReader(pdfPath), new PdfWriter(out));
        PdfPage page = pdf.getFirstPage();
        PdfResources resources = page.getResources();
        PdfDictionary dict = resources.getResource(PdfName.Font);
        Map<String, PdfObject> fonts = new HashMap<>();
        dict.entrySet().forEach(e -> fonts.put(e.getKey().getValue(), e.getValue()));
//        PdfFont font = PdfFontFactory.createFont((PdfDictionary) fonts.get("T1_0"));
        for (int i = 2; i < page.getContentStreamCount(); i++) {
            System.out.println(i + "th stream");
            PdfStream stream = page.getContentStream(i);
            byte[] data = stream.getBytes(true);
            String str = new String(data);
            int btInd = str.indexOf("BT");
            if (btInd < 0) continue;
            int etInd = str.indexOf("ET", btInd);
            String txt = str.substring(btInd + 1, etInd);
            String[] fields = new String[]{"Имя", "Фамилия", "Должность"};
            int fi = 0;
            int ce = 0;
            while (ce != -1) {
//                System.out.println(str);
//            String fStr = txt.substring(0, txt.indexOf("Tf"));
//            fStr = fStr.substring(fStr.lastIndexOf("\n"));
//            fStr = fStr.substring(2, fStr.indexOf(" "));
//            System.out.println(fStr);
//            PdfFont font = PdfFontFactory.createFont((PdfDictionary) fonts.get(fStr));

                ce = txt.indexOf("Tj", ce + 1);   // content end
                int cs = txt.substring(0, ce).lastIndexOf("\n") + 2;
                String content = txt.substring(cs, ce - 1);
                int ind = 0;
                for (int  j = 0; j < content.length(); j += 4) {
                    int code = Integer.parseInt(content.substring(j, j + 4), 16);
//                System.out.print(String.valueOf((char) (code)) + " ");
                    System.out.println(fields[fi].charAt(ind++) - code);
                }

                fi++;
                System.out.println();
            }
//            System.out.println(font.getFontProgram().getGlyph(0x00f0).);
            System.out.println("--------");
        }

        byte[] bytes = out.toByteArray();
        pdfStream = new ByteArrayInputStream(bytes);
        pdf.close();
        out.flush();
    }

    public InputStream getPdfStream() {
        return pdfStream;
    }
}
