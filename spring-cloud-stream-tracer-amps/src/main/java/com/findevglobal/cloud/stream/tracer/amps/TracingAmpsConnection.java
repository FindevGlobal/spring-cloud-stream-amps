package com.findevglobal.cloud.stream.tracer.amps;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.exception.DisconnectedException;
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TracingAmpsConnection implements AmpsConnection {

    private final AmpsConnection delegate;
    private final AmpsTracer ampsTracer;

    public TracingAmpsConnection(AmpsConnection delegate, AmpsTracer ampsTracer) {
        this.delegate = delegate;
        this.ampsTracer = ampsTracer;
    }

    @Override
    public void subscribe(Command command, MessageHandler handler) throws AMPSException {
        MessageHandler tracingHandler = message -> {
            String messageCorrelationId = message.getCorrelationId();
            AmpsHeaderConverter ampsHeaderConverter = delegate.getAmpsHeaderConverter();
            MessageHeaders messageHeaders = ampsHeaderConverter.toMessageHeaders(messageCorrelationId);
            Map<?, ?> params = ampsHeaderConverter.get(messageHeaders,
                    AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, Map.class, new HashMap<>());
            Map<String, String> headerParams = params.entrySet().stream()
                    .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                    .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
            ampsTracer.consumeInScope(headerParams, message.getTopic(), () -> handler.invoke(message));
        };
        delegate.subscribe(command, tracingHandler);
    }

    @Override
    public void publish(Command command, MessageHeaders messageHeaders) throws AMPSException {
        try {
            ampsTracer.publishInScope(command.getTopic(), tracingHeaders -> {
                Map<Object, Object> publishingHeaders = new HashMap<>();
                Object initPublishingHeaders = messageHeaders.get(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS);
                if (initPublishingHeaders instanceof Map) {
                    publishingHeaders.putAll((Map<?, ?>) initPublishingHeaders);
                }
                publishingHeaders.putAll(tracingHeaders);

                MessageHeaderAccessor messageHeaderAccessor = new MessageHeaderAccessor();
                messageHeaderAccessor.copyHeaders(messageHeaders);
                messageHeaderAccessor.setHeader(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, publishingHeaders);
                messageHeaderAccessor.setHeader(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, Boolean.TRUE);
                try {
                    delegate.publish(command, messageHeaderAccessor.toMessageHeaders());
                } catch (AMPSException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof AMPSException) {
                throw (AMPSException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setAutoAck(boolean isAutoAckEnabled) {
        delegate.setAutoAck(isAutoAckEnabled);
    }

    @Override
    public void setHeartbeat(int intervalSeconds) throws DisconnectedException {
        delegate.setHeartbeat(intervalSeconds);
    }

    @Override
    public void setAckBatchSize(int batchSize) {
        delegate.setAckBatchSize(batchSize);
    }

    @Override
    public void setAckTimeout(long ackTimeout) {
        delegate.setAckTimeout(ackTimeout);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public AmpsHeaderConverter getAmpsHeaderConverter() {
        return delegate.getAmpsHeaderConverter();
    }
}
