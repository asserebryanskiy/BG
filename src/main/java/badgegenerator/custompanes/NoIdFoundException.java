package badgegenerator.custompanes;

/**
 * Is thrown, if a node doesn't have an ID.
 */
public class NoIdFoundException extends Exception{
    public NoIdFoundException(String message) {
        super(message);
    }
}
