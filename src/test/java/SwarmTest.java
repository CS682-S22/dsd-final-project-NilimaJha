import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import swarm.File;
import swarm.Swarm;
import swarm.SwarmMemberDetails;
import utility.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SwarmTest {
    private static Swarm swarm0 = null;
    private static File file0 = null;
    private static Swarm swarm1 = null;
    private static File file1 = null;

    /**
     * initialises the file
     */
    @Before
    public void init() {
        String fileName = "file1.jpg";
        Path path = Paths.get(fileName);
        try {
            long fileSize = Files.size(path);
            file0 = new File(fileName, fileSize, Utility.createChecksum(fileName),
                    Utility.getTotalPacketOfFile(fileSize), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        swarm0 = new Swarm(file0);

        fileName = "file3.jpg";
        path = Paths.get(fileName);
        try {
            long fileSize = Files.size(path);
            file1 = new File(fileName, fileSize, Utility.createChecksum(fileName),
                    Utility.getTotalPacketOfFile(fileSize), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        swarm1 = new Swarm(file1);
    }

    /**
     * test for the getFile method.
     */
    @Test
    public void getFileTest0() {
        Assertions.assertNotNull(swarm0.getFile());
    }

    /**
     * test for the getFile method.
     */
    @Test
    public void getFileTest1() {
        Assertions.assertNotNull(swarm1.getFile());
    }

    /**
     * test for the addPeer method.
     */
    @Test
    public void addPeerTest0() {
        SwarmMemberDetails swarmMemberDetails = new SwarmMemberDetails("Host-1", "localhost", 9000, null);
        Assertions.assertTrue(swarm1.addNewPeerInTheSwarm(swarmMemberDetails));
    }

    /**
     * test for the isPeer method.
     */
    @Test
    public void isPeerTest1() {
        SwarmMemberDetails swarmMemberDetails = new SwarmMemberDetails("Host-2", "localhost", 8099, null);
        swarm1.addNewPeerInTheSwarm(swarmMemberDetails);
        Assertions.assertTrue(swarm1.isAPeer("Host-2"));
    }

    /**
     * test for the isPeer method.
     */
    @Test
    public void isPeerTest3() {
        Assertions.assertFalse(swarm1.isAPeer("Host-3"));
    }

    /**
     * test for the getPeer method.
     */
    @Test
    public void getPeerNodeTest0() {
        SwarmMemberDetails swarmMemberDetails = new SwarmMemberDetails("Host-4", "localhost", 8089, null);
        swarm1.addNewPeerInTheSwarm(swarmMemberDetails);
        Assertions.assertNotNull(swarm1.getPeerNode("Host-4"));
    }

    /**
     * test for the getPeer method.
     */
    @Test
    public void getPeerNodeTest1() {
        SwarmMemberDetails swarmMemberDetails = new SwarmMemberDetails("Host-5", "localhost", 8079, null);
        swarm1.addNewPeerInTheSwarm(swarmMemberDetails);
        Assertions.assertEquals("Host-5", swarm1.getPeerNode("Host-5").getName());
    }
}
