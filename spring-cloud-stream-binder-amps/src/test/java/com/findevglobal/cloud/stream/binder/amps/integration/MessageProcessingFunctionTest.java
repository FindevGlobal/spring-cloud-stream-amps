package com.findevglobal.cloud.stream.binder.amps.integration;

import com.findevglobal.cloud.stream.binder.amps.integration.function.TestFunctionApp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = TestFunctionApp.class)
@ActiveProfiles("test-function")
public class MessageProcessingFunctionTest extends BaseDockerIntegrationTest {

    @Autowired
    private StreamBridge streamBridge;

    @Test
    public void checkCloudFunctions() {
        Dto dto = Dto.dto("test");
        streamBridge.send(
                "output1",
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
        streamBridge.send(
                "output2",
                MessageBuilder.withPayload(json).build()
        );
        streamBridge.send(
                "output2",
                MessageBuilder.withPayload(dto).build()
        );
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> Assertions.assertTrue(
                        Inputs.INPUT.contains(dto)
                ));
    }
}
