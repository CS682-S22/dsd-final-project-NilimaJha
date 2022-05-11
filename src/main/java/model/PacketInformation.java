package model;

/**
 *
 * @author nilimajha
 */
public class PacketInformation {
    private String fileName;
    private long packetNumber;
    private long packetSize;
    private long initialOffset;

    /**
     * Constructor
     * @param fileName
     * @param packetNumber
     * @param packetSize
     * @param initialOffset
     */
    public PacketInformation(String fileName, long packetNumber, long packetSize, long initialOffset) {
        this.fileName = fileName;
        this.packetNumber = packetNumber;
        this.packetSize = packetSize;
        this.initialOffset = initialOffset;
    }

    /**
     * getter for attribute fileName
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * getter for attribute packetNumber
     * @return packetNumber
     */
    public long getPacketNumber() {
        return packetNumber;
    }

    /**
     * getter for attribute packetSize
     * @return packetSize
     */
    public long getPacketSize() {
        return packetSize;
    }

    /**
     * getter for attribute initialOffset
     * @return initialOffset
     */
    public long getInitialOffset() {
        return initialOffset;
    }

    /**
     * setter for attribute fileName
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * setter for attribute packetNumber
     * @param packetNumber
     */
    public void setPacketNumber(long packetNumber) {
        this.packetNumber = packetNumber;
    }

    /**
     * setter for attribute packetSize
     * @param packetSize
     */
    public void setPacketSize(long packetSize) {
        this.packetSize = packetSize;
    }

    /**
     * setter for attribute initialOffset.
     * @param initialOffset
     */
    public void setInitialOffset(long initialOffset) {
        this.initialOffset = initialOffset;
    }
}
