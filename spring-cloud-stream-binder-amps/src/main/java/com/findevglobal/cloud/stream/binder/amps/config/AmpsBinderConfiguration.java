package com.findevglobal.cloud.stream.binder.amps.config;

import com.findevglobal.cloud.stream.binder.amps.AmpsMessageChannelBinder;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsExtendedBindingProperties;
import com.findevglobal.cloud.stream.binder.amps.provisioning.AmpsTopicProvisioner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({ AmpsExtendedBindingProperties.class, AmpsBinderConfigurationProperties.class })
public class AmpsBinderConfiguration {

    /**
     * {@link org.springframework.cloud.stream.binder.DefaultBinderFactory} registers shared beans
     * with the following prefix
     */
    public static final String DEFAULT_BINDER_FACTORY_BEAN_PREFIX = "_child";

    @Bean
    public AmpsTopicProvisioner ampsTopicProvisioner() {
        return new AmpsTopicProvisioner();
    }

    @Bean
    public AmpsMessageChannelBinder ampsMessageChannelBinder(
            AmpsBinderConfigurationProperties configurationProperties,
            AmpsTopicProvisioner ampsTopicProvisioner,
            AmpsExtendedBindingProperties ampsExtendedBindingProperties
    ) {
        return new AmpsMessageChannelBinder(
                configurationProperties, ampsTopicProvisioner, ampsExtendedBindingProperties);
    }
}
