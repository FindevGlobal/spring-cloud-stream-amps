package com.findevglobal.cloud.stream.binder.amps.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

import java.util.Map;

/**
 * AMPS specific extended binding properties class that extends from AbstractExtendedBindingProperties.
 */
@ConfigurationProperties("spring.cloud.stream.amps")
public class AmpsExtendedBindingProperties extends
        AbstractExtendedBindingProperties<AmpsConsumerProperties, AmpsProducerProperties, AmpsBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.amps.default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Map<String, AmpsBindingProperties> getBindings() {
        return this.doGetBindings();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return AmpsBindingProperties.class;
    }
}
