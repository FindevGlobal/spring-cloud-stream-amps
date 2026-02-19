package com.findevglobal.cloud.stream.tracer.amps;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.exception.DisconnectedException;
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TracingAmpsConnectionTest {

    private AmpsTracer ampsTracer;
    private AmpsConnection delegate;
    private AmpsHeaderConverter ampsHeaderConverter;
    private TracingAmpsConnection tracingAmpsConnection;

    // To Fix: Calling real methods is only possible when mocking non abstract method.
    private static abstract class AmpsHeaderConverterSub implements AmpsHeaderConverter {

    }

    @BeforeEach
    public void init() {
        ampsTracer = mock(AmpsTracer.class);
        delegate = mock(AmpsConnection.class);
        ampsHeaderConverter = mock(AmpsHeaderConverterSub.class);
        when(delegate.getAmpsHeaderConverter()).thenReturn(ampsHeaderConverter);
        when(ampsHeaderConverter.get(any(), any(), any(), any())).thenCallRealMethod();
        when(ampsHeaderConverter.get(any(), any(), any())).thenCallRealMethod();
        tracingAmpsConnection = new TracingAmpsConnection(delegate, ampsTracer);
    }

    @Test
    public void subscribe() throws AMPSException {
        ArgumentCaptor<MessageHandler> captor = ArgumentCaptor.forClass(MessageHandler.class);
        ArgumentCaptor<Runnable> traceCaptor = ArgumentCaptor.forClass(Runnable.class);
        MessageHandler messageHandler = mock(MessageHandler.class);
        Command command = mock(Command.class);

        tracingAmpsConnection.subscribe(command, messageHandler);

        verify(delegate).subscribe(same(command), captor.capture());

        Message message = mock(Message.class);
        when(message.getTopic()).thenReturn("topic");
        when(message.getCorrelationId()).thenReturn("correlation_id");
        when(ampsHeaderConverter.toMessageHeaders("correlation_id")).thenReturn(
                new MessageHeaders(singletonMap(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS,
                        singletonMap("HEADER", "VALUE")))
        );
        captor.getValue().invoke(message);
        verify(ampsTracer).consumeInScope(eq(singletonMap("HEADER", "VALUE")), eq("topic"), traceCaptor.capture());
        traceCaptor.getValue().run();
        verify(messageHandler).invoke(message);
    }

    @Test
    public void publish() throws Exception {
        ArgumentCaptor<MessageHeaders> captor = ArgumentCaptor.forClass(MessageHeaders.class);
        ArgumentCaptor<Consumer<Map<String, String>>> traceCaptor = ArgumentCaptor.forClass(Consumer.class);
        Command command = mock(Command.class);
        when(command.getTopic()).thenReturn("topic");

        Map<String, Object> headers = new HashMap<>();
        headers.put("CUSTOM_HEADER", "CUSTOM_VALUE");
        headers.put(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, singletonMap("CUSTOM_PARAM_HEADER", "CUSTOM_PARAM_VALUE"));
        MessageHeaders initHeaders = new MessageHeaders(headers);
        tracingAmpsConnection.publish(command, initHeaders);
        verify(ampsTracer).publishInScope(eq("topic"), traceCaptor.capture());
        traceCaptor.getValue().accept(singletonMap("TRACING_HEADER", "TRACING_VALUE"));
        verify(delegate).publish(same(command), captor.capture());

        MessageHeaders messageHeaders = captor.getValue();

        Assertions.assertEquals(
                "CUSTOM_VALUE",
                messageHeaders.get("CUSTOM_HEADER", String.class)
        );
        Assertions.assertEquals(
                Boolean.TRUE,
                messageHeaders.get(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, Boolean.class));
        Map params = messageHeaders.get(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, Map.class);
        Assertions.assertNotNull(params);
        Assertions.assertEquals(
                "CUSTOM_PARAM_VALUE",
                params.get("CUSTOM_PARAM_HEADER")
        );
        Assertions.assertEquals(
                "TRACING_VALUE",
                params.get("TRACING_HEADER")
        );
    }

    @Test
    public void getName() {
        when(delegate.getName()).thenReturn("TEST");
        Assertions.assertEquals(
                "TEST",
                tracingAmpsConnection.getName()
        );
    }

    @Test
    public void setAutoAck() {
        tracingAmpsConnection.setAutoAck(true);
        verify(delegate).setAutoAck(true);
    }

    @Test
    public void setHeartbeat() throws DisconnectedException {
        tracingAmpsConnection.setHeartbeat(1);
        verify(delegate).setHeartbeat(1);
    }

    @Test
    public void setAckBatchSize() {
        tracingAmpsConnection.setAckBatchSize(1);
        verify(delegate).setAckBatchSize(1);
    }

    @Test
    public void setAckTimeout() {
        tracingAmpsConnection.setAckTimeout(1);
        verify(delegate).setAckTimeout(1);
    }

    @Test
    public void getAmpsHeaderConverter() {
        tracingAmpsConnection.getAmpsHeaderConverter();
        verify(delegate).getAmpsHeaderConverter();
    }

    @Test
    public void close() {
        tracingAmpsConnection.close();
        verify(delegate).close();
    }
}