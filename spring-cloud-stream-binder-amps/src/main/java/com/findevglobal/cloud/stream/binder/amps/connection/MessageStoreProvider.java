package com.findevglobal.cloud.stream.binder.amps.connection;

import com.crankuptheamps.client.Store;

/**
 * AMPS message store provider
 */
public interface MessageStoreProvider {
    /**
     * Provides a message store for a client
     * @param clientName
     * @return a message store
     */
    Store getPublishMessageStore(String clientName);
}
