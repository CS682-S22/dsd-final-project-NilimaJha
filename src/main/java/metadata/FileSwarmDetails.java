package metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class to store file related details and also about the hosts available in the Swarm.
 * @author nilimajha
 */
public class FileSwarmDetails {
    private static final Logger logger = LogManager.getLogger(FileSwarmDetails.class);
    private String fileName;
    private long fileSize;
    private byte[] checksum;
    private long totalPackets;
    // map to map the name of the peer with the other file related details on that peer.
    private ConcurrentHashMap<String, FileAvailabilityDetails> peerNameToFileAvailabilityDetailsMap;

    /**
     * Constructor
     * @param fileName
     * @param fileSize
     * @param checksum
     * @param totalPackets
     */
    public FileSwarmDetails(String fileName, long fileSize, byte[] checksum, long totalPackets) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.totalPackets = totalPackets;
        this.peerNameToFileAvailabilityDetailsMap = new ConcurrentHashMap<>();
    }

    /**
     * add new peer in the swarm of this file.
     * @param peerName
     * @return
     */
    public boolean addMemberInSwarm(String peerName, boolean entireFileAvailable) {
        FileAvailabilityDetails hostFileAvailabilityDetails = new FileAvailabilityDetails(peerName, fileName, totalPackets, entireFileAvailable);
        FileAvailabilityDetails fileAvailabilityDetails = peerNameToFileAvailabilityDetailsMap.putIfAbsent(peerName, hostFileAvailabilityDetails);
        if (fileAvailabilityDetails == null) {
            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] New host " + peerName +
                    " is added to the list of host for file " + fileName);
        }
        return true;
    }

    /**
     * update the availablePacketList of the peer for the file.
     * @return
     */
    public boolean updateSwarmMemberPacketAvailableInfo(String peerName, long packetNumber) {
        if (peerNameToFileAvailabilityDetailsMap.containsKey(peerName)) {
            peerNameToFileAvailabilityDetailsMap.get(peerName).updatePacketInfo(packetNumber);
        }
        return true;
    }

    /**
     * method returns the list of all the peer name that are available in the swarm of the file.
     * @return peerNameList
     */
    public List<String> getAllPeerNameList() {
        List<String> peerNameList = new ArrayList<>();
        for (Map.Entry<String, FileAvailabilityDetails> eachPeer : peerNameToFileAvailabilityDetailsMap.entrySet()) {
            peerNameList.add(eachPeer.getKey());
        }
        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] PeerNameList " + peerNameList);
        return peerNameList;
    }

    /**
     * method returns the list of all the peer name that are available in the swarm of the file.
     * @return peerNameList
     */
    public List<String> getPeerNameListWithGivenPacket(long packetNumber) {
        List<String> peerNameList = new ArrayList<>();
        for (Map.Entry<String, FileAvailabilityDetails> eachPeer : peerNameToFileAvailabilityDetailsMap.entrySet()) {
            if (eachPeer.getValue().isEntireFileAvailable()
                    || eachPeer.getValue().isPacketAvailable(packetNumber)) {
                peerNameList.add(eachPeer.getKey());
            }
        }
        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] PeerNameList for packet "
                + packetNumber + " of file " + fileName + " is " + peerNameList);
        return peerNameList;
    }

    /**
     * getter for attribute fileName.
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * getter for attribute fileSize.
     * @return fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * getter for attribute checksum.
     * @return checksum
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * getter for attribute totalPackets.
     * @return totalPackets
     */
    public long getTotalPackets() {
        return totalPackets;
    }
}
