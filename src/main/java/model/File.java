package model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.Constants;
import utility.Utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author nilimajha
 */
public class File {
    private static final Logger logger = LogManager.getLogger(File.class);
    private String fileName;
    private long fileSize;
    private byte[] checksum;
    private long totalPackets;
    private String tempFileName;
    private volatile boolean entireFileAvailable;
    private ConcurrentHashMap<Long, PacketInformation> eachDownloadedPacketInfo;
    private List<Long> listOfToBeDownloadedPacketNumber;
    private volatile AtomicLong offset;
    private FileInputStream finalFileReader = null; // reads from final file.
    private FileOutputStream finalFileWriter = null; // writes to final file.
    private FileInputStream tempFileReader = null; // reads from temp file.
    private FileOutputStream tempFileWriter = null;
    private ReentrantReadWriteLock packetNumberListLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock tempFileWriterLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock tempFileReaderLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock finalFileReaderLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock finalFileWriterLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock statusChangeLock = new ReentrantReadWriteLock();
    /**
     * Constructor
     * @param fileName
     * @param fileSize
     * @param checksum
     * @param totalPackets
     * @param entireFileAvailable
     */
    public File(String fileName, long fileSize, byte[] checksum, long totalPackets, boolean entireFileAvailable) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.totalPackets = totalPackets;
        this.tempFileName = "temp " + fileName;
        this.entireFileAvailable = entireFileAvailable;
        if (!entireFileAvailable) {
            this.eachDownloadedPacketInfo = new ConcurrentHashMap<>();
            this.listOfToBeDownloadedPacketNumber = new ArrayList<>();
            for (long i = 0; i < totalPackets; i++) {
                this.listOfToBeDownloadedPacketNumber.add(i);
            }
            this.tempFileWriter = fileWriterInitializer(tempFileName);
            this.tempFileReader = fileReaderInitializer(tempFileName);
            this.finalFileWriter = fileWriterInitializer(fileName);
            this.offset = new AtomicLong(0);
        }
        this.finalFileReader = fileReaderInitializer(fileName);
    }

    /**
     * initialises the FileInputStream named fileWriter of the class and deletes the file if already exist.
     * @param fileName
     */
    public FileOutputStream fileWriterInitializer(String fileName) {
        FileOutputStream fileWriter = null;
        java.io.File outputFile = new java.io.File(fileName);
        if(outputFile.exists()){
            outputFile.delete();
        }  //deleting file if exist
        try {
            fileWriter = new FileOutputStream(fileName, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileWriter;
    }

    /**
     * initialises the FileInputStream named fileReader of the class.
     * @param fileName
     */
    private FileInputStream fileReaderInitializer(String fileName) {
        FileInputStream fileReader = null;
        try {
            fileReader = new FileInputStream(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileReader;
    }

    /**
     * used at loadBalancer to return one of the follower broker information
     * to the consumer, To facilitate read from follower.
     * @return BrokerInfo
     */
    public long getNextPacketToDownloadInfo() {
        Random rand = new Random();
        long packetNumber = -1;
        packetNumberListLock.readLock().lock();
        if (listOfToBeDownloadedPacketNumber.size() > 0) {
            long bound = listOfToBeDownloadedPacketNumber.size();
            long index = rand.nextInt((int) bound);
            packetNumber = listOfToBeDownloadedPacketNumber.get((int) index);
        }
        packetNumberListLock.readLock().unlock();
        return packetNumber;
    }

    /**
     *
     * @param packetNumber
     * @return
     */
    public boolean markPacketDownloaded(long packetNumber, byte[] packetData) {
        packetNumberListLock.writeLock().lock();
        boolean newPacketDownloaded = false;
        if (listOfToBeDownloadedPacketNumber.contains(packetNumber)) {
            try {
                writeOnTempFile(packetData); // writing the data of the packet on temp file.
                eachDownloadedPacketInfo.put(packetNumber, new PacketInformation(fileName,
                        packetNumber, packetData.length, offset.get())); // adding the new downloaded packet information into the map.
                listOfToBeDownloadedPacketNumber.remove(packetNumber); // removing downloaded packet number from the list of toBe downloaded list.
                offset.addAndGet(packetData.length);
                if (listOfToBeDownloadedPacketNumber.size() == 0) {
                    entireFileAvailable = true;
                    rearrangePackets();
                }
                newPacketDownloaded = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        packetNumberListLock.writeLock().unlock();
        return newPacketDownloaded;
    }

    /**
     * writing data directly on the file using FileOutputStream named fileWriter.
     */
    public void writeOnTempFile(byte[] message) throws IOException {
        tempFileWriterLock.writeLock().lock();        //flushing data on the file on file
        tempFileWriter.write(message);
        tempFileWriterLock.writeLock().unlock();
    }

    /**
     * writing data directly on the file using FileOutputStream named fileWriter.
     */
    public void writeOnFinalFile(byte[] message) throws IOException {
        finalFileWriterLock.writeLock().lock();        //flushing data on the file on file
        logger.info("\nWriting on final file " + finalFileWriter.getChannel().position());
        finalFileWriter.write(message);
        finalFileWriterLock.writeLock().unlock();
    }

    /**
     * method reads the data of the given packet number form the tempFile and returns it.
     * @param packetNumber
     */
    public byte[] readFromTempFile(long packetNumber) {
        tempFileReaderLock.writeLock().lock();
        byte[] packerMessage = null;
        long packetOffset;
        if (eachDownloadedPacketInfo.containsKey(packetNumber)) {
            packerMessage = new byte[(int) eachDownloadedPacketInfo.get(packetNumber).getPacketSize()];
            packetOffset = eachDownloadedPacketInfo.get(packetNumber).getInitialOffset();
            try {
                tempFileReader.getChannel().position(packetOffset);
                tempFileReader.read(packerMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tempFileReaderLock.writeLock().unlock();
        return packerMessage;
    }

    /**
     * method reads the data of the given packet number form the finalFile and returns it.
     * @param packetNumber
     */
    public byte[] readFromFinalFile(long packetNumber) {
        finalFileReaderLock.writeLock().lock();
        byte[] packetMessage = null;
        long packetOffset;
        if (entireFileAvailable && packetNumber < totalPackets) {
            packetOffset = Utility.offsetCalculator(packetNumber);
            logger.info("\n Reading packet " + packetNumber + " data from final file from offset " + packetOffset);
            if (packetNumber < (totalPackets - 2)) {
                logger.info("\nPacket is not the last. Last packet number " + (totalPackets - 1));
                packetMessage = new byte[Constants.MAX_PACKET_SIZE];
            } else {
                logger.info("\nPacket is the last. Last packet number " + (totalPackets - 1));
                packetMessage = new byte[(int) (fileSize - packetOffset)];
            }
            try {
                finalFileReader.getChannel().position(packetOffset);
                int totalBytesRead = finalFileReader.read(packetMessage);
                logger.info("\n total bytes read " + totalBytesRead);
            } catch (IOException e) {
                packetMessage = null;
                e.printStackTrace();
            }
        }
        finalFileReaderLock.writeLock().unlock();
        return packetMessage;
    }

    /**
     *
     * @return
     */
    public byte[] getPacketData(long packetNumber) {
        byte[] packetData = null;
        if (entireFileAvailable && packetNumber < totalPackets) {
            logger.info("\nPacket number " + packetNumber + " available.");
            packetData = readFromFinalFile(packetNumber);
        } else if (!entireFileAvailable) {
            logger.info("\nPacket number " + packetNumber + " available in temp file.");
            packetData = readFromTempFile(packetNumber);
        } else {
            logger.info("\nPacket number " + packetNumber + " is not available.");
        }
        return packetData;
    }

    /**
     * rearrange downloaded packets.
     * @return
     */
    public boolean rearrangePackets() throws IOException {
        if (eachDownloadedPacketInfo.size() == totalPackets) {
            long rearrangedPacketNumber = 0;
            finalFileWriterLock.writeLock().lock();
            while (rearrangedPacketNumber < totalPackets) {
                tempFileReaderLock.writeLock().lock();
                long currentPacketOffset = eachDownloadedPacketInfo.get(rearrangedPacketNumber).getInitialOffset();
                byte[] packetMessage = new byte[(int) eachDownloadedPacketInfo.get(rearrangedPacketNumber).getPacketSize()];
                tempFileReader.getChannel().position(currentPacketOffset);
                logger.info("\nReading data from offset " + currentPacketOffset + " from temp file.");
                tempFileReader.read(packetMessage);
                tempFileReaderLock.writeLock().unlock();
                writeOnFinalFile(packetMessage);
//                finalFileWriter.write(packetMessage);
                rearrangedPacketNumber++;
            }
            entireFileAvailable = true;
            fileReaderCloser(tempFileReader);
            fileWriterCloser(tempFileWriter);
            java.io.File outputFile = new java.io.File(tempFileName);
            if(outputFile.exists()){
                outputFile.delete();
            }
        }
        return true;
    }

    /**
     * return the status of attribute entireFileAvailable.
     * @return
     */
    public boolean isBeingDownloaded() {
        statusChangeLock.readLock().lock();
        boolean status = !entireFileAvailable;
        statusChangeLock.readLock().unlock();
        return status;
    }

    /**
     * return the size of the file.
     * @return
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     *
     * @return
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     *
     * @return
     */
    public long getTotalPackets() {
       return totalPackets;
    }

    /**
     *
     * @param packetNumber
     * @return
     */
    public boolean packetIsAvailable(long packetNumber) {
        return !isBeingDownloaded() || eachDownloadedPacketInfo.containsKey(packetNumber);
    }

    /**
     * method to close FileInputStream.
     * @param fileReader
     */
    private void fileReaderCloser(FileInputStream fileReader) {
        if (fileReader != null) {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method to close FileOutputStream.
     * @param fileReader
     */
    private void fileWriterCloser(FileOutputStream fileReader) {
        if (fileReader != null) {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
