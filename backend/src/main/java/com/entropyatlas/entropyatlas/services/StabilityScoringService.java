package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StabilityScoringService {

    private final ObjectMapper objectMapper;

    public StabilitySnapshot calculateStability(String entityId, JsonNode currentEntropy, JsonNode driftData) {
        double timingEntropy = currentEntropy.path("timingEntropy").asDouble(0.0);
        double locationEntropy = currentEntropy.path("locationEntropy").asDouble(0.0);
        double resourceEntropy = currentEntropy.path("resourceEntropy").asDouble(0.0);
        double actionEntropy = currentEntropy.path("actionEntropy").asDouble(0.0);
        double overallDriftVelocity = driftData.path("overallDriftVelocity").asDouble(0.0);

        // Sum of all entropy sources (max around 40 bits)
        double totalEntropy = timingEntropy + locationEntropy + resourceEntropy + actionEntropy;

        // Stability decreases as entropy and drift velocity increase
        double behavioralStabilityScore = 100.0 - (totalEntropy * 2.0 + Math.abs(overallDriftVelocity) * 5.0);
        behavioralStabilityScore = Math.max(0.0, Math.min(100.0, behavioralStabilityScore));

        double instabilityIndex = 100.0 - behavioralStabilityScore;

        // Entropy growth is the average change in entropy (which is overallDriftVelocity)
        double entropyGrowth = overallDriftVelocity;
        String volatilityTrend = determineVolatilityTrend(overallDriftVelocity);

        StabilitySnapshot snapshot = new StabilitySnapshot(
                UUID.randomUUID().toString(),
                entityId,
                Instant.parse(currentEntropy.path("timestamp").asText(java.time.Instant.now().toString())),
                Math.round(behavioralStabilityScore * 100.0) / 100.0,
                Math.round(instabilityIndex * 100.0) / 100.0,
                Math.round(entropyGrowth * 10000.0) / 10000.0,
                Math.round(overallDriftVelocity * 10000.0) / 10000.0,
                volatilityTrend
        );

        log.debug("Calculated stability snapshot for entity {}: {}", entityId, snapshot);
        return snapshot;
    }

    private String determineVolatilityTrend(double driftVelocity) {
        if (Math.abs(driftVelocity) > 1.5) return "HIGH";
        if (Math.abs(driftVelocity) > 0.5) return "MODERATE";
        return "LOW";
    }
}
