package peer;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import customeException.ConnectionClosedException;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.*;
import utility.Constants;
import utility.Utility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author nilimajha
 */
public class Host implements Runnable {
    private static final Logger logger = LogManager.getLogger(Host.class);
    private NodeInfo thisNodeInfo;
    private NodeInfo trackerNodeInfo;
    private List<String> availableFileNames;
    private boolean shutdown;
    private AllSwarms allSwarms;
    private volatile boolean registered;
    private ExecutorService threadPool = Executors.newFixedThreadPool(Constants.PEER_THREAD_POOL_SIZE); //thread pool of size 15

    /**
     *
     * @param thisNodeName
     * @param thisNodeIp
     * @param thisNodePort
     * @param trackerNodeName
     * @param trackerNodeIp
     * @param trackerNodePort
     * @param availableFileNames
     */
    public Host(String thisNodeName, String thisNodeIp, int thisNodePort, String trackerNodeName,
                String trackerNodeIp, int trackerNodePort, List<String> availableFileNames) {
        this.thisNodeInfo = new NodeInfo(thisNodeName, thisNodeIp, thisNodePort);
        this.trackerNodeInfo = new NodeInfo(trackerNodeName, trackerNodeIp, trackerNodePort);
        logger.info("\nNode : " + thisNodeName + ", " + thisNodeIp + ", " + thisNodePort);
        logger.info("\nNode2 : " + thisNodeInfo.getName() + ", " + thisNodeInfo.getIp() + ", " + thisNodeInfo.getPort());
        logger.info("\nTrackerNode : " + trackerNodeName + ", " + trackerNodeIp + ", " + trackerNodePort);
        logger.info("\nTrackerNode2 : " + trackerNodeInfo.getName() + ", " + trackerNodeInfo.getIp() + ", " + trackerNodeInfo.getPort());
        this.availableFileNames = availableFileNames;
        this.allSwarms = AllSwarms.getAllSwarm(thisNodeInfo);
    }

