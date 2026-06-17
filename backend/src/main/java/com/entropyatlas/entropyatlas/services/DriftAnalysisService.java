package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.domain.DriftExplanation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriftAnalysisService {

    private final ObjectMapper objectMapper;

    public JsonNode analyzeDrift(String entityId, JsonNode currentEntropy, JsonNode previousEntropy) {
        ObjectNode driftScores = objectMapper.createObjectNode();
        driftScores.put("entityId", entityId);
        driftScores.put("timestamp", currentEntropy.path("timestamp").asText(java.time.Instant.now().toString()));

        double currentTiming = currentEntropy.path("timingEntropy").asDouble(0.0);
        double prevTiming = previousEntropy != null ? previousEntropy.path("timingEntropy").asDouble(currentTiming) : currentTiming;

        double currentLoc = currentEntropy.path("locationEntropy").asDouble(0.0);
        double prevLoc = previousEntropy != null ? previousEntropy.path("locationEntropy").asDouble(currentLoc) : currentLoc;

        double currentRes = currentEntropy.path("resourceEntropy").asDouble(0.0);
        double prevRes = previousEntropy != null ? previousEntropy.path("resourceEntropy").asDouble(currentRes) : currentRes;

        double currentAct = currentEntropy.path("actionEntropy").asDouble(0.0);
        double prevAct = previousEntropy != null ? previousEntropy.path("actionEntropy").asDouble(currentAct) : currentAct;

        // Calculate drift velocity as the differences in entropy dimensions
        double timingDrift = currentTiming - prevTiming;
        double locationDrift = currentLoc - prevLoc;
        double resourceDrift = currentRes - prevRes;
        double actionDrift = currentAct - prevAct;

        driftScores.put("timingEntropyDrift", Math.round(timingDrift * 100.0) / 100.0);
        driftScores.put("locationEntropyDrift", Math.round(locationDrift * 100.0) / 100.0);
        driftScores.put("resourceEntropyDrift", Math.round(resourceDrift * 100.0) / 100.0);
        driftScores.put("actionEntropyDrift", Math.round(actionDrift * 100.0) / 100.0);

        // Overall velocity is mean absolute drift across all dimensions
        double overallDriftVelocity = (Math.abs(timingDrift) + Math.abs(locationDrift) + Math.abs(resourceDrift) + Math.abs(actionDrift)) / 4.0;
        driftScores.put("overallDriftVelocity", Math.round(overallDriftVelocity * 100.0) / 100.0);

        log.debug("Analyzed drift for entity {}: {}", entityId, driftScores.toString());
        return driftScores;
    }

    public DriftExplanation generateDriftExplanation(String entityId, String snapshotId, JsonNode driftData) {
        double timingDrift = Math.abs(driftData.path("timingEntropyDrift").asDouble(0.0));
        double locDrift = Math.abs(driftData.path("locationEntropyDrift").asDouble(0.0));
        double resDrift = Math.abs(driftData.path("resourceEntropyDrift").asDouble(0.0));
        double actDrift = Math.abs(driftData.path("actionEntropyDrift").asDouble(0.0));

        double totalDrift = timingDrift + locDrift + resDrift + actDrift;
        Map<String, Double> contributions = new HashMap<>();
        
        if (totalDrift > 0) {
            contributions.put("Timing Entropy", Math.round((timingDrift / totalDrift) * 1000.0) / 10.0);
            contributions.put("Location Entropy", Math.round((locDrift / totalDrift) * 1000.0) / 10.0);
            contributions.put("Resource Affinity Drift", Math.round((resDrift / totalDrift) * 1000.0) / 10.0);
            contributions.put("Action Diversity Drift", Math.round((actDrift / totalDrift) * 1000.0) / 10.0);
        } else {
            contributions.put("Timing Entropy", 0.0);
            contributions.put("Location Entropy", 0.0);
            contributions.put("Resource Affinity Drift", 0.0);
            contributions.put("Action Diversity Drift", 0.0);
        }

        String factors = contributions.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> String.format("%s (%.1f%%)", entry.getKey(), entry.getValue()))
                .collect(java.util.stream.Collectors.joining(", "));
                
        String summary = factors.isEmpty() 
            ? "Behavioral drift analyzed for entity " + entityId + ". No significant factors detected."
            : "Behavioral drift detected for entity " + entityId + ". Primary factors: " + factors + ".";

        return new DriftExplanation(
                UUID.randomUUID().toString(),
                entityId,
                Instant.parse(driftData.path("timestamp").asText(java.time.Instant.now().toString())),
                snapshotId,
                summary,
                contributions
        );
    }
}
