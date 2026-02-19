# Multi-Binder Configuration

Spring Cloud Stream supports configuring multiple named binders in a single application. The AMPS binder fully supports this, allowing you to connect to multiple AMPS server clusters or combine AMPS with other messaging systems (e.g., Kafka).

## Default Binder

When using multiple binders, you should specify a default binder. All bindings without an explicit `binder` property will use the default:

```yaml
spring:
  cloud:
    stream:
      default-binder: amps1
```

## Defining Multiple AMPS Binders

Declare named binders under `spring.cloud.stream.binders`:

```yaml
spring:
  cloud:
    stream:
      binders:
        amps-primary:
          type: amps
          environment:
            spring.cloud.stream.amps.binder:
              name: primary
              brokers:
                - tcp://amps-primary-1:50000/json
                - tcp://amps-primary-2:50000/json
              publishAmpsHeader: true

        amps-secondary:
          type: amps
          environment:
            spring.cloud.stream.amps.binder:
              name: secondary
              brokers:
                - tcp://amps-secondary:50000/json
```

## Assigning Bindings to Binders

Map each binding to a specific binder:

```yaml
spring:
  cloud:
    stream:
      bindings:
        orders-in-0:
          destination: orders
          binder: amps-primary
        analytics-out-0:
          destination: analytics
          binder: amps-secondary
```

## Per-Binder Consumer/Producer Properties

AMPS-specific properties for bindings are scoped under each binder's environment:

```yaml
spring:
  cloud:
    stream:
      binders:
        amps-primary:
          type: amps
          environment:
            spring.cloud.stream.amps:
              binder:
                brokers:
                  - tcp://amps-primary:50000/json
              bindings:
                orders-in-0:
                  consumer:
                    sow: true
                    filter: "/status = 'ACTIVE'"
```

## Mixing AMPS with Other Binders

You can combine AMPS with Kafka, RabbitMQ, or any other Spring Cloud Stream binder. Each binder must have a `type` (e.g., `amps`, `kafka`, `rabbit`). Set a `default-binder` and assign specific bindings to specific binders:

```yaml
spring:
  cloud:
    stream:
      default-binder: amps1
      binders:
        amps1:
          type: amps
          environment:
            spring.cloud.stream.amps.binder:
              name: amps1
              brokers: amps-host-1:50000
        amps2:
          type: amps
          environment:
            spring.cloud.stream.amps.binder:
              name: amps2
              brokers: amps-host-2:50000
        kafka1:
          type: kafka
          environment:
            spring.cloud.stream.kafka.binder:
              brokers: kafka-host:9092
      bindings:
        output1:
          destination: /topic1
          binder: amps1
        input1:
          destination: /topic1/queue
          binder: amps2
        output2:
          destination: topic2
          binder: kafka1
```

## Bean Resolution in Multi-Binder

Spring Cloud Stream's `DefaultBinderFactory` creates **child application contexts** for each named binder. The AMPS binder handles this by searching for beans with both the original name and a `_child` suffix.

If you define custom beans (e.g., `BookmarkStoreProvider`, `Authenticator`) at the application level, they are automatically discovered by child binder contexts.

## Complete Multi-Binder Example

```yaml
spring:
  cloud:
    stream:
      binders:
        amps1:
          type: amps
          environment:
            spring.cloud.stream.amps:
              binder:
                name: trading-app
                brokers:
                  - tcp://amps-east-1:50000/json
                  - tcp://amps-east-2:50000/json
                publishAmpsHeader: true
                authenticatorBeanName: kerbAuth
              bindings:
                orders-in-0:
                  consumer:
                    sow: true
                    bookmarkType: MOST_RECENT

        amps2:
          type: amps
          environment:
            spring.cloud.stream.amps:
              binder:
                name: analytics-app
                brokers:
                  - tcp://amps-west-1:50000/json
              bindings:
                metrics-out-0:
                  producer:
                    ackType: Persisted

      bindings:
        orders-in-0:
          destination: orders
          binder: amps1
        metrics-out-0:
          destination: metrics
          binder: amps2

      function:
        definition: processOrders
```
