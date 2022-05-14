package host;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import connection.Connection;
import customeException.ConnectionClosedException;
import model.NodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.SetupForFileResponse;
import utility.Utility;

/**
 * Class keeps the connection with the tracker node and
 * has a method which is used to make a request to the tracker
 * over the connection and receive the response.
 * @author nilimajha
 */
public class ConnectionWithTracker {
    private static final Logger logger = LogManager.getLogger(ConnectionWithTracker.class);
    private NodeInfo trackerNodeInfo;
    private Connection trackerConnection;
    private Object waitObj = new Object();

    /**
     * Constructor
     * @param trackerNodeInfo
     */
    public ConnectionWithTracker(NodeInfo trackerNodeInfo, Connection connectionWithTracker) {
        this.trackerNodeInfo = trackerNodeInfo;
        this.trackerConnection = connectionWithTracker;
    }

    /**
     * sends the request message to the tracker node over the connection and
     * waits to receive the response and returns the response.
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
                            try {
                                Any setupResponse = Any.parseFrom(response);
                                if (setupResponse.is(SetupForFileResponse.SetupForFileResponseDetails.class)) {
                                    SetupForFileResponse.SetupForFileResponseDetails setupForFileResponse =
                                            setupResponse.unpack(SetupForFileResponse.SetupForFileResponseDetails.class);
                                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] added :" + setupForFileResponse.getAdded());
                                    if (!setupForFileResponse.getAdded()) {
                                        response = null;
                                        synchronized (waitObj) {
                                            try {
                                                waitObj.wait(500);
                                            } catch (InterruptedException e) {
                                                logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                                                        "] InterruptedException occurred. Error Message : "
                                                        + e.getMessage());
                                            }
                                        }
                                        trackerConnection.send(requestMessage);
                                    }
                                }
                            } catch (InvalidProtocolBufferException e) {
                                logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                                        "] InvalidProtocolBufferException occurred. Error Message : " + e.getMessage());
                            }
                        }
                    }
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Received Response. from tracker Node.");
                } catch (ConnectionClosedException e) {
                    e.printStackTrace();
                    trackerConnection.closeConnection();
                }
            } else {
                // connect to the tracker node
                while (!trackerConnection.isConnected()) {
                    try {
                        trackerConnection = Utility.establishConnection(
                                trackerNodeInfo.getIp(),
                                trackerNodeInfo.getPort(),
                                trackerConnection.isDelay(),
                                trackerConnection.getMaxDelay());
                    } catch (ConnectionClosedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return response;
    }

    /**
     * method used to close the tracker connection.
     * @return
     */
    public synchronized boolean closeConnection() {
        if (trackerConnection != null && trackerConnection.isConnected()) {
            logger.info("\nClosing the tracker connection.");
            trackerConnection.closeConnection();
        }
        return true;
    }

    /**
     * method returns the status of the trackerConnection.
     * @return
     */
    public boolean isConnectedWithTracker() {
        return trackerConnection != null && trackerConnection.isConnected();
    }
}
