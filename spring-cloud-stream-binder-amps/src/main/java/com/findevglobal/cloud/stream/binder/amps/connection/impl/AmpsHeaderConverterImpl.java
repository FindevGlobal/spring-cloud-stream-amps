package com.findevglobal.cloud.stream.binder.amps.connection.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AmpsHeaderConverterImpl implements AmpsHeaderConverter {

    private final Logger log = LoggerFactory.getLogger(AmpsHeaderConverterImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toCorrelationId(MessageHeaders messageHeaders) {
        Map<?, ?> params = get(messageHeaders,
                AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, Map.class, new HashMap<>());
        Map<String, String> headerParams = params.entrySet().stream()
                .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
        headerParams.computeIfAbsent(
                AmpsMessageHeaders.MESSAGE_CONTENT_TYPE,
                k -> get(messageHeaders, AmpsMessageHeaders.MESSAGE_CONTENT_TYPE, String.class, null)
        );
        headerParams.computeIfAbsent(
                AmpsMessageHeaders.MESSAGE_CLASS,
                k -> get(messageHeaders, AmpsMessageHeaders.MESSAGE_CLASS, String.class, null)
        );
        headerParams.computeIfAbsent(
                AmpsMessageHeaders.MESSAGE_VERSION,
                k -> get(messageHeaders, AmpsMessageHeaders.MESSAGE_VERSION, String.class, null)
        );
        String correlationId = null;
        try {
            correlationId = objectMapper.writeValueAsString(headerParams);
        } catch (Exception e) {
            log.error("Error when converting headers to string: {}", headerParams, e);
        }
        return correlationId == null
                ? null
                : Base64.getEncoder().encodeToString(correlationId.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public MessageHeaders toMessageHeaders(final String correlationId) {
        if (correlationId == null) {
            return new MessageHeaders(new HashMap<>());
        }
        try {
            String decodedCorrelationId = new String(Base64.getDecoder().decode(correlationId), StandardCharsets.UTF_8);
            Map<String, String> headerParams = objectMapper.readValue(decodedCorrelationId,  new TypeReference<Map<String, String>>() {});
            Map<String, Object> messageHeaders = new HashMap<>();
            String messageContentType = headerParams.remove(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE);
            if (messageContentType != null) {
                messageHeaders.put(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE, messageContentType);
            }
            String messageClass = headerParams.remove(AmpsMessageHeaders.MESSAGE_CLASS);
            if (messageClass != null) {
                messageHeaders.put(AmpsMessageHeaders.MESSAGE_CLASS, messageClass);
            }
            String messageVersion = headerParams.remove(AmpsMessageHeaders.MESSAGE_VERSION);
            if (messageVersion != null) {
                messageHeaders.put(AmpsMessageHeaders.MESSAGE_VERSION, messageVersion);
            }
            messageHeaders.put(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, headerParams);
            return new MessageHeaders(messageHeaders);
        } catch (Exception e) {
            log.warn("Error when converting correlation id to headers: {}", correlationId, e);
            return new MessageHeaders(new HashMap<>());
        }
    }
}
