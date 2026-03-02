# Spring Cloud Stream Binder for AMPS

Reference documentation for the Spring Cloud Stream Binder for AMPS (Advanced Message Processing System) by 60East Technologies.

---

## Overview

This project provides a Spring Cloud Stream binder implementation for [AMPS](https://www.crankuptheamps.com/) — a high-performance messaging platform. It allows Spring Cloud Stream applications to use AMPS as their underlying messaging transport, just like the Kafka or RabbitMQ binders.

The project consists of two modules:

| Module                              | Artifact       | Description                                                               |
| ----------------------------------- | -------------- |---------------------------------------------------------------------------|
| **spring-cloud-stream-binder-amps** | Core Binder    | AMPS integration with Spring Cloud Stream                                 |
| **spring-cloud-stream-tracer-amps** | Tracing Add-on | Distributed tracing (Micrometer / OpenTracing) support for AMPS messages  |

## Compatibility

| Dependency   | Version                |
| ------------ |------------------------|
| Spring Boot  | 3.5.x                  |
| Spring Cloud | 2025.0.x (Northfields) |
| AMPS Client  | 5.3.4.0                |
| Java         | 17+                    |

## Quick Links

- [Getting Started](getting-started/overview.md) — Learn the basics and set up your first application.
- [Programming Model](getting-started/programming-model.md) — Functional, annotation-based, and reactive programming styles.
- [Configuration Reference](reference/binder/configuration.md) — Full list of binder, consumer, and producer configuration properties.
- [Message Serialization](advanced/serialization.md) — JSON, Avro, XML, and custom serialization.
- [SOW Queries](advanced/sow.md) — Subscribe to SOW (State of the World) topics.
- [Distributed Tracing](tracing/index.md) — Integrate with Micrometer or OpenTracing.
- [Appendix: All Configuration Properties](appendix/configuration-properties.md) — Flat reference of every property.