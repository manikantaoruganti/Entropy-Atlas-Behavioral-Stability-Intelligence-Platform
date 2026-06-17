package com.entropyatlas.entropyatlas.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    public static final String BEHAVIOR_EVENTS_TOPIC = "behavior-events";
    public static final String ENTITY_FEATURES_TOPIC = "entity-features";
    public static final String ENTITY_ENTROPY_TOPIC = "entity-entropy";
    public static final String ENTITY_DRIFT_TOPIC = "entity-drift";
    public static final String ENTITY_STABILITY_TOPIC = "entity-stability";

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName()); // Using String for simplicity, will use JSON Serde later
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000); // Commit every 1 second
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1); // For local development
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public NewTopic behaviorEventsTopic() {
        return TopicBuilder.name(BEHAVIOR_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic entityFeaturesTopic() {
        return TopicBuilder.name(ENTITY_FEATURES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic entityEntropyTopic() {
        return TopicBuilder.name(ENTITY_ENTROPY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic entityDriftTopic() {
        return TopicBuilder.name(ENTITY_DRIFT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic entityStabilityTopic() {
        return TopicBuilder.name(ENTITY_STABILITY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
