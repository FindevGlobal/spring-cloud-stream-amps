package com.findevglobal.cloud.stream.binder.amps;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.fields.Field;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.BookmarkProvider;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsConsumerProperties;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsConsumerProperties.BookmarkType;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.findevglobal.cloud.stream.binder.amps.config.AmpsBinderConfiguration.DEFAULT_BINDER_FACTORY_BEAN_PREFIX;
import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasLength;

/**
 * AMPS messages producer
 */
public class AmpsMessageProducer extends MessageProducerSupport {

    private final AtomicReference<AmpsConnection[]> clients = new AtomicReference<>();
    private final AmpsConnectionFactory ampsConnectionFactory;
    private final ConsumerDestination ampsConsumerDestination;
    private final ExtendedConsumerProperties<AmpsConsumerProperties> properties;

    public AmpsMessageProducer(AmpsConnectionFactory ampsConnectionFactory,
                               ConsumerDestination ampsConsumerDestination,
                               ExtendedConsumerProperties<AmpsConsumerProperties> properties) {
        this.ampsConnectionFactory = ampsConnectionFactory;
        this.ampsConsumerDestination = ampsConsumerDestination;
        this.properties = properties;
    }

    @Override
    public String getComponentType() {
        return "amps:message-producer";
    }

    @Override
    protected void doStart() {
        super.doStart();
        if (clients.get() != null) {
            throw new MessagingException("Binding " + properties.getBindingName() + " is already initialized");
        }
        clients.set(new AmpsConnection[properties.getConcurrency()]);
        MessageHandler messageHandler = message -> {
            if (clients.get() == null) {
                throw new MessagingException("Client is closed");
            }
            Field dataRaw = message.getDataRaw();
            byte[] data = Arrays.copyOfRange(dataRaw.buffer, dataRaw.position, dataRaw.position + dataRaw.length);
            try {
                MessageBuilder<byte[]> messageBuilder = MessageBuilder.withPayload(data);
                if (hasLength(message.getTopic())) {
                    messageBuilder.setHeader(AmpsMessageHeaders.TOPIC, message.getTopic());
                }
                if (hasLength(message.getCorrelationId())) {
                    messageBuilder.setHeader(AmpsMessageHeaders.CORRELATION_ID, message.getCorrelationId());
                }
                if (hasLength(message.getBookmark())) {
                    messageBuilder.setHeader(AmpsMessageHeaders.BOOKMARK, message.getBookmark());
                }
                if (hasLength(message.getTimestamp())) {
                    messageBuilder.setHeader(AmpsMessageHeaders.TIMESTAMP, message.getTimestamp());
                }
                sendMessage(messageBuilder.build());
            } catch (Exception e) {
                // Skip messages couldn't be converted
                if (isMessageConversionException(e)) {
                    logger.error(e, "Failed to parse message: " + e.getMessage() + ", message: " + message.getData());
                } else {
                    throw e;
                }
            }
            try {
                message.ack();
            } catch (AMPSException e) {
                throw new MessagingException("Error when confirming the message", e);
            }
        };

        try {
            for (int i = 0; i < properties.getConcurrency(); i++) {
                clients.get()[i] = ampsConnectionFactory.getConnection();
                clients.get()[i].setAutoAck(false);
                Set<String> options = new LinkedHashSet<>();
                AmpsConsumerProperties config = properties.getExtension();
                if (config.getHeartbeat() != null) {
                    clients.get()[i].setHeartbeat((int) config.getHeartbeat().getSeconds());
                }
                if (config.isOof()) {
                    options.add(Message.Options.OOF);
                }
                if (config.isWithTimestamp()) {
                    options.add(Message.Options.Timestamp);
                }
                ofNullable(config.getOptions()).map(Arrays::asList)
                        .ifPresent(configOptions -> configOptions.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(s -> s.endsWith(",") ? s : s + ",")
                                .forEach(options::add));
                ofNullable(config.getAckBatchSize()).ifPresent(clients.get()[i]::setAckBatchSize);
                ofNullable(config.getAckTimeout()).map(Duration::toMillis).ifPresent(clients.get()[i]::setAckTimeout);
                ofNullable(config.getRate()).map(Message.Options::Rate).ifPresent(options::add);
                ofNullable(config.getMaxBacklog()).map(Message.Options::MaxBacklog).ifPresent(options::add);

                Command command = new Command(getCommandType())
                        .setTopic(ampsConsumerDestination.getName())
                        .setBookmark(getBookmark())
                        .setOptions(!options.isEmpty() ? String.join("", options) : null);

                ofNullable(config.getFilter()).ifPresent(command::setFilter);
                ofNullable(config.getTimeout()).map(Duration::toMillis).ifPresent(command::setTimeout);
                ofNullable(config.getBatchSize()).ifPresent(command::setBatchSize);
                clients.get()[i].subscribe(command, messageHandler);
            }
        } catch (AMPSException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMessageConversionException(Exception e) {
        if (e instanceof MessageConversionException) {
            return true;
        }
        /*
         * According to this discussion: https://github.com/spring-cloud/spring-cloud-function/issues/901
         * Spring Cloud Function suppresses all the exceptions and tries to propagate the raw bytes array
         * as a function argument which causes ClassCastException
         */
        if (e instanceof MessagingException
                && e.getCause() instanceof ClassCastException
                && e.getCause().getMessage() != null
                && e.getCause().getMessage().contains("[B cannot be cast to")) {
            return true;
        }
        return false;
    }

    private int getCommandType() {
        AmpsConsumerProperties config = properties.getExtension();
        if (config.isSnapshot()) {
            return Message.Command.SOW;
        } else if (config.isSow()) {
            return Message.Command.SOWAndSubscribe;
        } else {
           return Message.Command.Subscribe;
        }
    }

    private String getBookmark() {
        AmpsConsumerProperties config = properties.getExtension();
        if (config.getBookmarkProviderBeanName() != null) {
            BookmarkProvider bookmarkProvider = null;
            if (getApplicationContext().containsBean(config.getBookmarkProviderBeanName())) {
                bookmarkProvider = getApplicationContext().getBean(
                        config.getBookmarkProviderBeanName(), BookmarkProvider.class);
            } else if (getApplicationContext().containsBean(
                    config.getBookmarkProviderBeanName() + DEFAULT_BINDER_FACTORY_BEAN_PREFIX)) {
                bookmarkProvider = getApplicationContext().getBean(
                        config.getBookmarkProviderBeanName() + DEFAULT_BINDER_FACTORY_BEAN_PREFIX,
                        BookmarkProvider.class);
            }
            if (bookmarkProvider == null) {
                throw new NoSuchBeanDefinitionException(
                        "There is no such a bean with name " + config.getBookmarkProviderBeanName());
            }
            String providedBookmark = bookmarkProvider.getBookmark();
            if (providedBookmark != null) {
                return providedBookmark;
            }
        } else if (config.getBookmarkType() != null) {
            return config.getBookmarkType().getValue();
        }
        return BookmarkType.DEFAULT.getValue();
    }

    @Override
    protected void doStop() {
        super.doStop();
        AmpsConnection[] ampsConnections = clients.getAndSet(null);
        if (ampsConnections == null) {
            return;
        }
        for (AmpsConnection client : ampsConnections) {
            if (client != null) {
                client.close();
            }
        }
    }
}
