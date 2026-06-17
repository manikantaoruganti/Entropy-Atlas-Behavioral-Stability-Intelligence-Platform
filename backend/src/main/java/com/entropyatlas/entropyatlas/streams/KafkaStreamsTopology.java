package com.entropyatlas.entropyatlas.streams;

import com.entropyatlas.entropyatlas.config.KafkaConfig;
import com.entropyatlas.entropyatlas.domain.DriftExplanation;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import com.entropyatlas.entropyatlas.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.KeyValueStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamsTopology {

    private final FeatureExtractionService featureExtractionService;
    private final EntropyCalculationService entropyCalculationService;
    private final DriftAnalysisService driftAnalysisService;
    private final StabilityScoringService stabilityScoringService;
    private final ExplainabilityService explainabilityService;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder) {

        // Serdes for JSON (using String for simplicity, but a custom JSON Serde is better for production)
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(new JsonSerializer<>(objectMapper), new JsonDeserializer<>(JsonNode.class, objectMapper));
        final Serde<StabilitySnapshot> stabilitySnapshotSerde = Serdes.serdeFrom(new JsonSerializer<>(objectMapper), new JsonDeserializer<>(StabilitySnapshot.class, objectMapper));

        // 1. Ingest Behavior Events
        KStream<String, String> behaviorEventsStream = builder.stream(
                KafkaConfig.BEHAVIOR_EVENTS_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // 2. Feature Extraction Engine
        KStream<String, JsonNode> entityFeaturesStream = behaviorEventsStream
                .mapValues((key, value) -> {
                    try {
                        metricsService.incrementEventsIngested();
                        return featureExtractionService.extractFeatures(objectMapper.readValue(value, com.entropyatlas.entropyatlas.domain.BehaviorEvent.class));
                    } catch (Exception e) {
                        log.error("Error extracting features for event {}: {}", key, e.getMessage());
                        return null; // Handle errors gracefully
                    }
                })
                .filter((key, value) -> value != null)
                .peek((key, value) -> log.debug("Produced entity features for {}: {}", key, value.toString()));

        entityFeaturesStream.to(KafkaConfig.ENTITY_FEATURES_TOPIC, Produced.with(Serdes.String(), jsonSerde));

        // 3. Entropy Engine
        // Aggregate features over a tumbling window to calculate entropy
        KTable<String, JsonNode> entityEntropyTable = entityFeaturesStream
                .groupByKey(Grouped.with(Serdes.String(), jsonSerde))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5))) // 5-minute tumbling window
                .aggregate(
                        () -> objectMapper.createObjectNode().put("count", 0), // Initializer
                        (key, newValue, aggregate) -> { // Aggregator
                            // In a real scenario, you'd aggregate feature distributions here
                            // For simplicity, we'll just pass the latest feature set and count
                            ((ObjectNode) aggregate).put("latestFeatures", newValue);
                            ((ObjectNode) aggregate).put("count", aggregate.get("count").asInt() + 1);
                            return aggregate;
                        },
                        Materialized.<String, JsonNode, WindowStore<Bytes, byte[]>>as("entity-features-window-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(jsonSerde)
                )
                .toStream()
                .mapValues((windowedKey, aggregatedFeatures) -> {
                    try {
                        metricsService.incrementEntropyCalculations();
                        // Simplified: calculate entropy based on the latest features in the window
                        JsonNode latestFeatures = aggregatedFeatures.get("latestFeatures");
                        JsonNode previousEntropy = null; // Would fetch from a KTable of previous entropies
                        return entropyCalculationService.calculateEntropy(windowedKey.key(), latestFeatures, previousEntropy);
                    } catch (Exception e) {
                        log.error("Error calculating entropy for entity {}: {}", windowedKey.key(), e.getMessage());
                        return null;
                    }
                })
                .filter((key, value) -> value != null)
                .selectKey((windowedKey, value) -> windowedKey.key())
                .groupByKey(Grouped.with(Serdes.String(), jsonSerde))
                .reduce(
                        (aggValue, newValue) -> newValue, // Take the latest entropy for the entity
                        Materialized.<String, JsonNode, KeyValueStore<Bytes, byte[]>>as("entity-entropy-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(jsonSerde)
                );

        entityEntropyTable.toStream().to(KafkaConfig.ENTITY_ENTROPY_TOPIC, Produced.with(Serdes.String(), jsonSerde));

        // 4. Drift Analysis Engine & Stability Scoring Engine
        KStream<String, StabilitySnapshot> stabilityStream = entityEntropyTable.toStream()
                .leftJoin(
                        builder.globalTable(
                                KafkaConfig.ENTITY_ENTROPY_TOPIC,
                                Consumed.with(Serdes.String(), jsonSerde),
                                Materialized.as("global-entity-entropy-table")
                        ),
                        (key, currentEntropy) -> key,
                        (entityId, currentEntropy, previousEntropy) -> {
                            try {
                                metricsService.incrementDriftDetections();
                                JsonNode driftData = driftAnalysisService.analyzeDrift(entityId, currentEntropy, previousEntropy);
                                StabilitySnapshot snapshot = stabilityScoringService.calculateStability(entityId, currentEntropy, driftData);
                                metricsService.incrementStabilityUpdates();

                                // Persist drift explanation if significant drift is detected
                                if (snapshot.getDriftVelocity() > 20 || snapshot.getInstabilityIndex() > 70) { // Example threshold
                                    DriftExplanation explanation = driftAnalysisService.generateDriftExplanation(entityId, snapshot.getId(), driftData);
                                    explainabilityService.saveDriftExplanation(explanation);
                                    log.info("Generated drift explanation for entity {}: {}", entityId, explanation.getExplanationSummary());
                                }
                                return snapshot;
                            } catch (Exception e) {
                                log.error("Error analyzing drift or calculating stability for entity {}: {}", entityId, e.getMessage());
                                return null;
                            }
                        }
                )
                .filter((key, value) -> value != null)
                .peek((key, value) -> log.debug("Produced stability snapshot for {}: {}", key, value.toString()));

        stabilityStream.to(KafkaConfig.ENTITY_STABILITY_TOPIC, Produced.with(Serdes.String(), stabilitySnapshotSerde));

        return behaviorEventsStream; // Return the initial stream for chaining if needed
    }
}
