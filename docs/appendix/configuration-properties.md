# Configuration Properties Reference

This appendix provides a comprehensive flat reference of all configuration properties supported by the Spring Cloud Stream Binder for AMPS.

---

## Binder Properties

**Prefix:** `spring.cloud.stream.amps.binder`

| Property                                    | Type       | Default         | Description                                                                          |
| ------------------------------------------- | ---------- | --------------- | ------------------------------------------------------------------------------------ |
| `name`                                      | `String`   | `"default"`     | Logical client name. Used as prefix for unique AMPS client names.                    |
| `brokers`                                   | `String[]` | `["localhost"]` | AMPS broker addresses. Missing components filled from defaults.                      |
| `defaultBrokerTransport`                    | `String`   | `"tcp"`         | Default URI scheme (`tcp`, `tcps`).                                                  |
| `defaultBrokerPort`                         | `int`      | `50000`         | Default AMPS port.                                                                   |
| `defaultBrokerMessageType`                  | `String`   | `"json"`        | Default message type in URI path (`json`, `nvfix`, `fix`, `xml`, `binary`).          |
| `publishStoreSize`                          | `int`      | `1024`          | In-memory publish store capacity (number of messages).                               |
| `maxReconnectTime`                          | `Duration` | `0s`            | Length of time to wait trying to reconnect before declaring FAILED. 0 = try forever. |
| `heartBeatInterval`                         | `Duration` | `10s`           | Heartbeat interval for connection liveness detection.                                |
| `publishMessageStoreProviderBeanName`       | `String`   | `null`          | Bean name of custom `MessageStoreProvider`.                                          |
| `subscriptionBookmarkStoreProviderBeanName` | `String`   | `null`          | Bean name of custom `BookmarkStoreProvider`.                                         |
| `ampsHeaderConverterBeanName`               | `String`   | `null`          | Bean name of custom `AmpsHeaderConverter`.                                           |
| `publishAmpsHeader`                         | `boolean`  | `false`         | Auto-include AMPS correlation ID header on all publishes.                            |
| `username`                                  | `String`   | `null`          | AMPS authentication username. Falls back to OS user.                                 |
| `password`                                  | `String`   | `null`          | AMPS authentication password.                                                        |
| `authenticatorBeanName`                     | `String`   | `null`          | Bean name of custom `Authenticator`. Overrides username/password.                    |

---

## Consumer Properties

**Prefix:** `spring.cloud.stream.amps.bindings.<channelName>.consumer`

**Defaults prefix:** `spring.cloud.stream.amps.default.consumer`

| Property                   | Type           | Default   | Description                                                                                  |
| -------------------------- | -------------- | --------- | -------------------------------------------------------------------------------------------- |
| `snapshot`                 | `boolean`      | `false`   | Use SOW command (snapshot only, no live updates).                                            |
| `sow`                      | `boolean`      | `false`   | Use SOW-and-Subscribe command (snapshot + live updates).                                     |
| `oof`                      | `boolean`      | `false`   | Enable Out-of-Focus messages for SOW subscriptions.                                          |
| `rate`                     | `String`       | `null`    | Max delivery rate: messages/sec, bytes/sec (e.g. `100KB`), or replay multiple (e.g. `1.5X`). |
| `withTimestamp`            | `boolean`      | `false`   | Request AMPS timestamp on each message.                                                      |
| `ackBatchSize`             | `Integer`      | `null`    | Number of messages per ACK batch. Batches sow_delete acks for queue efficiency. Default 1.   |
| `ackTimeout`               | `Duration`     | `null`    | Maximum time before flushing partial ACK batch.                                              |
| `heartbeat`                | `Duration`     | `null`    | Per-subscription heartbeat interval.                                                         |
| `timeout`                  | `Duration`     | `null`    | Subscription command timeout.                                                                |
| `bookmarkType`             | `BookmarkType` | `DEFAULT` | Starting bookmark: `DEFAULT`, `MOST_RECENT`, `EPOCH`, `NOW`.                                 |
| `filter`                   | `String`       | `null`    | AMPS content filter expression.                                                              |
| `options`                  | `String[]`     | `null`    | Additional AMPS command options.                                                             |
| `maxBacklog`               | `Integer`      | `null`    | Max outstanding messages when consuming from a queue.                                        |
| `batchSize`                | `Integer`      | `null`    | SOW query batch size.                                                                        |
| `bookmarkProviderBeanName` | `String`       | `null`    | Bean name of custom `BookmarkProvider`. Overrides `bookmarkType`.                            |

