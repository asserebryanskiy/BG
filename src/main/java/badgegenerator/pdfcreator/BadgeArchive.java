package badgegenerator.pdfcreator;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

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
    private DoubleProperty progressCreatingCommonFile;

    BadgeArchive(String archivePath, BadgeCreator badgeCreator) throws FileNotFoundException {
        FileOutputStream dest = new FileOutputStream(archivePath);
        out = new ZipOutputStream(new
                BufferedOutputStream(dest));
        //out.setMethod(ZipOutputStream.DEFLATED);
        data = new byte[BUFFER];
        this.badgeCreator = badgeCreator;
        progressCreatingCommonFile = new SimpleDoubleProperty();
    }

    void createBadgeEntry(int i) throws IOException {
        // +1 because in human world count starts from 1 and not from zero,
        // thus, first badge should be badge_1.pdf
        String fileName = "badge_" + (i + 1) + ".pdf";
        ByteArrayInputStream bais = new
                ByteArrayInputStream(badgeCreator.createBadgeInMemory(i));
        BufferedInputStream origin = new BufferedInputStream(bais, BUFFER);
        ZipEntry entry = new ZipEntry(fileName);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1) {
            out.write(data, 0, count);
        }
        origin.close();
    }

    void createCommonBadge() throws IOException {
        String fileName = "allInOne" + ".pdf";
        File file = new File(badgeCreator.getCommonFilePath());
        file.deleteOnExit();
        InputStream is = new FileInputStream(file);
        BufferedInputStream origin = new BufferedInputStream(is, BUFFER);
        ZipEntry entry = new ZipEntry(fileName);
        out.putNextEntry(entry);
        int count;
        long fileSize = file.length();
        System.out.println("Available " + fileSize);
        int read = 0;
        int last = 0;
        while ((count = origin.read(data, 0,
                BUFFER)) != -1) {
            read += count;
            if (read - last > 10_000_000) {
                last = read;
                progressCreatingCommonFile.set((double) last / fileSize);
//                System.out.println(last);
//                task.pro
            }
            out.write(data, 0, count);
        }
        is.close();
        origin.close();
    }

    public void close() throws IOException {
        out.close();
    }

    public ReadOnlyDoubleProperty progressCreatingCommonFileProperty() {
        return progressCreatingCommonFile;
    }
}
