package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import com.entropyatlas.entropyatlas.repositories.BehaviorEventRepository;
import com.entropyatlas.entropyatlas.repositories.StabilitySnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic implementation of {@link RiskScoringService}. It aggregates a set of hard‑coded rule checks
 * that operate on historical behavioral events and the latest stability snapshot. The algorithm is fully
 * deterministic, with no stochastic ML component, making it suitable for a production‑grade risk engine.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoringServiceImpl implements RiskScoringService {

    private final BehaviorEventRepository behaviorEventRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;

    // Configuration constants – can be tuned without code change (future hook could expose via configuration).
    private static final int VELOCITY_WINDOW_SECONDS = 300; // 5 minutes
    private static final int VELOCITY_THRESHOLD = 5; // >5 events = spike
    private static final long LOCATION_DRIFT_MAX_AGE_SECONDS = 3600; // 1 hour
    private static final double AMOUNT_DEVIATION_MULTIPLIER = 3.0; // >3x mean = deviation
    private static final double STABILITY_SCORE_THRESHOLD = 50.0; // below = instability
    private static final double DRIFT_VELOCITY_THRESHOLD = 1.5; // above = instability

    @Override
    public RiskScoringResult calculateRisk(String entityId, double amount, String location, String deviceId, Instant timestamp) {
        // Gather historical events for the entity
        List<BehaviorEvent> history = behaviorEventRepository.findByEntityIdOrderByTimestampAsc(entityId);
        List<BehaviorEvent> recent = history.stream()
                .filter(e -> Duration.between(e.getTimestamp(), timestamp).getSeconds() <= VELOCITY_WINDOW_SECONDS)
                .collect(Collectors.toList());

        Set<String> triggered = new LinkedHashSet<>();
        Map<String, Double> evidence = new HashMap<>();
        double score = 0.10;

        // 1. Velocity Spike
        if (recent.size() > VELOCITY_THRESHOLD) {
            triggered.add("VELOCITY_SPIKE");
            double contribution = 0.35;
            evidence.put("VELOCITY_SPIKE", contribution);
            score += contribution;
        }

        // 2. Location Drift
        Optional<BehaviorEvent> lastEventOpt = history.isEmpty() ? Optional.empty() : Optional.of(history.get(history.size() - 1));
        if (lastEventOpt.isPresent()) {
            BehaviorEvent last = lastEventOpt.get();
            long deltaSec = Math.abs(Duration.between(last.getTimestamp(), timestamp).getSeconds());
            if (!Objects.equals(last.getLocation(), location) && deltaSec <= LOCATION_DRIFT_MAX_AGE_SECONDS) {
                triggered.add("LOCATION_DRIFT");
                double contribution = 0.25;
                evidence.put("LOCATION_DRIFT", contribution);
                score += contribution;
            }
        }

        // 3. Device Drift
        if (lastEventOpt.isPresent()) {
            String lastDevice = lastEventOpt.get().getMetadata() != null ? lastEventOpt.get().getMetadata().get("deviceId") : null;
            if (lastDevice != null && !lastDevice.equals(deviceId)) {
                triggered.add("DEVICE_DRIFT");
                double contribution = 0.20;
                evidence.put("DEVICE_DRIFT", contribution);
                score += contribution;
            }
        }

        // 4. Amount Deviation
        double avgAmount = history.stream()
                .filter(e -> e.getMetadata() != null && e.getMetadata().containsKey("amount"))
                .mapToDouble(e -> {
                    try { return Double.parseDouble(e.getMetadata().get("amount")); } catch (Exception ex) { return 0.0; }
                })
                .average()
                .orElse(0.0);
        if (avgAmount > 0 && amount > AMOUNT_DEVIATION_MULTIPLIER * avgAmount) {
            triggered.add("AMOUNT_DEVIATION");
            double contribution = 0.20;
            evidence.put("AMOUNT_DEVIATION", contribution);
            score += contribution;
        }

        // 5. Behavioral Instability (stability snapshot)
        Optional<StabilitySnapshot> snapOpt = stabilitySnapshotRepository.findFirstByEntityIdOrderByTimestampDesc(entityId);
        if (snapOpt.isPresent()) {
            StabilitySnapshot snap = snapOpt.get();
            boolean unstable = snap.getBehavioralStabilityScore() < STABILITY_SCORE_THRESHOLD || snap.getDriftVelocity() > DRIFT_VELOCITY_THRESHOLD;
            if (unstable) {
                triggered.add("BEHAVIORAL_INSTABILITY");
                double contribution = 0.30;
                evidence.put("BEHAVIORAL_INSTABILITY", contribution);
                score += contribution;
            }
        }

        // Clamp score between 0 and 1, then round to 2 decimal places to avoid floating-point precision drift
        score = Math.min(1.0, Math.max(0.0, score));
        score = Math.round(score * 100.0) / 100.0;

        // Determine risk level based on score thresholds – deterministic mapping.
        String level;
        if (score >= 0.85) {
            level = "CRITICAL";
        } else if (score >= 0.65) {
            level = "HIGH";
        } else if (score >= 0.35) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        // Confidence is 1.0 for deterministic rules; if we had partial evidence we could lower it.
        double confidence = 1.0;

        return new RiskScoringResult(score, level, "PAYMENT_ABUSE", confidence, evidence, triggered);
    }
}
