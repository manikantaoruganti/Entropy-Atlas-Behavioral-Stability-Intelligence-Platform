package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.config.EvaluationProperties;
import com.entropyatlas.entropyatlas.domain.*;
import com.entropyatlas.entropyatlas.model.EvaluationResult;
import com.entropyatlas.entropyatlas.repository.EvaluationResultRepository;
import com.entropyatlas.entropyatlas.repositories.*;
import com.entropyatlas.entropyatlas.tools.SyntheticRiskDatasetGenerator;
import com.entropyatlas.entropyatlas.utils.MetricsCalculator;
import com.entropyatlas.entropyatlas.api.dto.PaymentRiskEventRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that runs the full risk‑evaluation pipeline:
 *   1. Generates a deterministic synthetic dataset.
 *   2. Splits it into TRAINING, VALIDATION, HELD‑OUT TEST.
 *   3. Sequentially processes events through the behavioral pipeline to populate entropy/drift/stability state.
 *   4. Runs the actual RiskScoringService to calculate risk scores for validation and held-out test sets.
 *   5. Determines an optimal decision threshold on the validation set.
 *   6. Evaluates the model on the held‑out test set, computing all required metrics.
 *   7. Persists the {@link EvaluationResult} and cleans up temporary evaluation data.
 */
@Service
@Slf4j
public class RiskEvaluationService {

    private final EvaluationResultRepository resultRepo;
    private final EvaluationProperties properties;
    private final RiskScoringService riskScoringService;
    private final BehaviorEventRepository behaviorEventRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final DriftExplanationRepository driftExplanationRepository;
    private final FeatureExtractionService featureExtractionService;
    private final EntropyCalculationService entropyCalculationService;
    private final DriftAnalysisService driftAnalysisService;
    private final StabilityScoringService stabilityScoringService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RiskEvaluationService(EvaluationResultRepository resultRepo,
                                 EvaluationProperties properties,
                                 RiskScoringService riskScoringService,
                                 BehaviorEventRepository behaviorEventRepository,
                                 StabilitySnapshotRepository stabilitySnapshotRepository,
                                 DriftExplanationRepository driftExplanationRepository,
                                 FeatureExtractionService featureExtractionService,
                                 EntropyCalculationService entropyCalculationService,
                                 DriftAnalysisService driftAnalysisService,
                                 StabilityScoringService stabilityScoringService,
                                 ObjectMapper objectMapper) {
        this.resultRepo = resultRepo;
        this.properties = properties;
        this.riskScoringService = riskScoringService;
        this.behaviorEventRepository = behaviorEventRepository;
        this.stabilitySnapshotRepository = stabilitySnapshotRepository;
        this.driftExplanationRepository = driftExplanationRepository;
        this.featureExtractionService = featureExtractionService;
        this.entropyCalculationService = entropyCalculationService;
        this.driftAnalysisService = driftAnalysisService;
        this.stabilityScoringService = stabilityScoringService;
        this.objectMapper = objectMapper;
    }

