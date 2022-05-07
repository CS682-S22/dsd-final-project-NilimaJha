package model;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author nilimajha
 */
public class SwarmPeerInfo {
    private String peerName;
    private FileRelatedInfo fileRelatedInfo;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructor
     *
     * @param peerName
     */
    public SwarmPeerInfo(String peerName, String fileName, long fileSize, long totalPacket, boolean entireFileAvailable) {
        this.peerName = peerName;
        this.fileRelatedInfo = new FileRelatedInfo(fileName, fileSize, totalPacket, entireFileAvailable);
    }

    /**
     * method to add new packet in the list of the packet available at the current peer.
     * @param packetNum
     * @return
     */
    public boolean updatePacketInfo(long packetNum) {
        lock.writeLock().lock();
        if (!fileRelatedInfo.getAvailablePacketList().contains(packetNum)) {
            fileRelatedInfo.getAvailablePacketList().add(packetNum);
        }
        if (fileRelatedInfo.getAvailablePacketList().size() == fileRelatedInfo.getTotalPacket()) {
            fileRelatedInfo.setEntireFileAvailable(true);
        }
        lock.writeLock().unlock();
        return true;
    }

    /**
     *
     * @return
     */
    public FileRelatedInfo getFileRelatedInfo() {
        return fileRelatedInfo;
    }
}
