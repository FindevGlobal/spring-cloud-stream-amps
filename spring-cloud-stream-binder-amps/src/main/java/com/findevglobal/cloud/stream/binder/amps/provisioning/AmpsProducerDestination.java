package com.findevglobal.cloud.stream.binder.amps.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

public class AmpsProducerDestination implements ProducerDestination {

    private final String name;

    public AmpsProducerDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameForPartition(int partition) {
        throw new UnsupportedOperationException("Partitioning is not implemented for amps messaging.");
    }
}
