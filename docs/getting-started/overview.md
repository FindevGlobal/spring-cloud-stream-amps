# Overview

The **Spring Cloud Stream Binder for AMPS** is a binder implementation that connects [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) applications to [AMPS](https://www.crankuptheamps.com/) (Advanced Message Processing System), a high-performance messaging platform from 60East Technologies.

## What is AMPS?

AMPS is a publish-subscribe messaging platform designed for high-throughput, low-latency data distribution. Key AMPS features include:

- **SOW (State of the World)** — Queryable message cache that returns the most recent state for each record.
- **Publish & Subscribe** — Standard pub/sub messaging with content filtering.
- **Bookmarks** — Durable subscriptions with replay from any point in history.
- **High Availability** — Built-in replication and transparent failover via the HA Client.
- **Content Filtering** — Server-side filter expressions to control which messages a subscriber receives.
- **Queues** — Work-queue semantics with at-least-once delivery guarantees.

## What is Spring Cloud Stream?

[Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) is a framework for building message-driven microservices. It provides a binder abstraction that decouples application code from the underlying messaging middleware. By using this AMPS binder, you can leverage the same Spring Cloud Stream programming model you already know — with AMPS as the messaging transport.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                 Spring Cloud Stream                 │
│                                                     │
│   @Bean Consumer<Message<T>>    Function<T, R>      │
│          │                          │               │
│          ▼                          ▼               │
│   ┌─────────────────────────────────────────────┐   │
│   │         Spring Cloud Stream Binder SPI      │   │
│   └──────────────────┬──────────────────────────┘   │
│                      │                              │
│   ┌──────────────────▼──────────────────────────┐   │
│   │       AMPS Binder (this project)            │   │
│   │                                             │   │
│   │  AmpsMessageChannelBinder                   │   │
│   │    ├── AmpsMessageProducer (consumer side)  │   │
│   │    └── AmpsProducerMessageHandler (pub side)│   │
│   └──────────────────┬──────────────────────────┘   │
│                      │                              │
└──────────────────────┼──────────────────────────────┘
                       │
              ┌────────▼────────┐
              │   AMPS Server   │
              └─────────────────┘
```

## Features

- **Full Spring Cloud Stream compatibility** — Use the standard `@Bean` / `java.util.function` programming model.
- **SOW, SOW-and-Subscribe, and plain Subscribe** — Choose subscription modes via configuration.
- **Bookmark support** — Resume subscriptions from `MOST_RECENT`, `EPOCH`, `NOW`, or a custom bookmark.
- **Content filtering** — Apply AMPS filter expressions to subscriptions.
- **HA Client** — Automatic reconnection and failover across multiple AMPS instances.
- **Multi-binder support** — Configure multiple named AMPS binders in a single application.
- **Distributed tracing** — Transparent trace propagation via Micrometer or OpenTracing.
- **Custom header propagation** — Encode arbitrary metadata into AMPS correlation IDs.
- **Concurrent consumers** — Scale consumption with multiple parallel AMPS connections per binding.
- **Authentication** — Username/password or custom `Authenticator` beans.

## Modules

### spring-cloud-stream-binder-amps

The core binder module. Provides:

- `AmpsMessageChannelBinder` — The main binder implementation.
- Consumer endpoint (`AmpsMessageProducer`) — Subscribes to AMPS and pushes messages into Spring channels.
- Producer message handler (`AmpsProducerMessageHandler`) — Publishes Spring messages to AMPS topics.
- AMPS-specific configuration properties for binder, consumer, and producer.
- Connection management with HA client support.

### spring-cloud-stream-tracer-amps

An optional add-on module that adds distributed tracing to AMPS messages. It transparently wraps the AMPS connection layer to inject and extract trace context from message headers. Supports:

- **Micrometer** (B3 propagation)
- **OpenTracing** (via `GlobalTracer`)
