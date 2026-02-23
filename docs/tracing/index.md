# Distributed Tracing

The `spring-cloud-stream-tracer-amps` module provides transparent distributed tracing for AMPS messages. Trace context is automatically propagated through AMPS message headers, enabling end-to-end observability across services communicating via AMPS.

## How It Works

The tracer module uses the **decorator pattern** to wrap the AMPS connection layer:

```
AmpsConnectionFactoryProvider
    │
    ▼  (decorated by BeanPostProcessor)
TracingAmpsConnectionFactoryProvider
    │
    ▼
TracingAmpsConnectionFactory
    │
    ▼
TracingAmpsConnection
    ├── subscribe() → wraps handler with consumeInScope()
    └── publish()   → wraps call with publishInScope()
```

A `BeanPostProcessor` (`TracingAmpsConnectionFactoryProviderBeanPostProcessor`) automatically detects and wraps any `AmpsConnectionFactoryProvider` bean with the tracing decorator. No code changes are required in your application.

### Publishing

When a message is published:

1. A **producer span** is created.
2. Trace headers (e.g., B3 headers for Sleuth) are injected into the `ampsMessageHeaderParams` map.
3. The `ampsPublishHeader` flag is forced to `true` so the header converter encodes trace context into the AMPS correlation ID.
4. The message is published with the trace context embedded.
5. The span is closed.

### Consuming

When a message is received:

1. The correlation ID is decoded back into message headers.
2. Trace headers are extracted from `ampsMessageHeaderParams`.
3. A **consumer span** is created as a child of the extracted trace context.
4. The message handler runs within the span scope.
5. Errors are tagged on the span.
6. The span is closed.

## Dependency

Add the tracer module to your project:

```xml
<dependency>
    <groupId>com.findevglobal.cloud</groupId>
    <artifactId>spring-cloud-stream-tracer-amps</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Supported Tracing Libraries

| Library                          | Auto-Configuration               | Priority                       |
| -------------------------------- | -------------------------------- | ------------------------------ |
| [Spring Cloud Sleuth](sleuth.md) | `SleuthTracingAmpsConfiguration` | **Primary** (takes precedence) |
| [OpenTracing](opentracing.md)    | `OpenTracingAmpsConfiguration`   | Secondary (fallback)           |

If both Sleuth and OpenTracing are on the classpath, Sleuth takes precedence via `@ConditionalOnMissingBean`.

## Span Details

Two span types are created:

### Consumer Span

- **Name:** `amps.consume`
- **Kind:** `CONSUMER`
- **Tags:**
  - `amps.topic` — The topic being consumed

### Producer Span

- **Name:** `amps.produce`
- **Kind:** `PRODUCER`
- **Tags:**
  - `amps.topic` — The topic being published to

## Requirements

- The `publishAmpsHeader` property should be `true` (or `ampsPublishHeader` header set on messages) for trace context to be propagated. The tracer forces this automatically.
- The default `AmpsHeaderConverterImpl` (or a compatible custom implementation) must be used for correlation ID encoding/decoding.
