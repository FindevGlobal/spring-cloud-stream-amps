# Custom Header Propagation

The AMPS binder provides a mechanism for propagating custom metadata through AMPS messages via the correlation ID. This is the same mechanism used by the distributed tracing module.

## How It Works

AMPS messages don't have a general-purpose header map like Kafka or JMS. Instead, the binder uses the **AMPS correlation ID** field to carry encoded metadata:

```
Spring Message Headers  →  JSON  →  Base64  →  AMPS Correlation ID
```

On the consumer side, the process is reversed:

```
AMPS Correlation ID  →  Base64 decode  →  JSON parse  →  Spring Message Headers
```

## Enabling Header Propagation

Header propagation requires one of:

- **Global:** Set `spring.cloud.stream.amps.binder.publishAmpsHeader=true`
- **Per-message:** Set the `ampsPublishHeader` header to `true` on individual messages

## Sending Custom Headers

### Via `ampsMessageHeaderParams`

The `ampsMessageHeaderParams` header accepts a `Map<String, String>` of arbitrary key-value pairs:

```java
@Bean
public Function<String, Message<String>> enrichAndForward() {
    return input -> {
        Map<String, String> params = new HashMap<>();
        params.put("source-system", "order-gateway");
        params.put("processing-tier", "fast");
        params.put("correlation-key", UUID.randomUUID().toString());

        return MessageBuilder.withPayload(input)
            .setHeader(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, true)
            .setHeader(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, params)
            .build();
    };
}
```

### Via Typed Headers

For well-known metadata, use the dedicated headers:

```java
MessageBuilder.withPayload(payload)
    .setHeader(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, true)
    .setHeader(AmpsMessageHeaders.MESSAGE_CLASS, "TradeConfirmation")
    .setHeader(AmpsMessageHeaders.MESSAGE_VERSION, "3.1")
    .setHeader(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE, "application/json")
    .build();
```

## Receiving Custom Headers

On the consumer side, all encoded headers are automatically decoded:

```java
@Bean
public Consumer<Message<byte[]>> consume() {
    return message -> {
        // Typed headers
        String messageClass = message.getHeaders()
            .get(AmpsMessageHeaders.MESSAGE_CLASS, String.class);

        // Custom params
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) message.getHeaders()
            .get(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS);

        if (params != null) {
            String source = params.get("source-system");
            System.out.println("Source: " + source);
        }
    };
}
```

## Custom Header Converter

The default `AmpsHeaderConverterImpl` uses JSON + Base64 encoding. To customize this behavior (e.g., use a more compact binary format), implement the `AmpsHeaderConverter` interface:

```java
public interface AmpsHeaderConverter {
    /**
     * Encode Spring message headers into an AMPS correlation ID string.
     */
    String toCorrelationId(MessageHeaders headers);

    /**
     * Decode an AMPS correlation ID string back into Spring message headers.
     */
    Map<String, Object> toMessageHeaders(String correlationId);
}
```

Register it as a bean and reference it in the binder configuration:

```java
@Bean
public AmpsHeaderConverter compactHeaderConverter() {
    return new MyCompactHeaderConverter();
}
```

```yaml
spring.cloud.stream.amps.binder:
  ampsHeaderConverterBeanName: compactHeaderConverter
```

## Encoded Format

The default encoding produces a JSON object, which is then Base64-encoded:

```json
{
  "contentType": "application/json",
  "messageClass": "Order",
  "messageVersion": "1.0",
  "source-system": "order-gateway",
  "processing-tier": "fast"
}
```

The `contentType`, `messageClass`, and `messageVersion` fields are extracted into their dedicated headers on decode. All other fields are placed in the `ampsMessageHeaderParams` map.
