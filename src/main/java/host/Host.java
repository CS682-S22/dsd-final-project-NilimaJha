package host;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import connection.Connection;
import customeException.ConnectionClosedException;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.*;
import swarm.AllSwarms;
import swarm.File;
import swarm.Swarm;
import swarm.SwarmMemberDetails;
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
 * Host class that contains logic related to the operation on a host.
 *
 * @author nilimajha
 */
public class Host implements Runnable {
    private static final Logger logger = LogManager.getLogger(Host.class);
    private NodeInfo thisNodeInfo;
    private NodeInfo trackerNodeInfo;
    private List<String> availableFileNames;
    private boolean delay;
    private int maxDelay;
    private boolean shutdown;
    private AllSwarms allSwarms;
    private volatile boolean registered;
    private ExecutorService threadPool = Executors.newFixedThreadPool(Constants.PEER_THREAD_POOL_SIZE); //thread pool of size 15

    /**
     * Constructor
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
                String trackerNodeIp, int trackerNodePort, List<String> availableFileNames, boolean delay, int maxDelay) {
        this.thisNodeInfo = new NodeInfo(thisNodeName, thisNodeIp, thisNodePort);
        this.trackerNodeInfo = new NodeInfo(trackerNodeName, trackerNodeIp, trackerNodePort);
        this.availableFileNames = availableFileNames;
        this.delay = delay;
        this.maxDelay = maxDelay;
        this.allSwarms = AllSwarms.getAllSwarm(thisNodeInfo);
    }

    /**
     * run opens a serverSocket and keeps listening for
     * new connection request from producer or consumer.
     * once it receives a connection request it creates a
     * connection object and hands it to the RequestProcessor class object.
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
                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] [INFO] " + thisNodeInfo.getName() +
                        " Server is listening on IP : " + thisNodeInfo.getIp() + " & Port : " + thisNodeInfo.getPort());
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
                    newConnection = new Connection(socketChannel, delay, maxDelay);
                    // give this connection to requestProcessor
                    HostRequestProcessor requestProcessor = new HostRequestProcessor(thisNodeInfo, newConnection);
                    threadPool.execute(requestProcessor);
                }
            }
        } catch (IOException e) {
            logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                    "] IOException while opening serverSocket connection. Error Message : " + e.getMessage());
        }
    }

    /**
     * calls createSwarmForEachAvailableFile method to create swarm for all the available files.
     * calls connectAndRegisterToTrackerNode method to register itself at trackerNode.
     */
    public void initialSetup() {
        // for all the files available creating a swarm.
        createSwarmForEachAvailableFile();
        // connecting to trackerNode and doing initial registration.
        connectAndRegisterToTrackerNode();
    }