    /**
     * Runs the evaluation pipeline and returns the persisted result.
     */
    @Transactional
    public EvaluationResult runEvaluation() {
        log.info("Starting honest held-out risk evaluation for version: {}", properties.getDatasetVersion());

        // 1. Generate deterministic dataset
        SyntheticRiskDatasetGenerator generator = new SyntheticRiskDatasetGenerator(properties.getDatasetVersion());
        List<List<PaymentRiskEventRequest>> partitions = generator.generateDataset();
        List<PaymentRiskEventRequest> training = partitions.get(0);
        List<PaymentRiskEventRequest> validation = partitions.get(1);
        List<PaymentRiskEventRequest> heldOut = partitions.get(2);

        // Sort events chronologically to preserve temporal/behavioral ordering
        training.sort(Comparator.comparing(PaymentRiskEventRequest::getTimestamp));
        validation.sort(Comparator.comparing(PaymentRiskEventRequest::getTimestamp));
        heldOut.sort(Comparator.comparing(PaymentRiskEventRequest::getTimestamp));

        // 2. Clean up any leftover evaluation data
        cleanEvaluationData();

        // 3. Process training events to establish behavioral baseline
        Map<String, JsonNode> lastEntropyMap = new HashMap<>();
        log.info("Ingesting {} training events to build behavioral history...", training.size());
        for (PaymentRiskEventRequest ev : training) {
            processPipelineEvent(ev, lastEntropyMap);
        }

        // 4. Process validation events and compute risk scores
        log.info("Evaluating {} validation events...", validation.size());
        List<Double> validationScores = new ArrayList<>();
        List<String> validationGroundTruths = new ArrayList<>();
        for (PaymentRiskEventRequest ev : validation) {
            processPipelineEvent(ev, lastEntropyMap);
            RiskScoringService.RiskScoringResult scoringResult = riskScoringService.calculateRisk(
                    ev.getEntityId(),
                    ev.getAmount().doubleValue(),
                    ev.getLocation(),
                    ev.getDeviceId(),
                    ev.getTimestamp()
            );
            validationScores.add(scoringResult.getScore());
            validationGroundTruths.add(ev.getGroundTruth());
        }

        // 5. Process held-out test events and compute risk scores
        log.info("Evaluating {} held-out test events...", heldOut.size());
        List<Double> testScores = new ArrayList<>();
        List<String> testGroundTruths = new ArrayList<>();
        List<Long> testLatencies = new ArrayList<>();
        for (PaymentRiskEventRequest ev : heldOut) {
            processPipelineEvent(ev, lastEntropyMap);
            long startTime = System.currentTimeMillis();
            RiskScoringService.RiskScoringResult scoringResult = riskScoringService.calculateRisk(
                    ev.getEntityId(),
                    ev.getAmount().doubleValue(),
                    ev.getLocation(),
                    ev.getDeviceId(),
                    ev.getTimestamp()
            );
            long latency = System.currentTimeMillis() - startTime;
            testScores.add(scoringResult.getScore());
            testGroundTruths.add(ev.getGroundTruth());
            testLatencies.add(Math.max(1L, latency));
        }

        // 6. Tune decision threshold on the validation set only
        double bestThreshold = 0.5;
        double bestF1 = -1.0;
        String[] gtVal = validationGroundTruths.toArray(new String[0]);
        for (double thresh = 0.05; thresh <= 0.95; thresh += 0.05) {
            String[] predVal = new String[validationScores.size()];
            for (int i = 0; i < validationScores.size(); i++) {
                predVal[i] = validationScores.get(i) > thresh ? "RISK" : "NORMAL";
            }
            long[] dummyLatencies = new long[validationScores.size()];
            MetricsCalculator.Metrics m = MetricsCalculator.compute(gtVal, predVal, dummyLatencies, 0.0, 0.0);
            if (m.f1 > bestF1) {
                bestF1 = m.f1;
                bestThreshold = thresh;
            }
        }
        log.info("Optimal validation threshold selected: {} with F1: {}", bestThreshold, bestF1);

        // 7. Evaluate chosen threshold on the held-out test set
        String[] gtTest = testGroundTruths.toArray(new String[0]);
        String[] predTest = new String[testScores.size()];
        for (int i = 0; i < testScores.size(); i++) {
            predTest[i] = testScores.get(i) > bestThreshold ? "RISK" : "NORMAL";
        }
        long[] latenciesArray = new long[testLatencies.size()];
        for (int i = 0; i < testLatencies.size(); i++) {
            latenciesArray[i] = testLatencies.get(i);
        }
        MetricsCalculator.Metrics testMetrics = MetricsCalculator.compute(gtTest, predTest, latenciesArray,
                properties.getFalsePositiveCost(), properties.getFalseNegativeCost());

        // 8. Persist results
        EvaluationResult result = new EvaluationResult();
        result.setDatasetVersion(properties.getDatasetVersion());
        result.setEvaluationTimestamp(Instant.now());
        result.setModelVersion("behavioral-risk-threshold:" + bestThreshold);
        result.setTruePositives(testMetrics.tp);
        result.setTrueNegatives(testMetrics.tn);
        result.setFalsePositives(testMetrics.fp);
        result.setFalseNegatives(testMetrics.fn);
        result.setPrecision(testMetrics.precision);
        result.setRecall(testMetrics.recall);
        result.setF1Score(testMetrics.f1);
        result.setFalsePositiveRate(testMetrics.falsePositiveRate);
        result.setFalseNegativeRate(testMetrics.falseNegativeRate);
        result.setFalsePositiveCost(testMetrics.falsePositiveCost);
        result.setFalseNegativeCost(testMetrics.falseNegativeCost);
        result.setAvgDetectionLatencyMs(testMetrics.avgDetectionLatencyMs);

        EvaluationResult saved = resultRepo.save(result);
        log.info("Honest evaluation complete. F1: {}, Precision: {}, Recall: {}", saved.getF1Score(), saved.getPrecision(), saved.getRecall());

        // 9. Clean up temporary evaluation database records to prevent database bloat
        cleanEvaluationData();

        return saved;
    }

