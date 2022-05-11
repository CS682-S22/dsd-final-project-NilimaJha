package model;

import customeException.ConnectionClosedException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * PEER
 * @author nilimajha
 */
public class PeerNodeInfo extends NodeInfo {
    private Connection connection;
    private ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();

    /**
     * Constructor
     *
     * @param name
     * @param ip
     * @param port
     */
    public PeerNodeInfo(String name, String ip, int port, Connection connection) {
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
                connectionLock.writeLock().tryLock(500, TimeUnit.MILLISECONDS);
                try {
                    connection.send(requestMassage);
                    while (response == null) {
                        response = connection.receive();
                    }
                } catch (ConnectionClosedException e) {
                    e.printStackTrace();
                }
                connectionLock.writeLock().unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
