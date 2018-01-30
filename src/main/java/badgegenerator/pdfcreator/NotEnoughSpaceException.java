package badgegenerator.pdfcreator;

/**
 * Created by andreyserebryanskiy on 30/01/2018.
 */
public class NotEnoughSpaceException extends Exception {
    private final long querySpace;
    private final long availableSpace;

    public NotEnoughSpaceException(long querySpace, long availableSpace) {
        this.querySpace = querySpace;
        this.availableSpace = availableSpace;
    }

    public long getQuerySpace() {
        return querySpace;
    }

    public long getAvailableSpace() {
        return availableSpace;
    }
}
