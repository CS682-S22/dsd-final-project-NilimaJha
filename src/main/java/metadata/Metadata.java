package metadata;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import model.NodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.SwarmMemberInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton class to store all files metadata.
 * @author nilimajha
 */
public class Metadata {
    private static final Logger logger = LogManager.getLogger(Metadata.class);
    private ConcurrentHashMap<String, NodeInfo> availableHostsNameToDetailMap;
    private ConcurrentHashMap<String, FileSwarmDetails> fileNameToFileSwarmDetailsMap;
    private static Metadata metadata = null;

    /**
     * Constructor
     */
    private Metadata() {
        this.availableHostsNameToDetailMap = new ConcurrentHashMap<>();
        this.fileNameToFileSwarmDetailsMap = new ConcurrentHashMap<>();
    }

    /**
     * public method to initialise the metadata obj and return it.
     * if the metadata onj is already created it will return the same obj.
     * @return
     */
    public static Metadata getMetadata() {
        if (metadata == null) {
            metadata = new Metadata();
        }
        return metadata;
    }

    /**
     * add new host node info to the list of the hosts.
     * @param peerName
     * @return
     */
    public boolean addNewHost(String peerName, String peerIp, int peerPort) {
        NodeInfo peerInfo = new NodeInfo(peerName, peerIp, peerPort);
        availableHostsNameToDetailMap.putIfAbsent(peerName, peerInfo);
        return true;
    }

    /**
     * add new file info that is available in the cluster to be downloaded.
     * @param fileName
     * @return
     */
    public boolean addFile(String fileName, long fileSize, byte[] checksum, long totalPackets) {
        FileSwarmDetails fileSwarm = new FileSwarmDetails(fileName, fileSize, checksum, totalPackets);
        FileSwarmDetails fileSwarmDetails = fileNameToFileSwarmDetailsMap.putIfAbsent(fileName, fileSwarm);
        if (fileSwarmDetails == null) {
            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] New File "
                    + fileName + " is added to the list of files.");
        }
        return true;
    }

    /**
     * add new member to the swarm for a given file.
     * @param fileName
     * @return true/false
     */
    public boolean addMemberToTheSwarm(String fileName, String peerName, boolean entireFileAvailable) {
        boolean added = false;
        synchronized (this) {
            if (fileNameToFileSwarmDetailsMap.containsKey(fileName)) {
                added = fileNameToFileSwarmDetailsMap.get(fileName).addMemberInSwarm(peerName, entireFileAvailable);
            }
        }
        return added;
    }

    /**
     * calls updateSwarmMemberPacketAvailableInfo() of the FileSwarm class to add new packet
     * available info about the file on the given peer.
     * @return
     */
    public boolean updateMemberAvailablePacketInfo(String fileName, String peerName, long packetNumber) {
        if (fileNameToFileSwarmDetailsMap.containsKey(fileName)) {
            fileNameToFileSwarmDetailsMap.get(fileName).updateSwarmMemberPacketAvailableInfo(peerName, packetNumber);
        }
        return true;
    }

    /**
     * returns the list of swarm member information in byte array form.
     * @param fileName
     * @return swarmMemberInfoList
     */
    public List<ByteString> getAllPeerInfoOfASwarm(String fileName) {
        List<ByteString> swarmMemberInfoList = new ArrayList<>();
        if (fileNameToFileSwarmDetailsMap.containsKey(fileName)) {
            List<String> swarmMemberNameList = fileNameToFileSwarmDetailsMap.get(fileName).getAllPeerNameList();
            for (String eachMemberName : swarmMemberNameList) {
                if (availableHostsNameToDetailMap.containsKey(eachMemberName)) {
                    Any swarmMemberInfo = Any.pack(SwarmMemberInfo.SwarmMemberInfoDetails.newBuilder()
                            .setPeerName(availableHostsNameToDetailMap.get(eachMemberName).getName())
                            .setPeerIp(availableHostsNameToDetailMap.get(eachMemberName).getIp())
                            .setPeerPort(availableHostsNameToDetailMap.get(eachMemberName).getPort())
                            .build());
                    swarmMemberInfoList.add(ByteString.copyFrom(swarmMemberInfo.toByteArray()));
                }
            }
        }
        return swarmMemberInfoList;
    }

    /**
     * returns the list of swarm member information in byte array form on which given packet of the file is available.
     * @param fileName
     * @return swarmMemberInfoList
     */
    public List<ByteString> getPeerInfoOfASwarmForPacket(String fileName, long packetNumber) {
        List<ByteString> swarmMemberInfoList = new ArrayList<>();
        if (fileNameToFileSwarmDetailsMap.containsKey(fileName)) {
            List<String> swarmMemberNameList = fileNameToFileSwarmDetailsMap.get(fileName)
                    .getPeerNameListWithGivenPacket(packetNumber);
            for (String eachMemberName : swarmMemberNameList) {
                if (availableHostsNameToDetailMap.containsKey(eachMemberName)) {
                    Any swarmMemberInfo = Any.pack(SwarmMemberInfo.SwarmMemberInfoDetails.newBuilder()
                            .setPeerName(availableHostsNameToDetailMap.get(eachMemberName).getName())
                            .setPeerIp(availableHostsNameToDetailMap.get(eachMemberName).getIp())
                            .setPeerPort(availableHostsNameToDetailMap.get(eachMemberName).getPort())
                            .build());
                    swarmMemberInfoList.add(ByteString.copyFrom(swarmMemberInfo.toByteArray()));
                }
            }
        }
        return swarmMemberInfoList;
    }

    /**
     * method returns the given file Size.
     * @param fileName
     * @return fileSize
     */
    public long getFileSize(String fileName) {
        long fileSize = 0;
        if (fileNameToFileSwarmDetailsMap.containsKey(fileName)) {
            fileSize = fileNameToFileSwarmDetailsMap.get(fileName).getFileSize();
        }
        return fileSize;
    }

    /**
     * getter for the checksum of the file with the given file name.
     * @param fileName
     * @return
     */
    public byte[] getChecksum(String fileName) {
       byte[] checksum = null;
       if (fileNameToFileSwarmDetailsMap.containsKey(fileName)) {
           checksum = fileNameToFileSwarmDetailsMap.get(fileName).getChecksum();
       }
       return checksum;
    }

    /**
     * getter for the attribute totalNumberOfPacket of the file with given file name.
     * @param fileName
     * @return
     */
    public long getTotalNumberOfPackets(String fileName) {
        long totalNumberOfPacket = 0;
        if (fileNameToFileSwarmDetailsMap.containsKey(fileName)) {
            totalNumberOfPacket = fileNameToFileSwarmDetailsMap.get(fileName).getTotalPackets();
        }
        return totalNumberOfPacket;
    }
}
