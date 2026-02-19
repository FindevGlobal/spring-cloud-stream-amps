package com.findevglobal.cloud.stream.binder.amps.connection;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.exception.DisconnectedException;
import org.springframework.messaging.MessageHeaders;

/**
 * Connection to AMPS
 */
public interface AmpsConnection {

    /**
     * @return the name of the Client.
     */
    String getName();

    /**
     * Enables or disables auto-acking.
     *
     * @param isAutoAckEnabled
     */
    void setAutoAck(boolean isAutoAckEnabled);

    /**
     * Requests a server heartbeat, and configures the client to close the connection
     * if a heartbeat (or other activity) is not seen on the connection after two heartbeat intervals.
     * @param intervalSeconds
     */
    void setHeartbeat(int intervalSeconds) throws DisconnectedException;

    /**
     * Sets the current ACK batch size.
     * @param batchSize
     */
    void setAckBatchSize(int batchSize);

    /**
     * Sets the ack timeout -- the maximum time to let a success ack be cached before sending.
     * @param ackTimeout
     */
    void setAckTimeout(long ackTimeout);

    /**
     * Subscribe to the Amps topic
     *
     * @param command
     * @param handler
     */
    void subscribe(Command command, MessageHandler handler) throws AMPSException;

    /**
     * Publish a message to the Amps topic
     * @param command
     * @param messageHeaders
     */
    void publish(Command command, MessageHeaders messageHeaders) throws AMPSException;

    /**
     * @return amps header converter
     */
    AmpsHeaderConverter getAmpsHeaderConverter();

    /**
     * Disconnect from the AMPS server.
     */
    void close();
}
