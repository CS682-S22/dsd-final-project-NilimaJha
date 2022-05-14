package tracker;

import connection.Connection;
import model.NodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Tracker Node class here trackerNode server keeps listening for the request on the given ip and port.
 * @author nilimajha
 */
public class TrackerNode implements Runnable {
    private static final Logger logger = LogManager.getLogger(TrackerNode.class);
    private NodeInfo thisNodeInfo;
    private boolean delay;
    private int maxDelay;
    private boolean shutdown = false;
    private ExecutorService threadPool = Executors.newFixedThreadPool(Constants.TRACKER_NODE_THREAD_POOL_SIZE);

    /**
     * Constructor
     * @param trackerName
     * @param trackerIp
     * @param trackerPort
     */
    public TrackerNode(String trackerName, String trackerIp, int trackerPort, boolean delay, int maxDelay) {
        this.thisNodeInfo = new NodeInfo(trackerName, trackerIp, trackerPort);
        this.delay = delay;
        this.maxDelay = maxDelay;
    }

    /**
     * opens a serverSocket and keeps listening for
     * new connection request from host nodes.
     */
    public void startTrackerNode() {
        AsynchronousServerSocketChannel serverSocket = null;
        try {
            serverSocket = AsynchronousServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(thisNodeInfo.getIp(), thisNodeInfo.getPort()));
            // keeps on running when shutdown is false
            while (!shutdown) {
                logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] [INFO] " + thisNodeInfo.getName() +
                        " Tracker Server is listening on IP : " + thisNodeInfo.getIp() + " & Port : " + thisNodeInfo.getPort());
                Future<AsynchronousSocketChannel> acceptFuture = serverSocket.accept();
                AsynchronousSocketChannel socketChannel = null;
                try {
                    socketChannel = acceptFuture.get();
                    if (shutdown) {
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("\n[ThreadId : " + Thread.currentThread().getId() + "] Exception while establishing " +
                            "connection. Error Message : " + e.getMessage());
                }

                //checking if the socketChannel is valid.
                if ((socketChannel != null) && (socketChannel.isOpen())) {
                    Connection connection = null;
                    connection = new Connection(socketChannel, delay, maxDelay);
                    // give this connection to handler
                    TrackerRequestProcessor handler = new TrackerRequestProcessor(connection, thisNodeInfo);
                    threadPool.execute(handler);
                }
            }
        } catch (IOException e) {
            logger.error("\n[ThreadId : " + Thread.currentThread().getId() + "] IOException while opening serverSocket" +
                    " connection. Error Message : " + e.getMessage());
        }
    }

    /**
     * method starts the trackerNode mechanism.
     */
    @Override
    public void run() {
        startTrackerNode();
    }
}
