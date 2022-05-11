package utility;

/**
 * Constant class to hold various Constants.
 * @author nilimajha
 */
public class Constants {
    public final static int BUFFER_SIZE = 60000;
    public final static  int READ_TIMEOUT_TIME = 60000;
    public final static int TRACKER_NODE_THREAD_POOL_SIZE = 20;
    public final static int PEER_THREAD_POOL_SIZE = 30;
    public final static int EACH_FILE_THREAD_POOL_SIZE = 5;
    public final static int MAX_PACKET_SIZE = 1000; //50000;
    public final static  int TOTAL_IN_MEMORY_MESSAGE_SIZE = 10; // number of message after which it will be flushed on to the segment file.
    public final static String INITIAL_SETUP = "INITIAL_SETUP";
    public final static String PUBLISH_REQUEST = "PUBLISH";
    public final static String PULL_REQUEST = "PULL";
    public final static String DATA = "DATA";
    public final static String NODE = "NODE";
    public final static String TRACKER_NODE = "TRACKER_NODE";
    public final static String CONSUMER_PULL = "PULL";
    public final static String CONSUMER_PUSH = "PUSH";
    public final static String MESSAGE = "MESSAGE";
    public final static int MESSAGE_BATCH_SIZE = 10;
    public final static int FLUSH_FREQUENCY = 6000;
    public final static int TIMEOUT_IF_DATA_NOT_YET_AVAILABLE = 6000;
    public final static String MESSAGE_NOT_AVAILABLE = "MESSAGE_NOT_AVAILABLE";
    public final static String TOPIC_NOT_AVAILABLE = "TOPIC_NOT_AVAILABLE";

    public final static int MAX_RETRIES = 2;
    public final static int RETRIES_TIMEOUT = 15000;
    public final static long TIMEOUT_NANOS = 30000000000L; // 1 milli = 1000000 nano.
    public final static int HEARTBEAT_CHECK_TIMER_TIMEOUT = 15000;
    public final static int HEARTBEAT_SEND_TIMER_TIMEOUT = 15000;
    public final static String HEARTBEAT_CONNECTION = "HEARTBEAT_CONNECTION";
    public final static String DATA_CONNECTION = "DATA_CONNECTION";
    public final static String SYNCHRONOUS = "SYNCHRONOUS";
    public final static String CATCHUP = "CATCHUP";
    public final static String CATCHUP_CONNECTION = "CATCHUP_CONNECTION";
    public final static String START_SYNC = "START_SYNC";
    public final static String DB_SNAPSHOT = "DB_SNAPSHOT";
}
