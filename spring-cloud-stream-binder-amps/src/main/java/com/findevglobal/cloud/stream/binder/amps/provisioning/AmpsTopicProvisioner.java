package com.findevglobal.cloud.stream.binder.amps.provisioning;

import com.findevglobal.cloud.stream.binder.amps.properties.AmpsConsumerProperties;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * AMPS implementation for {@link ProvisioningProvider}.
 */
public class AmpsTopicProvisioner implements ProvisioningProvider<
        ExtendedConsumerProperties<AmpsConsumerProperties>, ExtendedProducerProperties<AmpsProducerProperties>> {

    @Override
    public ProducerDestination provisionProducerDestination(String name,
               ExtendedProducerProperties<AmpsProducerProperties> properties) {
        return new AmpsProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
               ExtendedConsumerProperties<AmpsConsumerProperties> properties) {
        return new AmpsConsumerDestination(name);
    }
}
