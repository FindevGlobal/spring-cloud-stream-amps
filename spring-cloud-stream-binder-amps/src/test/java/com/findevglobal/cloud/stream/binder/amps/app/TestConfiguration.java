package com.findevglobal.cloud.stream.binder.amps.app;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.connection.BookmarkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
