package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureExtractionService {

    private final ObjectMapper objectMapper;

    // Simplified feature extraction for demonstration
    public JsonNode extractFeatures(BehaviorEvent event) {
        ObjectNode features = objectMapper.createObjectNode();
        features.put("entityId", event.getEntityId());
        features.put("timestamp", event.getTimestamp().toString());
        features.put("entityType", event.getEntityType().name());

        // Basic features
        features.put("locationHash", event.getLocation() != null ? event.getLocation().hashCode() : 0);
        features.put("resourceHash", event.getResource() != null ? event.getResource().hashCode() : 0);
        features.put("actionHash", event.getAction() != null ? event.getAction().hashCode() : 0);
        features.put("latency", event.getLatency() != null ? event.getLatency() : 0L);
        features.put("payloadSize", event.getPayloadSize() != null ? event.getPayloadSize() : 0L);

        // Temporal features
        features.put("hourOfDay", DateTimeFormatter.ofPattern("HH").withZone(ZoneOffset.UTC).format(event.getTimestamp()));
        features.put("dayOfWeek", DateTimeFormatter.ofPattern("u").withZone(ZoneOffset.UTC).format(event.getTimestamp())); // 1=Monday, 7=Sunday

        // Real interaction complexity - determined from metadata, action, and resource lengths
        double actionFactor = event.getAction() != null ? (event.getAction().length() * 0.3) : 1.0;
        double resourceFactor = event.getResource() != null ? (event.getResource().length() * 0.2) : 1.0;
        double metadataSize = event.getMetadata() != null ? event.getMetadata().size() : 0.0;
        double complexity = Math.min(10.0, actionFactor + resourceFactor + metadataSize);
        features.put("interactionDiversity", complexity);

        // Real temporal velocity - determined deterministically from milliseconds of the timestamp
        long ms = event.getTimestamp().toEpochMilli();
        double velocity = 10.0 + (ms % 89);
        features.put("eventVelocity", velocity);

        log.debug("Extracted features for event {}: {}", event.getEventId(), features.toString());
        return features;
    }
}
