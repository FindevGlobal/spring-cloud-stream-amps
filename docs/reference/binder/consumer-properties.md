# Consumer Properties

AMPS-specific consumer properties control how the binder subscribes to AMPS topics and processes messages.

**Prefix:** `spring.cloud.stream.amps.bindings.<channelName>.consumer`

These properties can also be set as defaults for all bindings under `spring.cloud.stream.amps.default.consumer`.

---

## Subscription Mode

### `snapshot`

- **Type:** `boolean`
- **Default:** `false`

When `true`, the consumer performs a **SOW query** (snapshot only). The binder issues an AMPS `SOW` command, receives the current state-of-the-world, and **does not** continue subscribing for live updates.

!!! note
`snapshot` and `sow` are mutually exclusive. If both are `true`, `snapshot` takes precedence.

### `sow`

- **Type:** `boolean`
- **Default:** `false`

When `true`, the consumer performs a **SOW-and-Subscribe**. The binder first receives the current state (SOW) and then continues to receive live updates as they arrive.

When both `snapshot` and `sow` are `false` (the default), the consumer uses a plain **Subscribe** command.

| `snapshot` |   `sow`   | AMPS Command      |
| :--------: | :-------: | :---------------- |
|  `false`   |  `false`  | `Subscribe`       |
|  `false`   |  `true`   | `SOWAndSubscribe` |
|   `true`   | _ignored_ | `SOW`             |

See [SOW Queries](../../advanced/sow.md) for a detailed guide.

---

## Content Filtering

### `filter`

- **Type:** `String`
- **Default:** `null`

An AMPS content filter expression applied server-side to limit which messages the consumer receives. The filter syntax depends on the message type (e.g., JSON XPath for `json` message types).

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      filter: "/status = 'ACTIVE' AND /quantity > 100"
```

---

## Bookmarks

### `bookmarkType`

- **Type:** `BookmarkType` enum
- **Default:** `DEFAULT`
- **Values:** `DEFAULT`, `MOST_RECENT`, `EPOCH`, `NOW`

Controls the starting bookmark for subscriptions.

| Value         | Behavior                                                                     |
| ------------- | ---------------------------------------------------------------------------- |
| `DEFAULT`     | Use the AMPS client default (typically the beginning of the transaction log) |
| `MOST_RECENT` | Resume from the most recent bookmark stored by the bookmark store            |
| `EPOCH`       | Replay all messages from the beginning of the transaction log                |
| `NOW`         | Start from the current point in the transaction log (no replay)              |

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      bookmarkType: MOST_RECENT
```

### `bookmarkProviderBeanName`

- **Type:** `String`
- **Default:** `null`

Name of a Spring bean implementing `BookmarkProvider` that supplies a custom initial bookmark string. When set, this overrides `bookmarkType`.

See [Bookmarks](../../advanced/bookmarks.md) for advanced bookmark scenarios.

---

## Timing & Throughput

### `rate`

- **Type:** `String`
- **Default:** `null`

Rate-limiting option for the subscription. Sets the maximum message delivery rate for a bookmark subscription. This option is only valid for bookmark subscriptions that do not use the live option. The rate can be specified as:

- Number of messages per second (e.g., `"1000"`)
- Number of bytes per second (e.g., `"100KB"`)
- A multiple of the original replay rate (e.g., `"1.5X"`)

### `timeout`

- **Type:** `Duration`
- **Default:** `null`

Sets the timeout for the subscription command. If the client does not receive and consume a processed ack within the specified timeout, an exception is thrown. The timeout is monitored in the AMPS client, not on the server.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      timeout: 30s
```

### `batchSize`

- **Type:** `Integer`
- **Default:** `null`

Batch size for SOW queries. Sets the number of records returned in a batch during a SOW query.

### `maxBacklog`

- **Type:** `Integer`
- **Default:** `null`

Formats the `max_backlog` option for a command, used when subscribing to a queue. When consuming from a queue, this option sets the maximum number of messages the client is willing to have outstanding at a given time.

---

## Acknowledgment

### `ackBatchSize`

- **Type:** `Integer`
- **Default:** `null`

Number of messages to accumulate before sending a batch ACK (which are `sow_delete` messages) to the server. When combined with `maxBacklog` and the AMPS server's `MaxPerSubscriptionBacklog` configuration, greater network efficiency can be achieved when using message queues. Setting this parameter causes calls to `ack` and successful auto-acks to be batched; unsuccessful/cancel acks are sent immediately. Default batch size is 1.

### `ackTimeout`

- **Type:** `Duration`
- **Default:** `null`

Sets the ack timeout — the maximum time to let a success ack be cached before sending. Ensures acknowledgments are sent even if the batch isn't full.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      ackBatchSize: 100
      ackTimeout: 5s
```

---

## Heartbeat

### `heartbeat`

- **Type:** `Duration`
- **Default:** `null`

Per-subscription heartbeat interval. Requests a server heartbeat and configures the client to close the connection if a heartbeat (or other activity) is not seen on the connection after two heartbeat intervals. This is separate from the binder-level `heartBeatInterval`.

---

## Out-of-Focus (OOF)

### `oof`

- **Type:** `boolean`
- **Default:** `false`

When `true`, enables **Out-of-Focus (OOF)** messages for SOW subscriptions. An OOF message is sent when a record no longer matches the subscription filter or has been deleted from the SOW cache.

!!! info
OOF messages are only meaningful for SOW-and-Subscribe subscriptions with a filter.

---

## Timestamp

### `withTimestamp`

- **Type:** `boolean`
- **Default:** `false`

When `true`, AMPS will include a header with the time at which AMPS processed the incoming publish command for this message. The timestamp is an ISO-8601 formatted string, propagated as the `ampsTimestamp` message header.

---

## Custom Options

### `options`

- **Type:** `String[]`
- **Default:** `null`

Additional AMPS command options to include in the subscription. Each string is added verbatim to the command's options.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      options:
        - replace
        - no_empties
```

---

## Concurrency

Consumer concurrency is controlled by the standard Spring Cloud Stream property:

```yaml
spring:
  cloud:
    stream:
      bindings:
        orders-in-0:
          consumer:
            concurrency: 3
```

This creates 3 parallel AMPS connections for the binding, each subscribing to the same topic. See [Concurrent Consumers](../../advanced/concurrency.md) for details.

---

## Complete Example

```yaml
spring:
  cloud:
    stream:
      amps:
        bindings:
          orders-in-0:
            consumer:
              sow: true
              filter: "/status = 'ACTIVE'"
              bookmarkType: MOST_RECENT
              rate: "50"
              ackBatchSize: 100
              ackTimeout: 5s
              heartbeat: 15s
              timeout: 30s
              oof: true
              withTimestamp: true
              batchSize: 200
              maxBacklog: 10000
              options:
                - replace
      bindings:
        orders-in-0:
          destination: orders
          group: order-service
          consumer:
            concurrency: 2
```
