package com.findevglobal.cloud.stream.tracer.amps;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;

public class TracingAmpsConnectionFactory implements AmpsConnectionFactory {
    private final AmpsConnectionFactory delegate;
    private final AmpsTracer ampsTracer;

    public TracingAmpsConnectionFactory(AmpsConnectionFactory delegate, AmpsTracer ampsTracer) {
        this.delegate = delegate;
        this.ampsTracer = ampsTracer;
    }

    @Override
    public AmpsConnection getConnection() {
        return new TracingAmpsConnection(delegate.getConnection(), ampsTracer);
    }
}
