package com.findevglobal.cloud.stream.binder.amps.connection;

/**
 * AMPS subscription bookmark provider
 */
public interface BookmarkProvider {
    /**
     * @return bookmark
     */
    String getBookmark();
}
