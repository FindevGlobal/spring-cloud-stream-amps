package com.findevglobal.cloud.stream.binder.amps.integration;

import com.findevglobal.cloud.stream.binder.amps.AmpsMessageHeaders;
import com.findevglobal.cloud.stream.binder.amps.integration.annotation.Producer;
import com.findevglobal.cloud.stream.binder.amps.integration.annotation.TestApp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
public class MessageProcessingTest extends BaseDockerIntegrationTest {

    @Autowired
    private Producer processor;

    @Test
    public void checkMessageConversion() {
        Dto dto = Dto.dto("test");
        processor.output1().send(
                MessageBuilder.withPayload(dto).build()
        );
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> Assertions.assertTrue(
                        Inputs.INPUT.contains(dto)
                ));
    }

    @Test
    public void ackBadMessages() {
        String json = "{\"user\":{\"id\":\"test\"}}";
        Dto dto = Dto.dto("test");
        processor.output1().send(
                MessageBuilder.withPayload(json).build()
        );
        processor.output1().send(
                MessageBuilder.withPayload(dto).build()
        );
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> Assertions.assertTrue(
                        Inputs.INPUT.contains(dto)
                ));
    }

    @Test
    public void checkAmpsMessageHeader() {
        String correlationId = "amps-correlation-id";
        Dto dto = Dto.dto("test");
        processor.output2().send(
                MessageBuilder.withPayload(dto).setHeader(AmpsMessageHeaders.CORRELATION_ID, correlationId).build()
        );
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> Assertions.assertFalse(
                        Inputs.INPUT.isEmpty()
                ));
        Message<Dto> message = (Message) Inputs.INPUT.get(0);
        Assertions.assertEquals(dto, message.getPayload());
        Assertions.assertEquals(correlationId, message.getHeaders().get(AmpsMessageHeaders.CORRELATION_ID));
        Assertions.assertEquals("/topic2/queue", message.getHeaders().get(AmpsMessageHeaders.TOPIC));
        Assertions.assertNotNull(message.getHeaders().get(AmpsMessageHeaders.TIMESTAMP));
        Assertions.assertNotNull(message.getHeaders().get(AmpsMessageHeaders.BOOKMARK));
    }


}
