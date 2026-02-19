package com.findevglobal.cloud.stream.tracer.amps.config;

import com.findevglobal.cloud.stream.tracer.amps.AmpsTracer;
import com.findevglobal.cloud.stream.tracer.amps.OpenTracingTracer;
import io.opentracing.util.GlobalTracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(GlobalTracer.class)
public class OpenTracingAmpsConfiguration {
    @Bean
    @ConditionalOnMissingBean
    AmpsTracer ampsTracer() {
        return new OpenTracingTracer(GlobalTracer.get());
    }
}
