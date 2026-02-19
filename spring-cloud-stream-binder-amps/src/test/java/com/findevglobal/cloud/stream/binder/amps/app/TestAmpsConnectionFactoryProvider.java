package com.findevglobal.cloud.stream.binder.amps.app;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestAmpsConnectionFactoryProvider implements AmpsConnectionFactoryProvider {
    private final List<AmpsBinderConfigurationProperties> binderProperties = new ArrayList<>();
    private final List<AmpsConnection> clients = new ArrayList<>();

    @Override
    public AmpsConnectionFactory getConnectionFactory(final AmpsBinderConfigurationProperties properties) {
        binderProperties.add(properties);
        return () -> {
            AmpsConnection client = Mockito.mock(AmpsConnection.class);
            clients.add(client);
            return client;
        };
    }

    public List<AmpsBinderConfigurationProperties> getBinderProperties() {
        return Collections.unmodifiableList(binderProperties);
    }

    public List<AmpsConnection> getClients() {
        return Collections.unmodifiableList(clients);
    }
}
