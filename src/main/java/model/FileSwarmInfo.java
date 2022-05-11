package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nilimajha
 */
public class FileSwarmInfo {
    private String fileName;
    private long fileSize;
    private byte[] checksum;
    private long totalPackets;
    private ConcurrentHashMap<String, SwarmPeerInfo> swarmPeersInfo; // map to map the name of the peer with the other file related details.

    /**
     * Constructor
     * @param fileName
     */
    public FileSwarmInfo(String fileName, long fileSize, byte[] checksum, long totalPackets) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.totalPackets = totalPackets;
        this.swarmPeersInfo = new ConcurrentHashMap<>();
    }

    /**
     * add new peer in the swarm of this file.
     * @param peerName
     * @return
     */
    public boolean addMemberInSwarm(String peerName, boolean entireFileAvailable) {
        SwarmPeerInfo peerInfo = new SwarmPeerInfo(peerName, fileName, fileSize, totalPackets, entireFileAvailable);
        swarmPeersInfo.putIfAbsent(peerName, peerInfo);
        return true;
    }

    /**
     * update the availablePacketList of the peer for the file.
     * @return
     */
    public boolean updateSwarmMemberPacketAvailableInfo(String peerName, long packetNumber) {
        if (swarmPeersInfo.containsKey(peerName)) {
            swarmPeersInfo.get(peerName).updatePacketInfo(packetNumber);
        }
        return true;
    }

    /**
     * method returns the list of all the peer name that are available in the swarm of the file.
     * @return peerNameList
     */
    public List<String> getAllPeerNameList() {
        List<String> peerNameList = new ArrayList<>();
        for (Map.Entry<String, SwarmPeerInfo> eachPeer : swarmPeersInfo.entrySet()) {
            peerNameList.add(eachPeer.getKey());
        }
        return peerNameList;
    }

    /**
     * method returns the list of all the peer name that are available in the swarm of the file.
     * @return peerNameList
     */
    public List<String> getPeerNameListWithGivenPacket(long packetNumber) {
        List<String> peerNameList = new ArrayList<>();
        for (Map.Entry<String, SwarmPeerInfo> eachPeer : swarmPeersInfo.entrySet()) {
            if (eachPeer.getValue().getFileRelatedInfo().isEntireFileAvailable()
                    || eachPeer.getValue().getFileRelatedInfo().isPacketAvailable(packetNumber)) {
                peerNameList.add(eachPeer.getKey());
            }
        }
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
