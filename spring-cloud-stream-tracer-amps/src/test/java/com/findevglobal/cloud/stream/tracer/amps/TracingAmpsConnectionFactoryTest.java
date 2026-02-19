package com.findevglobal.cloud.stream.tracer.amps;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TracingAmpsConnectionFactoryTest {

    @Test
    public void getConnection() {
        AmpsConnectionFactory delegate = mock(AmpsConnectionFactory.class);
        AmpsTracer ampsTracer = mock(AmpsTracer.class);
        TracingAmpsConnectionFactory connectionFactory = new TracingAmpsConnectionFactory(delegate, ampsTracer);

        when(delegate.getConnection()).thenReturn(mock(AmpsConnection.class));

        Assertions.assertTrue(
                connectionFactory.getConnection() instanceof TracingAmpsConnection
        );
    }
}