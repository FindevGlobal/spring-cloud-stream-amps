package com.findevglobal.cloud.stream.binder.amps.integration.function;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findevglobal.cloud.stream.binder.amps.integration.Dto;
import com.findevglobal.cloud.stream.binder.amps.integration.Inputs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@SpringBootApplication
public class TestFunctionApp {
    public static void main(final String[] args) {
        SpringApplication.run(TestFunctionApp.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    @Bean
    public Consumer<Dto> input1() {
        return Inputs.INPUT::add;
    }

    @Bean
    public Function<Dto, Dto> input2output3() {
        return Function.identity();
    }

    @Bean
    public Consumer<Dto> input3() {
        return Inputs.INPUT::add;
    }

}
