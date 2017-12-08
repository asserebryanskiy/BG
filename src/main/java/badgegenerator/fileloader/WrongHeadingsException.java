package badgegenerator.fileloader;

/**
 * Is thrown if PdfFieldExtractor failed to find excel headings in pdf file.
 *
 * Created by andreyserebryanskiy on 06/12/2017.
 */
public class WrongHeadingsException extends Exception {
    public WrongHeadingsException(String message) {
        super(message);
    }
}
