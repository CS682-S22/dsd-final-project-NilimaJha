package model;

import customeException.ConnectionClosedException;
import utility.Utility;

/**
 *
 * @author nilimajha
 */
public class ConnectionWithTracker {
    private NodeInfo trackerNodeInfo;
    private Connection trackerConnection;
//    private static ConnectionWithTracker connectionWithTracker = null;

//    /**
//     * Constructor
//     * @param trackerNodeInfo
//     */
//    private ConnectionWithTracker(NodeInfo trackerNodeInfo) {
//        this.trackerNodeInfo = trackerNodeInfo;
//    }

    /**
     * Constructor
     * @param trackerNodeInfo
     */
    public ConnectionWithTracker(NodeInfo trackerNodeInfo, Connection connectionWithTracker) {
        this.trackerNodeInfo = trackerNodeInfo;
        this.trackerConnection = connectionWithTracker;
    }

//    /**
//     *
//     * @param trackerNodeInfo
//     * @return
//     */
//    public static ConnectionWithTracker getConnectionWithTracker(NodeInfo trackerNodeInfo) {
//        if (connectionWithTracker == null) {
//            connectionWithTracker = new ConnectionWithTracker(trackerNodeInfo);
//        }
//        return connectionWithTracker;
//    }

    /**
     *
     * @param trackerConnection
     */
    public void setTrackerConnection(Connection trackerConnection) {
        this.trackerConnection = trackerConnection;
    }

    /**
     * sends the request message to the tracker node over the connection.
     * @param requestMessage
     * @return
     */
    public synchronized byte[] makeRequestToTracker(byte[] requestMessage) {
        byte[] response = null;
        while(response == null) {
            if (trackerConnection.isConnected()) {
                try {
                    trackerConnection.send(requestMessage);
                    while (response == null) {
                        response = trackerConnection.receive();
                        if (response != null) {
                            break;
                        }
                    }
                } catch (ConnectionClosedException e) {
                    e.printStackTrace();
                    trackerConnection.closeConnection();
                }
            } else {
                // connect to the tracker node
                while (!trackerConnection.isConnected()) {
                    try {
                        trackerConnection = Utility.establishConnection(trackerNodeInfo.getIp(), trackerNodeInfo.getPort());
                    } catch (ConnectionClosedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return response;
    }

    /**
     *
     * @return
     */
    public boolean isConnectedWithTracker() {
        return trackerConnection != null && trackerConnection.isConnected();
    }
}
