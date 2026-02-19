# SOW Queries

AMPS provides a powerful **State of the World (SOW)** feature — a server-side queryable cache that maintains the latest state of each record. The AMPS binder supports three subscription modes that leverage SOW.

## Subscription Modes

### Plain Subscribe

The default mode. The consumer receives only **live updates** as they are published. No historical data is delivered.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      # snapshot: false (default)
      # sow: false (default)
```

### SOW (Snapshot Only)

The consumer receives a **one-time snapshot** of the current state from the SOW cache and then stops. No live updates are delivered after the snapshot completes.

This is useful for loading initial state on startup or performing periodic queries.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      snapshot: true
```

!!! note "AMPS Command"
This translates to the AMPS `SOW` command.

### SOW-and-Subscribe

The consumer first receives the **full SOW snapshot**, then seamlessly transitions to receiving **live updates**. This is the most common mode for applications that need both historical state and real-time data.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      sow: true
```

!!! note "AMPS Command"
This translates to the AMPS `SOWAndSubscribe` command.

## Decision Matrix

| `snapshot` |  `sow`  | Command           | Historical Data | Live Updates |
| :--------: | :-----: | :---------------- | :-------------: | :----------: |
|  `false`   | `false` | `Subscribe`       |       No        |     Yes      |
|  `false`   | `true`  | `SOWAndSubscribe` |       Yes       |     Yes      |
|   `true`   |  _any_  | `SOW`             |       Yes       |      No      |

## Content Filtering with SOW

Combine SOW queries with content filters to receive only matching records:

```yaml
spring.cloud.stream.amps.bindings:
  active-orders-in-0:
    consumer:
      sow: true
      filter: "/status = 'ACTIVE' AND /region = 'US'"
```

## Out-of-Focus (OOF) Messages

When using SOW-and-Subscribe with a filter, enable OOF to receive notifications when a record **no longer matches** the filter or is **deleted** from the SOW cache:

```yaml
spring.cloud.stream.amps.bindings:
  active-orders-in-0:
    consumer:
      sow: true
      filter: "/status = 'ACTIVE'"
      oof: true
```

An OOF message is delivered when:

- A record's field is updated such that it no longer matches the filter.
- A record is deleted from the SOW.

## Batch Size

Control how many SOW records the server sends per batch:

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      sow: true
      batchSize: 500
```

## Timeout

Set a timeout for the SOW query. If the server doesn't respond within this period, the subscription fails:

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      snapshot: true
      timeout: 60s
```

## Example: Full SOW Configuration

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
              filter: "/quantity > 100"
              oof: true
              bookmarkType: MOST_RECENT
              batchSize: 200
              timeout: 30s
              ackBatchSize: 50
              ackTimeout: 5s
      bindings:
        orders-in-0:
          destination: orders
```
