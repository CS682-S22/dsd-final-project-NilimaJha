package driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        String configFileName = Utility.getConfigFilename(args);

        if (hostType.equals(Constants.TRACKER_NODE)) {

        } else {

        }
    }
}
