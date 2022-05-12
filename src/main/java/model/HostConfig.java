package model;

import java.util.List;

/**
 *
 * @author nilimajha
 */
public class HostConfig {
    private String NodeName;
    private String NodeIP;
    private int NodePort;
    private String TrackerNodeName;
    private String TrackerNodeIp;
    private int TrackerNodePort;
    private List<String> AvailableFiles;
    private List<String> Download;

    /**
     * Constructor
     *
     * @param nodeName
     * @param nodeIp
     * @param nodePort
     * @param trackerNodeName
     * @param trackerNodeIP
     * @param trackerNodePort
     * @param availableFiles
     * @param download
     */
    public HostConfig(String nodeName, String nodeIp, int nodePort, String trackerNodeName, String trackerNodeIP,
                      int trackerNodePort, List<String> availableFiles, List<String> download) {
        NodeName = nodeName;
        NodeIP = nodeIp;
        NodePort = nodePort;
        TrackerNodeName = trackerNodeName;
        TrackerNodeIp = trackerNodeIP;
        TrackerNodePort = trackerNodePort;
        AvailableFiles = availableFiles;
        Download = download;
    }

    /**
     * getter for the attribute NodeName
     * @return NodeName
     */
    public String getNodeName() {
        return NodeName;
    }

    /**
     * setter for the attribute NodeName
     * @param nodeName
     */
    public void setNodeName(String nodeName) {
        NodeName = nodeName;
    }

    /**
     * getter for the attribute NodeIp
     * @return NodeIp
     */
    public String getNodeIp() {
        return NodeIP;
    }

    /**
     * setter for the attribute NodeIp
     * @param nodeIp
     */
    public void setNodeIp(String nodeIp) {
        NodeIP = nodeIp;
    }

    /**
     * getter for the attribute NodePort
     * @return NodePort
     */
    public int getNodePort() {
        return NodePort;
    }

    /**
     * setter for the attribute NodePort
     * @param nodePort
     */
    public void setNodePort(int nodePort) {
        NodePort = nodePort;
    }

    /**
     * getter for the attribute TrackerNodeName
     * @return TrackerNodeName
     */
    public String getTrackerNodeName() {
        return TrackerNodeName;
    }

    /**
     * setter for the attribute TrackerNodeName
     * @param trackerNodeName
     */
    public void setTrackerNodeName(String trackerNodeName) {
        TrackerNodeName = trackerNodeName;
    }

    /**
     * getter for the attribute TrackerNodeIp
     * @return TrackerNodeIp
     */
    public String getTrackerNodeIp() {
        return TrackerNodeIp;
    }

    /**
     * setter for the attribute TrackerNodeIp
     * @param trackerNodeIp
     */
    public void setTrackerNodeIp(String trackerNodeIp) {
        TrackerNodeIp = trackerNodeIp;
    }

    /**
     * getter for the attribute TrackerNodePort
     * @return TrackerNodePort
     */
    public int getTrackerNodePort() {
        return TrackerNodePort;
    }

    /**
     * setter for the attribute TrackerNodePort
     * @param trackerNodePort
     */
    public void setTrackerNodePort(int trackerNodePort) {
        TrackerNodePort = trackerNodePort;
    }

    /**
     * getter for the attribute AvailableFiles
     * @return AvailableFiles
     */
    public List<String> getAvailableFiles() {
        return AvailableFiles;
    }

    /**
     * setter for the attribute AvailableFiles
     * @param availableFiles
     */
    public void setAvailableFiles(List<String> availableFiles) {
        AvailableFiles = availableFiles;
    }

    /**
     * getter for the attribute Download
     * @return Download
     */
    public List<String> getDownload() {
        return Download;
    }

    /**
     * setter for the attribute Download
     * @param download
     */
    public void setDownload(List<String> download) {
        Download = download;
    }
}
