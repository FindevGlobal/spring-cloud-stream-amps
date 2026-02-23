package com.findevglobal.cloud.stream.binder.amps.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

@ExtendWith(SpringExtension.class)
public abstract class BaseDockerIntegrationTest {

    @DynamicPropertySource
    public static void setup(final DynamicPropertyRegistry registry) {
        GenericContainer<?> ampsContainer = new GenericContainer("vdesabou/amps:latest")
                .withExposedPorts(50000)
                .withCopyFileToContainer(MountableFile.forClasspathResource("defaultAmpsConfig.xml"), "/config.xml")
                .withStartupTimeout(Duration.ofMinutes(2L));

        Startables.deepStart(ampsContainer).join();
        Runtime.getRuntime().addShutdownHook(new Thread(ampsContainer::stop));
        registry.add("spring.cloud.stream.amps.binder.brokers", () ->
                String.format("tcp://%s:%d/json",
                        ampsContainer.getContainerIpAddress(), ampsContainer.getFirstMappedPort()));
    }

    @BeforeEach
    public final void before() {
        Inputs.INPUT.clear();
    }
}