    /**
     * method creates swarm for each available files.
     */
    public void createSwarmForEachAvailableFile() {
        // Creating Swarm for each available file.
        if (availableFileNames != null) {
            for (String eachFile : availableFileNames) {
                try {
                    Path path = Paths.get(eachFile);
                    long fileSize = Files.size(path);
                    File file = new File(eachFile, fileSize, Utility.createChecksum(eachFile),
                            Utility.getTotalPacketOfFile(fileSize), true);
                    allSwarms.addNewFileSwarm(eachFile, file);
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                            "] Successfully Created Swarm for file " + eachFile);
                } catch (IOException e) {
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                            "] Error Message : " + e.getMessage());
                }
            }
        }
    }

    /**
     * method that sets up a connection with tracker node and sends its information to it to register itself.
     * once registered the connection with tracker is closed.
     * @return registered
     */
    public boolean connectAndRegisterToTrackerNode() {
        Connection trackerConnection = null;
        while(trackerConnection == null) {
            try {
                trackerConnection = Utility.establishConnection(
                        trackerNodeInfo.getIp(),
                        trackerNodeInfo.getPort(),
                        delay,
                        maxDelay);
            } catch (ConnectionClosedException e) {
                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] " + e.getMessage());
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
                            // registration successful
                            registrationSuccessful = true;
                            registered = true;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("\n[ThreadId : " +Thread.currentThread().getId() +
                                "] InvalidProtocolBufferException occurred. Error Message : " + e.getMessage());
                    }
                }
            } catch (ConnectionClosedException e) {
                connectAndRegisterToTrackerNode();
            }
        }
        logger.info("\n[ThreadId : " +Thread.currentThread().getId() + "] Closing the Connection with Tracker...");
        trackerConnection.closeConnection();
        return registered;
    }

    /**
     * method creates an instance of the FileDownloader class to start downloading the file with given file name.
     * @param fileName name of the file to be downloaded.
     */
    public void download(String fileName) {
        logger.info("\n[ThreadId : " +Thread.currentThread().getId() + "] Starting to download file  :" + fileName);
        FileDownloader fileDownloader = new FileDownloader(fileName, trackerNodeInfo);
    }

    /**
     * class that starts downloading the file with the given name.
     *
     * @author nilimajha
     */
    public class FileDownloader {
        private String fileName;
        private NodeInfo trackerNodeInfo;
        private AllSwarms allSwarms = AllSwarms.getAllSwarm(thisNodeInfo);
        private volatile boolean downloadStatusPrinted = false;
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
         * method that creates connection with the tracker node and
         * stores this tracker connection for the entire duration when this file is being downloaded.
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
                    Connection connection = Utility.establishConnection(
                            trackerNodeInfo.getIp(),
                            trackerNodeInfo.getPort(),
                            delay,
                            maxDelay);
                    if (connection != null) {
                        this.connectionWithTracker = new ConnectionWithTracker(trackerNodeInfo, connection);
                    }
                } catch (ConnectionClosedException e) {
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] " + e.getMessage());
                }
            }
            if (connectionWithTracker != null && this.connectionWithTracker.isConnectedWithTracker()) {
                boolean setUpDone = false;
                Any any = Any.pack(SetupForFileMessage.SetupForFileMessageDetails.newBuilder()
                        .setSenderName(thisNodeInfo.getName())
                        .setSenderIp(thisNodeInfo.getIp())
                        .setSenderPort(thisNodeInfo.getPort())
                        .setFileToBeDownloaded(fileName)
                        .build());
                while (!setUpDone) {
                    if (connectionWithTracker != null && connectionWithTracker.isConnectedWithTracker()) {
                        byte[] response = connectionWithTracker.makeRequestToTracker(any.toByteArray());
                        try {
                            Any responseAny = Any.parseFrom(response);
                            if (responseAny.is(SetupForFileResponse.SetupForFileResponseDetails.class)) {
                                setUpDone = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                                    "] InvalidProtocolBufferException occurred. Error Message : " + e.getMessage());
                        }
                    } else {
                        connectToTracker();
                    }
                }
            }
            return  true;
        }

        /**
         * method first gets the file to be downloaded information with
         * the information of the peer from where this file can be downloaded from tracker node.
         *
         * sets up connection with each of the peer from the list of peer provided by tracker node.
         *
         * starts n number of threads to download file packets randomly from peer.
         */
        public void startThreadToDownload() {
            if (connectionWithTracker != null && connectionWithTracker.isConnectedWithTracker()) {
                // request file to be downloaded information from tracker node.
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
                            File file = new File(fileInfo.getFileName(),
                                    fileInfo.getFileSize(),
                                    fileInfo.getChecksum().toByteArray(),
                                    fileInfo.getTotalNumberOfPackets(),
                                    false);
                            // creating swarm for this file.
                            allSwarms.addNewFileSwarm(fileInfo.getFileName(), file);
                            // creating connection with all the member available in this file swarm.
                            for (ByteString memberInfoByteString : fileInfo.getSwarmMemberInfoList()) {
                                Any memberInfoAny = Any.parseFrom(memberInfoByteString);
                                if (memberInfoAny.is(SwarmMemberInfo.SwarmMemberInfoDetails.class)) {
                                    SwarmMemberInfo.SwarmMemberInfoDetails swarmMemberInfoDetails =
                                            memberInfoAny.unpack(SwarmMemberInfo.SwarmMemberInfoDetails.class);
                                    if (!swarmMemberInfoDetails.getPeerName().equals(thisNodeInfo.getName())) {
                                        try {
                                            Connection peerConnection =
                                                    Utility.establishConnection(swarmMemberInfoDetails.getPeerIp(),
                                                            swarmMemberInfoDetails.getPeerPort(), delay, maxDelay);
                                            if (peerConnection != null) {
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
                                                            = initialMessageResponse.unpack(InitialSetupDoneAck.InitialSetupDoneAckDetail.class);
//                                                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
//                                                            "] For file " + fileName + " connected to "
//                                                            + swarmMemberInfoDetails.getPeerName());
                                                }
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
                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                                        "] Starting download thread " + i + " for file " + fileName);
                                thread.start();
                            }
                        } else {
                            // file is not available.
                            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] File is not available.");
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                            "] InvalidProtocolBufferException occurred. Error message : " + e.getMessage());
                }
            } else {
                connectToTracker();
                startDownload();
            }
        }

        /**
         * method downloads file by downloading one packet at a time.
         */
        public void startDownload() {
            while (connectionWithTracker.isConnectedWithTracker() && allSwarms.isBeingDownloaded(fileName)) {
                long nextPacketNumber = allSwarms.getNextPacketNumber(fileName);
                Any requestPacketInfo = Any.pack(RequestFileInfo.RequestFileInfoDetails.newBuilder()
                        .setSenderName(thisNodeInfo.getName())
                        .setFileName(fileName)
                        .setPacketNumber(nextPacketNumber)
                        .setDetailForPacket(true)
                        .build());
                byte[] response = connectionWithTracker.makeRequestToTracker(requestPacketInfo.toByteArray());
                try {
                    Any packetInfoResponse = Any.parseFrom(response);
                    if (packetInfoResponse.is(FileMetadata.FileMetadataDetails.class)) {
//                        logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
//                                "] Got information from Tracker for packet " + nextPacketNumber + " of File " + fileName);
                        FileMetadata.FileMetadataDetails packetDetails = packetInfoResponse.unpack(FileMetadata.FileMetadataDetails.class);
                        List<ByteString> swarmMembersInfoWithThisPacket = packetDetails.getSwarmMemberInfoList();
                        // extracting the name of the peer in the swarm with the packet.
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

//                        logger.info("\n[ThreadId : " +Thread.currentThread().getId() +
//                        "] List of Peer in the swarm for file " + fileName + ", packet "
//                        + nextPacketNumber + " -> " + swarmMembersNameWithThisPacket);
                        Any downloadRequest = Any.pack(DownloadRequest.DownloadRequestDetail.newBuilder()
                                .setFileName(fileName)
                                .setPacketNumber(nextPacketNumber)
                                .build());
                        while (!allSwarms.packetOfFileIsAvailable(fileName, nextPacketNumber)) {
                            logger.info("\n[ThreadId : " +Thread.currentThread().getId() + "] file "
                            + fileName + " packet " + nextPacketNumber + " available at "
                            + swarmMembersNameWithThisPacket);

                            // downloading packet from peer.
                            for (String peerName : swarmMembersNameWithThisPacket) {
                                Swarm swarm = allSwarms.getSwarm(fileName);
                                if (swarm.isAPeer(peerName)) {
                                    byte[] downloadResponse = swarm.getPeerNode(peerName).makeRequestAndGetResponseFromPeer(downloadRequest.toByteArray());
                                    if (downloadResponse != null) {
                                        Any any = Any.parseFrom(downloadResponse);
                                        if (any.is(DownloadRequestResponse.DownloadRequestResponseDetail.class)) {
                                            DownloadRequestResponse.DownloadRequestResponseDetail actualData =
                                                    any.unpack(DownloadRequestResponse.DownloadRequestResponseDetail.class);
                                            logger.info("\n[ThreadId : " +Thread.currentThread().getId() +
                                                    "] Downloaded packet from " + peerName + " for file " + fileName +
                                                    " actual data available " + actualData.getDataAvailable());
                                            //extract actual data, add it to temp file & update temp map
                                            boolean markedDownloaded = allSwarms.addDownloadedPacket(
                                                    fileName,
                                                    nextPacketNumber,
                                                    actualData.getPacketData().toByteArray());
                                            // if markedDownloaded -> true -> update tracker node.
                                            if (markedDownloaded) {
                                                // update packet available at tracker node.
                                                Any updateMessage = Any.pack(UpdateFilePacketMetadataMetadata.UpdateFilePacketMetadataDetails
                                                        .newBuilder()
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

                                            if (!allSwarms.isBeingDownloaded(fileName) && !downloadStatusPrinted) {
                                                logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                                                        "] File " + fileName + " Successfully Downloaded...");
                                                allSwarms.closeAllConnection(fileName);
                                                // close connection with tracker for this file.
                                                connectionWithTracker.closeConnection();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    logger.error("\n[ThreadId : " + Thread.currentThread().getId() +
                            "] InvalidProtocolBufferException occurred. Error message : " + e.getMessage());
                }
            }
        }
    }
}
