# Micrometer Integration

The AMPS tracer integrates with [Micrometer](https://micrometer.io) for distributed tracing using B3 propagation.

## Auto-Configuration

`MicrometerTracingAmpsConfiguration` is activated when:

1. Micrometer's `Tracer` bean is present on the classpath.
2. The property `spring.micrometer.amps.enabled` is `true` (default).

## Setup

Add both dependencies:

```xml
<dependency>
    <groupId>com.findevglobal.cloud</groupId>
    <artifactId>spring-cloud-stream-tracer-amps</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing</artifactId>
</dependency>
```

No additional configuration is needed. The tracer auto-configures itself.

## Disabling

To disable Micrometer integration for AMPS while keeping Micrometer active for other components:

```yaml
spring:
  micrometer:
    amps:
      enabled: false
```

## Propagation Format

The Micrometer integration uses **B3 propagation** headers:

| Header              | Description                        |
| ------------------- | ---------------------------------- |
| `X-B3-TraceId`      | 128-bit or 64-bit trace identifier |
| `X-B3-SpanId`       | 64-bit span identifier             |
| `X-B3-ParentSpanId` | Parent span identifier             |
| `X-B3-Sampled`      | Sampling decision (`1` or `0`)     |

These headers are injected into the `ampsMessageHeaderParams` map and encoded into the AMPS correlation ID.

## Span Details

The `MicrometerTracer` creates spans using Micrometer's `Tracer` and `Propagator` APIs:

### Consumer Span

```
Span Name: amps.consume
Kind: CONSUMER
Tags:
  amps.topic: <topic-name>
```

### Producer Span

```
Span Name: amps.produce
Kind: PRODUCER
Tags:
  amps.topic: <topic-name>
```

## Documented Spans

The module defines documented spans for Micrometer's span documentation:

| Span Name       | Kind       | Tag          |
| --------------- | ---------- | ------------ |
| `amps.consumer` | `CONSUMER` | `amps.topic` |
| `amps.producer` | `PRODUCER` | `amps.topic` |

## Example Trace Flow

```
Service A (Producer)                    Service B (Consumer)
┌─────────────────────┐                ┌─────────────────────┐
│ amps.produce span   │──── AMPS ────→ │ amps.consume span   │
│ traceId: abc123     │   message      │ traceId: abc123     │
│ spanId:  def456     │   with B3      │ parentSpanId: def456│
│ topic:   orders     │   headers      │ topic:   orders     │
└─────────────────────┘                └─────────────────────┘
```
