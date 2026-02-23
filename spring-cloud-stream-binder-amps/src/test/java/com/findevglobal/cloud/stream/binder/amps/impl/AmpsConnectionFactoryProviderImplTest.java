package com.findevglobal.cloud.stream.binder.amps.impl;

import com.findevglobal.cloud.stream.binder.amps.BaseDockerIntegrationTest;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.impl.AmpsConnectionFactoryImpl;
import com.findevglobal.cloud.stream.binder.amps.connection.impl.AmpsConnectionFactoryProviderImpl;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AmpsConnectionFactoryProviderImplTest extends BaseDockerIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    private AmpsConnectionFactoryProviderImpl provider;

    @BeforeEach
    public void setUp() {
        provider = new AmpsConnectionFactoryProviderImpl(applicationContext);
    }

    @Test
    public void testGetConnectionFactoryWithDefaultValues() {
        AmpsBinderConfigurationProperties properties = new AmpsBinderConfigurationProperties();
        properties.setName("testClient");
        properties.setHeartBeatInterval(Duration.ofSeconds(30));
        properties.setBrokers(getAmpsConnectionUrl());

        AmpsConnectionFactory factory = provider.getConnectionFactory(properties);

        assertNotNull(factory);
        assertTrue(factory instanceof AmpsConnectionFactoryImpl);

        AmpsConnectionFactoryImpl factoryImpl = (AmpsConnectionFactoryImpl) factory;
        assertEquals("testClient", ReflectionTestUtils.getField(factoryImpl, "clientName"));
        assertEquals(Duration.ofSeconds(30), ReflectionTestUtils.getField(factoryImpl, "heartbeat"));
        assertArrayEquals(new String[]{getAmpsConnectionUrl()},
                (String[]) ReflectionTestUtils.getField(factoryImpl, "urls"));
        assertNotNull(ReflectionTestUtils.getField(factoryImpl, "publishMessageStoreProvider"));
        assertNotNull(ReflectionTestUtils.getField(factoryImpl, "bookmarkStoreProvider"));
        assertNotNull(ReflectionTestUtils.getField(factoryImpl, "ampsHeaderConverter"));
        assertFalse((Boolean) ReflectionTestUtils.getField(factoryImpl, "publishAmpsHeader"));
        assertNull(ReflectionTestUtils.getField(factoryImpl, "username"));
        assertNull(ReflectionTestUtils.getField(factoryImpl, "password"));
        assertNull(ReflectionTestUtils.getField(factoryImpl, "authenticator"));
    }

    @Test
    public void testGetConnectionFactoryWithCustomValues() {
        AmpsBinderConfigurationProperties properties = new AmpsBinderConfigurationProperties();
        properties.setName("customClient");
        properties.setHeartBeatInterval(Duration.ofSeconds(60));
        properties.setBrokers(getAmpsConnectionUrl());
        properties.setPublishAmpsHeader(true);
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setAmpsHeaderConverterBeanName("customAmpsHeaderConverter");
        properties.setSubscriptionBookmarkStoreProviderBeanName("customAmpsBookmarkStore");
        properties.setPublishMessageStoreProviderBeanName("customAmpsPublishStore");
        properties.setAuthenticatorBeanName("customAmpsAuthenticator");

        AmpsConnectionFactory factory = provider.getConnectionFactory(properties);

        assertNotNull(factory);
        assertTrue(factory instanceof AmpsConnectionFactoryImpl);

        AmpsConnectionFactoryImpl factoryImpl = (AmpsConnectionFactoryImpl) factory;
        assertEquals("customClient", ReflectionTestUtils.getField(factoryImpl, "clientName"));
        assertEquals(Duration.ofSeconds(60), ReflectionTestUtils.getField(factoryImpl, "heartbeat"));
        assertArrayEquals(new String[]{getAmpsConnectionUrl()},
                (String[]) ReflectionTestUtils.getField(factoryImpl, "urls"));
        assertNotNull(ReflectionTestUtils.getField(factoryImpl, "publishMessageStoreProvider"));
        assertNotNull(ReflectionTestUtils.getField(factoryImpl, "bookmarkStoreProvider"));
        assertNotNull(ReflectionTestUtils.getField(factoryImpl, "ampsHeaderConverter"));
        assertTrue((Boolean) ReflectionTestUtils.getField(factoryImpl, "publishAmpsHeader"));
        assertEquals("user", ReflectionTestUtils.getField(factoryImpl, "username"));
        assertEquals("pass", ReflectionTestUtils.getField(factoryImpl, "password"));
        assertNotNull(ReflectionTestUtils.getField(factoryImpl, "authenticator"));
    }
}
