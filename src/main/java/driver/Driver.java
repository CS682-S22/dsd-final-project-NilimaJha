package driver;

import model.HostConfig;
import model.TrackerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import host.Host;
import tracker.TrackerNode;
import utility.Constants;
import utility.Utility;

/**
 * Driver class that contains main method.
 * @author nilimajha
 */
public class Driver {
    private static final Logger logger = LogManager.getLogger(Driver.class);

    /**
     * main method
     *
     * @param args input args
     */
    public static void main(String[] args) {
        //validate args
        if (!Utility.argsIsValid(args)) {
            logger.info("\nArgument provided is invalid.");
            System.exit(0);
        }
        //parseArgs
        String hostType = Utility.getTypeFromArgs(args);
        String hostName = Utility.getNameFromArgs(args);
        boolean delay = Utility.getDelayStatusFromArgs(args);
        int maxDelay = Utility.getMaxDelayFromArgs(args);
        String configFileName = Utility.getConfigFilename(args);


        if (hostType.equals(Constants.TRACKER_NODE)) {
            createAndStartTrackerNode(configFileName, hostName, delay, maxDelay);
        } else if (hostType.equals(Constants.NODE)){
            createAndStartHostNode(configFileName, hostName, delay, maxDelay);
        } else {
            logger.info("Host type is not supported...");
        }
    }

    /**
     * method creates an instance of TrackerNode and starts it.
     * @param configFileName
     * @param trackerName
     */
    public static void createAndStartTrackerNode(String configFileName, String trackerName, boolean delay, int maxDelay) {
        TrackerConfig trackerConfig = Utility.extractTrackerInfo(configFileName, trackerName);
        TrackerNode trackerNode = new TrackerNode(
                trackerConfig.getName(),
                trackerConfig.getTrackerNodeIP(),
                trackerConfig.getTrackerNodePort(),
                delay, maxDelay);
        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Tracker name : " + trackerConfig.getName());

        Thread thread = new Thread(trackerNode);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("\n[ThreadId : " + Thread.currentThread().getId() + "] InterruptedException occurred while waiting for broker thread to join. Error Message : " + e.getMessage());
        }
    }

    /**
     * method creates an instance of the Host and starts it,
     * also if that host needs  to download some data it will trigger download file mechanism for that file.
     * @param configFileName
     * @param trackerName
     */
    public static void createAndStartHostNode(String configFileName, String trackerName, boolean delay, int maxDelay) {
        HostConfig hostConfig = Utility.extractHostInfo(configFileName, trackerName);
        Host host = new Host(
                hostConfig.getNodeName(),
                hostConfig.getNodeIp(),
                hostConfig.getNodePort(),
                hostConfig.getTrackerNodeName(),
                hostConfig.getTrackerNodeIp(),
                hostConfig.getTrackerNodePort(),
                hostConfig.getAvailableFiles(),
                delay, maxDelay);
        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Host name : " + hostConfig.getNodeName());

        Thread thread = new Thread(host);
        thread.start();

        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] files needed to download " + hostConfig.getDownload());
        if (hostConfig.getDownload() != null) {
            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] this host will download." + hostConfig.getDownload());
            for (String downloadFileName : hostConfig.getDownload()) {
                Runnable runnable = () -> {
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] file Name " + downloadFileName);
                    host.download(downloadFileName);
                };
                Thread fileDownloadThread = new Thread(runnable);
                fileDownloadThread.start();
            }
        }
    }
}
