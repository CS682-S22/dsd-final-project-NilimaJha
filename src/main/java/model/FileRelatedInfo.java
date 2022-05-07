package model;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author nilimajha
 */
public class FileRelatedInfo {
    private String fileName;
    private long fileSize;
    private long totalPacket;
    private boolean entireFileAvailable;
    private ArrayList<Long> availablePacketList;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructor
     * @param fileName
     * @param fileSize
     * @param totalPacket
     */
    public FileRelatedInfo (String fileName, long fileSize, long totalPacket, boolean entireFileAvailable) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.totalPacket = totalPacket;
        this.entireFileAvailable = entireFileAvailable;
        this.availablePacketList = new ArrayList<>();
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
    public long getFileSize() {
        return fileSize;
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
    public ReentrantReadWriteLock getLock() {
        return lock;
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
     * @param fileSize
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
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
     * @param packetAvailable
     */
    public void setAvailablePacketList(ArrayList<Long> packetAvailable) {
        this.availablePacketList = packetAvailable;
    }

    /**
     *
     * @param lock
     */
    public void setLock(ReentrantReadWriteLock lock) {
        this.lock = lock;
    }

    /**
     *
     * @param entireFileAvailable
     */
    public void setEntireFileAvailable(boolean entireFileAvailable) {
        this.entireFileAvailable = entireFileAvailable;
    }

}
