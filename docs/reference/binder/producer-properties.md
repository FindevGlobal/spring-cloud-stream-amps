# Producer Properties

AMPS-specific producer properties control how the binder publishes messages to AMPS topics.

**Prefix:** `spring.cloud.stream.amps.bindings.<channelName>.producer`

These properties can also be set as defaults for all bindings under `spring.cloud.stream.amps.default.producer`.

---

## Acknowledgment

### `ackType`

- **Type:** `AmpsAckType` enum
- **Default:** `Processed`
- **Values:** `None`, `Received`, `Parsed`, `Processed`, `Persisted`, `Completed`

Controls the level of acknowledgment the AMPS server sends back after receiving a published message.

| Value       | Description                                                                 |
| ----------- | --------------------------------------------------------------------------- |
| `None`      | No acknowledgment. Fire-and-forget.                                         |
| `Received`  | Server received the message.                                                |
| `Parsed`    | Server successfully parsed the message.                                     |
| `Processed` | Server processed the message (default).                                     |
| `Persisted` | Message was written to the transaction log.                                 |
| `Completed` | Message fully processed and persisted through all downstream action chains. |

!!! tip
Higher acknowledgment levels provide stronger delivery guarantees but increase latency. Use `Persisted` or `Completed` when message durability is critical.

```yaml
spring.cloud.stream.amps.bindings:
  orders-out-0:
    producer:
      ackType: Persisted
```

---

## Timeout

### `timeout`

- **Type:** `Duration`
- **Default:** `10s`

Sets the timeout for this command. If the client does not receive and consume a processed ack within the specified timeout, this method throws an exception. The timeout is monitored in the AMPS client, not on the server, and the client receive thread receives and consumes the ack from the server.

```yaml
spring.cloud.stream.amps.bindings:
  orders-out-0:
    producer:
      timeout: 30s
```

---

## Message Expiration

### `expiration`

- **Type:** `Duration`
- **Default:** `null` (no expiration)

Sets the expiration for this command. For a publish command to a SOW topic or a queue, the expiration sets the amount of time to retain the message in the SOW or the queue. After this duration, the AMPS server automatically removes the message. The value is converted to seconds for the AMPS protocol.

```yaml
spring.cloud.stream.amps.bindings:
  orders-out-0:
    producer:
      expiration: 5m
```

---

## Payload Types

The AMPS producer handler accepts the following message payload types:

| Payload Type | Behavior                                        |
| ------------ | ----------------------------------------------- |
| `String`     | Published directly as the message data          |
| `byte[]`     | Converted to `String` (UTF-8) before publishing |

!!! warning
Other payload types will cause a `MessagingException`.

---

## Complete Example

```yaml
spring:
  cloud:
    stream:
      amps:
        bindings:
          orders-out-0:
            producer:
              ackType: Persisted
              timeout: 15s
              expiration: 60s
      bindings:
        orders-out-0:
          destination: processed-orders
```
