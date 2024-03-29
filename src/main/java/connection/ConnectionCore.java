package connection;

import customeException.ConnectionClosedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A wrapper class to wrap over connectionSockets.
 * Class to represent connection between two hosts.
 * and stores related information.
 * @author nilimajha
 */
public class ConnectionCore {
    private static final Logger logger = LogManager.getLogger(ConnectionCore.class);
    private AsynchronousSocketChannel connectionSocket;
    private boolean isConnected;
    private Future<Integer> incomingMessage;
    private ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    private Queue<byte[]> messageQueue = new LinkedList<>();

    /**
     * Constructor to initialise class attributes.
     * @param connectionSocket
     */
    public ConnectionCore(AsynchronousSocketChannel connectionSocket) {
        this.connectionSocket = connectionSocket;
        this.isConnected = true;
        this.incomingMessage = this.connectionSocket.read(buffer);
    }

    /**
     * returns the message received over
     * the channelSocket in array of byte.
     * @return byte[] message
     */
    public byte[] receive() throws ConnectionClosedException {
        if (!messageQueue.isEmpty()) {
            return messageQueue.poll();
        }

        byte[] messageByte = null;
        int totalDataToRead = 0;

        if (incomingMessage == null) {
            incomingMessage = connectionSocket.read(buffer);
        }
        if (isConnected) {
            try {
                int readHaveSomething = incomingMessage.get(Constants.READ_TIMEOUT_TIME, TimeUnit.MILLISECONDS);
                boolean readIsDone = incomingMessage.isDone();

                if (readHaveSomething != -1 && readIsDone) {
                    incomingMessage = null;
                    totalDataToRead = buffer.position();
                    while (buffer.position() > 4) {
                        buffer.flip();
                        int nextMessageSize = buffer.getInt();
                        if (totalDataToRead >=  4 + nextMessageSize ) {
                            messageByte = new byte[nextMessageSize];
                            buffer.get(messageByte, 0, nextMessageSize);
                            messageQueue.add(messageByte);
                            buffer.compact();
                            totalDataToRead = buffer.position();
                        } else {
                            buffer.position(0);
                            buffer.compact();
                            break;
                        }
                    }
                } else {
                    if (readHaveSomething == -1) {
                        logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] Closing the connection....");
                        closeConnection();
                    }
                }
            } catch (TimeoutException e) {
                return messageQueue.poll();
            } catch (InterruptedException e) {
                logger.error("\n[ThreadId : " + Thread.currentThread().getId() + "] InterruptedException while establishing connection. Error Message : " + e.getMessage());
            } catch (ExecutionException e) {
                closeConnection();
                throw new ConnectionClosedException("connection.Connection is closed by other host!!!");
            }
        } else {
            throw new ConnectionClosedException("connection.Connection is closed by other host!!!");
        }
        return messageQueue.poll();
    }

    /**
     * methods sends byte message over the channelSocket.
     * and returns true on success and false if not successful.
     * @param message
     * @return true/false
     */
    public boolean send(byte[] message) throws ConnectionClosedException {
        if (isConnected && connectionSocket.isOpen()) {
            ByteBuffer buffer = ByteBuffer.allocate(message.length + 10);
            buffer.putInt(message.length); //size of the next message.
            buffer.put(message); //actual message
            buffer.flip();

            Future result = connectionSocket.write(buffer);
            try {
                result.get();
            } catch (InterruptedException e) {
                logger.error("\n[ThreadId : " + Thread.currentThread().getId() + "] InterruptedException while writing on the connectionSocket. Error Message : "
                        + e.getMessage());
                return false;
            } catch (ExecutionException e) {
                isConnected = false;
                throw new ConnectionClosedException("Connection is closed by other host!!!");
            }
            buffer.clear();
            return true;
        }
        return false;
    }

    /**
     * returns the status of connectionSocket.
     * @return true/false
     */
    public boolean connectionIsOpen() {
        return connectionSocket.isOpen();
    }

    /**
     * closes the connectionSocket
     * @return true/false
     */
    public boolean closeConnection() {
        try {
            isConnected = false;
            connectionSocket.close();
        } catch (IOException e) {
            logger.info("\n[ThreadId : " + Thread.currentThread().getId() + "] IOException occurred while closing the connectionSocket. Error Message : " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * return the value of the boolean variable isConnected.
     * @return isConnected
     */
    public boolean isConnected() {
        return isConnected;
    }
}
