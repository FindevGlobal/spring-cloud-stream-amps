package com.findevglobal.cloud.stream.tracer.amps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.docs.AssertingSpanBuilder;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SleuthTracer implements AmpsTracer {
    private static final Logger log = LoggerFactory.getLogger(SleuthTracer.class);
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

    public SleuthTracer(BeanFactory beanFactory) {
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
        AssertingSpanBuilder spanBuilder = AssertingSpanBuilder
                .of(AmpsSpan.AMPS_CONSUMER_SPAN,
                        propagator().extract(ampsMessageHeaders, extractor).kind(Span.Kind.CONSUMER))
                .name(AmpsSpan.AMPS_CONSUMER_SPAN.getName());
        if (StringUtils.hasLength(messageTopic)) {
            spanBuilder.tag(AmpsSpan.ConsumerTags.TOPIC, messageTopic);
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
        AssertingSpanBuilder spanBuilder = AssertingSpanBuilder
                .of(AmpsSpan.AMPS_PRODUCER_SPAN, tracer().spanBuilder().kind(Span.Kind.PRODUCER))
                .name(AmpsSpan.AMPS_PRODUCER_SPAN.getName());
        if (StringUtils.hasLength(messageTopic)) {
            spanBuilder.tag(AmpsSpan.ProducerTags.TOPIC, messageTopic);
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