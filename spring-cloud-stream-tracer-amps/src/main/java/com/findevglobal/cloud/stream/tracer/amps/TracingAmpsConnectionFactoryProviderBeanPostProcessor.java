package com.findevglobal.cloud.stream.tracer.amps;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class TracingAmpsConnectionFactoryProviderBeanPostProcessor implements BeanPostProcessor {

    private final AmpsTracer ampsTracer;

    public TracingAmpsConnectionFactoryProviderBeanPostProcessor(AmpsTracer ampsTracer) {
        this.ampsTracer = ampsTracer;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof AmpsConnectionFactoryProvider && !(bean instanceof TracingAmpsConnectionFactoryProvider)) {
            return new TracingAmpsConnectionFactoryProvider((AmpsConnectionFactoryProvider) bean, ampsTracer);
        }
        return bean;
    }
}
