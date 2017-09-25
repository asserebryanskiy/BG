package badgegenerator.pdfeditor;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class is used to create archive of badges
 */
public class BadgeArchive {
    private final int BUFFER = 2048;
    private final ZipOutputStream out;
    private final byte[] data;
    private final BadgeCreator badgeCreator;

    // ToDo: create test, that checks font in created document
    public BadgeArchive(String archivePath, BadgeCreator badgeCreator) throws FileNotFoundException {
        FileOutputStream dest = new FileOutputStream(archivePath);
        out = new ZipOutputStream(new
                BufferedOutputStream(dest));
        //out.setMethod(ZipOutputStream.DEFLATED);
        data = new byte[BUFFER];
        this.badgeCreator = badgeCreator;
    }

    public void createBadgeEntry(int i) throws IOException {
        BufferedInputStream origin;
        String fileName = "badge_" + i + ".pdf";
        ByteArrayInputStream bais = new
                ByteArrayInputStream(badgeCreator.createBadgeInMemory(i));
        origin = new
                BufferedInputStream(bais, BUFFER);
        ZipEntry entry = new ZipEntry(fileName);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0,
                BUFFER)) != -1) {
            out.write(data, 0, count);
        }
        origin.close();
    }

    public void createCommonBadge() throws IOException {
        BufferedInputStream origin;
        String fileName = "allInOne" + ".pdf";
        ByteArrayInputStream bais = new
                ByteArrayInputStream(badgeCreator.createCommonBadge());
        origin = new
                BufferedInputStream(bais, BUFFER);
        ZipEntry entry = new ZipEntry(fileName);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0,
                BUFFER)) != -1) {
            out.write(data, 0, count);
        }
        origin.close();
    }

    public ZipOutputStream getOutputStream() {
        return out;
    }
}
