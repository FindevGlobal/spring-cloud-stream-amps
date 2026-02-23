package com.findevglobal.cloud.stream.tracer.amps;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.mock.MockTracer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class OpenTracingTracerTest {

    private MockTracer tracer;
    private OpenTracingTracer openTracingTracer;

    @BeforeEach
    public void init() {
        tracer = new MockTracer();
        openTracingTracer = new OpenTracingTracer(tracer);
    }

    @Test
    public void consumeInScope() {
        AtomicReference<String> spanId = new AtomicReference<>();
        AtomicReference<String> traceId = new AtomicReference<>();

        openTracingTracer.consumeInScope(new HashMap<>(), "topic", () -> {
            Span span = tracer.activeSpan();
            Assertions.assertNotNull(span);
            SpanContext context = span.context();
            spanId.set(context.toSpanId());
            traceId.set(context.toTraceId());
        });
        Assertions.assertNotNull(spanId.get());
        Assertions.assertNotNull(traceId.get());
    }

    @Test
    public void publishInScope() {
        AtomicReference<Map<String, String>> params = new AtomicReference<>();
        openTracingTracer.publishInScope("topic", params::set);
        Assertions.assertNotNull(
                params.get().get("traceid")
        );
        Assertions.assertNotNull(
                params.get().get("spanid")
        );
    }
}
