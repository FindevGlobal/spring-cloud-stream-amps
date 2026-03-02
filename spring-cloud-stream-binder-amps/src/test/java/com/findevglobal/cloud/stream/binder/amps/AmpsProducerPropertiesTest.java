package com.findevglobal.cloud.stream.binder.amps;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.Message;
import com.findevglobal.cloud.stream.binder.amps.app.TestAmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.app.TestApp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("amps-producer-test")
public class AmpsProducerPropertiesTest {
    @Autowired
    private TestAmpsConnectionFactoryProvider ampsConnectionFactoryProvider;

    @Autowired
    private StreamBridge streamBridge;

    @Test
    public void checkSendMessage() {
        streamBridge.send(
                "output3",
                MessageBuilder.withPayload("TEST").build()
        );
        Command command = getPublishCommand("topic3");

        Assertions.assertEquals("TEST", command.getData());
        Assertions.assertEquals((int) Duration.ofSeconds(200).getSeconds(), command.getExpiration());
        Assertions.assertEquals(Message.AckType.Persisted, command.getAckType());
        Assertions.assertEquals(Duration.ofSeconds(100).toMillis(), command.getTimeout());
    }

    private Command getPublishCommand(final String topic) {
        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        return ampsConnectionFactoryProvider.getClients().stream().map(client -> {
            try {
                verify(client, atLeastOnce()).publish(commandCaptor.capture(), any());
            } catch (Throwable e) {
                return null;
            }
            return commandCaptor.getValue();
        }).filter(c -> c != null && topic.equals(c.getTopic())).findFirst().get();
    }
}
