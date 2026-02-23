package com.findevglobal.cloud.stream.binder.amps.connection.impl;

import com.crankuptheamps.client.Authenticator;
import com.crankuptheamps.client.MemoryBookmarkStore;
import com.crankuptheamps.client.MemoryPublishStore;
import com.crankuptheamps.client.exception.StoreException;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import com.findevglobal.cloud.stream.binder.amps.connection.BookmarkStoreProvider;
import com.findevglobal.cloud.stream.binder.amps.connection.MessageStoreProvider;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;
import org.springframework.context.ApplicationContext;

import static com.findevglobal.cloud.stream.binder.amps.config.AmpsBinderConfiguration.DEFAULT_BINDER_FACTORY_BEAN_PREFIX;

public class AmpsConnectionFactoryProviderImpl implements AmpsConnectionFactoryProvider {

    private final ApplicationContext applicationContext;

    public AmpsConnectionFactoryProviderImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public AmpsConnectionFactory getConnectionFactory(AmpsBinderConfigurationProperties properties) {
        // publish message store provider
        String publishMessageStoreBeanName = properties.getPublishMessageStoreProviderBeanName();
        MessageStoreProvider publishMessageStoreProvider = null;
        if (publishMessageStoreBeanName != null) {
            if (applicationContext.containsBean(publishMessageStoreBeanName)) {
                publishMessageStoreProvider = applicationContext.getBean(
                        publishMessageStoreBeanName, MessageStoreProvider.class);
            } else if (applicationContext.containsBean(
                    publishMessageStoreBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX)) {
                publishMessageStoreProvider = applicationContext.getBean(
                        publishMessageStoreBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX,
                        MessageStoreProvider.class);
            }
            if (publishMessageStoreProvider == null) {
                throw new RuntimeException("Error when finding bean: " + publishMessageStoreBeanName);
            }
        } else {
            publishMessageStoreProvider = (clientName) -> {
                try {
                    return new MemoryPublishStore(properties.getPublishStoreSize());
                } catch (StoreException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        // bookmark store provider
        String subscriptionBookmarkStoreBeanName = properties.getSubscriptionBookmarkStoreProviderBeanName();
        BookmarkStoreProvider bookmarkStoreProvider = null;
        if (subscriptionBookmarkStoreBeanName != null) {
            if (applicationContext.containsBean(subscriptionBookmarkStoreBeanName)) {
                bookmarkStoreProvider = applicationContext.getBean(
                        subscriptionBookmarkStoreBeanName, BookmarkStoreProvider.class);
            } else if (applicationContext.containsBean(
                    subscriptionBookmarkStoreBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX)) {
                bookmarkStoreProvider = applicationContext.getBean(
                        subscriptionBookmarkStoreBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX,
                        BookmarkStoreProvider.class);
            }
            if (bookmarkStoreProvider == null) {
                throw new RuntimeException("Error when finding bean: " + subscriptionBookmarkStoreBeanName);
            }
        } else {
            bookmarkStoreProvider = clientName -> new MemoryBookmarkStore();
        }

        // amps header converter
        String ampsHeaderConverterBeanName = properties.getAmpsHeaderConverterBeanName();
        AmpsHeaderConverter ampsHeaderConverter = null;
        if (ampsHeaderConverterBeanName != null) {
            if (applicationContext.containsBean(ampsHeaderConverterBeanName)) {
                ampsHeaderConverter = applicationContext.getBean(
                        ampsHeaderConverterBeanName, AmpsHeaderConverter.class);
            } else if (applicationContext.containsBean(
                    ampsHeaderConverterBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX)) {
                ampsHeaderConverter = applicationContext.getBean(
                        ampsHeaderConverterBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX,
                        AmpsHeaderConverter.class);
            }
            if (ampsHeaderConverter == null) {
                throw new RuntimeException("Error when finding bean: " + ampsHeaderConverterBeanName);
            }
        } else {
            ampsHeaderConverter = new AmpsHeaderConverterImpl();
        }

        // authenticator
        String authenticatorBeanName = properties.getAuthenticatorBeanName();
        Authenticator authenticator = null;
        if (authenticatorBeanName != null) {
            if (applicationContext.containsBean(authenticatorBeanName)) {
                authenticator = applicationContext.getBean(authenticatorBeanName, Authenticator.class);
            } else if (applicationContext.containsBean(
                    authenticatorBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX)) {
                authenticator = applicationContext.getBean(
                        authenticatorBeanName + DEFAULT_BINDER_FACTORY_BEAN_PREFIX,
                        Authenticator.class);
            }
            if (authenticator == null) {
                throw new RuntimeException("Error when finding bean: " + authenticatorBeanName);
            }
        }

        return new AmpsConnectionFactoryImpl(
                properties.getName(),
                properties.getHeartBeatInterval(),
                properties.getAmpsConnectionStrings(),
                publishMessageStoreProvider,
                bookmarkStoreProvider,
                ampsHeaderConverter,
                properties.isPublishAmpsHeader(),
                properties.getUsername(),
                properties.getPassword(),
                authenticator);
    }
}
