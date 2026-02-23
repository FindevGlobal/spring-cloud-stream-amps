package com.findevglobal.cloud.stream.tracer.amps;

import org.springframework.cloud.sleuth.docs.DocumentedSpan;
import org.springframework.cloud.sleuth.docs.TagKey;

public enum AmpsSpan implements DocumentedSpan {

    /**
     * Span created on the Amps consumer side.
     */
    AMPS_CONSUMER_SPAN {
        @Override
        public String getName() {
            return TracingConstants.AMPS_CONSUMER;
        }

        @Override
        public TagKey[] getTagKeys() {
            return ConsumerTags.values();
        }

        @Override
        public String prefix() {
            return TracingConstants.AMPS_COMPONENT + ".";
        }
    },

    /**
     * Span created on the Amps consumer side.
     */
    AMPS_PRODUCER_SPAN {
        @Override
        public String getName() {
            return TracingConstants.AMPS_PRODUCER;
        }

        @Override
        public TagKey[] getTagKeys() {
            return ProducerTags.values();
        }

        @Override
        public String prefix() {
            return TracingConstants.AMPS_COMPONENT + ".";
        }
    };

    enum ConsumerTags implements TagKey {
        /**
         * Name of the Kafka topic.
         */
        TOPIC {
            @Override
            public String getKey() {
                return TracingConstants.AMPS_TOPIC;
            }
        },
    }

    enum ProducerTags implements TagKey {
        /**
         * Name of the Amps topic.
         */
        TOPIC {
            @Override
            public String getKey() {
                return TracingConstants.AMPS_TOPIC;
            }
        },
    }
}
