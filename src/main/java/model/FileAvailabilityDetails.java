package model;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * class stores the fileAvailability details on the peer.
 * @author nilimajha
 */
public class FileAvailabilityDetails {
    private String peerName;
    private String fileName;
    private long totalPacket;
    private boolean entireFileAvailable;
    private ArrayList<Long> availablePacketList;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructor
     *
     * @param peerName
     */
    public FileAvailabilityDetails(String peerName, String fileName, long totalPacket, boolean entireFileAvailable) {
        this.peerName = peerName;
        this.fileName = fileName;
        this.totalPacket = totalPacket;
        this.entireFileAvailable = entireFileAvailable;
        this.availablePacketList = new ArrayList<>();
    }

    /**
     * method to add new packet in the list of the packet available at the current peer.
     * @param packetNum
     * @return
     */
    public boolean updatePacketInfo(long packetNum) {
        lock.writeLock().lock();
        if (!getAvailablePacketList().contains(packetNum)) {
            getAvailablePacketList().add(packetNum);
        }
        if (getAvailablePacketList().size() == getTotalPacket()) {
            setEntireFileAvailable(true);
        }
        lock.writeLock().unlock();
        return true;
    }

    /**
     *
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * @return
     */
    public long getTotalPacket() {
        return totalPacket;
    }

    /**
     *
     * @return
     */
    public ArrayList<Long> getAvailablePacketList() {
        return availablePacketList;
    }

    /**
     * checks if the packet with given packet number is available.
     * @return true/false
     */
    public boolean isPacketAvailable(long packetNumber) {
        if (availablePacketList.contains(packetNumber)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public boolean isEntireFileAvailable() {
        return entireFileAvailable;
    }

    /**
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     *
     * @param totalPacket
     */
    public void setTotalPacket(int totalPacket) {
        this.totalPacket = totalPacket;
    }

    /**
     *
     * @param entireFileAvailable
     */
    public void setEntireFileAvailable(boolean entireFileAvailable) {
        this.entireFileAvailable = entireFileAvailable;
    }
}
