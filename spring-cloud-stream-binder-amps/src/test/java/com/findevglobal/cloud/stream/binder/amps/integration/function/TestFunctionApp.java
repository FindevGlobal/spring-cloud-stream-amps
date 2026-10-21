package com.findevglobal.cloud.stream.binder.amps.integration.function;

import com.findevglobal.cloud.stream.binder.amps.integration.Dto;
import com.findevglobal.cloud.stream.binder.amps.integration.Inputs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@SpringBootApplication
public class TestFunctionApp {
    public static void main(final String[] args) {
        SpringApplication.run(TestFunctionApp.class, args);
    }

    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
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
