package com.findevglobal.cloud.stream.binder.amps.properties;

import com.crankuptheamps.client.Message;

import java.time.Duration;

/**
 * Extended producer properties for AMPS binder.
 */
public class AmpsProducerProperties {
    private AmpsAckType ackType = AmpsAckType.Processed;

    private Duration timeout = Duration.ofSeconds(10);

    private Duration expiration;

    /**
     * {@link Message.AckType}
     */
    public enum AmpsAckType {
        None(Message.AckType.None),
        Received(Message.AckType.Received),
        Parsed(Message.AckType.Parsed),
        Processed(Message.AckType.Processed),
        Persisted(Message.AckType.Persisted),
        Completed(Message.AckType.Completed);

        private int ampsAckType;

        public int toAmpsAckType() {
            return ampsAckType;
        }

        AmpsAckType(int ampsAckType) {
            this.ampsAckType = ampsAckType;
        }
    }

    /**
     * {@link com.crankuptheamps.client.Command#setAckType}
     * <br>
     * @return amps ackType value
     */
    public AmpsAckType getAckType() {
        return ackType;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setAckType}
     * <br>
     * Sets amps ackType value
     * @param ackType
     */
    public void setAckType(AmpsAckType ackType) {
        this.ackType = ackType;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setTimeout}
     * <br>
     * @return amps waiting timeout
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setTimeout}
     * <br>
     * Sets amps waiting timeout
     * @param timeout
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setExpiration}
     * <br>
     * Amps message expiration timeout
     */
    public Duration getExpiration() {
        return expiration;
    }

    /**
     * {@link com.crankuptheamps.client.Command#setExpiration}
     * <br>
     * Sets amps message expiration timeout
     * @param expiration
     */
    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }
}
