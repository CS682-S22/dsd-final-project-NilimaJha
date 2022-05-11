package model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * PEER
 * @author nilimajha
 */
public class Swarm {
    private ConcurrentHashMap<String, PeerNodeInfo> peerNameToDetailMap;
    private File file;
//    private ConnectionWithTracker connectionWithTracker = new ConnectionWithTracker();

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
    public boolean addNewPeerInTheSwarm(PeerNodeInfo peerNodeInfo) {
        peerNameToDetailMap.putIfAbsent(peerNodeInfo.getName(), peerNodeInfo);
        return true;
    }

    /**
     *
     * @param packetNumber
     * @return
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
     *
     * @return
     */
    public boolean isBeingDownloaded() {
        return file.isBeingDownloaded();
    }

    /**
     *
     * @return
     */
    public boolean isAPeer(String peerName) {
        return peerNameToDetailMap.containsKey(peerName);
    }

    /**
     *
     * @return
     */
    public long getFileSize() {
        return file.getFileSize();
    }

    /**
     *
     * @return
     */
    public byte[] getChecksum() {
       return file.getChecksum();
    }

    /**
     *
     * @return
     */
    public long getTotalPackets() {
        return file.getTotalPackets();
    }

    /**
     *
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     *
     * @param peerName
     * @return
     */
    public PeerNodeInfo getPeerNode(String peerName) {
        if (peerNameToDetailMap.containsKey(peerName)) {
            return peerNameToDetailMap.get(peerName);
        }
        return null;
    }
}
