package swarm;

import model.NodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
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
     * @param thisPeerInfo
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
     * method add a swarm for a new file.
     * @return
     */
    public boolean addNewFileSwarm(String fileName, File file) {
        Swarm swarm = new Swarm(file);
        Swarm existingSwarm = fileToItsSwarmMap.putIfAbsent(fileName, swarm);
        if (existingSwarm == null) {
            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Swarm for file " + fileName + " is created successfully.");
        }
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
     * method returns the data of the given packet number of the given file.
     * @param fileName
     * @param packetNumber
     * @return packetData
     */
    public byte[] getPacketDataFromFile(String fileName, long packetNumber) {
        byte[] packetData = null;
        if (fileToItsSwarmMap.containsKey(fileName)) {
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
     * returns the download status of the given file.
     * @param fileName
     * @return fileIsBeingDownloaded
     */
    public boolean isBeingDownloaded(String fileName) {
        boolean fileIsBeingDownloaded = false;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            fileIsBeingDownloaded = fileToItsSwarmMap.get(fileName).isBeingDownloaded();
        }
        return fileIsBeingDownloaded;
    }

    /**
     * checks if the given peer is available in this peer given file Swarm.
     * @param fileName
     * @param peerName
     * @return peerAvailable
     */
    public boolean peerAvailableInFileSwarm(String fileName, String peerName) {
        boolean peerAvailable = false;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            peerAvailable = fileToItsSwarmMap.get(fileName).isAPeer(peerName);
        }
        return peerAvailable;
    }

    /**
     * returns the size of the file with given name if exist otherwise 0.
     * @return fileSize
     */
    public long getFileSize(String fileName) {
        long fileSize = 0;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            fileSize = fileToItsSwarmMap.get(fileName).getFileSize();
        }
        return fileSize;
    }

    /**
     * returns the given file checksum value.
     * @return checksum
     */
    public byte[] getFileChecksum(String fileName) {
        byte[] checksum = null;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            checksum = fileToItsSwarmMap.get(fileName).getChecksum();
        }
        return checksum;
    }

    /**
     * getter for the total packet of a file
     * @return totalPackets
     */
    public long getFileTotalPackets(String fileName) {
        long totalPackets = 0;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            totalPackets = fileToItsSwarmMap.get(fileName).getTotalPackets();
        }
        return totalPackets;
    }

    /**
     * getter for the availabilityStatus of the given packet o the given file.
     * @return true/false
     */
    public boolean packetOfFileIsAvailable(String fileName, long packetNumber) {
        if (fileToItsSwarmMap.containsKey(fileName)) {
            return fileToItsSwarmMap.get(fileName).getFile().packetIsAvailable(packetNumber);
        }
        return  false;
    }

    /**
     * getter for the swarm obj associated with the given file.
     * @param fileName
     * @return swarm
     */
    public Swarm getSwarm(String fileName) {
        Swarm swarm = null;
        if (fileToItsSwarmMap.containsKey(fileName)) {
            swarm = fileToItsSwarmMap.get(fileName);
        }
        return swarm;
    }

    /**
     * updates packetAvailable information of a file for the given packetNumber.
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

    /**
     * method closes all the connections that was created to the download of the given file from peers.
     * @param fileName
     * @return
     */
    public boolean closeAllConnection(String fileName) {
        // close connections used for this downloading from the peer.
        if (fileToItsSwarmMap.containsKey(fileName)) {
            Swarm swarm = fileToItsSwarmMap.get(fileName);
            for(Map.Entry<String, SwarmMemberDetails> eachPeer : swarm.getPeerNameToDetailMap().entrySet()) {
                swarm.closeConnection(eachPeer.getKey());
            }
        }
        return true;
    }
}
