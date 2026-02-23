# Spring Cloud Stream Binder for AMPS

A [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) binder implementation for [AMPS](https://www.crankuptheamps.com/) (Advanced Message Processing System) — a high-performance messaging platform by 60East Technologies.

This binder allows Spring Cloud Stream applications to use AMPS as their messaging transport, just like the Kafka or RabbitMQ binders.

## Modules

| Module                            | Description                                                  |
| --------------------------------- | ------------------------------------------------------------ |
| `spring-cloud-stream-binder-amps` | Core binder — AMPS integration with Spring Cloud Stream      |
| `spring-cloud-stream-tracer-amps` | Distributed tracing (Sleuth / OpenTracing) for AMPS messages |

## Compatibility

| Dependency   | Version            |
| ------------ | ------------------ |
| Spring Boot  | 2.7.x              |
| Spring Cloud | 2021.0.x (Jubilee) |
| AMPS Client  | 5.3.4.0            |
| Java         | 8+                 |

## Quick Start

Add the binder dependency:

```xml
<dependency>
    <groupId>com.findevglobal.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-amps</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Configure your `application.yml`:

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          name: my-application
          brokers:
            - tcp://localhost:50000/json
      bindings:
        consume-in-0:
          destination: my-topic
        produce-out-0:
          destination: another-topic
```

Write a consumer:

```java
@SpringBootApplication
public class MyApp {

    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }

    @Bean
    public Consumer<Message<byte[]>> consume() {
        return message -> System.out.println("Received: " + new String(message.getPayload()));
    }
}
```

## Features

- **Full Spring Cloud Stream compatibility** — Use `Consumer`, `Function`, `Supplier` or legacy `@StreamListener`.
- **SOW (State of the World)** — Snapshot-only, SOW-and-subscribe, or plain subscribe modes.
- **Bookmark support** — Resume from `MOST_RECENT`, `EPOCH`, `NOW`, or a custom bookmark.
- **Content filtering** — Apply AMPS filter expressions to subscriptions.
- **HA Client** — Automatic reconnection and failover across multiple AMPS instances.
- **Multi-binder support** — Multiple named AMPS binders in a single application.
- **Distributed tracing** — Transparent trace propagation via Spring Cloud Sleuth or OpenTracing.
- **Custom header propagation** — Encode arbitrary metadata into AMPS correlation IDs.
- **Concurrent consumers** — Scale with multiple parallel AMPS connections per binding.
- **Message serialization** — JSON (default), Avro (JSON/binary), XML, or custom converters.
- **Authentication** — Username/password or custom `Authenticator` beans.

## Documentation

Full documentation is available [spring-cloud-stream-amps](https://findevglobal.github.io/spring-cloud-stream-amps):

### Documentation Sections

- **[Getting Started](docs/getting-started/overview.md)** — Overview, quick start, and programming model
- **[Reference](docs/reference/binder/configuration.md)** — Binder, consumer, and producer configuration properties
- **[Connection Management](docs/reference/connection.md)** — HA client, stores, and header converter
- **[Distributed Tracing](docs/tracing/index.md)** — Sleuth and OpenTracing integration
- **[Advanced Topics](docs/advanced/sow.md)** — SOW queries, bookmarks, multi-binder, serialization, authentication
- **[Appendix](docs/appendix/configuration-properties.md)** — All configuration properties reference

## License

This project is licensed under the [MIT License](LICENSE).
