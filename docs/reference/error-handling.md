# Error Handling

The AMPS binder includes several error-handling mechanisms to deal with message processing failures, connection issues, and publishing errors.

## Consumer Error Handling

### Message Conversion Errors

When a consumed AMPS message cannot be converted to a Spring `Message`, the binder catches the following exceptions:

- **`MessageConversionException`** — Thrown when the payload cannot be converted to the expected type.
- **`ClassCastException`** — Specifically handles a known Spring Cloud Function issue where `byte[]` payloads cause `[B cannot be cast to...` errors.

In both cases, the binder:

1. **Logs** the error (including the original AMPS message data for debugging).
2. **Acknowledges** the message to the AMPS server to prevent redelivery.
3. **Continues** processing subsequent messages.

!!! info "Poison Pill Prevention"
This behavior prevents a single malformed message from blocking the consumer. The message is effectively skipped after logging.

### Custom Error Handling

For application-level error handling, use Spring Cloud Stream's standard error channel mechanism:

```java
@ServiceActivator(inputChannel = "orders-in-0.errors")
public void handleError(ErrorMessage errorMessage) {
    log.error("Error processing message: {}", errorMessage.getPayload().getMessage());
}
```

Or the global error channel:

```java
@ServiceActivator(inputChannel = "errorChannel")
public void handleGlobalError(ErrorMessage errorMessage) {
    log.error("Global error: {}", errorMessage.getPayload().getMessage());
}
```

## Producer Error Handling

All publish failures are wrapped in a `MessagingException` and propagated to the caller. Common failure scenarios:

| Scenario                 | Behavior                                                                        |
| ------------------------ | ------------------------------------------------------------------------------- |
| AMPS server unreachable  | `MessagingException` wrapping the connection error                              |
| Publish timeout          | `MessagingException` if the ACK is not received within the configured `timeout` |
| Unsupported payload type | `MessagingException` (only `String` and `byte[]` are supported)                 |

## Connection Error Handling

### HA Client Reconnection

The AMPS `HAClient` handles connection failures automatically:

- **`ExponentialDelayStrategy`** — Reconnection delay starts small and doubles on each failure, up to `maxReconnectTime`.
- **`DefaultServerChooser`** — Cycles through all configured broker URLs until one is available.
- **Heartbeat detection** — Stale connections are detected via the `heartBeatInterval` heartbeat, triggering automatic reconnection.

### Failed Writes

A `FailedWriteHandler` is registered on each AMPS connection. When the server cannot persist a message (e.g., SOW key violation), the handler logs the failure details. The message remains in the publish store for potential replay.

### Connection Close

During shutdown, the binder:

1. Calls `publishFlush()` with a 30-second timeout to ensure all outgoing messages are delivered.
2. Calls `flushAcks()` to send any pending acknowledgments.
3. Closes the connection.

Exceptions during `publishFlush()` and `flushAcks()` are **silently swallowed** to ensure a clean shutdown. Any data loss in this scenario is logged.

## Error Summary

| Layer      | Error Type                 | Handling                        |
| ---------- | -------------------------- | ------------------------------- |
| Consumer   | Message conversion failure | Log, ACK, skip                  |
| Consumer   | Application error          | Spring error channel            |
| Producer   | Publish failure            | `MessagingException` propagated |
| Connection | Server unavailable         | HA Client auto-reconnect        |
| Connection | Failed write               | Logged via `FailedWriteHandler` |
| Connection | Shutdown errors            | Silently swallowed, logged      |
