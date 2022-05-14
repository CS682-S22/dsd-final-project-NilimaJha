package connection;

import customeException.ConnectionClosedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * class that adds a delay injection layer in to the Connection.
 * @author nilimajha
 */
public class Connection extends ConnectionCore {
    private static final Logger logger = LogManager.getLogger(Connection.class);
    private boolean delay;
    private int maxDelay;
    private Object delayObject = new Object();

    /**
     * Constructor to initialise class attributes.
     *
     * @param connectionSocket
     */
    public Connection(AsynchronousSocketChannel connectionSocket, boolean delay, int maxDelay) {
        super(connectionSocket);
        this.delay = delay;
        this.maxDelay = maxDelay;
    }

    /**
     * method sends data over the connection socket and also apply delay if required.
     * @param message
     * @return true/false
     * @throws ConnectionClosedException
     */
    public boolean send(byte[] message) throws ConnectionClosedException {
        if (delay) {
            int delayAmount = randomNumber(0, maxDelay);
            if (delayAmount > 0) {
                synchronized (delayObject) {
                    try {
                        delayObject.wait(delayAmount);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return super.send(message);
    }

    /**
     * Generates random number
     * in between min and max provided (inclusive)
     * reference: https://www.educative.io/edpresso/how-to-generate-random-numbers-in-java
     * @param min
     * @param max
     * @return
     */
    public int randomNumber(int min, int max) {
        return (int)Math.floor(Math.random()*(max-min+1)+min);
    }

    /**
     * getter for the attribute delay.
     * @return true/false
     */
    public boolean isDelay() {
        return delay;
    }

    /**
     * getter for the attribute maxDelay
     * @return maxDelay
     */
    public int getMaxDelay() {
        return maxDelay;
    }


}
