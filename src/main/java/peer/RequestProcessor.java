package peer;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import customeException.ConnectionClosedException;
import model.AllSwarms;
import model.Connection;
import model.NodeInfo;
import model.PeerNodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.DownloadRequest;
import proto.DownloadRequestResponse;
import proto.InitialMessage;
import proto.InitialSetupDoneAck;
import utility.Utility;

/**
 *
 * @author nilimajha
 */
public class RequestProcessor implements Runnable {
    private static final Logger logger = LogManager.getLogger(RequestProcessor.class);
    private NodeInfo thisNodeInfo;
    private NodeInfo peerNodeInfo = null;
    private String fileName;
    private Connection connectionWithPeer;
    private AllSwarms allSwarms;

    /**
     *
     * @param thisNodeInfo
     * @param newConnection
     */
    public RequestProcessor(NodeInfo thisNodeInfo, Connection newConnection) {
        this.thisNodeInfo = thisNodeInfo;
        this.connectionWithPeer = newConnection;
        this.allSwarms = AllSwarms.getAllSwarm(thisNodeInfo);
    }

    /**
     *
     */
    @Override
    public void run() {
        start();
    }

    /**
     * start receiving message from the connection and send response accordingly
     */
    public void start() {
        // start receiving message
        while (peerNodeInfo == null) {
            try {
                byte[] receivedMessage = connectionWithPeer.receive();
                if (receivedMessage != null) { // received initial message
                    // call decode packet and then call decode message inside
                    try {
                        Any any = Any.parseFrom(receivedMessage);
                        if (any.is(InitialMessage.InitialMessageDetails.class)) {
                            parseInitialMessage(any);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("\n[ThreadId: " + Thread.currentThread().getId() + "] InvalidProtocolBufferException occurred decoding Initial Packet. Error Message : " + e.getMessage());
                    }
                }
            } catch (ConnectionClosedException e) {
                logger.info(e.getMessage());
                //close the connection.
                connectionWithPeer.closeConnection(); //if connection is closed by other end before sending initial message then close connection.
            }
        }

        handlePeer();
    }

    /**
     * decode InitialSetupMessage updates necessary attributes and sends back the response.
     * @param any
     */
    public boolean parseInitialMessage(Any any) {
        if (any.is(InitialMessage.InitialMessageDetails.class)) {
            // decode received message
            try {
                InitialMessage.InitialMessageDetails initialMessageDetails =
                        any.unpack(InitialMessage.InitialMessageDetails.class);
                if (peerNodeInfo == null) {
                    peerNodeInfo = new NodeInfo(
                            initialMessageDetails.getPeerName(),
                            initialMessageDetails.getPeerIp(),
                            initialMessageDetails.getPeerPort());
                    fileName = initialMessageDetails.getFileToDownload();

                    // check is you are downloading this file
                    if (allSwarms.fileAvailable(fileName)
                            && allSwarms.isBeingDownloaded(fileName)
                            && !allSwarms.peerAvailableInFileSwarm(fileName, peerNodeInfo.getName())) {
                        try {
                            Connection connection = Utility.establishConnection(initialMessageDetails.getPeerIp(),
                                    initialMessageDetails.getPeerPort());
                            if (connection != null) {
                                PeerNodeInfo newPeer = new PeerNodeInfo(peerNodeInfo.getName(), peerNodeInfo.getIp(),
                                        peerNodeInfo.getPort(), connection);
                                allSwarms.addNewPeerInTheFileSwarm(initialMessageDetails.getFileToDownload(), newPeer);
                            }
                        } catch (ConnectionClosedException e) {
                            e.printStackTrace();
                        }
                    }
                    // yes -> check is peer already exist
                        // yes -> do not add this peer again.
                        // no  -> create another connection with this peer and add this peer into the swarm
                    // no -> do not add this peer.
                    // start listening for the incoming request.
                } else {
                    try {
                        // send initial setup ack
                        connectionWithPeer.send(getInitialSetupACK());
                    } catch (ConnectionClosedException e) {
                        logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] " + e.getMessage());
                        connectionWithPeer.closeConnection();
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                logger.error("\n[ThreadId: " + Thread.currentThread().getId() + "] InvalidProtocolBufferException occurred. Error Message : " + e.getMessage());
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    public byte[] getInitialSetupACK() {
        Any initialSetupDoneAck = Any.pack(InitialSetupDoneAck.InitialSetupDoneAckDetail.newBuilder()
                .setSuccessful(true)
                .setFileToDownload(fileName)
                .build());
        return initialSetupDoneAck.toByteArray();
    }

    /**
     *
     */
    public void handlePeer() {
        logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] Handle Producer.");
        while (connectionWithPeer.connectionIsOpen()) {
            try {
                byte[] message = connectionWithPeer.receive();
                if (message != null) {
                    try {
                        Any any = Any.parseFrom(message);
                        if (any.is(DownloadRequest.DownloadRequestDetail.class)) {
                            DownloadRequest.DownloadRequestDetail downloadRequestMessage =
                                    any.unpack(DownloadRequest.DownloadRequestDetail.class);
                            // get packet data from the file in the allSwarm
                            byte[] packetData = allSwarms.getPacketDataFromFile(downloadRequestMessage.getFileName(),
                                    downloadRequestMessage.getPacketNumber());
                            Any responseMessage = null;
                            // create response message.
                            if (packetData == null) {
                                responseMessage = Any.pack(DownloadRequestResponse.DownloadRequestResponseDetail
                                        .newBuilder()
                                        .setFileName(downloadRequestMessage.getFileName())
                                        .setPacketNumber(downloadRequestMessage.getPacketNumber())
                                        .setDataAvailable(false)
                                        .build());
                            } else {
                                responseMessage = Any.pack(DownloadRequestResponse.DownloadRequestResponseDetail
                                        .newBuilder()
                                        .setFileName(downloadRequestMessage.getFileName())
                                        .setPacketNumber(downloadRequestMessage.getPacketNumber())
                                        .setDataAvailable(true)
                                        .setPacketData(ByteString.copyFrom(packetData))
                                        .build());
                            }
                            connectionWithPeer.send(responseMessage.toByteArray());
                        }
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("\n[ThreadId: " + Thread.currentThread().getId() + "] InvalidProtocolBufferException occurred while decoding publish message. Error Message : " + e.getMessage());
                    }
                }
            } catch (ConnectionClosedException e) {
                logger.info(e.getMessage());
                connectionWithPeer.closeConnection();
            }
        }
    }
}
