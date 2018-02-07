package badgegenerator;

import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Created by andreyserebryanskiy on 30/01/2018.
 */
public class Util {
    public static String getPathForResource(String name) throws URISyntaxException {
        return Paths.get(Util.class
                .getResource(name).toURI())
                .toFile()
                .getAbsolutePath();
    }
}