---

## Producer Properties

**Prefix:** `spring.cloud.stream.amps.bindings.<channelName>.producer`

**Defaults prefix:** `spring.cloud.stream.amps.default.producer`

| Property     | Type          | Default     | Description                                                                                        |
| ------------ | ------------- | ----------- | -------------------------------------------------------------------------------------------------- |
| `ackType`    | `AmpsAckType` | `Processed` | Publish acknowledgment level: `None`, `Received`, `Parsed`, `Processed`, `Persisted`, `Completed`. |
| `timeout`    | `Duration`    | `10s`       | Timeout for publish ack. Monitored client-side, not on server.                                     |
| `expiration` | `Duration`    | `null`      | Message TTL for SOW topics and queues.                                                             |

---

## Tracer Properties

| Property                     | Type      | Default | Description                                         |
| ---------------------------- | --------- | ------- | --------------------------------------------------- |
| `spring.sleuth.amps.enabled` | `boolean` | `true`  | Enable/disable Sleuth integration for AMPS tracing. |

---

## Enums

### `BookmarkType`

| Value         | AMPS Constant                  | Description                              |
| ------------- | ------------------------------ | ---------------------------------------- |
| `DEFAULT`     | `null`                         | Use AMPS client default                  |
| `MOST_RECENT` | `Client.Bookmarks.MOST_RECENT` | Resume from last stored bookmark         |
| `EPOCH`       | `Client.Bookmarks.EPOCH`       | Replay from beginning of transaction log |
| `NOW`         | `Client.Bookmarks.NOW`         | Start from current position (no replay)  |

### `AmpsAckType`

| Value       | AMPS Constant               | Description                         |
| ----------- | --------------------------- | ----------------------------------- |
| `None`      | `Message.AckType.None`      | No acknowledgment (fire-and-forget) |
| `Received`  | `Message.AckType.Received`  | Server received the message         |
| `Parsed`    | `Message.AckType.Parsed`    | Server parsed the message           |
| `Processed` | `Message.AckType.Processed` | Server processed the message        |
| `Persisted` | `Message.AckType.Persisted` | Message written to transaction log  |
| `Completed` | `Message.AckType.Completed` | Fully processed and persisted       |

---

## Message Headers

| Header Key                | Type                 | Description                                                                            |
| ------------------------- | -------------------- | -------------------------------------------------------------------------------------- |
| `ampsTopic`               | `String`             | AMPS topic name                                                                        |
| `ampsCorrelationId`       | `String`             | Correlation ID. Arbitrary string for application use; must contain valid Base64 chars. |
| `ampsBookmark`            | `String`             | AMPS bookmark — identifier to locate a message in the transaction log                  |
| `ampsTimestamp`           | `String`             | ISO-8601 timestamp set by AMPS when it processes the message                           |
| `ampsMessageClass`        | `String`             | Message class name (for header encoding)                                               |
| `ampsMessageVersion`      | `String`             | Message class version (for header encoding)                                            |
| `ampsMessageContentType`  | `String`             | Content type (for header encoding)                                                     |
| `ampsMessageHeaderParams` | `Map<String,String>` | Additional custom parameters (for header encoding)                                     |
| `ampsPublishHeader`       | `Boolean`            | Per-message flag to include AMPS header on publish                                     |
