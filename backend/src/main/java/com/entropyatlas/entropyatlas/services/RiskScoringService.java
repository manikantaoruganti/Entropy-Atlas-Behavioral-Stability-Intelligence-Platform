package com.entropyatlas.entropyatlas.services;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Service responsible for deterministic risk scoring based on behavioral signals and transaction telemetry.
 * <p>
 * The implementation is pure Java logic without any ML models – suitable for a production‑grade deterministic engine.
 */
public interface RiskScoringService {

    /**
     * Calculate the risk score for a given transaction.
     *
     * @param entityId   the identifier of the entity (e.g., user or merchant)
     * @param amount     the transaction amount (numeric)
     * @param location   the geo‑location string from the event
     * @param deviceId   the device identifier associated with the transaction
     * @param timestamp  the timestamp of the transaction event
     * @return a {@link RiskScoringResult} containing score, level and explanatory data
     */
    RiskScoringResult calculateRisk(String entityId, double amount, String location, String deviceId, Instant timestamp);

    /**
     * Immutable holder for a risk scoring outcome.
     */
    class RiskScoringResult {
        private final double score; // 0.0 – 1.0
        private final String level; // LOW, MEDIUM, HIGH, CRITICAL
        private final String type;  // PAYMENT_ABUSE (constant for this engine)
        private final double confidence; // 0.0 – 1.0 reflecting deterministic certainty
        private final Map<String, Double> evidence; // signal -> contribution weight
        private final Set<String> triggeredSignals; // names of signals that fired

        public RiskScoringResult(double score, String level, String type, double confidence,
                                 Map<String, Double> evidence, Set<String> triggeredSignals) {
            this.score = score;
            this.level = level;
            this.type = type;
            this.confidence = confidence;
            this.evidence = evidence == null ? Collections.emptyMap() : evidence;
            this.triggeredSignals = triggeredSignals == null ? Collections.emptySet() : triggeredSignals;
        }

        public double getScore() { return score; }
        public String getLevel() { return level; }
        public String getType() { return type; }
        public double getConfidence() { return confidence; }
        public Map<String, Double> getEvidence() { return evidence; }
        public Set<String> getTriggeredSignals() { return triggeredSignals; }
    }
}
