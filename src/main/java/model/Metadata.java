package model;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import proto.SwarmMemberInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton class to store all files metadata.
 * @author nilimajha
 */
public class Metadata {
    private ConcurrentHashMap<String, NodeInfo> peerNameToItsInfoMap;
    private ConcurrentHashMap<String, FileSwarmInfo> fileToSwarmMap;
    private static Metadata metadata = null;

    /**
     * Constructor
     */
    private Metadata() {
        this.peerNameToItsInfoMap = new ConcurrentHashMap<>();
        this.fileToSwarmMap = new ConcurrentHashMap<>();
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
     * add new peer node infor to the list of the peers.
     * @param peerName
     * @return
     */
    public boolean addNewPeer(String peerName, String peerIp, int peerPort) {
        NodeInfo peerInfo = new NodeInfo(peerName, peerIp, peerPort);
        peerNameToItsInfoMap.putIfAbsent(peerName, peerInfo);
        return true;
    }

    /**
     * add new file info that is available in the cluster to be downloaded.
     * @param fileName
     * @return
     */
    public boolean addFile(String fileName, long fileSize, byte[] checksum, long totalPackets) {
        FileSwarmInfo fileSwarm = new FileSwarmInfo(fileName, fileSize, checksum, totalPackets);
        fileToSwarmMap.putIfAbsent(fileName, fileSwarm);
        return true;
    }

    /**
     * add new member to the swarm for a given file.
     * @param fileName
     * @return
     */
    public boolean addMemberToTheSwarm(String fileName, String peerName, boolean entireFileAvailable) {
        fileToSwarmMap.get(fileName).addMemberInSwarm(peerName, entireFileAvailable);
        return true;
    }

    /**
     * calls updateSwarmMemberPacketAvailableInfo() of the FileSwarm class to add new packet
     * available info about the file on the given peer.
     * @return
     */
    public boolean updateMemberAvailablePacketInfo(String fileName, String peerName, long packetNumber) {
        if (fileToSwarmMap.containsKey(fileName)) {
            fileToSwarmMap.get(fileName).updateSwarmMemberPacketAvailableInfo(peerName, packetNumber);
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
        if (fileToSwarmMap.containsKey(fileName)) {
            List<String> swarmMemberNameList = fileToSwarmMap.get(fileName).getAllPeerNameList();
            for (String eachMemberName : swarmMemberNameList) {
                if (peerNameToItsInfoMap.containsKey(eachMemberName)) {
                    Any swarmMemberInfo = Any.pack(SwarmMemberInfo.SwarmMemberInfoDetails.newBuilder()
                            .setPeerName(peerNameToItsInfoMap.get(eachMemberName).getName())
                            .setPeerIp(peerNameToItsInfoMap.get(eachMemberName).getIp())
                            .setPeerPort(peerNameToItsInfoMap.get(eachMemberName).getPort())
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
        if (fileToSwarmMap.containsKey(fileName)) {
            List<String> swarmMemberNameList = fileToSwarmMap.get(fileName).getPeerNameListWithGivenPacket(packetNumber);
            for (String eachMemberName : swarmMemberNameList) {
                if (peerNameToItsInfoMap.containsKey(eachMemberName)) {
                    Any swarmMemberInfo = Any.pack(SwarmMemberInfo.SwarmMemberInfoDetails.newBuilder()
                            .setPeerName(peerNameToItsInfoMap.get(eachMemberName).getName())
                            .setPeerIp(peerNameToItsInfoMap.get(eachMemberName).getIp())
                            .setPeerPort(peerNameToItsInfoMap.get(eachMemberName).getPort())
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
        if (fileToSwarmMap.containsKey(fileName)) {
            fileSize = fileToSwarmMap.get(fileName).getFileSize();
        }
        return fileSize;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public byte[] getChecksum(String fileName) {
       byte[] checksum = null;
       if (fileToSwarmMap.containsKey(fileName)) {
           checksum = fileToSwarmMap.get(fileName).getChecksum();
       }
       return checksum;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public long getTotalNumberOfPackets(String fileName) {
        long totalNumberOfPacket = 0;
        if (fileToSwarmMap.containsKey(fileName)) {
            totalNumberOfPacket = fileToSwarmMap.get(fileName).getTotalPackets();
        }
        return totalNumberOfPacket;
    }
}
