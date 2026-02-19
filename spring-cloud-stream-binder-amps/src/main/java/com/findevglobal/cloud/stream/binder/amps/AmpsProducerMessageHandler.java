package com.findevglobal.cloud.stream.binder.amps;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.CommandId;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import java.time.Duration;

/**
 * A Message Handler for AMPS
 */
public class AmpsProducerMessageHandler extends AbstractMessageHandler implements Lifecycle {
    private volatile AmpsConnection client;
    private final AmpsConnectionFactory ampsConnectionFactory;
    private final ProducerDestination destination;
    private final ExtendedProducerProperties<AmpsProducerProperties> producerProperties;

    public AmpsProducerMessageHandler(AmpsConnectionFactory ampsConnectionFactory,
                                      ProducerDestination destination,
                                      ExtendedProducerProperties<AmpsProducerProperties> producerProperties) {
        this.ampsConnectionFactory = ampsConnectionFactory;
        this.destination = destination;
        this.producerProperties = producerProperties;
    }

    @Override
    public String getComponentType() {
        return "amps:producer-message-handler";
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        if (client == null) {
            throw new MessagingException("Producer is not initialized");
        }
        Command syncPublish = new Command(com.crankuptheamps.client.Message.Command.Publish);
        Object payload = message.getPayload();
        if (payload instanceof String) {
            syncPublish.setData((String) payload);
        } else if (payload instanceof byte[]) {
            byte[] bytes = (byte[]) payload;
            syncPublish.setData(bytes, 0, bytes.length);
        } else {
            throw new MessagingException(message, "Payload should be string or bytes");
        }
        Duration messageExpiration = producerProperties.getExtension()
                .getExpiration();
        if (messageExpiration != null && messageExpiration.getSeconds() > 0) {
            syncPublish.setExpiration((int) messageExpiration.getSeconds());
        }
        try {
            syncPublish.setAckType(producerProperties.getExtension().getAckType().toAmpsAckType())
                    .setCommandId(CommandId.nextIdentifier())
                    .setTopic(destination.getName())
                    .setTimeout(producerProperties.getExtension().getTimeout().toMillis());
            client.publish(syncPublish, message.getHeaders());
        } catch (Exception e) {
            throw new MessagingException(message, e);
        }
    }

    @Override
    public void start() {
        client = ampsConnectionFactory.getConnection();
    }

    @Override
    public void stop() {
        if (client == null) {
            return;
        }
        client.close();
        client = null;
    }

    @Override
    public boolean isRunning() {
        return client != null;
    }
}
