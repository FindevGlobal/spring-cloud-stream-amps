package com.findevglobal.cloud.stream.tracer.amps;

import brave.Tracing;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BravePropagator;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.propagation.Propagator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MicrometerTracerTest {

    private BeanFactory beanFactory;
    private MicrometerTracer micrometerTracer;

    @BeforeEach
    public void init() {
        beanFactory = mock(BeanFactory.class);
        micrometerTracer = new MicrometerTracer(beanFactory);
    }

    @Test
    public void consumeInScope() {
        Tracing tracing = Tracing.newBuilder().build();
        BraveTracer tracer = new BraveTracer(
                tracing.tracer(),
                new BraveCurrentTraceContext(tracing.currentTraceContext()),
                new BraveBaggageManager()
        );
        BravePropagator propagator = new BravePropagator(tracing);
        when(beanFactory.getBean(Tracer.class)).thenReturn(tracer);
        when(beanFactory.getBean(Propagator.class)).thenReturn(propagator);
        AtomicReference<String> spanId = new AtomicReference<>();
        AtomicReference<String> traceId = new AtomicReference<>();

        micrometerTracer.consumeInScope(new HashMap<>(), "topic", () -> {
            Span span = tracer.currentSpan();
            Assertions.assertNotNull(span);
            TraceContext context = span.context();
            spanId.set(context.spanId());
            traceId.set(context.traceId());
        });
        Assertions.assertNotNull(spanId.get());
        Assertions.assertNotNull(traceId.get());
    }

    @Test
    public void publishInScope() {
        Tracing tracing = Tracing.newBuilder().build();
        BraveTracer tracer = new BraveTracer(
                tracing.tracer(),
                new BraveCurrentTraceContext(tracing.currentTraceContext()),
                new BraveBaggageManager()
        );
        BravePropagator propagator = new BravePropagator(tracing);
        when(beanFactory.getBean(Tracer.class)).thenReturn(tracer);
        when(beanFactory.getBean(Propagator.class)).thenReturn(propagator);

        AtomicReference<Map<String, String>> params = new AtomicReference<>();
        micrometerTracer.publishInScope("topic", params::set);
        Assertions.assertNotNull(
                params.get().get("X-B3-Sampled")
        );
        Assertions.assertNotNull(
                params.get().get("X-B3-TraceId")
        );
        Assertions.assertNotNull(
                params.get().get("X-B3-SpanId")
        );
    }
}
