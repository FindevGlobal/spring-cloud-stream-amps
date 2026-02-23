# Message Headers

The AMPS binder defines a set of message headers for conveying AMPS-specific metadata. These headers are available on consumed messages and can be set on outgoing messages.

All header constants are defined in the `AmpsMessageHeaders` class.

## Standard Headers

These headers are automatically populated on consumed messages:

| Header Key          | Constant                            | Type     | Direction | Description                                                                                                                                                                   |
| ------------------- | ----------------------------------- | -------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ampsTopic`         | `AmpsMessageHeaders.TOPIC`          | `String` | Inbound   | The AMPS topic the message was received from                                                                                                                                  |
| `ampsCorrelationId` | `AmpsMessageHeaders.CORRELATION_ID` | `String` | Both      | The correlation ID for this message. An arbitrary string that can be used for application purposes. It is not interpreted by AMPS. Must only contain valid Base64 characters. |
| `ampsBookmark`      | `AmpsMessageHeaders.BOOKMARK`       | `String` | Inbound   | The AMPS Bookmark for this message — an identifier assigned by AMPS to locate a message in the transaction log                                                                |
| `ampsTimestamp`     | `AmpsMessageHeaders.TIMESTAMP`      | `String` | Inbound   | The timestamp for this message, an ISO-8601 formatted string set by AMPS at the time it processes the message (when `withTimestamp=true`)                                     |

### Example: Reading Headers

```java
@Bean
public Consumer<Message<byte[]>> consume() {
    return message -> {
        String topic = message.getHeaders().get("ampsTopic", String.class);
        String bookmark = message.getHeaders().get("ampsBookmark", String.class);
        String timestamp = message.getHeaders().get("ampsTimestamp", String.class);

        System.out.println("Topic: " + topic);
        System.out.println("Bookmark: " + bookmark);
        System.out.println("Timestamp: " + timestamp);
    };
}
```

## Header Encoding Headers

These headers control the AMPS header converter, which encodes Spring message headers into the AMPS correlation ID:

| Header Key                | Constant                                   | Type                  | Direction | Description                                                   |
| ------------------------- | ------------------------------------------ | --------------------- | --------- | ------------------------------------------------------------- |
| `ampsMessageClass`        | `AmpsMessageHeaders.MESSAGE_CLASS`         | `String`              | Both      | Message class name for header encoding                        |
| `ampsMessageVersion`      | `AmpsMessageHeaders.MESSAGE_VERSION`       | `String`              | Both      | Message class version                                         |
| `ampsMessageContentType`  | `AmpsMessageHeaders.MESSAGE_CONTENT_TYPE`  | `String`              | Both      | Content type of the message                                   |
| `ampsMessageHeaderParams` | `AmpsMessageHeaders.MESSAGE_HEADER_PARAMS` | `Map<String, String>` | Both      | Map of additional key-value parameters                        |
| `ampsPublishHeader`       | `AmpsMessageHeaders.PUBLISH_AMPS_HEADER`   | `Boolean`             | Outbound  | Set to `true` to include AMPS header on this specific message |

## AMPS Header Encoding

When `publishAmpsHeader` is enabled (globally or per-message), the `AmpsHeaderConverter` encodes the following headers into the AMPS correlation ID:

1. `ampsMessageContentType` → `contentType` field
2. `ampsMessageClass` → `messageClass` field
3. `ampsMessageVersion` → `messageVersion` field
4. `ampsMessageHeaderParams` → additional fields from the map

The default encoding is **Base64(JSON)**. For example:

```json
{
  "contentType": "application/json",
  "messageClass": "Order",
  "messageVersion": "1.0",
  "traceId": "abc123",
  "spanId": "def456"
}
```

Is encoded as:

```
eyJjb250ZW50VHlwZSI6ImFwcGxpY2F0aW9uL2pzb24iLCJtZXNzYWdlQ2xhc3MiOiJPcmRlciIsIm1lc3NhZ2VWZXJzaW9uIjoiMS4wIiwidHJhY2VJZCI6ImFiYzEyMyIsInNwYW5JZCI6ImRlZjQ1NiJ9
```

On the consumer side, the correlation ID is decoded back into the individual headers.

### Example: Publishing with Custom Headers

```java
@Bean
public Function<String, Message<String>> process() {
    return input -> {
        Map<String, String> params = new HashMap<>();
        params.put("source", "order-service");
        params.put("region", "us-east");

        return MessageBuilder.withPayload(input)
            .setHeader(AmpsMessageHeaders.PUBLISH_AMPS_HEADER, true)
            .setHeader(AmpsMessageHeaders.MESSAGE_CLASS, "Order")
            .setHeader(AmpsMessageHeaders.MESSAGE_VERSION, "2.0")
            .setHeader(AmpsMessageHeaders.MESSAGE_CONTENT_TYPE, "application/json")
            .setHeader(AmpsMessageHeaders.MESSAGE_HEADER_PARAMS, params)
            .build();
    };
}
```
