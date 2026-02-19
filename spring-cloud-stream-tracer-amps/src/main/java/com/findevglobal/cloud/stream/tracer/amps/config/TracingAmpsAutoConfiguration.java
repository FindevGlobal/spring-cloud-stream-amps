package com.findevglobal.cloud.stream.tracer.amps.config;

import com.findevglobal.cloud.stream.tracer.amps.AmpsTracer;
import com.findevglobal.cloud.stream.tracer.amps.TracingAmpsConnectionFactoryProviderBeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@AutoConfigureAfter(SleuthTracingAmpsConfiguration.class)
@Import({SleuthTracingAmpsConfiguration.class, OpenTracingAmpsConfiguration.class})
public class TracingAmpsAutoConfiguration {

    @Bean
    @ConditionalOnBean(AmpsTracer.class)
    static TracingAmpsConnectionFactoryProviderBeanPostProcessor tracingAmpsConnectionFactoryProviderBeanPostProcessor(
            AmpsTracer ampsTracer) {
        return new TracingAmpsConnectionFactoryProviderBeanPostProcessor(ampsTracer);
    }
}
