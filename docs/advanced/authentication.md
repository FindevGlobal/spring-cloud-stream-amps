# Authentication

The AMPS binder supports multiple authentication mechanisms for connecting to secured AMPS servers.

## Username and Password

The simplest authentication method. Set the `username` and `password` properties:

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          username: my-service-account
          password: s3cret
          brokers:
            - tcp://amps-server:50000/json
```

The binder creates a password-based `DefaultAuthenticator` and injects the username into each broker URI's userinfo component.

!!! note "Username Fallback"
If `username` is not set, the binder falls back to `System.getProperty("user.name")` — the OS username of the running process.

## Custom Authenticator

For advanced authentication (e.g., Kerberos, token-based, certificate-based), implement the AMPS `Authenticator` interface and register it as a Spring bean:

```java
import com.crankuptheamps.client.Authenticator;

@Bean
public Authenticator kerberosAuthenticator() {
    return new KerberosAuthenticator(
        "HTTP/amps-server@EXAMPLE.COM",
        "/etc/keytabs/service.keytab"
    );
}
```

Reference it in the binder configuration:

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          authenticatorBeanName: kerberosAuthenticator
          brokers:
            - tcp://amps-server:50000/json
```

!!! info
When `authenticatorBeanName` is set, the `username` and `password` properties are ignored for authentication. However, `username` is still used for the client URI if provided.

## TLS / SSL

For encrypted connections, use the `tcps` transport scheme in your broker URIs:

```yaml
spring:
  cloud:
    stream:
      amps:
        binder:
          brokers:
            - tcps://amps-server:50443/json
          defaultBrokerTransport: tcps
```

TLS configuration (trust stores, client certificates) is handled at the JVM level via standard Java system properties:

```bash
java -Djavax.net.ssl.trustStore=/path/to/truststore.jks \
     -Djavax.net.ssl.trustStorePassword=changeit \
     -Djavax.net.ssl.keyStore=/path/to/keystore.jks \
     -Djavax.net.ssl.keyStorePassword=changeit \
     -jar my-app.jar
```

## Multi-Binder Authentication

In multi-binder configurations, each binder can have its own authentication settings:

```yaml
spring:
  cloud:
    stream:
      binders:
        amps-prod:
          type: amps
          environment:
            spring.cloud.stream.amps.binder:
              brokers:
                - tcps://amps-prod:50443/json
              authenticatorBeanName: prodAuthenticator

        amps-dev:
          type: amps
          environment:
            spring.cloud.stream.amps.binder:
              brokers:
                - tcp://amps-dev:50000/json
              username: dev-user
              password: dev-password
```