    private void processPipelineEvent(PaymentRiskEventRequest request, Map<String, JsonNode> lastEntropyMap) {
        BehaviorEvent event = mapToBehaviorEvent(request);
        try {
            JsonNode currentFeatures = featureExtractionService.extractFeatures(event);
            JsonNode previousEntropy = lastEntropyMap.get(request.getEntityId());
            JsonNode currentEntropy = entropyCalculationService.calculateEntropy(request.getEntityId(), currentFeatures, previousEntropy);
            lastEntropyMap.put(request.getEntityId(), currentEntropy);

            JsonNode driftData = driftAnalysisService.analyzeDrift(request.getEntityId(), currentEntropy, previousEntropy);
            StabilitySnapshot snapshot = stabilityScoringService.calculateStability(request.getEntityId(), currentEntropy, driftData);

            behaviorEventRepository.save(event);
            stabilitySnapshotRepository.save(snapshot);
        } catch (Exception e) {
            log.error("Error processing pipeline event for entity {}: {}", request.getEntityId(), e.getMessage());
        }
    }

    private BehaviorEvent mapToBehaviorEvent(PaymentRiskEventRequest request) {
        BehaviorEvent event = new BehaviorEvent();
        String eventId = request.getTransactionId();
        if (eventId == null || eventId.isBlank()) {
            eventId = "evt_" + UUID.randomUUID().toString().substring(0, 8);
        }
        event.setEventId(eventId);
        event.setEntityId(request.getEntityId());

        Entity.EntityType type = Entity.EntityType.USER;
        try {
            type = Entity.EntityType.valueOf(request.getEntityType() != null ? request.getEntityType() : "USER");
        } catch (IllegalArgumentException ignored) {}
        event.setEntityType(type);

        event.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now());
        event.setLocation(request.getLocation());
        event.setResource(request.getResource() != null ? request.getResource() : "/v1/payments");
        event.setAction(request.getAction() != null ? request.getAction() : "CHARGE");
        event.setLatency(100L);
        event.setPayloadSize(200L);

        Map<String, String> meta = new HashMap<>();
        if (request.getMetadata() != null) {
            meta.putAll(request.getMetadata());
        }
        meta.put("amount", request.getAmount().toString());
        meta.put("deviceId", request.getDeviceId());
        meta.put("currency", request.getCurrency());
        event.setMetadata(meta);

        return event;
    }

    private void cleanEvaluationData() {
        log.info("Cleaning up temporary evaluation behavior events, stability snapshots, and drift explanations...");
        List<BehaviorEvent> eventsToDelete = behaviorEventRepository.findAll().stream()
                .filter(e -> e.getEntityId() != null && e.getEntityId().startsWith("eval-entity-"))
                .collect(Collectors.toList());
        if (!eventsToDelete.isEmpty()) {
            behaviorEventRepository.deleteAll(eventsToDelete);
        }

        List<StabilitySnapshot> snapshotsToDelete = stabilitySnapshotRepository.findAll().stream()
                .filter(s -> s.getEntityId() != null && s.getEntityId().startsWith("eval-entity-"))
                .collect(Collectors.toList());
        if (!snapshotsToDelete.isEmpty()) {
            stabilitySnapshotRepository.deleteAll(snapshotsToDelete);
        }

        List<DriftExplanation> explanationsToDelete = driftExplanationRepository.findAll().stream()
                .filter(d -> d.getEntityId() != null && d.getEntityId().startsWith("eval-entity-"))
                .collect(Collectors.toList());
        if (!explanationsToDelete.isEmpty()) {
            driftExplanationRepository.deleteAll(explanationsToDelete);
        }
    }
}
