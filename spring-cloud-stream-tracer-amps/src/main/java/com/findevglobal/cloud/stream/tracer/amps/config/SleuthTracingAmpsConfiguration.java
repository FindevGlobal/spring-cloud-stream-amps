package com.findevglobal.cloud.stream.tracer.amps.config;

import com.findevglobal.cloud.stream.tracer.amps.AmpsTracer;
import com.findevglobal.cloud.stream.tracer.amps.SleuthTracer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(BraveAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.sleuth.amps.enabled", matchIfMissing = true)
public class SleuthTracingAmpsConfiguration {
    @Bean
    @ConditionalOnMissingBean
    AmpsTracer ampsTracer(BeanFactory beanFactory) {
        return new SleuthTracer(beanFactory);
    }
}
