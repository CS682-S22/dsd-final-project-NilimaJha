import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import swarm.File;
import utility.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTest {
    private static File file1 = null;
    private static File file2 = null;

    /**
     * initialises the file
     */
    @Before
    public void init() {
        String fileName = "file1.jpg";
        Path path = Paths.get(fileName);
        try {
            long fileSize = Files.size(path);
            file1 = new File(fileName, fileSize, Utility.createChecksum(fileName),
                    Utility.getTotalPacketOfFile(fileSize), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileName = "file3.jpg";
        path = Paths.get(fileName);
        try {
            long fileSize = Files.size(path);
            file2 = new File(fileName, fileSize, Utility.createChecksum(fileName),
                    Utility.getTotalPacketOfFile(fileSize), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test for getPacketData method.
     */
    @Test
    public void getPacketDataTest0() {
        Assertions.assertNotEquals(null, file1.getPacketData(0));
    }

    /**
     * test for getPacketData method.
     */
    @Test
    public void getPacketDataTest1() {
        Assertions.assertNull(file2.getPacketData(0));
    }

    /**
     * test for isBeingDownloaded method.
     */
    @Test
    public void isBeingDownloadedTest0() {
        Assertions.assertFalse(file1.isBeingDownloaded());
    }

    /**
     * test for isBeingDownloaded method.
     */
    @Test
    public void isBeingDownloadedTest1() {
        Assertions.assertTrue(file2.isBeingDownloaded());
    }

    /**
     * test for packetIsAvailable method.
     */
    @Test
    public void packetIsAvailableTest0() {
        Assertions.assertTrue(file1.packetIsAvailable(file1.getTotalPackets() - 6));
    }

    /**
     * test for packetIsAvailable method.
     */
    @Test
    public void packetIsAvailableTest1() {
        Assertions.assertFalse(file1.packetIsAvailable(file1.getTotalPackets() + 1));
    }

    /**
     * test for packetIsAvailable method.
     */
    @Test
    public void packetIsAvailableTest2() {
        Assertions.assertFalse(file1.packetIsAvailable(file1.getTotalPackets()));
    }

    /**
     * test for packetIsAvailable method.
     */
    @Test
    public void packetIsAvailableTest3() {
        Assertions.assertFalse(file2.packetIsAvailable(0));
    }

    /**
     * test for fileWriter method.
     */
    @Test
    public void fileWriterTest0() {
        Assertions.assertNull(file1.getTempFileWriter());
    }

    /**
     * test for fileWriter method.
     */
    @Test
    public void fileWriterTest1() {
        Assertions.assertNotNull(file2.getTempFileWriter());
    }

    /**
     * test for fileWriter method.
     */
    @Test
    public void fileWriterTest2() {
        Assertions.assertNotNull(file2.getFinalFileWriter());
    }

    /**
     * test for fileWriter method.
     */
    @Test
    public void fileReaderTest0() {
        Assertions.assertNotNull(file1.getFinalFileReader());
    }

    /**
     * test for fileWriter method.
     */
    @Test
    public void fileReaderTest1() {
        Assertions.assertNotNull(file2.getFinalFileReader());
    }

    /**
     * test for fileWriter method.
     */
    @Test
    public void fileReaderTest2() {
        Assertions.assertNull(file1.getTempFileReader());
    }

    /**
     * test for fileWriter method.
     */
    @Test
    public void fileReaderTest3() {
        Assertions.assertNotNull(file2.getTempFileReader());
    }
}
