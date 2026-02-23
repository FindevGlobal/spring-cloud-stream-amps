# Connection Management

The AMPS binder uses a layered connection abstraction to manage AMPS client connections with support for high availability, authentication, and custom stores.

## Architecture

```
AmpsConnectionFactoryProvider
    │
    ▼
AmpsConnectionFactory ─── creates ──→ AmpsConnection
    │                                     │
    │                                     ├── subscribe()
    │                                     ├── publish()
    │                                     ├── setHeartbeat()
    │                                     ├── setAckBatchSize()
    │                                     ├── setAckTimeout()
    │                                     └── close()
    │
    ├── MessageStoreProvider (publish store)
    ├── BookmarkStoreProvider (bookmark store)
    ├── AmpsHeaderConverter (header encoding)
    └── Authenticator (optional)
```

## Interfaces

### `AmpsConnection`

Wraps an AMPS `HAClient` and provides methods for subscribing, publishing, configuring heartbeats, acknowledgment batching, and closing connections.

### `AmpsConnectionFactory`

Creates `AmpsConnection` instances. Each consumer binding and producer binding gets its own connection(s).

### `AmpsConnectionFactoryProvider`

Creates `AmpsConnectionFactory` from the binder configuration. This is the top-level entry point resolved by the binder during initialization.

## HA Client

The binder uses the AMPS **High-Availability Client** (`HAClient`). Key behaviors:

- **Server Chooser** — A `DefaultServerChooser` is configured with all broker URLs from the `brokers` property. If one server becomes unavailable, the client automatically fails over to the next.

- **Reconnection Strategy** — An `ExponentialDelayStrategy` is used, doubling the reconnection delay on each failure up to the `maxReconnectTime` cap.

- **Unique Client Names** — Each connection gets a unique name in the format `{name}_{PID}_{sequence}`, ensuring no client name collisions on the AMPS server.

- **Heartbeats** — The binder-level `heartBeatInterval` keeps connections alive and enables quick detection of stale connections.

## Connection Lifecycle

### Consumer Connections

When a consumer binding starts:

1. The binder creates `concurrency` number of AMPS connections (default is 1).
2. Each connection:
   - Connects and logs on to the AMPS server.
   - Configures heartbeat, ACK batch size, and ACK timeout.
   - Issues the subscription command (Subscribe, SOW, or SOWAndSubscribe).
3. As messages arrive, they are converted to Spring `Message<byte[]>` and dispatched to the bound channel.

When stopping:

1. All connections are flushed (publishes and ACKs).
2. Connections are closed gracefully.

### Producer Connections

When a producer binding starts:

1. A single AMPS connection is created.
2. The connection is opened to the AMPS server.

When a message is published:

1. The payload is extracted (String or byte[]).
2. If `publishAmpsHeader` is enabled, the correlation ID is set from encoded headers.
3. The message is published to the destination topic with the configured ACK type and timeout.

When stopping:

1. The connection is flushed and closed.

## Custom Stores

### Publish Store

The publish store buffers outgoing messages for at-least-once delivery. If the connection fails, buffered messages are replayed on reconnection.

- **Default:** `MemoryPublishStore` with `publishStoreSize` entries.
- **Custom:** Provide a `MessageStoreProvider` bean and set `publishMessageStoreProviderBeanName`.

```java
@Bean
public MessageStoreProvider myPublishStore() {
    return () -> new SOWStore("./publish-store", 1);
}
```

### Bookmark Store

The bookmark store persists consumed message bookmarks for durable subscriptions. On reconnection, the client can resume from the last acknowledged bookmark.

- **Default:** `MemoryBookmarkStore` (bookmarks lost on restart).
- **Custom:** Provide a `BookmarkStoreProvider` bean and set `subscriptionBookmarkStoreProviderBeanName`.

```java
@Bean
public BookmarkStoreProvider myBookmarkStore() {
    return () -> new LoggedBookmarkStore("./bookmark-store");
}
```

## Header Converter

The `AmpsHeaderConverter` encodes/decodes Spring message headers to/from AMPS correlation IDs.

- **Default:** `AmpsHeaderConverterImpl` — serializes headers to JSON, then Base64-encodes as the correlation ID.
- **Custom:** Provide an `AmpsHeaderConverter` bean and set `ampsHeaderConverterBeanName`.

See [Custom Header Propagation](../advanced/headers.md) for details.

## Bean Resolution

The binder resolves extension beans (stores, converter, authenticator) from the Spring `ApplicationContext`. Due to Spring Cloud Stream's `DefaultBinderFactory` creating child application contexts, the binder tries two name variants:

1. `{beanName}`
2. `{beanName}_child`

This ensures beans declared at the parent level are correctly resolved in multi-binder scenarios.
