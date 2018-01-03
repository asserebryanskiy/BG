package badgegenerator;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.TextRegionEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by andreyserebryanskiy on 05/12/2017.
 */
public class PdfTest {
    private static final String helveticaPath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/fonts/Helvetica.otf";
    private static final String freesetPath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/fonts/freeset.ttf";
    private static final String circePath = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/fonts/CRC35.OTF";
    private static final String srcPdf = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/pdfs/newYear.pdf";
    private static final String targetPdf = "/Users/andreyserebryanskiy/IdeaProjects/badgeGenerator/src/test/testResources/pdfs/newYearWithLine.pdf";

    public static void main(String[] args) throws IOException {
//        parse();
//        getStream();
//        create();
        addCenterLine();
    }

    private static void addCenterLine() throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfReader(srcPdf), new PdfWriter(targetPdf));
        Rectangle pageSize = pdf.getFirstPage().getPageSize();
        float middle = pageSize.getWidth() / 2;
        PdfCanvas pdfCanvas = new PdfCanvas(pdf.getFirstPage());
        pdfCanvas.moveTo(middle, 0)
                .lineTo(middle, pageSize.getHeight())
                .stroke();

        pdf.close();
    }

    private static void parse() throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfReader(srcPdf));
        Rectangle rect = pdf.getFirstPage().getMediaBox();

        ContentFilter contentFilter = new ContentFilter(rect);
        FilteredEventListener listener = new FilteredEventListener();
        listener.attachEventListener(new LocationTextExtractionStrategy(), contentFilter);
        new PdfCanvasProcessor(listener).processPageContent(pdf.getFirstPage());
        pdf.close();
    }

    private static class ContentFilter extends TextRegionEventFilter {

        ContentFilter(Rectangle filterRect) {
            super(filterRect);
        }

        @Override
        public boolean accept(IEventData data, EventType type) {
            if (type.equals(EventType.RENDER_TEXT)) {
                TextRenderInfo renderInfo = (TextRenderInfo) data;
                String text = renderInfo.getText();
                if (text != null) {
                    System.out.println(text);
                }
                return false;
            }
            return false;
        }
    }

    private static void create() throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfReader(srcPdf), new PdfWriter(targetPdf));
        PdfCanvas pdfCanvas = new PdfCanvas(pdf.getFirstPage());
        PdfFont freeset = getPdfFont(freesetPath);
        PdfFont helvetica = getPdfFont(helveticaPath);
        PdfFont circe = getPdfFont(circePath);
        float pdfWidth = pdf.getFirstPage().getPageSize().getWidth();
        float duty = helvetica.getWidth("ДОЛЖНОСТЬ В КОМПАНИИ", 15);
        pdfCanvas.beginText()
                .setFontAndSize(helvetica, 15)
                .setColor(Color.RED, true)
                .moveText(50, 300)
//                .moveText(pdfWidth / 2 - duty / 2, 300)
                .showText("Должность в компании");
        float surname = freeset.getWidth("ФАМИЛИЯ", 10);
        float shift = 0;
//        float shift = duty / 2 - surname / 2;
        pdfCanvas.setFontAndSize(freeset, 10)
                .setColor(Color.GREEN, true)
                .moveText(shift, -30)
                .showText("Фамилия");
        shift = 0;
//        shift = surname / 2 - circe.getWidth("Имя", 20) / 2;
        pdfCanvas.setFontAndSize(circe, 20)
                .setColor(Color.BLUE, true)
                .moveText(shift, -30)
                .showText("Имя")
                .endText();

        pdf.close();
    }

    private static PdfFont getPdfFont(String path) throws IOException {
        InputStream fontInputStream = new FileInputStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int a;
        while((a = fontInputStream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, a);
        }
        baos.flush();
        return PdfFontFactory.createFont(baos.toByteArray(),
                PdfEncodings.IDENTITY_H, true);
    }


    private static void getStream() throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfReader(srcPdf));
        PdfDictionary dict = pdf.getFirstPage().getPdfObject();
        PdfObject object = dict.get(PdfName.Contents);
        if (object instanceof PdfStream) {
            System.out.println("Entered");
            PdfStream stream = (PdfStream) object;
            byte[] data = stream.getBytes();
            String str = new String(data, "UTF8");
//            System.out.println(str);
            int tdInd = str.indexOf("Td");
            String someWord = str.substring(tdInd + 4, str.indexOf("Tj") - 1);
            int ind = 0;
            for (int i = 0; i < someWord.length(); i += 4) {
                int pos = Integer.parseInt(someWord.substring(i, i + 4).toLowerCase(), 16);
                pos += 818;
                System.out.print((char) pos);
//                System.out.println("Менеджерская".charAt(ind++) - pos);
            }
            System.out.println();
            System.out.println(someWord);
            System.out.println(someWord.length() / 4);
        }
        pdf.close();
    }
}
