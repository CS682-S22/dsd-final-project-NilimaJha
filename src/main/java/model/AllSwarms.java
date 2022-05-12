package model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import peer.RequestProcessor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * class to store information of all the swarm at the peer.
 * @author nilimajha
 */
public class AllSwarms {
    private static final Logger logger = LogManager.getLogger(AllSwarms.class);
    private NodeInfo thisPeerInfo;
    //file info
    private ConcurrentHashMap<String, Swarm> fileToItsSwarmMap;
    private static AllSwarms allSwarms = null;

    /**
     * Constructor
     */
    private AllSwarms(NodeInfo thisPeerInfo) {
        this.thisPeerInfo = thisPeerInfo;
        this.fileToItsSwarmMap = new ConcurrentHashMap<>();
    }

    /**
     * method that make sure only single instance of this class.
     * @param thisPeerInfo
     * @return allSwarm
     */
    public static AllSwarms getAllSwarm(NodeInfo thisPeerInfo) {
        if(allSwarms == null) {
            allSwarms = new AllSwarms(thisPeerInfo);
        }
        return allSwarms;
    }

    /**
     * method add a swarm for the new file.
     * @return
     */
    public boolean addNewFileSwarm(String fileName, File file) {
        Swarm swarm = new Swarm(file);
        fileToItsSwarmMap.putIfAbsent(fileName, swarm);
        return true;
    }

    /**
     * method add new peer in the already existing swarm of the file.
     * @return
     */
    public boolean addNewPeerInTheFileSwarm(String fileName, SwarmMemberDetails peerNodeInfo) {
        if (fileToItsSwarmMap.containsKey(fileName)) {
            fileToItsSwarmMap.get(fileName).addNewPeerInTheSwarm(peerNodeInfo);
        }
        return true;
    }

    /**
     *
     * @param fileName
     * @param packetNumber
     * @return
     */
    public byte[] getPacketDataFromFile(String fileName, long packetNumber) {
        byte[] packetData = null;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            logger.info("\n" + fileName + " available.");
            packetData = fileToItsSwarmMap.get(fileName).getPacketData(packetNumber);
        }
        return packetData;
    }

    /**
     * method to check if swarm for the given file is available or not.
     * @param fileName
     * @return true/false
     */
    public boolean fileAvailable(String fileName) {
        return fileToItsSwarmMap.containsKey(fileName);
    }

    /**
     * method returns a random packet number to be downloaded from the file with given fileName.
     * @param fileName
     */
    public long getNextPacketNumber(String fileName) {
        long packetNumber = -1;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            packetNumber = fileToItsSwarmMap.get(fileName).nextPacketNumber();
        }
        return packetNumber;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public boolean isBeingDownloaded(String fileName) {
        boolean fileIsBeingDownloaded = false;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            fileIsBeingDownloaded = fileToItsSwarmMap.get(fileName).isBeingDownloaded();
        }
        return fileIsBeingDownloaded;
    }

    /**
     *
     * @param fileName
     * @param peerName
     * @return
     */
    public boolean peerAvailableInFileSwarm(String fileName, String peerName) {
        boolean peerAvailable = false;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            peerAvailable = fileToItsSwarmMap.get(fileName).isAPeer(peerName);
        }
        return peerAvailable;
    }

    /**
     *
     * @return
     */
    public long getFileSize(String fileName) {
        long fileSize = 0;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            fileSize = fileToItsSwarmMap.get(fileName).getFileSize();
        }
        return fileSize;
    }

    /**
     *
     * @return
     */
    public byte[] getFileChecksum(String fileName) {
        byte[] fileSize = null;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            fileSize = fileToItsSwarmMap.get(fileName).getChecksum();
        }
        return fileSize;
    }

    /**
     *
     * @return
     */
    public long getFileTotalPackets(String fileName) {
        long totalPackets = 0;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            totalPackets = fileToItsSwarmMap.get(fileName).getTotalPackets();
        }
        return totalPackets;
    }

    /**
     *
     * @return
     */
    public boolean packetOfFileIsAvailable(String fileName, long packetNumber) {
        if (fileToItsSwarmMap.containsKey(fileName)) {
            return fileToItsSwarmMap.get(fileName).getFile().packetIsAvailable(packetNumber);
        }
        return  false;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public Swarm getSwarm(String fileName) {
        Swarm swarm = null;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            swarm = fileToItsSwarmMap.get(fileName);
        }
        return swarm;
    }

    /**
     *
     * @param fileName
     * @param packetNumber
     * @param packetData
     * @return
     */
    public boolean addDownloadedPacket(String fileName, long packetNumber, byte[] packetData) {
        boolean markedDownloaded = false;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            markedDownloaded = fileToItsSwarmMap.get(fileName).getFile().markPacketDownloaded(packetNumber, packetData);
        }
        return markedDownloaded;
    }
}
