package com.findevglobal.cloud.stream.tracer.amps;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class TracingAmpsConnectionFactoryProviderBeanPostProcessorTest {

    private final TracingAmpsConnectionFactoryProviderBeanPostProcessor beanPostProcessor =
            new TracingAmpsConnectionFactoryProviderBeanPostProcessor(mock(AmpsTracer.class));

    @Test
    public void wrapAmpsConnectionFactoryProvider() {
        Assertions.assertTrue(
                beanPostProcessor.postProcessAfterInitialization(mock(AmpsConnectionFactoryProvider.class), "test")
                instanceof TracingAmpsConnectionFactoryProvider
        );
    }

    @Test
    public void ignoreTracingAmpsConnectionFactoryProvider() {
        TracingAmpsConnectionFactoryProvider tracingAmpsConnectionFactoryProvider
                = mock(TracingAmpsConnectionFactoryProvider.class);
        Assertions.assertSame(
                tracingAmpsConnectionFactoryProvider,
                beanPostProcessor.postProcessAfterInitialization(tracingAmpsConnectionFactoryProvider, "test")
        );
    }

    @Test
    public void ignoreOtherClasses() {
        Object bean = new Object();
        Assertions.assertSame(
                bean,
                beanPostProcessor.postProcessAfterInitialization(bean, "test")
        );
    }
}