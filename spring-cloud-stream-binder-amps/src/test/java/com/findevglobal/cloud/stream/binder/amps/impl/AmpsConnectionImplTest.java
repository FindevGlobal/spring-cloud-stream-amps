package com.findevglobal.cloud.stream.binder.amps.impl;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.exception.DisconnectedException;
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import com.findevglobal.cloud.stream.binder.amps.connection.impl.AmpsConnectionImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AmpsConnectionImplTest {

    private HAClient client;

    private final AmpsHeaderConverter testConverter = new AmpsHeaderConverter() {
        @Override
        public String toCorrelationId(final MessageHeaders messageHeaders) {
            return "AUTO_GENERATED_ID";
        }

        @Override
        public MessageHeaders toMessageHeaders(final String correlationId) {
            return new MessageHeaders(new HashMap<>());
        }
    };

    @BeforeEach
    public void init() {
        client = mock(HAClient.class);
    }

    @Test
    public void publishWithoutCorrelationId() throws AMPSException {
        Command command = mock(Command.class);
        AmpsConnectionImpl connection = new AmpsConnectionImpl(client, testConverter, false);
        connection.publish(command, new MessageHeaders(singletonMap("otherHeader", "value")));
        verify(client).executeAsync(command, null);
        // No correlation ID should be set
        verify(command, never()).setCorrelationId(any());
    }

    @Test
    public void publishWithPublishAmpsHeaderFlag() throws AMPSException {
        Command command = mock(Command.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("someHeader", "value");
        headers.put(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, Boolean.TRUE);

        AmpsConnectionImpl connection = new AmpsConnectionImpl(client, testConverter, false);

        connection.publish(command, new MessageHeaders(headers));

        verify(client).executeAsync(command, null);
        ArgumentCaptor<String> correlationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(command).setCorrelationId(correlationIdCaptor.capture());
        Assertions.assertEquals("AUTO_GENERATED_ID", correlationIdCaptor.getValue());
    }

    @Test
    public void publishWithExplicitCorrelationIdAndPublishAmpsHeader() throws AMPSException {
        Command command = mock(Command.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put(AmpsMessageHeaders.CORRELATION_ID, "EXPLICIT_ID");
        headers.put(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, Boolean.TRUE);

        AmpsConnectionImpl connection = new AmpsConnectionImpl(client, testConverter, false);

        connection.publish(command, new MessageHeaders(headers));

        verify(client).executeAsync(command, null);
        ArgumentCaptor<String> correlationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(command).setCorrelationId(correlationIdCaptor.capture());
        Assertions.assertEquals("EXPLICIT_ID", correlationIdCaptor.getValue());
    }

    @Test
    public void publishWithoutAmpsHeader() throws AMPSException {
        Command command = mock(Command.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("someHeader", "value");
        headers.put(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, Boolean.FALSE);
        AmpsConnectionImpl connection = new AmpsConnectionImpl(client, testConverter, false);

        connection.publish(command, new MessageHeaders(headers));

        verify(client).executeAsync(command, null);
        verify(command, never()).setCorrelationId(any());
    }

    @Test
    public void getName() {
        when(client.getName()).thenReturn("TEST");
        Assertions.assertEquals(
                "TEST",
                ampsConnection().getName()
        );
    }

    @Test
    public void setAutoAck() {
        ampsConnection().setAutoAck(true);
        verify(client).setAutoAck(true);
    }

    @Test
    public void setHeartbeat() throws DisconnectedException {
        ampsConnection().setHeartbeat(1);
        verify(client).setHeartbeat(1);
    }

    @Test
    public void setAckBatchSize() {
        ampsConnection().setAckBatchSize(1);
        verify(client).setAckBatchSize(1);
    }

    @Test
    public void setAckTimeout() {
        ampsConnection().setAckTimeout(1);
        verify(client).setAckTimeout(1);
    }

    @Test
    public void subscribe() throws AMPSException {
        Command command = mock(Command.class);
        MessageHandler handler = mock(MessageHandler.class);
        ampsConnection().subscribe(command, handler);
        verify(client).executeAsync(command, handler);
    }

    @Test
    public void getAmpsHeaderConverter() {
        Assertions.assertSame(
                testConverter,
                ampsConnection().getAmpsHeaderConverter()
        );
    }

    @Test
    public void close() throws AMPSException {
        ampsConnection().close();
        verify(client).publishFlush(30000);
        verify(client).flushAcks();
        verify(client).close();
    }

    private AmpsConnectionImpl ampsConnection() {
        return new AmpsConnectionImpl(client, testConverter, true);
    }
}
