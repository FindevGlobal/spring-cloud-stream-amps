package com.findevglobal.cloud.stream.tracer.amps;

import io.micrometer.tracing.propagation.Propagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class MicrometerTracer implements AmpsTracer {
    private static final Logger log = LoggerFactory.getLogger(MicrometerTracer.class);
    private Tracer tracer;
    private Propagator propagator;
    private final Propagator.Setter<Map<String, String>> injector = (carrier, key, value) -> {
        if (carrier != null) {
            carrier.put(key, value);
        }
    };
    private final Propagator.Getter<Map<String, String>> extractor = (carrier, key) -> Optional.ofNullable(carrier)
            .map(c -> c.get(key))
            .orElse(null);

    private final BeanFactory beanFactory;

    public MicrometerTracer(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    private Tracer tracer() {
        if (this.tracer == null) {
            this.tracer = this.beanFactory.getBean(Tracer.class);
        }
        return this.tracer;
    }

    private Propagator propagator() {
        if (this.propagator == null) {
            this.propagator = this.beanFactory.getBean(Propagator.class);
        }
        return this.propagator;
    }

    @Override
    public void consumeInScope(Map<String, String> ampsMessageHeaders,
                               String messageTopic,
                               Runnable runnable) {
        var spanBuilder = propagator()
                .extract(ampsMessageHeaders, extractor)
                .name(TracingConstants.AMPS_CONSUMER)
                .kind(Span.Kind.CONSUMER);

        if (StringUtils.hasLength(messageTopic)) {
            spanBuilder.tag(TracingConstants.AMPS_TOPIC, messageTopic);
        }

        Span span = spanBuilder.start();

        try (Tracer.SpanInScope spanInScope = tracer().withSpan(span)) {
            if (log.isDebugEnabled()) {
                log.debug("Created consumer span {}", span);
            }
            runnable.run();
        } catch (Throwable e) {
            String text = e.getMessage();
            if (text == null) {
                text = e.getClass().getSimpleName();
            }
            span.tag("error", text);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public void publishInScope(String messageTopic, Consumer<Map<String, String>> runnable) {
        var spanBuilder = tracer().spanBuilder()
                .name(TracingConstants.AMPS_PRODUCER)
                .kind(Span.Kind.PRODUCER);
        if (StringUtils.hasLength(messageTopic)) {
            spanBuilder.tag(TracingConstants.AMPS_TOPIC, messageTopic);
        }

        Span span = spanBuilder.start();
        Map<String, String> tracingHeaders = new HashMap<>();
        propagator().inject(span.context(), tracingHeaders, injector);

        try (Tracer.SpanInScope spanInScope = tracer().withSpan(span)) {
            if (log.isDebugEnabled()) {
                log.debug("Created producer span {}", span);
            }
            runnable.accept(tracingHeaders);
        } catch (Throwable e) {
            String message = e.getMessage();
            if (message == null) {
                message = e.getClass().getSimpleName();
            }
            span.tag("error", message);
            throw e;
        } finally {
            span.end();
        }
    }
}