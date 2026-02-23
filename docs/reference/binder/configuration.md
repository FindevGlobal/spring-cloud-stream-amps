# Binder Configuration Properties

Binder-level properties configure the AMPS connection and behavior shared across all bindings.

**Prefix:** `spring.cloud.stream.amps.binder`

## Connection Properties

### `name`

- **Type:** `String`
- **Default:** `"default"`

The logical name used to identify this client when connecting to AMPS. It is used as a prefix for generating unique AMPS client names (`{name}_{PID}_{sequence}`).

```yaml
spring.cloud.stream.amps.binder:
  name: my-trading-app
```

### `brokers`

- **Type:** `String[]`
- **Default:** `["localhost"]`

A list of AMPS broker addresses. Each broker string is parsed as a URI. Missing components (transport, port, message type) are filled from defaults.

```yaml
spring.cloud.stream.amps.binder:
  brokers:
    - tcp://amps-primary:50000/json
    - tcp://amps-secondary:50000/json
```

!!! tip "Shorthand Broker Strings"
You can use shorthand and let the defaults fill in the gaps:

    - `"amps-server"` → resolves to `tcp://amps-server:50000/json`
    - `"amps-server:60000"` → resolves to `tcp://amps-server:60000/json`
    - `"tcps://amps-server:60000/nvfix"` → used as-is

### `defaultBrokerTransport`

- **Type:** `String`
- **Default:** `"tcp"`

Default transport scheme when not specified in the broker string. Common values: `tcp`, `tcps`.

### `defaultBrokerPort`

- **Type:** `int`
- **Default:** `50000`

Default AMPS port when not specified in the broker string.

### `defaultBrokerMessageType`

- **Type:** `String`
- **Default:** `"json"`

Default message type (URI path) when not specified in the broker string. Corresponds to AMPS message types such as `json`, `nvfix`, `fix`, `xml`, `binary`.

## HA & Reconnection

### `maxReconnectTime`

- **Type:** `Duration`
- **Default:** `0s` (immediate)

Length of time the AMPS client will wait trying to reconnect before declaring the connection `FAILED`. The binder uses an `ExponentialDelayStrategy` — the delay doubles on each failure up to this cap. A value of `0s` means the client will try to reconnect forever.

```yaml
spring.cloud.stream.amps.binder:
  maxReconnectTime: 30s
```

### `heartBeatInterval`

- **Type:** `Duration`
- **Default:** `10s`

Time interval for sending heartbeats to the AMPS server. The client configures itself to close the connection if a heartbeat (or other activity) is not seen on the connection after two heartbeat intervals. This enables quick detection of stale connections so the HA client can fail over.

```yaml
spring.cloud.stream.amps.binder:
  heartBeatInterval: 15s
```

## Publish Store

### `publishStoreSize`

- **Type:** `int`
- **Default:** `1024`

Size (number of messages) of the in-memory publish store. The publish store is used for at-least-once delivery guarantees — messages are stored until the server acknowledges them.

```yaml
spring.cloud.stream.amps.binder:
  publishStoreSize: 4096
```

### `publishMessageStoreProviderBeanName`

- **Type:** `String`
- **Default:** `null` (uses `MemoryPublishStore`)

Name of a Spring bean implementing `MessageStoreProvider` to provide a custom AMPS `Store` for publish persistence. When not set, a `MemoryPublishStore` of size `publishStoreSize` is used.

```yaml
spring.cloud.stream.amps.binder:
  publishMessageStoreProviderBeanName: myCustomPublishStore
```

## Bookmark Store

### `subscriptionBookmarkStoreProviderBeanName`

- **Type:** `String`
- **Default:** `null` (uses `MemoryBookmarkStore`)

Name of a Spring bean implementing `BookmarkStoreProvider` to provide a custom AMPS `BookmarkStore` for durable subscriptions. When not set, a `MemoryBookmarkStore` is used.

```yaml
spring.cloud.stream.amps.binder:
  subscriptionBookmarkStoreProviderBeanName: myBookmarkStore
```

## Header Converter

### `ampsHeaderConverterBeanName`

- **Type:** `String`
- **Default:** `null` (uses `AmpsHeaderConverterImpl`)

Name of a Spring bean implementing `AmpsHeaderConverter` to customize how Spring message headers are encoded into / decoded from AMPS correlation IDs.

The default implementation (`AmpsHeaderConverterImpl`) serializes headers to JSON and Base64-encodes the result into the AMPS correlation ID.

### `publishAmpsHeader`

- **Type:** `boolean`
- **Default:** `false`

When `true`, all published messages automatically include an AMPS header (correlation ID) with encoded Spring message headers. This is required for distributed tracing or custom header propagation.

Individual messages can override this via the `ampsPublishHeader` message header.

```yaml
spring.cloud.stream.amps.binder:
  publishAmpsHeader: true
```

## Authentication

### `username`

- **Type:** `String`
- **Default:** `null` (falls back to `System.getProperty("user.name")`)

Username for AMPS authentication. If specified, the username is injected into each broker URI's userinfo component.

### `password`

- **Type:** `String`
- **Default:** `null`

Password for AMPS authentication. Used with the default password-based authenticator.

### `authenticatorBeanName`

- **Type:** `String`
- **Default:** `null`

Name of a Spring bean implementing `com.crankuptheamps.client.Authenticator` for custom authentication logic. When set, the `username` and `password` properties are ignored.

```yaml
spring.cloud.stream.amps.binder:
  authenticatorBeanName: kerberosAuthenticator
```

---

## Complete Example

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          name: order-service
          brokers:
            - tcp://amps-1:50000/json
            - tcp://amps-2:50000/json
          defaultBrokerTransport: tcp
          defaultBrokerPort: 50000
          defaultBrokerMessageType: json
          heartBeatInterval: 10s
          maxReconnectTime: 30s
          publishStoreSize: 2048
          publishAmpsHeader: true
          username: app-user
          password: secret
```
