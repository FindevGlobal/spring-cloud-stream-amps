# OpenTracing Integration

The AMPS tracer integrates with the [OpenTracing](https://opentracing.io/) API for distributed tracing using the `GlobalTracer`.

## Auto-Configuration

`OpenTracingAmpsConfiguration` is activated when:

1. The `io.opentracing.util.GlobalTracer` class is on the classpath.
2. No `AmpsTracer` bean already exists (Sleuth takes precedence via `@ConditionalOnMissingBean`).

It creates an `OpenTracingTracer` bean implementing the `AmpsTracer` interface.

## Setup

Add the tracer dependency and an OpenTracing implementation (e.g., Jaeger):

```xml
<dependency>
    <groupId>com.findevglobal.cloud</groupId>
    <artifactId>spring-cloud-stream-tracer-amps</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-spring-jaeger-cloud-starter</artifactId>
    <version>3.3.1</version>
</dependency>
```

Ensure the OpenTracing tracer is registered with `GlobalTracer` (most starter libraries do this automatically).

## Propagation Format

The OpenTracing integration uses the **`TEXT_MAP`** format for context propagation. The specific headers depend on the tracer implementation:

### Jaeger (default)

| Header          | Description                                                        |
| --------------- | ------------------------------------------------------------------ |
| `uber-trace-id` | Encoded trace context (`{trace-id}:{span-id}:{parent-id}:{flags}`) |

### Zipkin (B3)

| Header              | Description            |
| ------------------- | ---------------------- |
| `X-B3-TraceId`      | Trace identifier       |
| `X-B3-SpanId`       | Span identifier        |
| `X-B3-ParentSpanId` | Parent span identifier |
| `X-B3-Sampled`      | Sampling decision      |

The headers are injected into the `ampsMessageHeaderParams` map and encoded into the AMPS correlation ID.

## Span Details

### Consumer Span

```
Operation Name: amps.consume
Kind: CONSUMER
Tags:
  component: amps
  amps.topic: <topic-name>
```

### Producer Span

```
Operation Name: amps.produce
Kind: PRODUCER
Tags:
  component: amps
  amps.topic: <topic-name>
```

Errors are automatically tagged on spans with `Tags.ERROR = true`.

## Priority

If both Spring Cloud Sleuth and OpenTracing are on the classpath, **Sleuth takes precedence**. The `OpenTracingAmpsConfiguration` is annotated with `@ConditionalOnMissingBean(AmpsTracer.class)`, so it only activates when no Sleuth-based `AmpsTracer` is already registered.

To force OpenTracing over Sleuth, disable the Sleuth integration:

```yaml
spring:
  sleuth:
    amps:
      enabled: false
```
