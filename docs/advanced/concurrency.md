# Concurrent Consumers

The AMPS binder supports running multiple parallel AMPS connections per consumer binding to increase throughput.

## Configuration

Consumer concurrency is controlled by the standard Spring Cloud Stream `concurrency` property:

```yaml
spring:
  cloud:
    stream:
      bindings:
        orders-in-0:
          destination: orders
          consumer:
            concurrency: 3
```

## How It Works

When `concurrency` is set to `N`, the binder creates **N independent AMPS connections**, each subscribing to the same topic. This means:

- Each connection establishes its own subscription to the AMPS server.
- Messages are distributed across the connections according to AMPS server behavior.
- Each connection has its own heartbeat, ACK batching, and bookmark tracking.

```
                    ┌──── AmpsConnection #1 ──── Subscribe(orders)
                    │
Consumer Binding ───┼──── AmpsConnection #2 ──── Subscribe(orders)
                    │
                    └──── AmpsConnection #3 ──── Subscribe(orders)
```

## When to Use

Concurrent consumers are most useful when:

- **Queue subscriptions** — AMPS queues distribute messages across subscribers, so multiple connections increase throughput.
- **High-volume topics** — Parallelizing message processing across connections.
- **CPU-bound processing** — Distributing processing load across multiple threads.

## Considerations

!!! warning "Duplicate Messages"
For non-queue subscriptions (plain pub/sub or SOW-and-subscribe), each connection receives **all messages**. Setting `concurrency > 1` will result in duplicate processing unless you're using AMPS queues.

!!! tip "Queue Subscriptions"
For queue-based consumption with at-least-once semantics, use the queue topic path:

    ```yaml
    spring:
      cloud:
        stream:
          bindings:
            orders-in-0:
              destination: orders/order-queue
              consumer:
                concurrency: 5
    ```

## Shutdown Behavior

When the application stops, all concurrent connections are flushed and closed:

1. Each connection's pending publishes are flushed (30-second timeout).
2. Each connection's pending ACKs are flushed.
3. All connections are closed.

## Example

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          brokers:
            - tcp://amps-server:50000/json
        bindings:
          orders-in-0:
            consumer:
              sow: true
              ackBatchSize: 50
              ackTimeout: 5s
      bindings:
        orders-in-0:
          destination: orders/order-queue
          consumer:
            concurrency: 4
```
