package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.domain.*;
import com.entropyatlas.entropyatlas.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class BehavioralIntelligencePipeline {

    private final BehaviorEventRepository behaviorEventRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final DriftExplanationRepository driftExplanationRepository;
    private final FeatureExtractionService featureExtractionService;
    private final EntropyCalculationService entropyCalculationService;
    private final DriftAnalysisService driftAnalysisService;
    private final StabilityScoringService stabilityScoringService;
    private final ExplainabilityService explainabilityService;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    @Transactional
    public StabilitySnapshot processEvent(BehaviorEvent event) {
        String entityId = event.getEntityId();
        log.info("Processing intelligence pipeline for entity: {}", entityId);

        // 1. Extract features for current event
        JsonNode currentFeatures = featureExtractionService.extractFeatures(event);

        // 2. Load previous snapshot and calculate/load previous entropy
        Optional<StabilitySnapshot> previousSnapshotOpt = stabilitySnapshotRepository.findFirstByEntityIdOrderByTimestampDesc(entityId);
        
        // Construct previous entropy JsonNode if previous snapshot exists
        ObjectNode previousEntropy = objectMapper.createObjectNode();
        if (previousSnapshotOpt.isPresent()) {
            StabilitySnapshot prevSnap = previousSnapshotOpt.get();
            previousEntropy.put("timingEntropy", 100.0 - prevSnap.getBehavioralStabilityScore());
            previousEntropy.put("locationEntropy", 100.0 - prevSnap.getBehavioralStabilityScore());
            previousEntropy.put("resourceEntropy", 100.0 - prevSnap.getBehavioralStabilityScore());
            previousEntropy.put("actionEntropy", 100.0 - prevSnap.getBehavioralStabilityScore());
            previousEntropy.put("timestamp", prevSnap.getTimestamp().toString());
        }

        // 3. Compute current entropy
        JsonNode currentEntropy = entropyCalculationService.calculateEntropy(entityId, currentFeatures, previousSnapshotOpt.isPresent() ? previousEntropy : null);

        // 4. Analyze drift
        JsonNode driftData = driftAnalysisService.analyzeDrift(entityId, currentEntropy, previousSnapshotOpt.isPresent() ? previousEntropy : null);

        // 5. Score stability
        StabilitySnapshot snapshot = stabilityScoringService.calculateStability(entityId, currentEntropy, driftData);

        // 6. Save snapshot
        StabilitySnapshot saved = stabilitySnapshotRepository.save(snapshot);
        metricsService.incrementStabilityUpdates();
        log.info("Saved stability snapshot for entity {}: score={}, drift={}", entityId, saved.getBehavioralStabilityScore(), saved.getDriftVelocity());

        // 7. Generate and save drift explanation if needed
        if (Math.abs(saved.getDriftVelocity()) > 0.05 || saved.getInstabilityIndex() > 30) {
            DriftExplanation explanation = driftAnalysisService.generateDriftExplanation(entityId, saved.getId(), driftData);
            explainabilityService.saveDriftExplanation(explanation);
        }

        metricsService.incrementEntropyCalculations();
        metricsService.incrementDriftDetections();

        return saved;
    }

}
