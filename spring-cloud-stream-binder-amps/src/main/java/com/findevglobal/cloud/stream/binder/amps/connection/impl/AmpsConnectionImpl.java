package com.findevglobal.cloud.stream.binder.amps.connection.impl;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.exception.DisconnectedException;
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import org.springframework.messaging.MessageHeaders;

import static org.springframework.util.StringUtils.hasLength;

public class AmpsConnectionImpl implements AmpsConnection {

    private final HAClient client;
    private final AmpsHeaderConverter ampsHeaderConverter;
    private final boolean publishAmpsHeader;

    public AmpsConnectionImpl(HAClient client,
                              AmpsHeaderConverter ampsHeaderConverter,
                              boolean publishAmpsHeader) {
        this.client = client;
        this.ampsHeaderConverter = ampsHeaderConverter;
        this.publishAmpsHeader = publishAmpsHeader;
    }

    @Override
    public String getName() {
        return client.getName();
    }

    @Override
    public void setAutoAck(boolean isAutoAckEnabled) {
        client.setAutoAck(isAutoAckEnabled);
    }

    @Override
    public void setHeartbeat(int intervalSeconds) throws DisconnectedException {
        client.setHeartbeat(intervalSeconds);
    }

    @Override
    public void setAckBatchSize(int batchSize) {
        client.setAckBatchSize(batchSize);
    }

    @Override
    public void setAckTimeout(long ackTimeout) {
        client.setAckTimeout(ackTimeout);
    }

    @Override
    public void subscribe(Command command, MessageHandler handler) throws AMPSException {
        client.executeAsync(command, handler);
    }

    @Override
    public void publish(Command command, MessageHeaders messageHeaders) throws AMPSException {
        String correlationId = ampsHeaderConverter.get(messageHeaders,
                AmpsMessageHeaders.CORRELATION_ID, String.class, null);
        boolean shouldPublishAmpsHeader = this.publishAmpsHeader || ampsHeaderConverter.get(messageHeaders,
                AmpsMessageHeaders.PUBLISH_AMPS_HEADER, Boolean.class, Boolean.FALSE);
        if (shouldPublishAmpsHeader && !hasLength(correlationId)) {
            correlationId = ampsHeaderConverter.toCorrelationId(messageHeaders);
        }
        if (hasLength(correlationId)) {
            command.setCorrelationId(correlationId);
        }
        client.executeAsync(command, null);
    }

    @Override
    public void close() {
        try {
            client.publishFlush(30_000);
        } catch (AMPSException e) {
            // Ignore
        }
        try {
            client.flushAcks();
        } catch (AMPSException e) {
            // Ignore
        }
        client.close();
    }

    @Override
    public AmpsHeaderConverter getAmpsHeaderConverter() {
        return ampsHeaderConverter;
    }
}
