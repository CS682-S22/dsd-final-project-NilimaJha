import connection.Connection;
import customeException.ConnectionClosedException;
import model.HostConfig;
import model.TrackerConfig;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import utility.Constants;
import utility.Utility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestClass {
    public static AsynchronousServerSocketChannel serverSocket = null;
    public static AsynchronousSocketChannel server = null;
    public static Future futureObjServer = null;

    /**
     * tests matchChecksum method
     */
    @Test
    public void createChecksumTest0 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file1.jpg");
        Assertions.assertNotEquals(null, checksum);
    }

    /**
     * tests matchChecksum method
     */
    @Test
    public void createChecksumTest1 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file5.mov");
        Assertions.assertNotEquals(null, checksum);
    }

    /**
     * tests matchChecksum method
     */
    @Test
    public void createChecksumTest2 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file2.jpg");
        Assertions.assertEquals(null, checksum);
    }

    /**
     * tests matchChecksum method
     */
    @Test
    public void createChecksumTest3 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file1.mov");
        Assertions.assertEquals(null, checksum);
    }

    /**
     * tests matchChecksum method
     */
    @Test
    public void checksumMatchedTest0 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file1.jpg");
        Assertions.assertFalse(Utility.matchChecksum(checksum, "file3.jpg"));
    }

    /**
     * tests matchChecksum method
     */
    @Test
    public void checksumMatchedTest1 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file1.jpg");
        Assertions.assertTrue(Utility.matchChecksum(checksum, "file1.jpg"));
    }

    /**
     * tests matchChecksum method
     */
    @Test
    public void checksumMatchedTest2 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file1.jpg");
        Assertions.assertFalse(Utility.matchChecksum(checksum, "file3.mov"));
    }

    /**
     * tests matchChecksum method
     */
    @Test
    public void checksumMatchedTest3 () {
        byte[] checksum = null;
        checksum = Utility.createChecksum("file5.mov");
        Assertions.assertTrue(Utility.matchChecksum(checksum, "file5.mov"));
    }

    /**
     * tests the totalPacket there can be of a given file.
     */
    @Test
    public void totalPacketOfFileTest0() {
        Path path = Paths.get("file1.jpg");
        try {
            long fileSize = Files.size(path);
            long shouldBeTotalPackets;
            shouldBeTotalPackets = (fileSize / Constants.MAX_PACKET_SIZE);
            if ((fileSize % Constants.MAX_PACKET_SIZE) != 0) {
                 shouldBeTotalPackets += 1;
            }
            Assertions.assertEquals(shouldBeTotalPackets, Utility.getTotalPacketOfFile(fileSize));
        } catch (IOException e) {
            System.out.println("\nIOException Error Message : " + e.getMessage());
        }
    }

    /**
     * tests the totalPacket there can be of a given file.
     */
    @Test
    public void totalPacketOfFileTest1() {
        Path path = Paths.get("file5.mov");
        try {
            long fileSize = Files.size(path);
            long shouldBeTotalPackets;
            shouldBeTotalPackets = (fileSize / Constants.MAX_PACKET_SIZE);
            if ((fileSize % Constants.MAX_PACKET_SIZE) != 0) {
                shouldBeTotalPackets += 1;
            }
            Assertions.assertEquals(shouldBeTotalPackets, Utility.getTotalPacketOfFile(fileSize));
        } catch (IOException e) {
            System.out.println("\nIOException Error Message : " + e.getMessage());
        }
    }

    /**
     * tests the offsetCalculator method.
     */
    @Test
    public void offsetCalculatorTest0() {
        long packetNumber = 0;
        Assertions.assertEquals(0, Utility.offsetCalculator(packetNumber));
    }

    /**
     * tests the offsetCalculator method.
     */
    @Test
    public void offsetCalculatorTest1() {
        long packetNumber = 20;
        long expectedOffset = 20 * Constants.MAX_PACKET_SIZE;
        Assertions.assertEquals(expectedOffset, Utility.offsetCalculator(packetNumber));
    }

    /**
     * tests the offsetCalculator method.
     */
    @Test
    public void offsetCalculatorTest2() {
        long packetNumber = 1;
        Assertions.assertEquals(Constants.MAX_PACKET_SIZE, Utility.offsetCalculator(packetNumber));
    }

    /**
     * tests the offsetCalculator method.
     */
    @Test
    public void offsetCalculatorTest3() {
        long packetNumber = 8;
        Assertions.assertNotEquals(60000, Utility.offsetCalculator(packetNumber));
    }

    /**
     * tests the establishConnection method.
     */
    @Test
    public void establishConnectionTest0() {
        boolean shutdown = false;
        // setting up server
        Thread serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = AsynchronousServerSocketChannel.open();
                    serverSocket.bind(new InetSocketAddress("localhost", 8008));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                futureObjServer = serverSocket.accept();
                try {
                    server = (AsynchronousSocketChannel) futureObjServer.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        //starting server on separate thread
        serverThread.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            connection = Utility.establishConnection("localhost", 8008, false, 0);
        } catch (ConnectionClosedException e) {
            e.printStackTrace();
        }
        Assertions.assertNotEquals(null, connection);
    }

    /**
     * tests the establishConnection method.
     */
    @Test
    public void establishConnectionTest1() {
        boolean shutdown = false;
        // setting up server
        Thread serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = AsynchronousServerSocketChannel.open();
                    serverSocket.bind(new InetSocketAddress("localhost", 8005));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                futureObjServer = serverSocket.accept();
                try {
                    server = (AsynchronousSocketChannel) futureObjServer.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        //starting server on separate thread
        serverThread.start();

        Connection connection = null;
        try {
            connection = Utility.establishConnection("localhost", 8006, false, 0);
        } catch (ConnectionClosedException e) {
            e.printStackTrace();
        }
        Assertions.assertNull(connection);
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractTrackerInfoTest0() {
        TrackerConfig trackerConfig = Utility.extractTrackerInfo("TrackerNodeConfig.json", "TRACKER_NODE");
        Assertions.assertNotEquals(null, trackerConfig);
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractTrackerInfoTest1() {
        TrackerConfig trackerConfig = Utility.extractTrackerInfo("TrackerNodeConfig.json", "Tracker_Node");
        Assertions.assertNull(trackerConfig);
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractTrackerInfoTest2() {
        TrackerConfig trackerConfig = Utility.extractTrackerInfo("TrackerNodeConfig.json", "TRACKER_NODE");
        Assertions.assertEquals("TRACKER_NODE", trackerConfig.getName());
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractTrackerInfoTest3() {
        TrackerConfig trackerConfig = Utility.extractTrackerInfo("TrackerNodeConfig.json", "TRACKER_NODE");
        Assertions.assertNotEquals(null, trackerConfig.getTrackerNodeIP());
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractTrackerInfoTest4() {
        TrackerConfig trackerConfig = Utility.extractTrackerInfo("TrackerNodeConfig.json", "TRACKER_NODE");
        Assertions.assertNotEquals(0, trackerConfig.getTrackerNodePort());
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractHostInfoTest0() {
        HostConfig hostConfig = Utility.extractHostInfo("HostConfig.json", "HOST-1");
        Assertions.assertEquals("HOST-1", hostConfig.getNodeName());
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractHostInfoTest1() {
        HostConfig hostConfig = Utility.extractHostInfo("HostConfig.json", "HOST-1");
        Assertions.assertNotEquals("HOST-2", hostConfig.getNodeName());
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractHostInfoTest3() {
        HostConfig hostConfig = Utility.extractHostInfo("HostConfig.json", "HOST-2");
        Assertions.assertEquals("HOST-2", hostConfig.getNodeName());
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractHostInfoTest4() {
        HostConfig hostConfig = Utility.extractHostInfo("HostConfig.json", "HOST-6");
        Assertions.assertNotEquals(0, hostConfig.getAvailableFiles().size());
    }

    /**
     * test for extractTrackerInfo method.
     */
    @Test
    public void extractHostInfoTest5() {
        HostConfig hostConfig = Utility.extractHostInfo("HostConfig.json", "HOST-5");
        Assertions.assertNull(hostConfig.getAvailableFiles());
    }

    /**
     * test for argValid method.
     */
    @Test
    public void argIsValidTest0() {
        String[] arg = new String[] {"-type", "TRACKER_NODE", "-name", "TRACKER_NODE", "-delay", "0",
                "-configFile", "TrackerNodeConfig.json"};
        Assertions.assertTrue(Utility.argsIsValid(arg));
    }

    /**
     * test for argValid method.
     */
    @Test
    public void argIsValidTest1() {
        String[] arg = new String[] {"-type", "NODE", "-name", "HOST-1", "-delay", "15",
                "-configFile", "HostConfig.json"};
        Assertions.assertTrue(Utility.argsIsValid(arg));
    }

    /**
     * test for argValid method.
     */
    @Test
    public void argIsValidTest2() {
        String[] arg = new String[] {"-type", "Tracker_Node", "-name", "TRACKER_NODE", "-delay", "25",
                "-configFile", "TrackerNodeConfig.json"};
        Assertions.assertFalse(Utility.argsIsValid(arg));
    }

    /**
     * test for argValid method.
     */
    @Test
    public void argIsValidTest3() {
        String[] arg = new String[] {"-type", "Tracker_Node", "-delay", "25", "-name", "TRACKER_NODE",
                "-configFile", "TrackerNodeConfig.json"};
        Assertions.assertFalse(Utility.argsIsValid(arg));
    }

    /**
     * test for argValid method.
     */
    @Test
    public void argIsValidTest4() {
        String[] arg = new String[] {"-type", "Tracker_Node", "-name", "TRACKER_NODE", "-delay",
                "-configFile", "TrackerNodeConfig.json"};
        Assertions.assertFalse(Utility.argsIsValid(arg));
    }

    /**
     * test for type method.
     */
    @Test
    public void typeIsValidTest0() {
        String[] arg = new String[] {"-type", "TRACKER_NODE", "-name", "Tracker_Node", "-delay", "0",
                "-configFile", "TrackerNodeConfig.json"};
        Assertions.assertTrue(Utility.typeIsValid(Utility.getTypeFromArgs(arg)));
    }

    /**
     * test for type method.
     */
    @Test
    public void typeIsValidTest1() {
        String[] arg = new String[] {"-type", "Tracker_Node", "-name", "Tracker_Node", "-delay", "0",
                "-configFile", "TrackerNodeConfig.json"};
        Assertions.assertFalse(Utility.typeIsValid(Utility.getTypeFromArgs(arg)));
    }

    /**
     * test for type method.
     */
    @Test
    public void typeIsValidTest2() {
        String[] arg = new String[] {"-type", "NODE", "-name", "HOST-1", "-delay", "15",
                "-configFile", "HostConfig.json"};
        Assertions.assertTrue(Utility.typeIsValid(Utility.getTypeFromArgs(arg)));
    }

    /**
     * test for type method.
     */
    @Test
    public void typeIsValidTest3() {
        String[] arg = new String[] {"-type", "Node", "-name", "HOST-1", "-delay", "15",
                "-configFile", "HostConfig.json"};
        Assertions.assertFalse(Utility.typeIsValid(Utility.getTypeFromArgs(arg)));
    }

    /**
     * test for type method.
     */
    @Test
    public void typeIsValidTest4() {
        String[] arg = new String[] {"-type", "HOST", "-name", "HOST-1", "-delay", "15",
                "-configFile", "HostConfig.json"};
        Assertions.assertFalse(Utility.typeIsValid(Utility.getTypeFromArgs(arg)));
    }
}
