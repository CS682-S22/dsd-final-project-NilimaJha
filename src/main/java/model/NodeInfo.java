package model;

/**
 * Class to keep the information of the node.
 * @author nilimajha
 */
public class NodeInfo {
    private String name;
    private String ip;
    private int port;

    /**
     * Constructor
     * @param name
     * @param ip
     * @param port
     */
    public NodeInfo(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    /**
     * getter for the attribute name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * getter for the attribute ip.
     * @return ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * getter for the attribute port
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * setter for the attribute name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * setter for the attribute ip.
     * @param ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * setter for the attribute port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
