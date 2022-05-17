package swarm;

import connection.Connection;
import customeException.ConnectionClosedException;
import model.NodeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class to store peer information and connection with that peer.
 * @author nilimajha
 */
public class SwarmMemberDetails extends NodeInfo {
    private static final Logger logger = LogManager.getLogger(SwarmMemberDetails.class);
    private Connection connection;
    private ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();

    /**
     * Constructor
     * @param name
     * @param ip
     * @param port
     */
    public SwarmMemberDetails(String name, String ip, int port, Connection connection) {
        super(name, ip, port);
        this.connection = connection;
    }

    /**
     * method sends the request, waits for the response and return the response received.
     * @param requestMassage
     */
    public byte[] makeRequestAndGetResponseFromPeer(byte[] requestMassage) {
        byte[] response =  null;
        if (connection != null && connection.isConnected()) {
            try {
                if(connectionLock.writeLock().tryLock(1, TimeUnit.MILLISECONDS)) {
                    try {
                        connection.send(requestMassage);
                        while (response == null) {
                            response = connection.receive();
                        }
                    } catch (ConnectionClosedException e) {
                        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] message :" + e.getMessage());
                    }
                    connectionLock.writeLock().unlock();
                }
            } catch (InterruptedException e) {
                logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                        "] InterruptedException occurred. Error Message : " + e.getMessage());
            }
        }
        return response;
    }

    /**
     * setter for the attribute connection.
     * @param connection
     */
    public void setConnection(Connection connection) {
        if (this.connection == null) {
            this.connection = connection;
        }
    }

    /**
     * method closes the connection with the peer.
     */
    public boolean closeConnection() {
       connectionLock.writeLock().lock();
       if (connection != null && connection.isConnected()) {
           logger.info("\n[ThreadId : " + Thread.currentThread().getId() +
                   "] Closing the connection with peer " + getName());
           connection.closeConnection();
           connection = null;
       }
       connectionLock.writeLock().unlock();
       return true;
    }
}
