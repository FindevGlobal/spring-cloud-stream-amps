# Quick Start

This guide walks you through building a minimal Spring Cloud Stream application that uses AMPS as the messaging transport.

## Prerequisites

- Java 17 or later
- An AMPS server instance (v5.x) running and accessible
- Maven 3.6+

## 1. Add Dependencies

The simplest way to get started is to add the AMPS binder directly, which includes the binder and all required transitive dependencies:

```xml
<dependency>
    <groupId>com.findevglobal.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-amps</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>
```

Make sure you also have the Spring Cloud Stream starter:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream</artifactId>
</dependency>
```

And, if using Spring Cloud BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2025.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 2. Configure AMPS Connection

Add the following to your `application.yml`:

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          name: my-application
          brokers:
            - tcp://localhost:50000/json
```

This tells the binder to connect to an AMPS server running on `localhost` port `50000` using the `json` message type.

## 3. Define Bindings

Map your function bindings to AMPS topics:

```yaml
spring:
  cloud:
    stream:
      bindings:
        consume-in-0:
          destination: my-topic
        produce-out-0:
          destination: another-topic
      function:
        definition: consume;produce
```

## 4. Write Your Application

=== "Consumer"

    ```java
    @SpringBootApplication
    public class AmpsConsumerApplication {

        public static void main(String[] args) {
            SpringApplication.run(AmpsConsumerApplication.class, args);
        }

        @Bean
        public Consumer<Message<byte[]>> consume() {
            return message -> {
                String payload = new String(message.getPayload());
                System.out.println("Received: " + payload);
            };
        }
    }
    ```

=== "Producer"

    ```java
    @SpringBootApplication
    public class AmpsProducerApplication {

        public static void main(String[] args) {
            SpringApplication.run(AmpsProducerApplication.class, args);
        }

        @Bean
        public Supplier<String> produce() {
            return () -> "{\"key\": \"value\"}";
        }
    }
    ```

=== "Processor (Function)"

    ```java
    @SpringBootApplication
    public class AmpsProcessorApplication {

        public static void main(String[] args) {
            SpringApplication.run(AmpsProcessorApplication.class, args);
        }

        @Bean
        public Function<Message<byte[]>, String> process() {
            return message -> {
                String input = new String(message.getPayload());
                return input.toUpperCase();
            };
        }
    }
    ```

## 5. Run the Application

```bash
mvn spring-boot:run
```

Your application will connect to the AMPS server and start consuming or producing messages.

## Complete Example Configuration

Here is a more complete configuration showcasing consumer and producer properties:

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          name: my-app
          brokers:
            - tcp://amps-server-1:50000/json
            - tcp://amps-server-2:50000/json
          heartBeatInterval: 10s
          maxReconnectTime: 30s
          publishStoreSize: 2048
        bindings:
          consume-in-0:
            consumer:
              sow: true
              filter: "/status = 'ACTIVE'"
              bookmarkType: MOST_RECENT
              ackBatchSize: 50
              ackTimeout: 5s
          produce-out-0:
            producer:
              ackType: Persisted
              timeout: 15s
              expiration: 60s
      bindings:
        consume-in-0:
          destination: orders
          group: order-processor
        produce-out-0:
          destination: processed-orders
```

## Next Steps

- Learn the [Programming Model](programming-model.md) in detail — functional, annotation-based, and reactive styles
- Learn about [AMPS Binder Configuration](../reference/binder/configuration.md)
- Explore [SOW Queries](../advanced/sow.md) for state-of-the-world subscriptions
- Configure [Message Serialization](../advanced/serialization.md) — JSON, Avro, XML, or custom
- Set up [Distributed Tracing](../tracing/index.md)
