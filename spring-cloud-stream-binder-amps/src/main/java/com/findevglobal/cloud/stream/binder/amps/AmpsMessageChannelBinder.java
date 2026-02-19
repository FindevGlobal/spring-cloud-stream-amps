package com.findevglobal.cloud.stream.binder.amps;

import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactory;
import com.findevglobal.cloud.stream.binder.amps.connection.AmpsConnectionFactoryProvider;
import com.findevglobal.cloud.stream.binder.amps.connection.impl.AmpsConnectionFactoryProviderImpl;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsBinderConfigurationProperties;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsConsumerProperties;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsExtendedBindingProperties;
import com.findevglobal.cloud.stream.binder.amps.properties.AmpsProducerProperties;
import com.findevglobal.cloud.stream.binder.amps.provisioning.AmpsTopicProvisioner;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import static com.findevglobal.cloud.stream.binder.amps.config.AmpsBinderConfiguration.DEFAULT_BINDER_FACTORY_BEAN_PREFIX;

/**
 * A org.springframework.cloud.stream.binder.Binder that uses AMPS as the underlying middleware.
 */
public class AmpsMessageChannelBinder
        extends AbstractMessageChannelBinder<ExtendedConsumerProperties<AmpsConsumerProperties>,
                ExtendedProducerProperties<AmpsProducerProperties>,
                AmpsTopicProvisioner>
        implements ExtendedPropertiesBinder<MessageChannel, AmpsConsumerProperties, AmpsProducerProperties> {

    private static final String AMPS_CONNECTION_FACTORY_PROVIDER_BEAN = "ampsConnectionFactoryProvider";
    private AmpsConnectionFactory ampsConnectionFactory;
    private final AmpsBinderConfigurationProperties configurationProperties;
    private final AmpsExtendedBindingProperties ampsExtendedBindingProperties;

    public AmpsMessageChannelBinder(
            AmpsBinderConfigurationProperties configurationProperties,
            AmpsTopicProvisioner provisioningProvider,
            AmpsExtendedBindingProperties ampsExtendedBindingProperties) {
        super(new String[0], provisioningProvider);
        this.configurationProperties = configurationProperties;
        this.ampsExtendedBindingProperties = ampsExtendedBindingProperties;
    }

    @Override
    protected void onInit() throws Exception {
        super.onInit();
        AmpsConnectionFactoryProvider factoryProvider = null;
        if (getApplicationContext().containsBean(AMPS_CONNECTION_FACTORY_PROVIDER_BEAN)) {
            factoryProvider = getApplicationContext().getBean(
                    AMPS_CONNECTION_FACTORY_PROVIDER_BEAN, AmpsConnectionFactoryProvider.class);
        } else if (getApplicationContext().containsBean(
                AMPS_CONNECTION_FACTORY_PROVIDER_BEAN + DEFAULT_BINDER_FACTORY_BEAN_PREFIX)) {
            factoryProvider = getApplicationContext().getBean(
                    AMPS_CONNECTION_FACTORY_PROVIDER_BEAN  + DEFAULT_BINDER_FACTORY_BEAN_PREFIX,
                    AmpsConnectionFactoryProvider.class);
        }
        if (factoryProvider == null) {
            factoryProvider = new AmpsConnectionFactoryProviderImpl(getApplicationContext());
        }
        this.ampsConnectionFactory = factoryProvider.getConnectionFactory(configurationProperties);
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
                                                          ExtendedProducerProperties<AmpsProducerProperties>
                                                                  producerProperties,
                                                          MessageChannel errorChannel) {
        return new AmpsProducerMessageHandler(ampsConnectionFactory, destination, producerProperties);
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination,
                                                     String group,
                                                     ExtendedConsumerProperties<AmpsConsumerProperties>
                                                                 properties) {
        AmpsMessageProducer ampsMessageProducer = new AmpsMessageProducer(
                ampsConnectionFactory, destination, properties);
        ampsMessageProducer.setApplicationContext(getApplicationContext());
        return ampsMessageProducer;
    }

    @Override
    public AmpsConsumerProperties getExtendedConsumerProperties(String channelName) {
        return ampsExtendedBindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public AmpsProducerProperties getExtendedProducerProperties(String channelName) {
        return ampsExtendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return ampsExtendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return ampsExtendedBindingProperties.getExtendedPropertiesEntryClass();
    }
}