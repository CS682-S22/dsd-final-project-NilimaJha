package peer;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import customeException.ConnectionClosedException;
import model.AllSwarms;
import model.Connection;
import model.NodeInfo;
import model.SwarmMemberDetails;
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
        logger.info("\nAssigned new connection to the request processor.");
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
            logger.info("\nWaiting for InitialMessage.");
            try {
                byte[] receivedMessage = connectionWithPeer.receive();
                if (receivedMessage != null) { // received initial message
                    // call decode packet and then call decode message inside
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
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] InitialMessageReceived from " + peerNodeInfo.getName() +
                            " regarding file " + fileName);

                    // send initial setup ack
                    try {
                        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Sending initialSetUpAck...");
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
                                    initialMessageDetails.getPeerPort());
                            if (peerConnection != null) {
                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Sending connection to " + peerNodeInfo.getName());

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
                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] initial response message received from " + peerNodeInfo.getName() + " for file " + fileName);
                                Any initialMessageResponse = Any.parseFrom(response);
                                if (initialMessageResponse.is(InitialSetupDoneAck.InitialSetupDoneAckDetail.class)) {
                                    InitialSetupDoneAck.InitialSetupDoneAckDetail initialSetupDoneAckDetail
                                            = initialMessageResponse.unpack(InitialSetupDoneAck.InitialSetupDoneAckDetail.class);
                                }

                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Adding new member in the swarm of file " + fileName);
                                SwarmMemberDetails newPeer = new SwarmMemberDetails(
                                        peerNodeInfo.getName(),
                                        peerNodeInfo.getIp(),
                                        peerNodeInfo.getPort(),
                                        peerConnection);
                                allSwarms.addNewPeerInTheFileSwarm(initialMessageDetails.getFileToDownload(), newPeer);
                            }
                        } catch (ConnectionClosedException e) {
                            e.printStackTrace();
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
        logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] Handle Peer.");
        while (connectionWithPeer.connectionIsOpen()) {
            logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] Here 1.");
            try {
                byte[] message = connectionWithPeer.receive();
                if (message != null) {
                    logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] Received something.");
                    try {
                        Any any = Any.parseFrom(message);
                        if (any.is(DownloadRequest.DownloadRequestDetail.class)) {
                            DownloadRequest.DownloadRequestDetail downloadRequestMessage =
                                    any.unpack(DownloadRequest.DownloadRequestDetail.class);
                            logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] Received Request is of type Download Request.");
                            // get packet data from the file in the allSwarm
                            logger.info("\n[ThreadId: " + Thread.currentThread().getId() + "] Getting packet details from file.");
                            byte[] packetData = allSwarms.getPacketDataFromFile(downloadRequestMessage.getFileName(),
                                    downloadRequestMessage.getPacketNumber());
                            Any responseMessage = null;
                            // create response message.
                            if (packetData == null) {
                                logger.info("\npacket Data is not available..");
                                responseMessage = Any.pack(DownloadRequestResponse.DownloadRequestResponseDetail
                                        .newBuilder()
                                        .setFileName(downloadRequestMessage.getFileName())
                                        .setPacketNumber(downloadRequestMessage.getPacketNumber())
                                        .setDataAvailable(false)
                                        .build());
                            } else {
                                logger.info("\npacket Data is not available.." + downloadRequestMessage.getPacketNumber());
                                responseMessage = Any.pack(DownloadRequestResponse.DownloadRequestResponseDetail
                                        .newBuilder()
                                        .setFileName(downloadRequestMessage.getFileName())
                                        .setPacketNumber(downloadRequestMessage.getPacketNumber())
                                        .setDataAvailable(true)
                                        .setPacketData(ByteString.copyFrom(packetData))
                                        .build());
                            }
                            logger.info("\nSending DownloadRequestResponse...");
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
