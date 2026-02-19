# Bookmarks

AMPS bookmarks enable **durable subscriptions** — the ability to resume message consumption from a specific point in the transaction log, even after a client restart.

## Bookmark Types

The `bookmarkType` consumer property controls the starting position for a subscription.

### `DEFAULT`

Uses the AMPS client default behavior. Typically starts from the beginning of the transaction log for new subscriptions, or from the last stored bookmark for resuming subscriptions.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      bookmarkType: DEFAULT
```

### `MOST_RECENT`

Resumes from the **most recent bookmark** stored in the bookmark store. This is the typical choice for durable subscriptions that should pick up where they left off.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      bookmarkType: MOST_RECENT
```

!!! tip
For `MOST_RECENT` to work correctly across restarts, use a persistent bookmark store (not the default in-memory store).

### `EPOCH`

Replays **all messages** from the beginning of the transaction log. Useful for rebuilding state or reprocessing.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      bookmarkType: EPOCH
```

### `NOW`

Starts from the **current point** in the transaction log. No historical messages are replayed. Only messages published after the subscription is established are received.

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      bookmarkType: NOW
```

## Bookmark Store

The bookmark store persists the last-consumed bookmark for each subscription. On reconnection or restart, the client can resume from the stored bookmark.

### Default (In-Memory)

By default, the binder uses a `MemoryBookmarkStore`. Bookmarks are lost when the application stops.

### Persistent Store

For durable bookmarks, provide a custom `BookmarkStoreProvider` bean:

```java
@Bean
public BookmarkStoreProvider myBookmarkStore() {
    return () -> new LoggedBookmarkStore("./data/bookmarks");
}
```

Then reference it in the binder configuration:

```yaml
spring.cloud.stream.amps.binder:
  subscriptionBookmarkStoreProviderBeanName: myBookmarkStore
```

## Custom Bookmark Provider

For advanced scenarios where you need full control over the initial bookmark value, implement the `BookmarkProvider` interface:

```java
@Bean
public BookmarkProvider customBookmarkProvider() {
    return () -> {
        // Return a specific bookmark string, e.g., from a database
        return myDatabase.getLastProcessedBookmark("orders");
    };
}
```

Reference it in the consumer configuration:

```yaml
spring.cloud.stream.amps.bindings:
  orders-in-0:
    consumer:
      bookmarkProviderBeanName: customBookmarkProvider
```

!!! note
When `bookmarkProviderBeanName` is set, it takes precedence over `bookmarkType`.

## Resolution Order

The binder resolves the initial bookmark in this order:

1. **`bookmarkProviderBeanName`** — If set, the bean's bookmark is used.
2. **`bookmarkType`** — Maps to the corresponding AMPS `Client.Bookmarks.*` constant.
3. **Fallback** — `DEFAULT` (AMPS client default).

## Example: Durable Subscription with Persistent Bookmarks

```java
@Configuration
public class BookmarkConfig {

    @Bean
    public BookmarkStoreProvider persistentBookmarkStore() {
        return () -> new LoggedBookmarkStore("./data/bookmarks");
    }
}
```

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          brokers:
            - tcp://amps-server:50000/json
          subscriptionBookmarkStoreProviderBeanName: persistentBookmarkStore
        bindings:
          orders-in-0:
            consumer:
              bookmarkType: MOST_RECENT
              sow: true
      bindings:
        orders-in-0:
          destination: orders
          group: order-processor
```
