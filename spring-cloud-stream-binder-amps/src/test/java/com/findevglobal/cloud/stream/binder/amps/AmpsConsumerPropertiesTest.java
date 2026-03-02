package com.findevglobal.cloud.stream.binder.amps;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;
import com.findevglobal.cloud.stream.binder.amps.app.TestAmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.app.TestApp;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("amps-consumer-test")
public class AmpsConsumerPropertiesTest {
    @Autowired
    private TestAmpsConnectionFactoryProvider ampsConnectionFactoryProvider;

    @Test
    public void checkSetAllProperties() throws AMPSException {
        Pair<AmpsConnection, Command> result = getSubscriptionCommand("topic1");
        AmpsConnection client = result.getKey();

        verify(client).setAutoAck(false);
        verify(client).setHeartbeat((int) Duration.ofSeconds(200).getSeconds());
        verify(client).setAckBatchSize(100);
        verify(client).setAckTimeout(Duration.ofSeconds(100).toMillis());

        Command command = result.getValue();

        Assertions.assertEquals(Message.Command.SOW, command.getCommand());
        Assertions.assertEquals("topic1", command.getTopic());
        Assertions.assertEquals(Client.Bookmarks.MOST_RECENT, command.getBookmark());
        Assertions.assertEquals("timestamp,op1,op2,rate=10,max_backlog=200,", command.getOptions());

        Assertions.assertEquals("filter", command.getFilter());
        Assertions.assertEquals(Duration.ofSeconds(300).toMillis(), command.getTimeout());
        Assertions.assertEquals(50, command.getBatchSize());
    }

    @Test
    public void checkBookmarkProvider() {
        Pair<AmpsConnection, Command> result = getSubscriptionCommand("topic2");
        Command command = result.getValue();

        Assertions.assertEquals("CUSTOM_BOOKMARK", command.getBookmark());
    }

    private Pair<AmpsConnection, Command> getSubscriptionCommand(final String topic) {
        return ampsConnectionFactoryProvider.getClients().stream().map(client -> {
            ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
            try {
                verify(client, atLeastOnce()).subscribe(commandCaptor.capture(), any());
            } catch (Exception e) {
                return null;
            }
            return Pair.of(client, commandCaptor.getValue());
        }).filter(c -> c != null && topic.equals(c.getValue().getTopic())).findFirst().get();
    }

}
