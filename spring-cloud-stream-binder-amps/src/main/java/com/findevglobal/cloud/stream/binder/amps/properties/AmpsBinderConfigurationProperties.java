package com.findevglobal.cloud.stream.binder.amps.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

/**
 * Configuration properties for the AMPS binder.
 * The properties in this class are prefixed with spring.cloud.stream.amps.binder.
 */
@ConfigurationProperties(prefix = "spring.cloud.stream.amps.binder")
public class AmpsBinderConfigurationProperties {
    private String name = "default";
    private String[] brokers = new String[] { "localhost" };
    private String defaultBrokerMessageType = "json";
    private String defaultBrokerTransport = "tcp";
    private int defaultBrokerPort = 50000;
    private int publishStoreSize = 1024;
    private Duration maxReconnectTime = Duration.ofSeconds(0);
    private Duration heartBeatInterval = Duration.ofSeconds(10);
    private String publishMessageStoreProviderBeanName;
    private String subscriptionBookmarkStoreProviderBeanName;
    private String ampsHeaderConverterBeanName;
    private String username;
    private String password;
    private String authenticatorBeanName;

    private boolean publishAmpsHeader = false;

    /**
     * The connection strings have the following parts:
     * @see <a href="https://devnull.crankuptheamps.com/documentation/html/current/dev-guides/cpp/html/chapters/first-program.html#connection-strings">amps docs</a>
     *
     * @return built connection strings
     */
    public String[] getAmpsConnectionStrings() {
        return Arrays.stream(this.brokers).map(this::toConnectionString).toArray(String[]::new);
    }

    private String toConnectionString(String host) {
        try {
            String resolvingHost = host.contains("://") ? host : (defaultBrokerTransport + "://" + host);
            URI uri = new URI(resolvingHost);
            return new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort() != -1 ? uri.getPort() : defaultBrokerPort,
                    StringUtils.hasText(uri.getPath()) ? uri.getPath() : ("/" + defaultBrokerMessageType),
                    uri.getQuery(),
                    uri.getFragment()).toString();
        } catch (Exception e) {
            throw new RuntimeException("Can't parse broker uri: " + host, e);
        }
    }

    /**
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.MessageStoreProvider}
     *
     * @return amps publish message store
     */
    public String getPublishMessageStoreProviderBeanName() {
        return publishMessageStoreProviderBeanName;
    }

    /**
     * Sets amps publish message store
     * <br>
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.MessageStoreProvider}
     * @param publishMessageStoreProviderBeanName
     */
    public void setPublishMessageStoreProviderBeanName(String publishMessageStoreProviderBeanName) {
        this.publishMessageStoreProviderBeanName = publishMessageStoreProviderBeanName;
    }

    /**
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.BookmarkStoreProvider}
     *
     * @return amps subscription bookmark store
     */
    public String getSubscriptionBookmarkStoreProviderBeanName() {
        return subscriptionBookmarkStoreProviderBeanName;
    }

    /**
     * Sets amps subscription bookmark store
     * <br>
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.BookmarkStoreProvider}
     * @param subscriptionBookmarkStoreProviderBeanName
     */
    public void setSubscriptionBookmarkStoreProviderBeanName(String subscriptionBookmarkStoreProviderBeanName) {
        this.subscriptionBookmarkStoreProviderBeanName = subscriptionBookmarkStoreProviderBeanName;
    }

    /**
     * <br>
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter}

     * @return amps header converter
     */
    public String getAmpsHeaderConverterBeanName() {
        return ampsHeaderConverterBeanName;
    }

    /**
     * Sets amps header converter
     * <br>
     * It has to be instance of {@link com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter}
     * @param ampsHeaderConverterBeanName
     */
    public void setAmpsHeaderConverterBeanName(String ampsHeaderConverterBeanName) {
        this.ampsHeaderConverterBeanName = ampsHeaderConverterBeanName;
    }

    /**
     * @return amps username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets amps username
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return amps password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets amps password
     * Used when no {@link #getAuthenticatorBeanName()} is provided.
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return amps source name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets amps source name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return amps brokers
     */
    public String[] getBrokers() {
        return brokers;
    }

    /**
     * Sets amps brokers
     * @param brokers
     */
    public void setBrokers(String... brokers) {
        this.brokers = brokers;
    }

    /**
     * @return default amps broker message type
     */
    public String getDefaultBrokerMessageType() {
        return defaultBrokerMessageType;
    }

    /**
     * Sets default amps broker message type
     * @param defaultBrokerMessageType
     */
    public void setDefaultBrokerMessageType(String defaultBrokerMessageType) {
        this.defaultBrokerMessageType = defaultBrokerMessageType;
    }

    /**
     * @return default amps broker transport
     */
    public String getDefaultBrokerTransport() {
        return defaultBrokerTransport;
    }

    /**
     * @param defaultBrokerTransport default amps broker transport
     */
    public void setDefaultBrokerTransport(String defaultBrokerTransport) {
        this.defaultBrokerTransport = defaultBrokerTransport;
    }

    /**
     * @return default amps broker port
     */
    public int getDefaultBrokerPort() {
        return defaultBrokerPort;
    }

    /**
     * @param defaultBrokerPort default amps broker port
     */
    public void setDefaultBrokerPort(int defaultBrokerPort) {
        this.defaultBrokerPort = defaultBrokerPort;
    }

    /**
     * @return default amps broker store size
     */
    public int getPublishStoreSize() {
        return publishStoreSize;
    }

    /**
     * @param publishStoreSize default amps broker store size
     */
    public void setPublishStoreSize(int publishStoreSize) {
        this.publishStoreSize = publishStoreSize;
    }

    /**
     * @return amps broker max reconnect time
     */
    public Duration getMaxReconnectTime() {
        return maxReconnectTime;
    }

    /**
     * Sets amps broker max reconnect time
     * @param maxReconnectTime
     */
    public void setMaxReconnectTime(Duration maxReconnectTime) {
        this.maxReconnectTime = maxReconnectTime;
    }

    /**
     * @return amps broker heart beat interval
     */
    public Duration getHeartBeatInterval() {
        return heartBeatInterval;
    }

    /**
     * Sets amps broker heart beat interval
     * @param heartBeatInterval
     */
    public void setHeartBeatInterval(Duration heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    /**
     * @return if the amps header should be included in the request
     */
    public boolean isPublishAmpsHeader() {
        return publishAmpsHeader;
    }

    /**
     * Sets if the amps header should be included in the request
     * @param publishAmpsHeader
     */
    public void setPublishAmpsHeader(boolean publishAmpsHeader) {
        this.publishAmpsHeader = publishAmpsHeader;
    }

    /**
     * It has to be instance of {@link com.crankuptheamps.client.Authenticator}
     * Providing Authenticator takes precedence over {@link #getPassword()} authentication
     * @return Authenticator bean name
     */
    public String getAuthenticatorBeanName() {
        return authenticatorBeanName;
    }

    /**
     * Sets Authenticator bean name
     * <br>
     * It has to be instance of {@link com.crankuptheamps.client.Authenticator}
     * @param authenticatorBeanName
     */
    public void setAuthenticatorBeanName(String authenticatorBeanName) {
        this.authenticatorBeanName = authenticatorBeanName;
    }
}
