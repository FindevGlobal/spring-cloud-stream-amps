package com.findevglobal.cloud.stream.binder.amps;

import com.findevglobal.cloud.stream.binder.amps.app.TestAmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.app.TestApp;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("amps-binder-test")
public class AmpsBinderPropertiesTest {
    @Autowired
    private TestAmpsConnectionFactoryProvider ampsConnectionFactoryProvider;

    @Test
    public void checkSetAllProperties() {
        AmpsBinderConfigurationProperties p = getProperties("all-properties");
        Assertions.assertArrayEquals(new String[] {"tcp://instance-1:50000/json", "tcp://instance-2:50000/json"}, p.getBrokers());
        Assertions.assertEquals("binary", p.getDefaultBrokerMessageType());
        Assertions.assertEquals("https", p.getDefaultBrokerTransport());
        Assertions.assertEquals(10000, p.getDefaultBrokerPort());
        Assertions.assertEquals(2048, p.getPublishStoreSize());
        Assertions.assertEquals(Duration.ofSeconds(100), p.getMaxReconnectTime());
        Assertions.assertEquals(Duration.ofSeconds(200), p.getHeartBeatInterval());
        Assertions.assertEquals("storeProvider", p.getPublishMessageStoreProviderBeanName());
        Assertions.assertEquals("bookmarkProvider", p.getSubscriptionBookmarkStoreProviderBeanName());
        Assertions.assertEquals("authBean", p.getAuthenticatorBeanName());
        Assertions.assertTrue(p.isPublishAmpsHeader());
    }

    @Test
    public void checkAmpsConnectionStringBuilding() {
        AmpsBinderConfigurationProperties p = getProperties("connection-url");
        Assertions.assertArrayEquals(new String[] {
                "tcp://instance-1:50000/json",
                "https://instance-2:50000/json",
                "tcp://instance-1:10000/json",
                "tcp://instance-2:50000/binary",
                "https://instance-1:50000/binary",
                "https://instance-2:10000/binary"
        }, p.getAmpsConnectionStrings());
    }

    private AmpsBinderConfigurationProperties getProperties(final String name) {
        return ampsConnectionFactoryProvider.getBinderProperties().stream().filter(
                p -> name.equals(p.getName())
        ).findFirst().get();
    }
}
