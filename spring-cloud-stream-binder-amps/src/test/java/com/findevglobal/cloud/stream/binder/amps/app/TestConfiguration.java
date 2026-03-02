package com.findevglobal.cloud.stream.binder.amps.app;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.connection.BookmarkProvider;
import com.findevglobal.cloud.stream.binder.amps.integration.Dto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class TestConfiguration {

    @Bean
    public BookmarkProvider customBookMarkProvider() {
        return () -> "CUSTOM_BOOKMARK";
    }

    @Bean
    public AmpsConnectionFactoryProvider ampsConnectionFactoryProvider() {
        return new TestAmpsConnectionFactoryProvider();
    }

    @Bean
    public Function<Dto, Dto> processor() {
        return Function.identity();
    }

    @Bean
    public Consumer<Dto> input1() {
        return dto -> {};
    }

    @Bean
    public Consumer<Dto> input2() {
        return dto -> {};
    }
}
