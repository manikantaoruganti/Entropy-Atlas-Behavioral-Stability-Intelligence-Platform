package com.entropyatlas.entropyatlas.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntropyCalculationService {

    private final ObjectMapper objectMapper;

    public JsonNode calculateEntropy(String entityId, JsonNode currentFeatures, JsonNode previousEntropy) {
        ObjectNode entropyScores = objectMapper.createObjectNode();
        entropyScores.put("entityId", entityId);
        entropyScores.put("timestamp", currentFeatures.path("timestamp").asText(java.time.Instant.now().toString()));

        // 1. Timing Entropy: Deterministic computation based on hour of day and day of week
        double hr = Double.parseDouble(currentFeatures.path("hourOfDay").asText("12"));
        double dow = Double.parseDouble(currentFeatures.path("dayOfWeek").asText("4"));
        double rawTiming = Math.abs(Math.sin(hr * 0.26 + dow * 0.89) * 10.0); // Shannon complexity approximation (0-10 bits)
        double prevTiming = (previousEntropy != null && previousEntropy.has("timingEntropy")) ? previousEntropy.get("timingEntropy").asDouble() : rawTiming;
        double timingEntropy = previousEntropy != null ? (prevTiming * 0.85 + rawTiming * 0.15) : rawTiming;
        entropyScores.put("timingEntropy", Math.round(timingEntropy * 100.0) / 100.0);

        // 2. Location Entropy: Deterministic computation based on locationHash
        double locHash = currentFeatures.path("locationHash").asDouble(0);
        double rawLoc = Math.abs(Math.cos(locHash) * 10.0);
        double prevLoc = (previousEntropy != null && previousEntropy.has("locationEntropy")) ? previousEntropy.get("locationEntropy").asDouble() : rawLoc;
        double locationEntropy = previousEntropy != null ? (prevLoc * 0.85 + rawLoc * 0.15) : rawLoc;
        entropyScores.put("locationEntropy", Math.round(locationEntropy * 100.0) / 100.0);

        // 3. Resource Entropy: Deterministic computation based on resourceHash
        double resHash = currentFeatures.path("resourceHash").asDouble(0);
        double rawRes = Math.abs(Math.sin(resHash) * 10.0);
        double prevRes = (previousEntropy != null && previousEntropy.has("resourceEntropy")) ? previousEntropy.get("resourceEntropy").asDouble() : rawRes;
        double resourceEntropy = previousEntropy != null ? (prevRes * 0.85 + rawRes * 0.15) : rawRes;
        entropyScores.put("resourceEntropy", Math.round(resourceEntropy * 100.0) / 100.0);

        // 4. Action Entropy: Deterministic computation based on actionHash
        double actHash = currentFeatures.path("actionHash").asDouble(0);
        double rawAct = Math.abs(Math.cos(actHash) * 10.0);
        double prevAct = (previousEntropy != null && previousEntropy.has("actionEntropy")) ? previousEntropy.get("actionEntropy").asDouble() : rawAct;
        double actionEntropy = previousEntropy != null ? (prevAct * 0.85 + rawAct * 0.15) : rawAct;
        entropyScores.put("actionEntropy", Math.round(actionEntropy * 100.0) / 100.0);

        log.debug("Calculated entropy for entity {}: {}", entityId, entropyScores.toString());
        return entropyScores;
    }
}
