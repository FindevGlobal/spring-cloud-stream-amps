package com.findevglobal.cloud.stream.binder.amps.properties;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Message;

import java.time.Duration;

/**
 * Extended consumer properties for AMPS binder.
 */
public class AmpsConsumerProperties {
    private boolean snapshot;
    private boolean sow;
    private boolean oof;
    private String rate;
    private boolean withTimestamp;
    private Integer ackBatchSize;
    private Duration ackTimeout;
    private Duration heartbeat;
    private Duration timeout;
    private BookmarkType bookmarkType = BookmarkType.DEFAULT;
    private String filter;
    private String[] options;
    private Integer maxBacklog;
    private Integer batchSize;
    private String bookmarkProviderBeanName;

    public enum BookmarkType {
        DEFAULT(null),
        MOST_RECENT(Client.Bookmarks.MOST_RECENT),
        EPOCH(Client.Bookmarks.EPOCH),
        NOW(Client.Bookmarks.NOW);

        private final String value;

        BookmarkType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * {@link Message.Options#OOF oof}
     * <br>
     * @see <a href="https://devnull.crankuptheamps.com/documentation/html/current/user-guide/html/chapters/oof.html">amps docs</a>
     *
     * @return Out-of-Focus Messages flag
     */
    public boolean isOof() {
        return oof;
    }

    /**
     * {@link Message.Options#OOF oof}
     * <br>
     * @see <a href="https://devnull.crankuptheamps.com/documentation/html/current/user-guide/html/chapters/oof.html">amps docs</a>
     * Sets Out-of-Focus Messages flag
     * @param oof
     */
    public void setOof(boolean oof) {
        this.oof = oof;
    }

    /**
     * {@link Message.Options#Rate rate}
     * <br>
     *
     * @return amps broker connection rate
     */
    public String getRate() {
        return rate;
    }

    /**
     * {@link Message.Options#Rate rate}
     * <br>
     * amps broker connection rate
     * @param rate
     */
    public void setRate(String rate) {
        this.rate = rate;
    }

    /**
     * {@link Message.Options#Timestamp timestamp}
     * <br>
     * amps broker connection timestamp parameter
     * @return withTimestamp
     */
    public boolean isWithTimestamp() {
        return withTimestamp;
    }

    /**
     * {@link Message.Options#Timestamp timestamp}
     * <br>
     * amps broker connection timestamp parameter
     * @param withTimestamp
     */
    public void setWithTimestamp(boolean withTimestamp) {
        this.withTimestamp = withTimestamp;
    }

    /**
     * @see <a href="https://devnull.crankuptheamps.com/documentation/html/current/user-guide/html/chapters/sow.html">amps docs sow</a>
     * <br>
     * if you use SOW as an underlying topic for a queue
     * @return snapshot
     */
    public boolean isSnapshot() {
        return snapshot;
    }

    /**
     * @see <a href="https://devnull.crankuptheamps.com/documentation/html/current/user-guide/html/chapters/sow.html">amps docs sow</a>
     * <br>
     * if you use SOW as an underlying topic for a queue
     * @param snapshot
     */
    public void setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * @see <a href="https://devnull.crankuptheamps.com/documentation/html/current/user-guide/html/chapters/sow.html">amps docs sow</a>
     * <br>
     * if you use SOW as an underlying topic for a queue and subscribe to it
     * @return sow
     */
    public boolean isSow() {
        return sow;
    }

    /**
     * @see <a href="https://devnull.crankuptheamps.com/documentation/html/current/user-guide/html/chapters/sow.html">amps docs sow</a>
     * <br>
     * if you use SOW as an underlying topic for a queue and subscribe to it
     * @param sow
     */
    public void setSow(boolean sow) {
        this.sow = sow;
    }

    /**
     * {@link com.crankuptheamps.client.Client#setAckBatchSize}
     * <br>
     * @return amps broker connection ack batch size
     */
    public Integer getAckBatchSize() {
        return ackBatchSize;
    }

    /**
     * {@link com.crankuptheamps.client.Client#setAckBatchSize}
     * <br>
     * Sets amps broker connection ack batch size
     * @param ackBatchSize
     */
    public void setAckBatchSize(Integer ackBatchSize) {
        this.ackBatchSize = ackBatchSize;
    }

    /**
     * {@link com.crankuptheamps.client.Client#setAckTimeout}
     * <br>
     * @return amps broker connection ack timeout
     */
    public Duration getAckTimeout() {
        return ackTimeout;
    }

    /**
     * {@link com.crankuptheamps.client.Client#setAckTimeout}
     * <br>
     * Sets amps broker connection ack timeout
     * @param ackTimeout
     */
    public void setAckTimeout(Duration ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    /**
     * {@link com.crankuptheamps.client.Client#setHeartbeat(int)}
     * <br>
     * @return amps broker connection heartbeat
     */
    public Duration getHeartbeat() {
        return heartbeat;
    }

    /**
     * {@link com.crankuptheamps.client.Client#setHeartbeat(int)}
     * <br>
     * Sets amps broker connection heartbeat
     * @param heartbeat
     */
    public void setHeartbeat(Duration heartbeat) {
        this.heartbeat = heartbeat;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setTimeout}
     * <br>
     * @return amps broker connection timeout
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setTimeout}
     * <br>
     * Sets amps broker connection timeout
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setBookmark}
     * <br>
     * @return amps broker connection bookmark type
     */
    public BookmarkType getBookmarkType() {
        return bookmarkType;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setBookmark}
     * <br>
     * Sets amps broker connection bookmark type
     * @param bookmarkType
     */
    public void setBookmarkType(BookmarkType bookmarkType) {
        this.bookmarkType = bookmarkType;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setFilter}
     * <br>
     * @return amps broker connection filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setFilter}
     * <br>
     * Sets amps broker connection filter
     * @param filter
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setOptions}
     * <br>
     * @return amps broker connection options
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setOptions}
     * <br>
     * Sets amps broker connection options
     * @param options
     */
    public void setOptions(String... options) {
        this.options = options;
    }

    /**
     * {@link Message.Options#MaxBacklog}
     * <br>
     * @return amps broker connection max backlog
     */
    public Integer getMaxBacklog() {
        return maxBacklog;
    }

    /**
     * {@link Message.Options#MaxBacklog}
     * <br>
     * Sets amps broker connection max backlog
     * @param maxBacklog
     */
    public void setMaxBacklog(Integer maxBacklog) {
        this.maxBacklog = maxBacklog;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setBatchSize}
     * <br>
     * @return amps broker connection batch size
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setBatchSize}
     * <br>
     * Sets amps broker connection batch size
     * @param batchSize
     */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Bean name for providing initial bookmark
     * <br>
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.BookmarkProvider}
     * @return bookmarkProviderBeanName
     */
    public String getBookmarkProviderBeanName() {
        return bookmarkProviderBeanName;
    }

    /**
     * Sets bean name for providing initial bookmark
     * <br>
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.BookmarkProvider}
     * @param bookmarkProviderBeanName
     */
    public void setBookmarkProviderBeanName(String bookmarkProviderBeanName) {
        this.bookmarkProviderBeanName = bookmarkProviderBeanName;
    }
}
