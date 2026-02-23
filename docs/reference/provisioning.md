# Provisioning

The AMPS binder uses a **no-op provisioner**. Unlike brokers such as Kafka or RabbitMQ, AMPS topics and SOW configurations are managed on the server side and cannot be created dynamically by the client.

## How It Works

The `AmpsTopicProvisioner` implements the Spring Cloud Stream `ProvisioningProvider` interface but performs no actual provisioning. When a binding is created:

- **Consumer destinations** — The provisioner returns an `AmpsConsumerDestination` wrapping the destination name. The topic must already exist on the AMPS server.
- **Producer destinations** — The provisioner returns an `AmpsProducerDestination` wrapping the destination name. The topic must already exist on the AMPS server.

## Server-Side Configuration

Topics, SOW caches, transaction logs, and queues must be configured in the AMPS server configuration file (`amps-config.xml`). For example:

```xml
<SOW>
    <Topic>
        <Name>orders</Name>
        <MessageType>json</MessageType>
        <Key>/orderId</Key>
        <FileName>./sow/%n.sow</FileName>
    </Topic>
</SOW>

<TransactionLog>
    <JournalDirectory>./journal</JournalDirectory>
    <PreallocatedJournalFiles>1</PreallocatedJournalFiles>
    <MinJournalSize>10MB</MinJournalSize>
    <Topic>
        <Name>orders</Name>
        <MessageType>json</MessageType>
    </Topic>
</TransactionLog>

<Queue>
    <Name>orders/order-queue</Name>
    <MessageType>json</MessageType>
    <UnderlyingTopic>orders</UnderlyingTopic>
    <LeasePeriod>30s</LeasePeriod>
    <MaxBacklog>1000</MaxBacklog>
</Queue>
```

## Partitioning

!!! warning
Partitioning is **not supported** by the AMPS binder. Calling partition-related methods on AMPS destinations throws `UnsupportedOperationException`.

If you need partitioned behavior, consider:

- Using multiple AMPS topics (one per partition)
- Using AMPS content filtering to route specific messages to specific consumers
