package com.findevglobal.cloud.stream.tracer.amps;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TracingAmpsConnectionFactoryProviderTest {

    @Test
    public void getConnectionFactory() {
        AmpsConnectionFactoryProvider delegate = mock(AmpsConnectionFactoryProvider.class);
        AmpsTracer ampsTracer = mock(AmpsTracer.class);
        TracingAmpsConnectionFactoryProvider connectionFactoryProvider =
                new TracingAmpsConnectionFactoryProvider(delegate, ampsTracer);
        AmpsBinderConfigurationProperties properties = new AmpsBinderConfigurationProperties();

        AmpsConnectionFactory ampsConnectionFactory = mock(AmpsConnectionFactory.class);

        when(delegate.getConnectionFactory(properties)).thenReturn(ampsConnectionFactory);

        Assertions.assertTrue(connectionFactoryProvider.getConnectionFactory(properties)
                instanceof TracingAmpsConnectionFactory);
    }

}