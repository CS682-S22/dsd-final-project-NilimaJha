package utility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import connection.Connection;
import customeException.ConnectionClosedException;
import model.HostConfig;
import model.TrackerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.lang.Integer.parseInt;

/**
 * util.Utility class to store the helper functions.
 * @author nilimajha
 */
public class Utility {
    private static final Logger logger = LogManager.getLogger(Utility.class);

    /**
     * method check the validity of the argument provided.
     * @param args argument
     * @return true/false
     */
    public static boolean argsIsValid (String[] args) {
        boolean isValid = false;
        if (args.length == 8 && args[0].equals("-type") && args[2].equals("-name") && args[4].equals("-delay")
                && args[6].equals("-configFile") && typeIsValid(args[1]) && fileNameIsValid(args[7])) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * check the validity of the type provided in the argument.
     * @param type name provided in the argument
     * @return true/false
     */
    public static boolean typeIsValid (String type) {
        return type.equals(Constants.NODE) || type.equals(Constants.TRACKER_NODE);
    }

    /**
     * check the validity of the all the config file provided.
     * @param fileName list of files
     * @return true/false
     */
    public static boolean fileNameIsValid(String fileName) {
        boolean valid = true;
        if (getFileExtension(fileName) == null || !getFileExtension(fileName).equals(".json")) {
            valid = false;
        }
        return valid;
    }

    /**
     * extracts the extension of the given fileName that is in String format.
     * @param fileName file name
     * @return extension
     */
    public static String getFileExtension(String fileName) {
        String extension = null;
        int index = fileName.lastIndexOf(".");
        if (index > 0 && index < fileName.length() - 1) {
            extension = fileName.substring(index);
        }
        return extension;
    }

    /**
     * returns the type provided in the argument.
     * @param args argument
     * @return type
     */
    public static String getTypeFromArgs (String[] args) {
        return args[1];
    }

    /**
     * returns the name provided in the argument.
     * @param args arguments
     * @return name
     */
    public static String getNameFromArgs (String[] args) {
        return args[3];
    }

    /**
     * returns config file name provided in the argument.
     * @param args argument
     * @return filename
     */
    public static String getConfigFilename (String[] args) {
        return args[7];
    }

    /**
     *
     * @param args
     * @return
     */
    public static boolean getDelayStatusFromArgs(String[] args) {
        boolean delayStatus = false;
        if (parseInt(args[5]) > 0) {
            delayStatus =  true;
        }
        return delayStatus;
    }

    /**
     *
     * @param args
     * @return
     */
    public static int getMaxDelayFromArgs(String[] args) {
        int maxDelay = 0;
        maxDelay = parseInt(args[5]);
        return maxDelay;
    }

    /**
     * reads configFile and returns BrokerInformation class obj
     * which contains all the information of the producer or consumer
     * whose name is provided.
     * @param fileName config file name
     * @param name name provided in the args
     * @return BrokerConfig
     */
    public static HostConfig extractHostInfo(String fileName, String name) {
        List<HostConfig> peerNodeList = null;
        HostConfig peerConfig = null;
        try {
            Reader configReader = Files.newBufferedReader(Paths.get(fileName));
            peerNodeList = new Gson().fromJson(configReader, new TypeToken<List<HostConfig>>() {}.getType());
            configReader.close();

            for (HostConfig peerNodeInfo : peerNodeList) {
                if (peerNodeInfo.getNodeName().equals(name)) {
                    peerConfig = peerNodeInfo;
                    break;
                }
            }
        } catch (Exception ex) {
            logger.error("\nException Occurred. Error Message : " + ex.getMessage());
        }
        return peerConfig;
    }

    /**
     * reads configFile and returns BrokerInformation class obj
     * which contains all the information of the producer or consumer
     * whose name is provided.
     * @param fileName config file name
     * @param name name provided in the args
     * @return BrokerConfig
     */
    public static TrackerConfig extractTrackerInfo(String fileName, String name) {
        List<TrackerConfig> trackerDetailsList = null;
        TrackerConfig trackerInfo = null;
        try {
            Reader configReader = Files.newBufferedReader(Paths.get(fileName));
            trackerDetailsList = new Gson().fromJson(configReader, new TypeToken<List<TrackerConfig>>() {}.getType());
            configReader.close();

            for (TrackerConfig eachHostInfo : trackerDetailsList) {
                if (eachHostInfo.getName().equals(name)) {
                    trackerInfo = eachHostInfo;
                    break;
                }
            }
        } catch (Exception ex) {
            logger.error("\nException Occurred. Error Message : " + ex.getMessage());
        }
        return trackerInfo;
    }

    /**
     * tries to establish connection with the host running on the given IP and Port.
     * @param nodeIP
     * @param nodePort
     * @return Connection or null
     */
    public static Connection establishConnection(String nodeIP, int nodePort, boolean delay, int maxDelay) throws ConnectionClosedException {
        AsynchronousSocketChannel clientSocket = null;
        Connection connection = null;
        try {
            clientSocket = AsynchronousSocketChannel.open();
            InetSocketAddress brokerAddress = new InetSocketAddress(nodeIP, nodePort);
//            logger.info("\n[Connecting] Host at IP : " + nodeIP + " Port : " + nodePort);
            Future<Void> futureSocket = clientSocket.connect(brokerAddress);
            futureSocket.get();
            connection = new Connection(clientSocket, delay, maxDelay); //connection established with this member.
        } catch (IOException | ExecutionException | InterruptedException e) {
            logger.error(e.getMessage());
            throw new ConnectionClosedException("No Host running on the given IP & port!!!");
        }
        return connection;
    }

    /**
     * creates checksum of the file given and returns it.
     * @return
     */
    public static byte[] createChecksum(String fileName) {
        MessageDigest digest = null;
        byte[] checksum = null;
        byte[] chunk = new byte[1000];
        int count = 0;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fileInputStream = new FileInputStream(fileName);) {
                while ((count = fileInputStream.read(chunk)) != -1) {
                    digest.update(chunk, 0, count);
                }
                checksum = digest.digest();
            } catch (IOException e) {
                logger.error("\nThreadId : " + Thread.currentThread().getId() +
                        "] IOException occurred. Error Message : " + e.getMessage());
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("\nThreadId : " + Thread.currentThread().getId() +
                    "] NoSuchAlgorithmException occurred. Error Message : " + e.getMessage());
        }
        return checksum;
    }

    /**
     * compare two checksum value.
     * @return true/false
     */
    public static boolean matchChecksum (byte[] checksum, String fileName) {
        int i = 0;
        while (i < checksum.length) {
            byte[] createdChecksum = createChecksum(fileName);
            if (createdChecksum == null) {
                return false;
            } else if (checksum[i] != createdChecksum[i]) {
                return false;
            }
            i++;
        }
        return true;
    }

    /**
     * creates checksum of the file given and returns it.
     * @return
     */
    public static long getTotalPacketOfFile(long fileSize) {
        double totalPackets = 0;
        if (fileSize > 0) {
            totalPackets = (double) fileSize / (double) Constants.MAX_PACKET_SIZE;
        }
        return (long) Math.ceil(totalPackets);
    }

    /**
     * method calculates the offset of the current packet in the file.
     * @param packetNumber
     * @return packetOffset
     */
    public static long offsetCalculator(long packetNumber) {
        return packetNumber * Constants.MAX_PACKET_SIZE;
    }
}
