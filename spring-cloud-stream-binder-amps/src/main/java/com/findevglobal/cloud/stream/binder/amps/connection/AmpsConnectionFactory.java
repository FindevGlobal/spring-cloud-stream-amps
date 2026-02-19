package com.findevglobal.cloud.stream.binder.amps.connection;

/**
 * Common interface for establishing connections to AMPS
 */
public interface AmpsConnectionFactory {
    /**
     * Provides an AMPS connection
     * @return AMPS connection
     */
    AmpsConnection getConnection();
}
