package com.findevglobal.cloud.stream.binder.amps.impl;

import com.crankuptheamps.client.MemoryBookmarkStore;
import com.crankuptheamps.client.MemoryPublishStore;
import com.crankuptheamps.client.exception.StoreException;
import com.findevglobal.cloud.stream.binder.amps.BaseDockerIntegrationTest;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import com.findevglobal.cloud.stream.binder.amps.connection.impl.AmpsConnectionFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;

import java.time.Duration;
import java.util.HashMap;

public class AmpsConnectionFactoryImplTest extends BaseDockerIntegrationTest {

    @Test
    public void getConnection() {
        AmpsConnectionFactoryImpl factory = new AmpsConnectionFactoryImpl(
                "clientName",
                Duration.ofSeconds(10),
                new String[]{getAmpsConnectionUrl()},
                clientName -> {
                    try {
                        return new MemoryPublishStore(1024);
                    } catch (StoreException e) {
                        throw new RuntimeException(e);
                    }
                },
                clientName -> new MemoryBookmarkStore(),
                new AmpsHeaderConverter() {
                    @Override
                    public String toCorrelationId(final MessageHeaders messageHeaders) {
                        return "AUTO_GENERATED_ID";
                    }

                    @Override
                    public MessageHeaders toMessageHeaders(final String correlationId) {
                        return new MessageHeaders(new HashMap<>());
                    }
                },
                true,
                null,
                null,
                null
        );
        Assertions.assertNotNull(factory.getConnection());
    }
}
