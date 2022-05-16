package host;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import connection.Connection;
import customeException.ConnectionClosedException;
import swarm.AllSwarms;
import model.NodeInfo;
import swarm.SwarmMemberDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.DownloadRequest;
import proto.DownloadRequestResponse;
import proto.InitialMessage;
import proto.InitialSetupDoneAck;
import utility.Utility;

/**
 * Class to handle connection requested by other host.
 *
 * @author nilimajha
 */
public class HostRequestProcessor implements Runnable {
    private static final Logger logger = LogManager.getLogger(HostRequestProcessor.class);
    private NodeInfo thisNodeInfo;
    private NodeInfo peerNodeInfo = null;
    private String fileName;
    private Connection connectionWithPeer;
    private AllSwarms allSwarms;

    /**
     * Constructor
     *
     * @param thisNodeInfo
     * @param newConnection
     */
    public HostRequestProcessor(NodeInfo thisNodeInfo, Connection newConnection) {
        this.thisNodeInfo = thisNodeInfo;
        this.connectionWithPeer = newConnection;
        this.allSwarms = AllSwarms.getAllSwarm(thisNodeInfo);
    }

    /**
     * method calls start method.
     */
    @Override
    public void run() {
        start();
    }

    /**
     * method waits for the initial setup message from the peer to do basic initial setup
     * then calls handlePeer method to serve the requests from this peer over this connection.
     */
    public void start() {
        // start receiving message
        while (peerNodeInfo == null) {
            logger.info("\nWaiting for InitialMessage.");
            try {
                byte[] receivedMessage = connectionWithPeer.receive();
                if (receivedMessage != null) { // received initial message
                    // call decode packet and then call decode message inside
                    /*TODO: */
                    logger.info("\nReceived something.");
                    try {
                        Any any = Any.parseFrom(receivedMessage);
                        logger.info("\nReceived something. :" + any.is(InitialMessage.InitialMessageDetails.class));
                        if (any.is(InitialMessage.InitialMessageDetails.class)) {
                            logger.info("\nReceived Initial Message.");
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
     * decode InitialSetupMessage, updates necessary attributes and sends back the response.
     * also sets up another connection with this peer and adds it to the swarm,
     * if it is also still downloading the same file.
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

                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                            "] Serving Connection with " + initialMessageDetails.getPeerName() +
                            " for download Request of file " + fileName);

                    // send initial setup ack
                    try {
                        connectionWithPeer.send(getInitialSetupACK());
                    } catch (ConnectionClosedException e) {
                        logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] " + e.getMessage());
                        connectionWithPeer.closeConnection();
                    }

                    // check is you are downloading this file
                    if (allSwarms.fileAvailable(fileName)
                            && allSwarms.isBeingDownloaded(fileName)
                            && !allSwarms.peerAvailableInFileSwarm(fileName, peerNodeInfo.getName())) {
                        try {
                            Connection peerConnection = Utility.establishConnection(initialMessageDetails.getPeerIp(),
                                    initialMessageDetails.getPeerPort(), connectionWithPeer.isDelay(), connectionWithPeer.getMaxDelay());
                            if (peerConnection != null) {
                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                                        "] Establishing another connection with " + peerNodeInfo.getName() +
                                        " to download file " + fileName);

                                // do initial setup
                                Any initialMessage = Any.pack(InitialMessage.InitialMessageDetails
                                        .newBuilder()
                                        .setPeerName(thisNodeInfo.getName())
                                        .setPeerIp(thisNodeInfo.getIp())
                                        .setPeerPort(thisNodeInfo.getPort())
                                        .setFileToDownload(fileName)
                                        .build());
                                peerConnection.send(initialMessage.toByteArray());
                                byte[] response = null;
                                while (response == null) {
                                    response = peerConnection.receive();
                                }
                                Any initialMessageResponse = Any.parseFrom(response);
                                if (initialMessageResponse.is(InitialSetupDoneAck.InitialSetupDoneAckDetail.class)) {
                                    InitialSetupDoneAck.InitialSetupDoneAckDetail initialSetupDoneAckDetail
                                            = initialMessageResponse.unpack(
                                                    InitialSetupDoneAck.InitialSetupDoneAckDetail.class);
                                }

                                // adding new peer in the fileSwarm.
                                SwarmMemberDetails newPeer = new SwarmMemberDetails(
                                        peerNodeInfo.getName(),
                                        peerNodeInfo.getIp(),
                                        peerNodeInfo.getPort(),
                                        peerConnection);
                                allSwarms.addNewPeerInTheFileSwarm(initialMessageDetails.getFileToDownload(), newPeer);
                            }
                        } catch (ConnectionClosedException e) {
                            logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] " + e.getMessage());
                        }
                    }
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
                logger.error("\n[ThreadId: " + Thread.currentThread().getId() +
                        "] InvalidProtocolBufferException occurred. Error Message : " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * method creates and returns the ack for the initial setup message.
     * @return initialSetupDoneAck byte array
     */
    public byte[] getInitialSetupACK() {
        Any initialSetupDoneAck = Any.pack(InitialSetupDoneAck.InitialSetupDoneAckDetail.newBuilder()
                .setSuccessful(true)
                .setFileToDownload(fileName)
                .build());
        return initialSetupDoneAck.toByteArray();
    }

    /**
     * method continuously waits to receive request message from the peer over the connection
     * and sends back appropriate response.
     */
    public void handlePeer() {
        while (connectionWithPeer.connectionIsOpen()) {
            try {
                byte[] message = connectionWithPeer.receive();
                if (message != null) {
                    try {
                        Any any = Any.parseFrom(message);
                        if (any.is(DownloadRequest.DownloadRequestDetail.class)) {
                            DownloadRequest.DownloadRequestDetail downloadRequestMessage =
                                    any.unpack(DownloadRequest.DownloadRequestDetail.class);
                            logger.info("\n[ThreadId: " + Thread.currentThread().getId() +
                                    "] Received Download Request for packet " + downloadRequestMessage.getPacketNumber()
                                    + " of file " + fileName + " from " + peerNodeInfo.getName());
                            // getting packet data of the file from the allSwarm.
                            byte[] packetData = allSwarms.getPacketDataFromFile(downloadRequestMessage.getFileName(),
                                    downloadRequestMessage.getPacketNumber());
                            Any responseMessage = null;
                            // create response message.
                            if (packetData == null) {
                                // packet data is not available.
                                responseMessage = Any.pack(DownloadRequestResponse.DownloadRequestResponseDetail
                                        .newBuilder()
                                        .setFileName(downloadRequestMessage.getFileName())
                                        .setPacketNumber(downloadRequestMessage.getPacketNumber())
                                        .setDataAvailable(false)
                                        .build());
                            } else {
                                // packet data is available.
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
                        logger.error("\n[ThreadId: " + Thread.currentThread().getId() +
                                "] InvalidProtocolBufferException occurred while decoding publish message." +
                                " Error Message : " + e.getMessage());
                    }
                }
            } catch (ConnectionClosedException e) {
                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] " + e.getMessage());
                connectionWithPeer.closeConnection();
            }
        }
    }
}
