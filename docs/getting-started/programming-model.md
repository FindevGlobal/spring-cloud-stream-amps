# Programming Model

The AMPS binder supports the **functional** model.

## Functional Programming Model

Starting with Spring Cloud Stream 3.x, the recommended approach is to use standard Java functional interfaces (`Consumer`, `Function`, `Supplier`) exposed as Spring beans.

### Consumer (Receiving Messages)

Create a class implementing `java.util.function.Consumer` and register it as a Spring `@Component`:

```java
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer implements Consumer<Detail> {

    @Override
    public void accept(Detail dto) {
        System.out.println("input: " + dto);
    }
}
```

The binding name is derived from the bean name: `messageConsumer-in-0` (bean name + `-in-` + index).

```yaml
spring:
  cloud:
    stream:
      bindings:
        messageConsumer-in-0:
          destination: /topic1/queue
```

### Producer (Sending Messages)

Use `StreamBridge` to send messages programmatically:

```java
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {
    private final StreamBridge streamBridge;

    public MessageProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendMessage(Detail dto) {
        streamBridge.send("output", dto);
    }
}
```

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: /topic1
```

### Processor (Receiving and Sending)

Implement `java.util.function.Function` to receive a message, process it, and forward the result:

```java
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessor implements Function<Detail, Detail> {

    @Override
    public Detail apply(Detail detail) {
        System.out.println("input:" + detail);
        return detail;
    }
}
```

Specify both input and output bindings:

```yaml
spring:
  cloud:
    stream:
      bindings:
        messageProcessor-in-0:
          destination: /topic1/queue
        messageProcessor-out-0:
          destination: /topic2
```

### Reactive Streams

You can use Project Reactor's `Flux` for reactive processing:

```java
import java.util.function.Function;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class MessageProcessor implements Function<Flux<String>, Flux<String>> {

    @Override
    public Flux<String> apply(Flux<String> input) {
        return input.map(String::toUpperCase);
    }
}
```

---

## Destination Naming

AMPS uses topic names as destinations. For queue-based consumption, append the queue name to the topic path:

| Pattern        | Description                               |
| -------------- | ----------------------------------------- |
| `/topic`       | Plain topic for pub/sub                   |
| `/topic/queue` | Queue on a topic (at-least-once delivery) |

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: /topic1
        input:
          destination: /topic1/queue
```

!!! tip "AMPS Queues"
When consuming from a queue, each message is delivered to only one consumer (work-queue semantics). This is useful for load distribution across multiple consumer instances.

---

## Reading AMPS Message Headers

Regardless of the programming model, you can access AMPS-specific headers on consumed messages:

```java
import org.springframework.messaging.Message;
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;

public void process(Message<Detail> message) {
    System.out.println("CORRELATION_ID: " +
        message.getHeaders().get(AmpsMessageHeaders.CORRELATION_ID));
    System.out.println("TIMESTAMP: " +
        message.getHeaders().get(AmpsMessageHeaders.TIMESTAMP));
    System.out.println("BOOKMARK: " +
        message.getHeaders().get(AmpsMessageHeaders.BOOKMARK));
    System.out.println("PAYLOAD: " + message.getPayload());
}
```

See [Message Headers](../reference/headers.md) for the full list of available headers.

## Setting Correlation ID on Publish

You can set a custom AMPS correlation ID when publishing a message:

```java
import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

public void sendMessage(Detail detail, String correlationId) {
    producer.output().send(
        MessageBuilder.withPayload(detail)
            .setHeader(AmpsMessageHeaders.CORRELATION_ID, correlationId)
            .build()
    );
}
```

!!! note "Correlation ID Encoding"
The correlation ID is an arbitrary string, but it must only contain characters that are valid Base64-encoded characters. See [Custom Header Propagation](../advanced/headers.md) for details on automatic header encoding.
