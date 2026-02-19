package com.findevglobal.cloud.stream.tracer.amps;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class OpenTracingTracer implements AmpsTracer {

    private static final Logger log = LoggerFactory.getLogger(OpenTracingTracer.class);

    private final Tracer tracer;

    public OpenTracingTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void consumeInScope(Map<String, String> ampsMessageHeaders,
                               String messageTopic,
                               Runnable runnable) {
        Tracer.SpanBuilder spanBuilder = tracer
                .buildSpan(TracingConstants.AMPS_CONSUMER)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                .withTag(Tags.COMPONENT.getKey(), TracingConstants.AMPS_COMPONENT)
                .withTag(TracingConstants.AMPS_TOPIC, Optional.ofNullable(messageTopic).orElse(""));

        SpanContext parent = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(ampsMessageHeaders));
        if (parent != null) {
            spanBuilder.asChildOf(parent);
        }
        Span span = spanBuilder.start();
        try (Scope scope = tracer.activateSpan(span)) {
            if (log.isDebugEnabled()) {
                log.debug("Created consumer span {}", span);
            }
            runnable.run();
        } catch (Throwable e) {
            String text = e.getMessage();
            if (text == null) {
                text = e.getClass().getSimpleName();
            }
            span.setTag(Tags.ERROR.getKey(), text);
            throw e;
        } finally {
            span.finish();
        }
    }

    @Override
    public void publishInScope(String messageTopic, Consumer<Map<String, String>> runnable) {
        Span span = tracer
                .buildSpan(TracingConstants.AMPS_PRODUCER)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
                .withTag(Tags.COMPONENT.getKey(), TracingConstants.AMPS_COMPONENT)
                .withTag(TracingConstants.AMPS_TOPIC, Optional.ofNullable(messageTopic).orElse(""))
                .start();
        Map<String, String> tracingHeaders = new HashMap<>();
        tracer.inject(span.context(), Format.Builtin.TEXT_MAP, new TextMapAdapter(tracingHeaders));
        try (Scope scope = tracer.activateSpan(span)) {
            if (log.isDebugEnabled()) {
                log.debug("Created producer span {}", span);
            }
            runnable.accept(tracingHeaders);
        } catch (Throwable e) {
            String text = e.getMessage();
            if (text == null) {
                text = e.getClass().getSimpleName();
            }
            span.setTag(Tags.ERROR.getKey(), text);
            throw e;
        } finally {
            span.finish();
        }
    }
}
