package badgegenerator.pdfeditor;

/**
 * Is thrown, if node doesn't have parent.
 */
public class NoParentFoundException extends Exception{
    public NoParentFoundException(String message) {
        super(message);
    }
}
