package swarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * class to store swarm information of a file at a host.
 * @author nilimajha
 */
public class Swarm {
    private static final Logger logger = LogManager.getLogger(Swarm.class);
    private ConcurrentHashMap<String, SwarmMemberDetails> peerNameToDetailMap;
    private File file;

    /**
     * Constructor
     * @param file
     */
    public Swarm(File file) {
        this.peerNameToDetailMap = new ConcurrentHashMap<>();
        this.file = file;
    }

    /**
     * add new peer in the swarm.
     * @param peerNodeInfo
     * @return
     */
    public boolean addNewPeerInTheSwarm(SwarmMemberDetails peerNodeInfo) {
        SwarmMemberDetails swarmMemberDetails = peerNameToDetailMap.putIfAbsent(peerNodeInfo.getName(), peerNodeInfo);
        if (swarmMemberDetails != null) {
            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Adding new peer "
                    + peerNodeInfo.getName() + " in the Swarm for file " + file.getFileName());
        }
        return true;
    }

    /**
     * gets packet data from the File class object and returns it.
     * @param packetNumber
     * @return packetData
     */
    public byte[] getPacketData(long packetNumber) {
        byte[] packetData = null;
        packetData = file.getPacketData(packetNumber);
        return packetData;
    }

    /**
     * gets a random packet number from the numbers that are not yet downloaded.
     * if there is no packet left to download then return -1.
     * @return nextPacketNum or -1
     */
    public long nextPacketNumber() {
        long nextPacketNum = -1;
        if (file.isBeingDownloaded()) {
            nextPacketNum = file.getNextPacketToDownloadInfo();
        }
        return nextPacketNum;
    }

    /**
     * method gets the downloaded status of the file and returns it.
     * @return true/false
     */
    public boolean isBeingDownloaded() {
        return file.isBeingDownloaded();
    }

    /**
     * returns the availability of the given peer in the swarm
     * @return true/false
     */
    public boolean isAPeer(String peerName) {
        return peerNameToDetailMap.containsKey(peerName);
    }

    /**
     * method returns the size of the file.
     * @return fileSize
     */
    public long getFileSize() {
        return file.getFileSize();
    }

    /**
     * method returns the checksum of the file.
     * @return checksum
     */
    public byte[] getChecksum() {
       return file.getChecksum();
    }

    /**
     * method returns the totalPackets of the file
     * @return totalPackets
     */
    public long getTotalPackets() {
        return file.getTotalPackets();
    }

    /**
     * method returns the File class object.
     * @return file
     */
    public File getFile() {
        return file;
    }

    /**
     * returns peerNode's information from he peerNameToDetailsMap.
     * @param peerName
     * @return swarmMemberDetails
     */
    public SwarmMemberDetails getPeerNode(String peerName) {
        if (peerNameToDetailMap.containsKey(peerName)) {
            return peerNameToDetailMap.get(peerName);
        }
        return null;
    }

    /**
     * getter for the attribute peerNameToDetailMap.
     * @return peerNameToDetailMap
     */
    public ConcurrentHashMap<String, SwarmMemberDetails> getPeerNameToDetailMap() {
        return peerNameToDetailMap;
    }

    /**
     * closes the connection with the peer of given name.
     * @param peerName
     * @return true/false
     */
    public boolean closeConnection(String peerName) {
        return peerNameToDetailMap.get(peerName).closeConnection();
    }
}
