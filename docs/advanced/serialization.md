# Message Serialization

Spring Cloud Stream provides a flexible content-type negotiation and message serialization mechanism. The AMPS binder leverages this fully, allowing you to use JSON, Avro, XML, or custom serialization formats.

## Content-Type Resolution

Spring Cloud Stream resolves the `contentType` for message conversion using three mechanisms, in order of precedence:

### 1. Message Header

Set the `contentType` directly on the message header. This takes the highest priority:

```java
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.http.MediaType;

public void sendMessage(Detail detail) {
    producer.output().send(
        MessageBuilder.withPayload(detail)
            .setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
            .build()
    );
}
```

### 2. Binding Configuration

Set the `contentType` per binding in `application.yml`:

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: /topic
          content-type: application/xml
        input:
          destination: /topic/queue
          content-type: application/xml
```

### 3. Default

If `contentType` is not set in either the message header or the binding, the default `application/json` is used.

---

## JSON (Default)

JSON is the default serialization format. No additional configuration is needed:

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: /topic
          # content-type: application/json  (implicit default)
```

Spring Cloud Stream automatically uses Jackson to serialize/deserialize POJOs to/from JSON.

---

## Avro Serialization

The binder supports Apache Avro serialization for both JSON-encoded and binary-encoded Avro messages.

### Avro JSON

Set the content-type to `application/avro+json`:

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: /topic
          content-type: application/avro+json
        input:
          destination: /topic/queue
          content-type: application/avro+json
```

### Avro Binary

Set the content-type to `application/avro+binary`:

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: /topic
          content-type: application/avro+binary
        input:
          destination: /topic/queue
          content-type: application/avro+binary
```

!!! note
Avro serialization requires the appropriate Avro message converter to be on the classpath. POJO classes should be Avro-generated.

---

## Custom MessageConverter

You can register custom `MessageConverter` implementations to support additional content types. Implement `org.springframework.messaging.converter.AbstractMessageConverter` and register it as a Spring `@Bean`:

```java
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeTypeUtils;
import org.springframework.stereotype.Component;

@Component
public class XmlMessageConverter extends AbstractMessageConverter {

    public XmlMessageConverter() {
        super(MimeTypeUtils.TEXT_XML);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Detail.class == clazz;
    }

    @Override
    protected Object convertToInternal(
            Object payload,
            MessageHeaders headers,
            Object conversionHint) {
        // Custom serialization logic: POJO → XML string/bytes
        return serializeToXml(payload);
    }

    @Override
    protected Object convertFromInternal(
            Message<?> message,
            Class<?> targetClass,
            Object conversionHint) {
        // Custom deserialization logic: XML bytes → POJO
        return deserializeFromXml(message.getPayload(), targetClass);
    }
}
```

The custom converter is automatically appended to the existing stack of `MessageConverter` beans. Then configure the binding to use the matching content-type:

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: /topic
          content-type: text/xml
        input:
          destination: /topic/queue
          content-type: text/xml
```

---

## Matching Content-Type to AMPS Message Type

AMPS topics are configured with a specific message type (e.g., `json`, `xml`, `binary`, `nvfix`). The message type is specified in the broker URI path:

```yaml
spring.cloud.stream.amps.binder:
  brokers:
    - tcp://amps-server:50000/json # JSON message type
    - tcp://amps-server:50001/nvfix # NVFIX message type
```

!!! tip "Best Practice"
Ensure the Spring Cloud Stream `contentType` format matches the AMPS topic message type. For example, use `application/json` content-type with AMPS topics configured for `json` message type.

| AMPS Message Type | Typical Spring Content-Type     |
| ----------------- | ------------------------------- |
| `json`            | `application/json`              |
| `xml`             | `application/xml` or `text/xml` |
| `binary`          | `application/octet-stream`      |
| `nvfix`           | Custom converter required       |
| `fix`             | Custom converter required       |

---

## Complete Example

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          brokers:
            - tcp://amps-server:50000/json
      bindings:
        orders-out-0:
          destination: /orders
          content-type: application/avro+json
        orders-in-0:
          destination: /orders/queue
          content-type: application/avro+json
```
