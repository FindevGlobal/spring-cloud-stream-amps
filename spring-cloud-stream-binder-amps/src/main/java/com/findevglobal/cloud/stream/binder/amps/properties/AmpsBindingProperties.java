package com.findevglobal.cloud.stream.binder.amps.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * Container object for AMPS specific extended producer and consumer binding properties.
 */
public class AmpsBindingProperties implements BinderSpecificPropertiesProvider {

    private final AmpsConsumerProperties consumer = new AmpsConsumerProperties();
    private final AmpsProducerProperties producer = new AmpsProducerProperties();

    @Override
    public AmpsConsumerProperties getConsumer() {
        return consumer;
    }

    @Override
    public AmpsProducerProperties getProducer() {
        return producer;
    }
}
