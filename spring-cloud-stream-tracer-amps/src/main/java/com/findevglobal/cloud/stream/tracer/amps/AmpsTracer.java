package com.findevglobal.cloud.stream.tracer.amps;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Amps tracer
 */
public interface AmpsTracer {

    /**
     * Tracing a consuming amps message
     */
    void consumeInScope(Map<String, String> ampsMessageHeaders, String messageTopic, Runnable runnable);

    /**
     * Tracing a producing amps message
     */
    void publishInScope(String messageTopic, Consumer<Map<String, String>> runnable);
}
