package badgegenerator.pdfcreator;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class is used to create archive of badges
 */
class BadgeArchive {
    private final int BUFFER = 2048;
    private final ZipOutputStream out;
    private final byte[] data;
    private final BadgeCreator badgeCreator;

    BadgeArchive(String archivePath, BadgeCreator badgeCreator) throws FileNotFoundException {
        FileOutputStream dest = new FileOutputStream(archivePath);
        out = new ZipOutputStream(new
                BufferedOutputStream(dest));
        //out.setMethod(ZipOutputStream.DEFLATED);
        data = new byte[BUFFER];
        this.badgeCreator = badgeCreator;
    }

    void createBadgeEntry(int i) throws IOException {
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

    void createCommonBadge() throws IOException {
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

    ZipOutputStream getOutputStream() {
        return out;
    }
}
