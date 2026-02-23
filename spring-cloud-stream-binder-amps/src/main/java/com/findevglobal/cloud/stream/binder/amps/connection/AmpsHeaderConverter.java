package com.findevglobal.cloud.stream.binder.amps.connection;

import org.springframework.messaging.MessageHeaders;

import java.util.Optional;

/**
 * Converter for message headers to correlation id and vice versa
 */
public interface AmpsHeaderConverter {

    /**
     * Convert message headers to correlation id
     * @param messageHeaders
     * @return correlation id
     */
    String toCorrelationId(MessageHeaders messageHeaders);

    /**
     * Convert correlation id to message headers
     * @param correlationId
     * @return message headers
     */
    MessageHeaders toMessageHeaders(String correlationId);

    default <T> T get(final MessageHeaders messageHeaders,
                            final String key,
                            final Class<T> clazz,
                            final T defaultValue) {
        return get(messageHeaders, key, clazz).orElse(defaultValue);
    }

    default <T> Optional<T> get(final MessageHeaders messageHeaders,
                                      final String key,
                                      final Class<T> clazz) {
        try {
            return Optional.ofNullable(messageHeaders).map(headers -> headers.get(key, clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
