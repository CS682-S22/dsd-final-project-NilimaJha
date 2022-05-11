package utility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import customeException.ConnectionClosedException;
import model.Connection;
import model.NodeInfo;
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
        if (args.length == 6 && args[0].equals("-type") && args[2].equals("-name")
                && args[4].equals("-configFile") && typeIsValid(args[1]) && fileNameIsValid(args[5])) {
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
        return type.equals(Constants.PEER) || type.equals(Constants.TRACKER_NODE);
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
        return args[5];
    }

    /**
     * reads configFile and returns BrokerInformation class obj
     * which contains all the information of the producer or consumer
     * whose name is provided.
     * @param fileName config file name
     * @param name name provided in the args
     * @return BrokerConfig
     */
    public static NodeInfo extractTrackerNodeInfo(String fileName, String name) {
        List<NodeInfo> trackerNodeList = null;
        NodeInfo trackerNodeInfo = null;
        try {
            Reader configReader = Files.newBufferedReader(Paths.get(fileName));
            trackerNodeList = new Gson().fromJson(configReader, new TypeToken<List<NodeInfo>>() {}.getType());
            configReader.close();

            for (NodeInfo currentTrackerNodeInfo : trackerNodeList) {
                if (currentTrackerNodeInfo.getName().equals(name)) {
                    trackerNodeInfo = currentTrackerNodeInfo;
                    break;
                }
            }
        } catch (Exception ex) {
            logger.error("\nException Occurred. Error Message : " + ex.getMessage());
        }
        return trackerNodeInfo;
    }

    /**
     * initialises the FileInputStream named fileWriter of the class and deletes the file if already exist.
     * @param outputFileName file on which writting is to be performed
     */
    public static FileOutputStream fileWriterInitializer (String outputFileName) {
        File outputFile = new File(outputFileName);
        FileOutputStream fileWriter = null;
        if(outputFile.exists()){
            outputFile.delete();
        }  //deleting file if exist
        try {
            fileWriter = new FileOutputStream(outputFileName, true);
        } catch (FileNotFoundException e) {
            logger.error("\nFileNotFoundException occurred while Initialising FileOutPutStream for file "
                    + outputFileName + ". Error Message : " + e.getMessage());
        }
        return fileWriter;
    }

    /**
     * initialises the FileInputStream named fileReader of the class.
     * @param inputFileName file from where read is to be performed
     * @return fileReader
     */
    public static BufferedReader fileReaderInitializer (String inputFileName) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFileName));
        } catch (IOException e) {
            logger.error("\nIOException occurred while initialising BufferedReader on file " + inputFileName +
                    ". Error Message : " + e.getMessage());
        }
        return bufferedReader;
    }

    /**
     * tries to establish connection with the host running on the given IP and Port.
     * @param nodeIP
     * @param nodePort
     * @return Connection or null
     */
    public static Connection establishConnection(String nodeIP, int nodePort) throws ConnectionClosedException {
        AsynchronousSocketChannel clientSocket = null;
        Connection connection = null;
        try {
            clientSocket = AsynchronousSocketChannel.open();
            InetSocketAddress brokerAddress = new InetSocketAddress(nodeIP, nodePort);
            logger.info("\n[Connecting To Member] BrokerIP : "
                    + nodeIP + " BrokerPort : " + nodePort);
            Future<Void> futureSocket = clientSocket.connect(brokerAddress);
            futureSocket.get();
            logger.info("\n[Connected to node.]");
            connection = new Connection(clientSocket); //connection established with this member.
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
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return checksum;
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
        long packetOffset = packetNumber * Constants.MAX_PACKET_SIZE;
        return packetOffset;
    }
}
