package com.findevglobal.cloud.stream.binder.amps.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import com.findevglobal.cloud.stream.binder.amps.connection.impl.AmpsHeaderConverterImpl;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class AmpsHeaderConverterImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AmpsHeaderConverterImpl converter = new AmpsHeaderConverterImpl();

    @Test
    public void testToCorrelationIdWithAllHeaders() {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        headers.put(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, singletonMap("CUSTOM_HEADER", "CUSTOM_VALUE"));
        headers.put(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE, "JSON");
        headers.put(AmpsMessageHeaders.MESSAGE_CLASS, "TestClass");
        headers.put(AmpsMessageHeaders.MESSAGE_VERSION, "1.0");
        MessageHeaders messageHeaders = new MessageHeaders(headers);

        // Act
        String correlationId = converter.toCorrelationId(messageHeaders);

        // Assert
        Assertions.assertNotNull(correlationId);
        Map<String, String> resultMap = decodeToMap(correlationId);
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("CUSTOM_HEADER", "CUSTOM_VALUE");
        expectedMap.put(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE, "JSON");
        expectedMap.put(AmpsMessageHeaders.MESSAGE_CLASS, "TestClass");
        expectedMap.put(AmpsMessageHeaders.MESSAGE_VERSION, "1.0");

        Assertions.assertEquals(expectedMap, resultMap);
    }

    @Test
    public void testToCorrelationIdWithOnlyParams() {
        // Arrange
        Map<String, Object> headerParams = new HashMap<>();
        headerParams.put("CUSTOM_HEADER", "CUSTOM_VALUE");
        headerParams.put("ANOTHER_HEADER", "ANOTHER_VALUE");

        Map<String, Object> headers = new HashMap<>();
        headers.put(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, headerParams);
        MessageHeaders messageHeaders = new MessageHeaders(headers);

        // Act
        String correlationId = converter.toCorrelationId(messageHeaders);

        // Assert
        Assertions.assertNotNull(correlationId);
        Map<String, String> resultMap = decodeToMap(correlationId);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("CUSTOM_HEADER", "CUSTOM_VALUE");
        expectedMap.put("ANOTHER_HEADER", "ANOTHER_VALUE");

        Assertions.assertEquals(expectedMap, resultMap);
    }

    @Test
    public void testToCorrelationIdWithNonStringParams() {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        Map<Object, Object> params = new HashMap<>();
        params.put("STRING_KEY", "STRING_VALUE");
        params.put(123, "NUMBER_KEY");
        params.put("NUMBER_VALUE", 456);
        params.put(LocalDateTime.now(), "DATE_VALUE");
        headers.put(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, params);
        MessageHeaders messageHeaders = new MessageHeaders(headers);

        // Act
        String correlationId = converter.toCorrelationId(messageHeaders);

        // Assert
        Assertions.assertNotNull(correlationId);
        Map<String, String> resultMap = decodeToMap(correlationId);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("STRING_KEY", "STRING_VALUE");
        // Non-string keys and values should be filtered out

        Assertions.assertEquals(expectedMap, resultMap);
    }

    @Test
    public void testToCorrelationIdEmptyHeaders() {
        // Arrange
        MessageHeaders messageHeaders = new MessageHeaders(new HashMap<>());

        // Act
        String correlationId = converter.toCorrelationId(messageHeaders);

        // Assert
        Assertions.assertNotNull(correlationId);
        Map<String, String> resultMap = decodeToMap(correlationId);

        Map<String, String> expectedMap = new HashMap<>();
        // Empty map expected

        Assertions.assertEquals(expectedMap, resultMap);
    }

    private @Nullable Map<String, String> decodeToMap(final String correlationId) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(correlationId), StandardCharsets.UTF_8);
            return objectMapper.readValue(decodedJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            Assertions.fail("Failed to decode correlation ID: " + e.getMessage());
            return null;
        }
    }

    @Test
    public void testToMessageHeadersWithValidCorrelationId() throws Exception {
        // Arrange
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE, "JSON");
        headerParams.put(AmpsMessageHeaders.MESSAGE_CLASS, "TestClass");
        headerParams.put(AmpsMessageHeaders.MESSAGE_VERSION, "1.0");
        headerParams.put("CUSTOM_HEADER", "CUSTOM_VALUE");
        String correlationId = Base64.getEncoder().encodeToString(
                objectMapper.writeValueAsString(headerParams).getBytes(StandardCharsets.UTF_8));

        // Act
        MessageHeaders messageHeaders = converter.toMessageHeaders(correlationId);

        // Assert
        Assertions.assertNotNull(messageHeaders);
        Assertions.assertEquals("JSON", messageHeaders.get(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE));
        Assertions.assertEquals("TestClass", messageHeaders.get(AmpsMessageHeaders.MESSAGE_CLASS));
        Assertions.assertEquals("1.0", messageHeaders.get(AmpsMessageHeaders.MESSAGE_VERSION));
        Assertions.assertEquals(singletonMap("CUSTOM_HEADER", "CUSTOM_VALUE"),
                messageHeaders.get(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS));
    }

    @Test
    public void testToMessageHeadersWithNullCorrelationId() {
        // Act
        MessageHeaders messageHeaders = converter.toMessageHeaders(null);

        // Assert
        Assertions.assertNotNull(messageHeaders);
        Assertions.assertEquals(2, messageHeaders.size());
        Assertions.assertTrue(messageHeaders.containsKey(MessageHeaders.ID));
        Assertions.assertTrue(messageHeaders.containsKey(MessageHeaders.TIMESTAMP));
    }

    @Test
    public void testToMessageHeadersWithInvalidCorrelationId() {
        // Arrange
        String invalidCorrelationId = "invalidCorrelationId";

        // Act
        MessageHeaders messageHeaders = converter.toMessageHeaders(invalidCorrelationId);

        // Assert
        Assertions.assertNotNull(messageHeaders);
        Assertions.assertEquals(2, messageHeaders.size());
        Assertions.assertTrue(messageHeaders.containsKey(MessageHeaders.ID));
        Assertions.assertTrue(messageHeaders.containsKey(MessageHeaders.TIMESTAMP));
    }

    @Test
    public void testToMessageHeadersWithEmptyCorrelationId() {
        // Arrange
        String emptyCorrelationId = Base64.getEncoder().encodeToString("{}".getBytes(StandardCharsets.UTF_8));

        // Act
        MessageHeaders messageHeaders = converter.toMessageHeaders(emptyCorrelationId);

        // Assert
        Assertions.assertNotNull(messageHeaders);
        Assertions.assertEquals(3, messageHeaders.size());
        Assertions.assertTrue(messageHeaders.containsKey(MessageHeaders.ID));
        Assertions.assertTrue(messageHeaders.containsKey(MessageHeaders.TIMESTAMP));
        Assertions.assertEquals(new HashMap<>(), messageHeaders.get(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS));
    }

}
