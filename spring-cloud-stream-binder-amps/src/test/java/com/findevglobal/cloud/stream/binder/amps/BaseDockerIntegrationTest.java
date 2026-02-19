package com.findevglobal.cloud.stream.binder.amps;

import com.crankuptheamps.client.Authenticator;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsHeaderConverter;
import com.findevglobal.cloud.stream.binder.amps.connection.BookmarkStoreProvider;
import com.findevglobal.cloud.stream.binder.amps.connection.MessageStoreProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

@ContextConfiguration(classes = {BaseDockerIntegrationTest.Context.class})
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackageClasses = BaseDockerIntegrationTest.class)
@ActiveProfiles({"test"})
public abstract class BaseDockerIntegrationTest {

    private static final GenericContainer<?> AMPS_CONTAINER = new GenericContainer("vdesabou/amps:latest")
            .withExposedPorts(50000)
            .withCopyFileToContainer(MountableFile.forClasspathResource("defaultAmpsConfig.xml"), "/config.xml")
            .withStartupTimeout(Duration.ofMinutes(2L));

    static {
        Startables.deepStart(AMPS_CONTAINER).join();
        Runtime.getRuntime().addShutdownHook(new Thread(AMPS_CONTAINER::stop));
    }

    /**
     * @return connection url to amps
     */
    protected String getAmpsConnectionUrl() {
        return String.format("tcp://%s:%d/json",
                AMPS_CONTAINER.getContainerIpAddress(), AMPS_CONTAINER.getFirstMappedPort());
    }

    @Configuration
    public static class Context {
        @Bean
        public MessageStoreProvider customAmpsPublishStore() {
            return Mockito.mock(MessageStoreProvider.class);
        }

        @Bean
        public Authenticator customAmpsAuthenticator() {
            return Mockito.mock(Authenticator.class);
        }

        @Bean
        public BookmarkStoreProvider customAmpsBookmarkStore() {
            return Mockito.mock(BookmarkStoreProvider.class);
        }

        @Bean
        public AmpsHeaderConverter customAmpsHeaderConverter() {
            return Mockito.mock(AmpsHeaderConverter.class);
        }
    }

}
