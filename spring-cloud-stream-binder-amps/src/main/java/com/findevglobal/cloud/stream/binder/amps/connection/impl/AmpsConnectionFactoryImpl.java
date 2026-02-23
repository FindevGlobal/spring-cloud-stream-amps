package com.findevglobal.cloud.stream.binder.amps.connection.impl;

import com.crankuptheamps.client.Authenticator;
import com.crankuptheamps.client.ConnectionInfo;
import com.crankuptheamps.client.DefaultServerChooser;
import com.crankuptheamps.client.ExponentialDelayStrategy;
import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.Message;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnection;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import com.findevglobal.cloud.stream.binder.amps.connection.BookmarkStoreProvider;
import com.findevglobal.cloud.stream.binder.amps.connection.MessageStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AmpsConnectionFactoryImpl implements AmpsConnectionFactory {

    private static final Logger log = LoggerFactory.getLogger(AmpsConnectionFactoryImpl.class);

    private final AtomicInteger sequence = new AtomicInteger(0);

    private final String clientName;
    private final Duration heartbeat;
    private final String[] urls;
    private final MessageStoreProvider publishMessageStoreProvider;
    private final BookmarkStoreProvider bookmarkStoreProvider;
    private final AmpsHeaderConverter ampsHeaderConverter;
    private final boolean publishAmpsHeader;
    private final String username;
    private final String password;
    private final Authenticator authenticator;

    public AmpsConnectionFactoryImpl(String clientName,
                                     Duration heartbeat,
                                     String[] urls,
                                     MessageStoreProvider publishMessageStoreProvider,
                                     BookmarkStoreProvider bookmarkStoreProvider,
                                     AmpsHeaderConverter ampsHeaderConverter,
                                     boolean publishAmpsHeader,
                                     String username,
                                     String password,
                                     Authenticator authenticator) {
        this.clientName = clientName;
        this.heartbeat = heartbeat;
        this.urls = urls;
        this.publishMessageStoreProvider = publishMessageStoreProvider;
        this.bookmarkStoreProvider = bookmarkStoreProvider;
        this.ampsHeaderConverter = ampsHeaderConverter;
        this.publishAmpsHeader = publishAmpsHeader;
        this.username = username;
        this.password = password;
        this.authenticator = authenticator;
    }

    @Override
    public AmpsConnection getConnection() {
        try {
            return new AmpsConnectionImpl(createAmpsClient(), ampsHeaderConverter, publishAmpsHeader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HAClient createAmpsClient() throws Exception {
        HAClient client = new HAClient(getClientName(clientName));
        client.setFailedWriteHandler((message, reason) ->
                log.error("Server failed to persist message {} for reason {}", message.copy(), toReasonName(reason)));
        try {
            // Setup stores
            String pClientName = client.getName();
            client.setBookmarkStore(bookmarkStoreProvider.getSubscriptionBookmarkStore(pClientName));
            client.setPublishStore(publishMessageStoreProvider.getPublishMessageStore(pClientName));
            Authenticator authenticator = this.authenticator != null
                    ? this.authenticator
                    : (password != null ? new PasswordAuthenticator(password) : null);

            DefaultServerChooser sc = new DefaultServerChooser() {
                @Override
                public Authenticator getCurrentAuthenticator() {
                    return authenticator;
                }

                @Override
                public void reportSuccess(final ConnectionInfo info) {
                    log.info("Client is now CONNECTED. ConnectionInfo={}", info);
                }

                @Override
                public void reportFailure(final Exception exception, final ConnectionInfo info) throws Exception {
                    log.info("Client is DISCONNECTED. ConnectionInfo={}", info, exception);
                    super.reportFailure(exception, info);
                }
            };
            for (String url : urls) {
                sc.add(fixUsername(url));
            }
            client.setServerChooser(sc);
            client.setReconnectDelayStrategy(new ExponentialDelayStrategy());
            // Determine heartbeat
            client.setHeartbeat((int) heartbeat.getSeconds());
            // Connect and logon - as it says
            client.connectAndLogon();
            return client;
        } catch (Exception e) {
            client.close();
            throw e;
        }
    }

    private static String toReasonName(int reasonCode) {
        Field[] fields = Message.Reason.class.getFields();
        for (Field field : fields) {
            try {
                if (field.getType() == int.class && field.getInt(null) == reasonCode) {
                    return field.getName();
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return "UNKNOWN";
    }

    private static class PasswordAuthenticator implements Authenticator {
        private final String password;

        PasswordAuthenticator(String password) {
            this.password = password;
        }

        @Override
        public String authenticate(String username, String currentPassword) {
            return this.password;
        }

        @Override
        public String retry(String username, String currentPassword) {
            return this.password;
        }

        @Override
        public void completed(String username, String currentPassword, int reason) {
        }
    }

    /**
     * Amps needs the user identity, and it is sent in the URL
     * amps://username@host:port/amps
     *
     * @param url
     *          amps connection to add username to
     * @return url with username added
     */
    private String fixUsername(String url) {
        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            String newUserInfo;

            if (!StringUtils.hasLength(username)) {
                if (!StringUtils.hasLength(userInfo)) {
                    newUserInfo = System.getProperty("user.name", "unknown_user").toLowerCase(Locale.US);
                } else {
                    return url;
                }
            } else {
                if (!StringUtils.hasLength(userInfo) && !Objects.equals(userInfo, username)) {
                    log.warn("Amps connection string has a username of {} which is NOT the property user of {}",
                            userInfo, username);
                }
                newUserInfo = username;
            }
            return new URI(uri.getScheme(), newUserInfo, uri.getHost(), uri.getPort(), uri.getPath(),
                    uri.getQuery(), uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            log.warn("Amps connection string of {} is malformed - Amps will probably reject the connection!", url);
        }
        return url;
    }

    private String getClientName(String pClientSuffix) {
        String processId = ManagementFactory.getRuntimeMXBean().getName();
        return String.format(Locale.US, "%s_%s_%d", pClientSuffix, processId, sequence.incrementAndGet());
    }
}
