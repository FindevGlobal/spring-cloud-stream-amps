package com.findevglobal.cloud.stream.tracer.amps;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;

public class TracingAmpsConnectionFactoryProvider implements AmpsConnectionFactoryProvider {

    private final AmpsConnectionFactoryProvider delegate;
    private final AmpsTracer ampsTracer;

    public TracingAmpsConnectionFactoryProvider(AmpsConnectionFactoryProvider delegate,
                                                AmpsTracer ampsTracer) {
        this.delegate = delegate;
        this.ampsTracer = ampsTracer;
    }

    @Override
    public AmpsConnectionFactory getConnectionFactory(AmpsBinderConfigurationProperties properties) {
        return new TracingAmpsConnectionFactory(delegate.getConnectionFactory(properties), ampsTracer);
    }
}
