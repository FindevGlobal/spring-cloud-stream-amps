package com.findevglobal.cloud.stream.binder.amps;

/**
 * The amps headers for a {@link org.springframework.messaging.Message}.
 */
public final class AmpsMessageHeaders {
    private AmpsMessageHeaders() {
    }

    /**
     * The key for the amps topic.
     * The value should be String.
     */
    public static final String TOPIC = "ampsTopic";

    /**
     * The key for the amps correlation id.
     * The value should be String.
     */
    public static final String CORRELATION_ID = "ampsCorrelationId";

    /**
     * The key for the amps bookmark.
     * The value should be String.
     */
    public static final String BOOKMARK = "ampsBookmark";

    /**
     * The key for the amps timestamp.
     * The value should be String.
     */
    public static final String TIMESTAMP = "ampsTimestamp";

    /**
     * The key for the amps message class name.
     * The value should be String.
     */
    public static final String MESSAGE_CLASS = "ampsMessageClass";

    /**
     * The key for the amps message class version.
     * The value should be String.
     */
    public static final String MESSAGE_VERSION = "ampsMessageVersion";

    /**
     * The key for the amps message content type.
     * The value should be String.
     */
    public static final String MESSAGE_CONTENT_TYPE = "ampsMessageContentType";

    /**
     * The key for the amps message header params.
     * The value should be {@code Map<String,String>}
     */
    public static final String MESSAGE_HEADER_PARAMS = "ampsMessageHeaderParams";

    /**
     * The key for publishing amp header param.
     * The value should be boolean.
     */
    public static final String PUBLISH_AMPS_HEADER = "ampsPublishHeader";
}
