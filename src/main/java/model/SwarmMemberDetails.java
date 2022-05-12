package model;

import customeException.ConnectionClosedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import peer.Host;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * PEER
 * @author nilimajha
 */
public class SwarmMemberDetails extends NodeInfo {
    private static final Logger logger = LogManager.getLogger(SwarmMemberDetails.class);
    private Connection connection;
    private ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();

    /**
     * Constructor
     *
     * @param name
     * @param ip
     * @param port
     */
    public SwarmMemberDetails(String name, String ip, int port, Connection connection) {
        super(name, ip, port);
        this.connection = connection;
    }

    /**
     * method downloads one packet at a time over the connection.
     * @param requestMassage
     */
    public byte[] downloadPacket(byte[] requestMassage) {
        byte[] response =  null;
        if (connection.isConnected()) {
            try {

                if(connectionLock.writeLock().tryLock(5, TimeUnit.MILLISECONDS)) {
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Acquired Lock.");
                    try {
                        connection.send(requestMassage);
                        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] sent download Request.");
                        while (response == null) {
                            response = connection.receive();
                        }
                        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Received some Response.");
                    } catch (ConnectionClosedException e) {
                        e.printStackTrace();
                    }
                    connectionLock.writeLock().unlock();
                    logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] releasing Lock.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
