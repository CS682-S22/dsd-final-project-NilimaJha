package tracker;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import customeException.ConnectionClosedException;
import model.Connection;
import model.Metadata;
import model.NodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.*;

import java.util.List;

/**
 *
 * @author nilimajha
 */
public class TrackerConnectionHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(TrackerConnectionHandler.class);
    private Connection connection;
    private String connectionWith;
    private NodeInfo thisTrackerInfo;
    private Metadata metadata;

    /**
     * Constructor
     * @param connection
     * @param trackerInfo
     */
    public TrackerConnectionHandler(Connection connection, NodeInfo trackerInfo) {
        this.connection = connection;
        this.thisTrackerInfo = trackerInfo;
        this.metadata = Metadata.getMetadata();
    }

    /**
     * receives the request message from the other end and
     * sends back the appropriate response over the same connection
     * and closes the connection.
     */
    public void start() {
        while (connection.isConnected()) {
            try {
                byte[] receivedRequest = connection.receive();
                if (receivedRequest != null) {
                    try {
                        Any any = Any.parseFrom(receivedRequest);
                        if (any.is(RegisterMessage.RegisterMessageDetails.class)) {
                            RegisterMessage.RegisterMessageDetails registerMessage =
                                    any.unpack(RegisterMessage.RegisterMessageDetails.class);
                            connectionWith = registerMessage.getSenderName();
                            // add peer in peer list.
                            metadata.addNewPeer(registerMessage.getSenderName(),
                                    registerMessage.getSenderIp(),
                                    registerMessage.getSenderPort());
                            // create file entry in file list.
                            if (registerMessage.getNumberOfFilesAvailable() > 0) {
                                List<ByteString> availableFileList = registerMessage.getFileInfoList();
                                for (ByteString eachFileInfoByteString : availableFileList) {
                                    Any eachFileInfo = Any.parseFrom(eachFileInfoByteString.toByteArray());
                                    if (eachFileInfo.is(FileInfo.FileInfoDetails.class)) {
                                        FileInfo.FileInfoDetails fileInfoDetails = eachFileInfo.unpack(FileInfo.FileInfoDetails.class);
                                        metadata.addFile(fileInfoDetails.getFileName(),
                                                fileInfoDetails.getFileSize(),
                                                fileInfoDetails.getChecksum().toByteArray(),
                                                fileInfoDetails.getTotalPackets());
                                        // adding this member in the current file swarm.
                                        metadata.addMemberToTheSwarm(fileInfoDetails.getFileName(), connectionWith,
                                                true);
                                    }
                                }
                            }
                            logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                                    "] Received register request from " + connectionWith +
                                    " of type RequestLeaderAndMembersInfo.");
                            Any registerResponse = Any.pack(RegisterResponse.RegisterResponseDetails.newBuilder()
                                    .setSenderName(thisTrackerInfo.getName())
                                    .build());
                            connection.send(registerResponse.toByteArray());
                        } else if (any.is(SetupForFileMessage.SetupForFileMessageDetails.class)) {
                            SetupForFileMessage.SetupForFileMessageDetails setupMessage = any.unpack(SetupForFileMessage.SetupForFileMessageDetails.class);
                            connectionWith = setupMessage.getSenderName();
                            Any setupResponse = Any.pack(SetupForFileResponse.SetupForFileResponseDetails.newBuilder()
                                    .setSenderName(thisTrackerInfo.getName())
                                    .build());
                            connection.send(setupResponse.toByteArray());
                        } else if (any.is(RequestFileInfo.RequestFileInfoDetails.class)) {
                            RequestFileInfo.RequestFileInfoDetails requestMessage =
                                    any.unpack(RequestFileInfo.RequestFileInfoDetails.class);
                            // extract file name from request message
                            byte[] responseMessage = getResponseForRequestFileDetail(requestMessage);
//                            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Received request from "
//                                    + connectionWith + " to update Leader info.");
                            connection.send(responseMessage);
                        } else if (any.is(UpdateFilePacketMetadataMetadata.UpdateFilePacketMetadataDetails.class)) {
                            UpdateFilePacketMetadataMetadata.UpdateFilePacketMetadataDetails updateRequest =
                                    any.unpack(UpdateFilePacketMetadataMetadata.UpdateFilePacketMetadataDetails.class);
                            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Received request from "
                                    + connectionWith + " to update membership Information at load-balancer");
                            metadata.updateMemberAvailablePacketInfo(updateRequest.getFileName(), connectionWith,
                                    updateRequest.getPacketNumber());
                            Any updateSuccessful = Any.pack(UpdateFilePacketMetadataSuccessful
                                    .UpdateFilePacketMetadataSuccessfulDetails.newBuilder()
                                    .setFileName(updateRequest.getFileName())
                                    .setPacketNumber(updateRequest.getPacketNumber())
                                    .build());
                            connection.send(updateSuccessful.toByteArray());
                        }
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("\n[ThreadId : " + Thread.currentThread().getId() + " InvalidProtocolBufferException " +
                                "occurred decoding message received at loadBalancer. Error Message : "
                                + e.getMessage());
                    }
                }
            } catch (ConnectionClosedException e) {
                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Closing the connection");
                connection.closeConnection();
            }
        }
    }

    /**
     * get requested info from metadata and prepare appropriate response.
     * @param requestMessage
     * @return fileMetadata
     */
    public byte[] getResponseForRequestFileDetail(RequestFileInfo.RequestFileInfoDetails requestMessage) {
        // check if file is available at any host.
        List<ByteString> allPeerInfoByteList;
        if (!requestMessage.getDetailForPacket()) {
            allPeerInfoByteList = metadata.getAllPeerInfoOfASwarm(requestMessage.getFileName());
        } else {
            allPeerInfoByteList = metadata.getPeerInfoOfASwarmForPacket(requestMessage.getFileName(), requestMessage.getPacketNumber());
        }
        Any fileMetadata;
        if (allPeerInfoByteList.isEmpty()) {
            // file info is not available
            if (!requestMessage.getDetailForPacket()) {
                fileMetadata = Any.pack(FileMetadata.FileMetadataDetails.newBuilder()
                        .setFileInfoAvailable(false)
                        .build());
            } else {
                fileMetadata = Any.pack(FileMetadata.FileMetadataDetails.newBuilder()
                        .setFileInfoAvailable(false)
                        .setFileInfoAvailable(false)
                        .build());
            }
        } else {
            if (!requestMessage.getDetailForPacket()) {
                // file info is available
                fileMetadata = Any.pack(FileMetadata.FileMetadataDetails.newBuilder()
                        .setFileInfoAvailable(true)
                        .setFileName(requestMessage.getFileName())
                        .setFileSize(metadata.getFileSize(requestMessage.getFileName()))
                        .setChecksum(ByteString.copyFrom(metadata.getChecksum(requestMessage.getFileName())))
                        .setTotalNumberOfPackets(metadata.getTotalNumberOfPackets(requestMessage.getFileName()))
                        .addAllSwarmMemberInfo(allPeerInfoByteList)
                        .build());
            } else {
                // file info is available
                fileMetadata = Any.pack(FileMetadata.FileMetadataDetails.newBuilder()
                        .setFileInfoAvailable(true)
                        .setPacketInfoAvailable(true)
                        .setFileName(requestMessage.getFileName())
                        .setFileSize(metadata.getFileSize(requestMessage.getFileName()))
                        .setChecksum(ByteString.copyFrom(metadata.getChecksum(requestMessage.getFileName())))
                        .setTotalNumberOfPackets(metadata.getTotalNumberOfPackets(requestMessage.getFileName()))
                        .addAllSwarmMemberInfo(allPeerInfoByteList)
                        .build());
            }

        }
        return fileMetadata.toByteArray();
    }

    /**
     * run method calls start method.
     */
    @Override
    public void run() {
        start();
    }
}
