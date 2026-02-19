package com.findevglobal.cloud.stream.binder.amps.connection;

import com.crankuptheamps.client.BookmarkStore;

/**
 * AMPS bookmark store provider
 */
public interface BookmarkStoreProvider {
    /**
     * Provides a bookmark store for a client
     * @param clientName
     * @return a bookmark store
     */
    BookmarkStore getSubscriptionBookmarkStore(String clientName);
}
