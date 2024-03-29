package swarm;

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
 * Class stores the file information.
 * Readers and writers for the file.
 * and also the list of packets not downloaded yet.
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
    private volatile double downloadPercentage = 0;
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
     * returns one packet number that are not yet downloaded.
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
     * when a packet is downloaded it is written on the temp file and
     * its number is removed from the list of notYetDownloaded packets.
     * if the downloaded packet is the last one to be downloaded then
     * rearrangePackets method is called to rearrange the packets.
     * @param packetNumber
     * @return newPacketDownloaded
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
                    downloadPercentageCalculator(true);
                } else {
                    downloadPercentageCalculator(false);
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
     * writing data on the Temporary File using FileOutputStream named fileWriter.
     * @param message
     */
    public void writeOnTempFile(byte[] message) throws IOException {
        tempFileWriterLock.writeLock().lock();        //flushing data on the file on file
        tempFileWriter.write(message);
        tempFileWriterLock.writeLock().unlock();
    }

    /**
     * writing data on the Final File using FileOutputStream named fileWriter.
     * @param message
     */
    public void writeOnFinalFile(byte[] message) throws IOException {
        finalFileWriterLock.writeLock().lock();        //flushing data on the file on file
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
//            logger.info("\n Reading packet " + packetNumber + " data from final file from offset " + packetOffset);
            if (packetNumber < (totalPackets - 1)) {
//                logger.info("\nPacket is not the last. Last packet number " + (totalPackets - 1));
                packetMessage = new byte[Constants.MAX_PACKET_SIZE];
            } else {
//                logger.info("\nPacket is the last. Last packet number " + (totalPackets - 1));
                packetMessage = new byte[(int) (fileSize - packetOffset)];
            }
            try {
                finalFileReader.getChannel().position(packetOffset);
                int totalBytesRead = finalFileReader.read(packetMessage);
            } catch (IOException e) {
                packetMessage = null;
                logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                        "] IOException occurred. Error message : " + e.getMessage());
            }
        }
        finalFileReaderLock.writeLock().unlock();
        return packetMessage;
    }

    /**
     * method reads data of the packet from tempFile if downloading is under progress and
     * from finalFile if entire file is available.
     * @return packetData
     */
    public byte[] getPacketData(long packetNumber) {
        byte[] packetData = null;
        if (entireFileAvailable && packetNumber < totalPackets) {
            packetData = readFromFinalFile(packetNumber);
        } else if (!entireFileAvailable) {
            packetData = readFromTempFile(packetNumber);
        }
        return packetData;
    }

    /**
     * rearrange downloaded packets from tempFile to finalFile.
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
                tempFileReader.read(packetMessage);
                tempFileReaderLock.writeLock().unlock();
                writeOnFinalFile(packetMessage);
                rearrangedPacketNumber++;
            }
            boolean fileNotCorrupted = Utility.matchChecksum(checksum, fileName);
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
     * getter for attribute file name.
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * return the size of the file.
     * @return fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * getter for attribute checksum
     * @return checksum
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * getter for attribute totalPacket.
     * @return totalPackets
     */
    public long getTotalPackets() {
       return totalPackets;
    }

    /**
     * checks if packet with given packet number is available or not and returns result.
     * @param packetNumber
     * @return true/false
     */
    public boolean packetIsAvailable(long packetNumber) {
        return (packetNumber < totalPackets) && (!isBeingDownloaded() || eachDownloadedPacketInfo.containsKey(packetNumber));
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
                logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                        "] IOException occurred. Error message :" + e.getMessage());
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
                logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                        "] IOException occurred. Error message :" + e.getMessage());
            }
        }
    }

    /**
     * method calculates download percentage.
     */
    public void downloadPercentageCalculator(boolean assembled) {
        double totalPacket;
        if (!assembled) {
            totalPacket = totalPackets + 20;

        } else {
            totalPacket = totalPackets;
        }
        downloadPercentage = ((double)(eachDownloadedPacketInfo.size()) / totalPacket) * 100;
        printDownloadPercentage();
    }

    /**
     * prints the download percentages.
     */
    public void printDownloadPercentage() {
        logger.info("\n----------------------File : " + fileName + " ---------> DOWNLOAD PERCENTAGE : " + downloadPercentage + "%");
    }

    /**
     * getter for the finalFileReader.
     * @return finalFileReader
     */
    public FileInputStream getFinalFileReader() {
        return finalFileReader;
    }

    /**
     * getter for the finalFileWriter
     * @return finalFileWriter
     */
    public FileOutputStream getFinalFileWriter() {
        return finalFileWriter;
    }

    /**
     * getter for the tempFileReader
     * @return tempFileReader
     */
    public FileInputStream getTempFileReader() {
        return tempFileReader;
    }

    /**
     * getter for the tempFileWriter
     * @return tempFileWriter
     */
    public FileOutputStream getTempFileWriter() {
        return tempFileWriter;
    }
}
