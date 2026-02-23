package com.findevglobal.cloud.stream.binder.amps.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;

public class AmpsConsumerDestination implements ConsumerDestination {
    private final String name;

    public AmpsConsumerDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
