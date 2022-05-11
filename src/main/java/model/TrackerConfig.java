package model;

/**
 * Config class to store information from tracker config file.
 * @author nilimajha
 */
public class TrackerConfig {
    private String Name;
    private String TrackerNodeIP;
    private int TrackerNodePort;

    /**
     *
     * @param name
     * @param trackerNodeIP
     * @param trackerNodePort
     */
    public TrackerConfig(String name, String trackerNodeIP, int trackerNodePort) {
        Name = name;
        TrackerNodeIP = trackerNodeIP;
        TrackerNodePort = trackerNodePort;
    }

    /**
     * getter for the attribute Name.
     * @return Name
     */
    public String getName() {
        return Name;
    }

    /**
     * setter for the attribute Name
     * @param name
     */
    public void setName(String name) {
        Name = name;
    }

    /**
     * getter for the attribute TrackerNodeIP
     * @return TrackerNodeIP
     */
    public String getTrackerNodeIP() {
        return TrackerNodeIP;
    }

    /**
     * setter for the attribute TrackerNodeIP
     * @param trackerNodeIP
     */
    public void setTrackerNodeIP(String trackerNodeIP) {
        TrackerNodeIP = trackerNodeIP;
    }

    /**
     * getter for the attribute TrackerNodePort
     * @return TrackerNodePort
     */
    public int getTrackerNodePort() {
        return TrackerNodePort;
    }

    /**
     * setter for the attribute TrackerNodePort.
     * @param trackerNodePort
     */
    public void setTrackerNodePort(int trackerNodePort) {
        TrackerNodePort = trackerNodePort;
    }
}