    /**
     * run opens a serverSocket and keeps listening for
     * new connection request from producer or consumer.
     * once it receives a connection request it creates a
     * connection object and hands it to the broker.RequestProcessor class object.
     */
    @Override
    public void run() {
        threadPool.execute(this::initialSetup);
        AsynchronousServerSocketChannel serverSocket = null;
        try {
            serverSocket = AsynchronousServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(thisNodeInfo.getIp(), thisNodeInfo.getPort()));
            // keeps on running when shutdown is false
            while (!shutdown) {
                logger.info("\n[Peer : " + thisNodeInfo.getName() + " Host is listening on IP : "
                        + thisNodeInfo.getIp() + " & Port : " + thisNodeInfo.getPort());
                Future<AsynchronousSocketChannel> acceptFuture = serverSocket.accept();
                AsynchronousSocketChannel socketChannel = null;

                try {
                    socketChannel = acceptFuture.get();
                    if (shutdown) {
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                            "] Exception while establishing connection. Error Message : " + e.getMessage());
                }

                //checking if the socketChannel is valid.
                if ((socketChannel != null) && (socketChannel.isOpen())) {
                    Connection newConnection = null;
                    newConnection = new Connection(socketChannel);
                    // give this connection to requestProcessor
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Received new Connection.");
                    RequestProcessor requestProcessor = new RequestProcessor(thisNodeInfo, newConnection);
                    threadPool.execute(requestProcessor);
                }
            }
        } catch (IOException e) {
            logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                    "] IOException while opening serverSocket connection. Error Message : " + e.getMessage());
        }
    }

    /**
     * connects to loadBalancer and gets the leader and member's info.
     * connects to all the member and updates its membership table.
     * it there is no member in the membership table then it registers itself as the leader.
     */
    public void initialSetup() {
        // for all the files available create a swarm.
        createSwarmForEachAvailableFile();
        // connect to trackerNode and do initial registration.
        connectAndRegisterToTrackerNode();
        // set trackerConnection in ConnectionWithTracker class.
//        connectionWithTracker.setTrackerConnection(trackerConnection);
        // application layer will start download of file to be downloaded.
    }

    /**
     *
     * @return
     */
    public boolean connectAndRegisterToTrackerNode() {
        logger.info("\nInside connectAndRegisterToTrackerNode()");
        Connection trackerConnection = null;
        while(trackerConnection == null) {
            try {
                logger.info("\nTrackerConnection is null...");
                trackerConnection = Utility.establishConnection(trackerNodeInfo.getIp(), trackerNodeInfo.getPort());
            } catch (ConnectionClosedException e) {
                e.printStackTrace();
            }
        }
        if (trackerConnection.isConnected()) {
            // register itself
            List<ByteString> listOfEachFileInfo = new ArrayList<>();
            if (availableFileNames != null) {
                for (String eachFile : availableFileNames) {
                    Any eachFileInfo = Any.pack(FileInfo.FileInfoDetails.newBuilder()
                            .setFileName(eachFile)
                            .setFileSize(allSwarms.getFileSize(eachFile))
                            .setChecksum(ByteString.copyFrom(allSwarms.getFileChecksum(eachFile)))
                            .setTotalPackets(allSwarms.getFileTotalPackets(eachFile))
                            .build());
                    listOfEachFileInfo.add(eachFileInfo.toByteString());
                }
            }
            logger.info("\nPreparing RegisterMessage for the tracker.");
            Any registerMessage = Any.pack(RegisterMessage.RegisterMessageDetails.newBuilder()
                    .setSenderName(thisNodeInfo.getName())
                    .setSenderIp(thisNodeInfo.getIp())
                    .setSenderPort(thisNodeInfo.getPort())
                    .setNumberOfFilesAvailable(listOfEachFileInfo.size())
                    .addAllFileInfo(listOfEachFileInfo)
                    .build());

            try {
                boolean registrationSuccessful = false;
                while (!registrationSuccessful) {
                    trackerConnection.send(registerMessage.toByteArray());
                    byte[] response = null;
                    while (response == null) {
                        response = trackerConnection.receive();
                    }
                    try {
                        Any registerResponse = Any.parseFrom(response);
                        if (registerResponse.is(RegisterResponse.RegisterResponseDetails.class)) {
                            logger.info("\nReceived ResponseMessage for the RegisterMessage from the tracker.");
                            registrationSuccessful = true;
                            registered = true;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("\nInvalidProtocolBufferException occurred. Error Message : " + e.getMessage());
                    }
                }
            } catch (ConnectionClosedException e) {
                connectAndRegisterToTrackerNode();
            }
        }
        logger.info("\nClosing the Connection with Tracker...");
        trackerConnection.closeConnection();
        return registered;
    }

    /**
     *
     */
    public void createSwarmForEachAvailableFile() {
        logger.info("\nCreating Swarm for each available file.");
        if (availableFileNames != null) {
            for (String eachFile : availableFileNames) {
                try {
                    Path path = Paths.get(eachFile);
                    long fileSize = Files.size(path);
                    File file = new File(eachFile, fileSize, Utility.createChecksum(eachFile),
                            Utility.getTotalPacketOfFile(fileSize), true);
                    allSwarms.addNewFileSwarm(eachFile, file);
                    logger.info("\nSuccessfully Created Swarm for file " + eachFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * @param fileName
     */
    public void download(String fileName) {
        logger.info("\n Starting to download file  :" + fileName);
        FileDownloader fileDownloader = new FileDownloader(fileName, trackerNodeInfo);
    }

    /**
     *
     * @author nilimajha
     */
    public class FileDownloader {
        private String fileName;
        private NodeInfo trackerNodeInfo;
        private AllSwarms allSwarms = AllSwarms.getAllSwarm(thisNodeInfo);
        private ConnectionWithTracker connectionWithTracker;

        /**
         * Constructor
         * @param fileName
         */
        public FileDownloader(String fileName, NodeInfo trackerNodeInfo) {
            this.fileName = fileName;
            this.trackerNodeInfo = trackerNodeInfo;
            // create connection with tracker and store its information in connections with tracker class
            connectToTracker();
            startThreadToDownload();
        }

        /**
         *
         * @return
         */
        public boolean connectToTracker() {
            Object waitObj = new Object();
            while (!registered) {
                synchronized (waitObj) {
                    try {
                        waitObj.wait(Constants.RETRIES_TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            while (this.connectionWithTracker == null || !this.connectionWithTracker.isConnectedWithTracker()) {
                try {
                    Connection connection = Utility.establishConnection(trackerNodeInfo.getIp(), trackerNodeInfo.getPort());
                    if (connection != null) {
                        this.connectionWithTracker = new ConnectionWithTracker(trackerNodeInfo, connection);
                    }
                } catch (ConnectionClosedException e) {
                    e.printStackTrace();
                }
            }
            if (this.connectionWithTracker != null || this.connectionWithTracker.isConnectedWithTracker()) {
                boolean setUpDone = false;
                Any any = Any.pack(SetupForFileMessage.SetupForFileMessageDetails.newBuilder()
                        .setSenderName(thisNodeInfo.getName())
                        .setSenderIp(thisNodeInfo.getIp())
                        .setSenderPort(thisNodeInfo.getPort())
                        .setFileToBeDownloaded(fileName)
                        .build());
                while (!setUpDone) {
                    if (connectionWithTracker != null || connectionWithTracker.isConnectedWithTracker()) {
                        byte[] response = connectionWithTracker.makeRequestToTracker(any.toByteArray());
                        try {
                            Any responseAny = Any.parseFrom(response);
                            if (responseAny.is(SetupForFileResponse.SetupForFileResponseDetails.class)) {
                                setUpDone = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    } else {
                        connectToTracker();
                    }
                }
            }
            return  true;
        }

        /**
         *
         */
        public void startThreadToDownload() {
            if (connectionWithTracker != null && connectionWithTracker.isConnectedWithTracker()) {
                Any requestFileInfoMessage = Any.pack(RequestFileInfo.RequestFileInfoDetails.newBuilder()
                        .setSenderName(thisNodeInfo.getName())
                        .setFileName(fileName)
                        .setDetailForPacket(false)
                        .build());
                byte[] fileInfoResponseMessage = connectionWithTracker.makeRequestToTracker(requestFileInfoMessage.toByteArray());
                try {
                    Any fileInfoResponseMessageAny = Any.parseFrom(fileInfoResponseMessage);
                    if (fileInfoResponseMessageAny.is(FileMetadata.FileMetadataDetails.class)) {
                        FileMetadata.FileMetadataDetails fileInfo = fileInfoResponseMessageAny.unpack(FileMetadata.FileMetadataDetails.class);
                        if (fileInfo.getFileInfoAvailable()) {
                            logger.info("\nFileName : " + fileInfo.getFileName() +
                                    " \nFileSize : " + fileInfo.getFileSize() +
                                    "\nCheckSum : " + fileInfo.getChecksum() +
                                    "\nTotalNumberOfPacket : " + fileInfo.getTotalNumberOfPackets());
                            File file = new File(fileInfo.getFileName(),
                                    fileInfo.getFileSize(),
                                    fileInfo.getChecksum().toByteArray(),
                                    fileInfo.getTotalNumberOfPackets(),
                                    false);
                            // creating swarm for this file.
                            allSwarms.addNewFileSwarm(fileInfo.getFileName(), file);
                            logger.info("\nInitialised file " + fileName + ". Total member in the swarm " + fileInfo.getSwarmMemberInfoList().size());
//                            List<NodeInfo> memberInSwarmInfo;
                            for (ByteString memberInfoByteString : fileInfo.getSwarmMemberInfoList()) {
                                Any memberInfoAny = Any.parseFrom(memberInfoByteString);
                                if (memberInfoAny.is(SwarmMemberInfo.SwarmMemberInfoDetails.class)) {
                                    SwarmMemberInfo.SwarmMemberInfoDetails swarmMemberInfoDetails =
                                            memberInfoAny.unpack(SwarmMemberInfo.SwarmMemberInfoDetails.class);
                                    if (!swarmMemberInfoDetails.getPeerName().equals(thisNodeInfo.getName())) {
                                        try {
                                            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] For file " + fileName + " connecting to " + swarmMemberInfoDetails.getPeerName());
                                            Connection peerConnection =
                                                    Utility.establishConnection(swarmMemberInfoDetails.getPeerIp(),
                                                            swarmMemberInfoDetails.getPeerPort());
                                            if (peerConnection != null) {
                                                // do initial setup
                                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Sending InitialMessage.");
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
                                                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Waiting for initialMessage Ack.");
                                                    response = peerConnection.receive();
                                                }
                                                Any initialMessageResponse = Any.parseFrom(response);
                                                if (initialMessageResponse.is(InitialSetupDoneAck.InitialSetupDoneAckDetail.class)) {
                                                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Received InitialMessage response.");
                                                    InitialSetupDoneAck.InitialSetupDoneAckDetail initialSetupDoneAckDetail
                                                            = initialMessageResponse.unpack(InitialSetupDoneAck.InitialSetupDoneAckDetail.class);
                                                }
                                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Adding the member to the swarm. for file " + fileName);
                                                // adding new member in the Swarm.
                                                SwarmMemberDetails swarmMemberDetails = new SwarmMemberDetails(
                                                        swarmMemberInfoDetails.getPeerName(),
                                                        swarmMemberInfoDetails.getPeerIp(),
                                                        swarmMemberInfoDetails.getPeerPort(),
                                                        peerConnection);
                                                allSwarms.addNewPeerInTheFileSwarm(fileInfo.getFileName(), swarmMemberDetails);
                                            }
                                        } catch (ConnectionClosedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            for (int i = 0; i < Constants.EACH_FILE_THREAD_POOL_SIZE; i++) {
                                Thread thread = new Thread(this::startDownload);
                                thread.start();
                                logger.info("\nStarting thread" + i);
                            }
                        } else {
                            // file is not available.
                            logger.info("\nFile is not available.");
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            } else {
                connectToTracker();
                startDownload();
            }
        }

        /**
         *
         */
        public void startDownload() {
            logger.info("\nInside startDownload.");
            while (connectionWithTracker.isConnectedWithTracker() && allSwarms.isBeingDownloaded(fileName)) {
                long nextPacketNumber = allSwarms.getNextPacketNumber(fileName);
                Any requestPacketInfo = Any.pack(RequestFileInfo.RequestFileInfoDetails.newBuilder()
                        .setSenderName(thisNodeInfo.getName())
                        .setFileName(fileName)
                        .setPacketNumber(nextPacketNumber)
                        .setDetailForPacket(true)
                        .build());

                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Making Request to Tracker for packet " + nextPacketNumber + " of File " + fileName);
                byte[] response = connectionWithTracker.makeRequestToTracker(requestPacketInfo.toByteArray());
                try {
                    Any packetInfoResponse = Any.parseFrom(response);
                    if (packetInfoResponse.is(FileMetadata.FileMetadataDetails.class)) {
                        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Response received from Tracker for packet " + nextPacketNumber + " of File " + fileName);
                        FileMetadata.FileMetadataDetails packetDetails = packetInfoResponse.unpack(FileMetadata.FileMetadataDetails.class);
                        List<ByteString> swarmMembersInfoWithThisPacket = packetDetails.getSwarmMemberInfoList();
                        List<String> swarmMembersNameWithThisPacket = new ArrayList<>();
                        for (ByteString eachMemberInfo : swarmMembersInfoWithThisPacket) {
                            Any swarmMemberInfo = Any.parseFrom(eachMemberInfo);
                            if (swarmMemberInfo.is(SwarmMemberInfo.SwarmMemberInfoDetails.class)) {
                                SwarmMemberInfo.SwarmMemberInfoDetails swarmMemberInfoDetails =
                                        swarmMemberInfo.unpack(SwarmMemberInfo.SwarmMemberInfoDetails.class);
                                if (!swarmMemberInfoDetails.getPeerName().equals(thisNodeInfo.getName())) {
                                    swarmMembersNameWithThisPacket.add(swarmMemberInfoDetails.getPeerName());
                                }
                            }
                        }

                        logger.info("\n[ThreadId : " +Thread.currentThread().getId() + "] List of Peer in the swarm for file " + fileName + ", packet " + nextPacketNumber + " -> " + swarmMembersNameWithThisPacket);
                        Any downloadRequest = Any.pack(DownloadRequest.DownloadRequestDetail.newBuilder()
                                .setFileName(fileName)
                                .setPacketNumber(nextPacketNumber)
                                .build());
                        while (!allSwarms.packetOfFileIsAvailable(fileName, nextPacketNumber)) {
                            logger.info("\n[ThreadId : " +Thread.currentThread().getId() + "] file " + fileName + " packet " + nextPacketNumber + " available at "
                                    + swarmMembersNameWithThisPacket);
                            for (String peerName : swarmMembersNameWithThisPacket) {
                                Swarm swarm = allSwarms.getSwarm(fileName);
                                if (swarm.isAPeer(peerName)) {
                                    logger.info("\n[ThreadId : " +Thread.currentThread().getId() + "] Downloading...");
                                    byte[] downloadResponse = swarm.getPeerNode(peerName).downloadPacket(downloadRequest.toByteArray());
                                    if (downloadResponse != null) {
                                        logger.info("\n[ThreadId : " +Thread.currentThread().getId() + "] Downloaded something from " + peerName + " for file " + fileName);
                                        Any any = Any.parseFrom(downloadResponse);
                                        if (any.is(DownloadRequestResponse.DownloadRequestResponseDetail.class)) {
                                            DownloadRequestResponse.DownloadRequestResponseDetail actualData =
                                                    any.unpack(DownloadRequestResponse.DownloadRequestResponseDetail.class);
                                            //extract actual data, add it to temp file & update temp map
                                            boolean markedDownloaded = allSwarms.addDownloadedPacket(fileName, nextPacketNumber, actualData.getPacketData().toByteArray());
                                            // if markedDownloaded -> true -> update tracker node.
                                            if (markedDownloaded) {
                                                // update tracker node.
                                                Any updateMessage = Any.pack(UpdateFilePacketMetadataMetadata.UpdateFilePacketMetadataDetails.newBuilder()
                                                        .setFileName(fileName)
                                                        .setPacketNumber(nextPacketNumber)
                                                        .build());
                                                boolean updated = false;
                                                while (!updated) {
                                                    byte[] responseMessage = connectionWithTracker.makeRequestToTracker(updateMessage.toByteArray());
                                                    Any responseMessageAny = Any.parseFrom(responseMessage);
                                                    if (responseMessageAny.is(UpdateFilePacketMetadataSuccessful.UpdateFilePacketMetadataSuccessfulDetails.class)) {
                                                        updated = true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
