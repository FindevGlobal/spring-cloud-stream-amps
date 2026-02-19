package com.findevglobal.cloud.stream.binder.amps.connection;

import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;

/**
 * AMPS connection factories provider
 */
public interface AmpsConnectionFactoryProvider {
    /**
     * Provides a connection factory
     * @param properties
     * @return a connection factory
     */
    AmpsConnectionFactory getConnectionFactory(AmpsBinderConfigurationProperties properties);
}
