import metadata.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import swarm.File;
import utility.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MetadataTest {
    Metadata metadata = null;
    /**
     * initialises the file
     */
    @Before
    public void init() {
        metadata = Metadata.getMetadata();

        String fileName = "file3.jpg";
        Path path = Paths.get(fileName);
        try {
            long fileSize = Files.size(path);
            File file2 = new File(fileName, fileSize, Utility.createChecksum(fileName),
                    Utility.getTotalPacketOfFile(fileSize), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test for the addNewFile method.
     */
    @Test
    public void addFileTest() {
        String fileName = "file1.jpg";
        Path path = Paths.get(fileName);
        try {
            long fileSize = Files.size(path);
            Assertions.assertTrue(metadata.addFile(fileName, fileSize, Utility.createChecksum(fileName),
                    Utility.getTotalPacketOfFile(fileSize)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test for the addNewHost method.
     */
    @Test
    public void addNewHostTest() {
        String peerName = "Host-1";
        String peerIp = "localhost";
        int peerPort = 8080;
        Assertions.assertTrue(metadata.addNewHost(peerName, peerIp, peerPort));
    }

    /**
     * test for the addMemberToTheSwarm method.
     */
    @Test
    public void addMemberToTheSwarmTest0() {
        String fileName = "file1.jpg";
        String peerName = "Host-1";
        boolean entireFileAvailable = true;
        Assertions.assertTrue(metadata.addMemberToTheSwarm(fileName, peerName, entireFileAvailable));
    }

    /**
     * test for the addMemberToTheSwarm method.
     */
    @Test
    public void addMemberToTheSwarmTest1() {
        String fileName = "file2.jpg";
        String peerName = "Host-1";
        boolean entireFileAvailable = true;
        Assertions.assertFalse(metadata.addMemberToTheSwarm(fileName, peerName, entireFileAvailable));
    }

    /**
     * test for the updateMemberAvailablePacketInfo method.
     */
    @Test
    public void updateMemberAvailablePacketInfoTest0() {
        String fileName = "file1.jpg";
        String peerName = "Host-2";
        Assertions.assertTrue(metadata.updateMemberAvailablePacketInfo(fileName, peerName, 5));
    }

    /**
     * test for the getPeerInfoOfASwarmForPacket method.
     */
    @Test
    public void getPeerInfoOfASwarmForPacketTest0() {
        String fileName = "file1.jpg";
        String peerName = "Host-2";
        metadata.updateMemberAvailablePacketInfo(fileName, peerName, 4);
        Assertions.assertNotNull(metadata.getPeerInfoOfASwarmForPacket(fileName, 4));
    }

    /**
     * test for the getPeerInfoOfASwarmForPacket method.
     */
    @Test
    public void getPeerInfoOfASwarmForPacketTest1() {
        String fileName = "file1.jpg";
        Assertions.assertTrue(metadata.getPeerInfoOfASwarmForPacket(fileName, 8).size() < 2);
    }
}
