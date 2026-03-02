package com.findevglobal.cloud.stream.tracer.amps.config;

import com.findevglobal.cloud.stream.tracer.amps.AmpsTracer;
import com.findevglobal.cloud.stream.tracer.amps.MicrometerTracer;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(Tracer.class)
@ConditionalOnProperty(value = "spring.micrometer.amps.enabled", matchIfMissing = true)
public class MicrometerTracingAmpsConfiguration {
    @Bean
    @ConditionalOnMissingBean
    AmpsTracer ampsTracer(BeanFactory beanFactory) {
        return new MicrometerTracer(beanFactory);
    }
}
