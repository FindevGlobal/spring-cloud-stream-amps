package com.findevglobal.cloud.stream.binder.amps.integration.annotation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findevglobal.cloud.stream.binder.amps.integration.Dto;
import com.findevglobal.cloud.stream.binder.amps.integration.Inputs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

@SpringBootApplication
@EnableBinding({Consumer.class, Producer.class})
public class TestApp {

    public static void main(final String[] args) {
        SpringApplication.run(TestApp.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    @StreamListener(Consumer.INPUT1)
    public void listener(final Dto dto) {
        Inputs.INPUT.add(dto);
    }

    @StreamListener(Consumer.INPUT2)
    public void listener(final Message<Dto> message) {
        Inputs.INPUT.add(message);
    }
}
