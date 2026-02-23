# AMPS Binder

The `AmpsMessageChannelBinder` is the core component that integrates AMPS with the Spring Cloud Stream binder SPI. It extends `AbstractMessageChannelBinder` and implements `ExtendedPropertiesBinder` to support AMPS-specific consumer and producer configuration.

## How It Works

When a Spring Cloud Stream application starts with the AMPS binder on the classpath, the following occurs:

1. **Auto-configuration** — `AmpsBinderConfiguration` is loaded via `META-INF/spring.binders`. It creates the `AmpsTopicProvisioner` and `AmpsMessageChannelBinder` beans.

2. **Connection factory resolution** — On initialization, the binder resolves an `AmpsConnectionFactoryProvider` bean from the application context. If none is found, a default `AmpsConnectionFactoryProviderImpl` is created. An `AmpsConnectionFactory` is built from the binder configuration properties.

3. **Consumer binding** — For each consumer binding, the binder creates an `AmpsMessageProducer` that opens one or more AMPS connections (based on concurrency), subscribes to the destination topic, and converts AMPS messages into Spring `Message<byte[]>`.

4. **Producer binding** — For each producer binding, the binder creates an `AmpsProducerMessageHandler` that opens an AMPS connection and publishes Spring messages to the destination topic.

## Binder Type

The binder type is `amps`. When using multiple binders, reference this type in your binder declaration:

```yaml
spring:
  cloud:
    stream:
      binders:
        my-amps:
          type: amps
          environment:
            spring.cloud.stream.amps.binder:
              brokers:
                - tcp://amps-server:50000/json
```

## Extended Properties

The AMPS binder supports extended (AMPS-specific) properties for consumer and producer bindings. These properties are under the `spring.cloud.stream.amps.bindings.<channelName>` namespace.

See:

- [Binder Configuration Properties](configuration.md)
- [Consumer Properties](consumer-properties.md)
- [Producer Properties](producer-properties.md)
