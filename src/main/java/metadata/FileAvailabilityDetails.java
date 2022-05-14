package metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * class stores the fileAvailability details on the peer.
 * @author nilimajha
 */
public class FileAvailabilityDetails {
    private static final Logger logger = LogManager.getLogger(FileAvailabilityDetails.class);
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
            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Adding packet " + packetNum +
                    " in the " + peerName + "'s haveList for file " + fileName);
            getAvailablePacketList().add(packetNum);
        }
        if (getAvailablePacketList().size() == getTotalPacket()) {
            setEntireFileAvailable(true);
        }
        lock.writeLock().unlock();
        return true;
    }

    /**
     * getter for the attribute fileName
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * getter for the attribute totalPacket
     * @return totalPacket
     */
    public long getTotalPacket() {
        return totalPacket;
    }

    /**
     * getter for the attribute availablePacketList
     * @return availablePacketList
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
     * getter for the attribute entireFileAvailable
     * @return entireFileAvailable
     */
    public boolean isEntireFileAvailable() {
        return entireFileAvailable;
    }

    /**
     * setter for the attribute fileName
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * setter for the attribute setTotalPaket
     * @param totalPacket
     */
    public void setTotalPacket(int totalPacket) {
        this.totalPacket = totalPacket;
    }

    /**
     * setter for the attribute entireFileAvailable.
     * @param entireFileAvailable
     */
    public void setEntireFileAvailable(boolean entireFileAvailable) {
        this.entireFileAvailable = entireFileAvailable;
    }
}
