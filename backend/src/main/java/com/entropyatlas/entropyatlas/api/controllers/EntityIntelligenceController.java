package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.repositories.*;
import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import com.entropyatlas.entropyatlas.domain.Entity;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import com.entropyatlas.entropyatlas.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
public class EntityIntelligenceController {

    private final EntityRepository entityRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final DriftExplanationRepository driftExplanationRepository;
    private final BehaviorEventRepository behaviorEventRepository;

    @GetMapping("/top-stable")
    public ResponseEntity<List<Map<String, Object>>> getTopStable() {
        return ResponseEntity.ok(getEntitiesRanked(true));
    }

    @GetMapping("/top-unstable")
    public ResponseEntity<List<Map<String, Object>>> getTopUnstable() {
        return ResponseEntity.ok(getEntitiesRanked(false));
    }

    @GetMapping("/high-drift")
    public ResponseEntity<List<Map<String, Object>>> getHighDrift() {
        List<Map<String, Object>> ranked = getEntitiesRanked(false);
        // Include entities with ANY drift velocity for richer data
        List<Map<String, Object>> highDrift = ranked.stream()
                .filter(m -> Math.abs((Double) m.get("driftVelocity")) > 0.05)
                .collect(Collectors.toList());
        return ResponseEntity.ok(highDrift);
    }

    @GetMapping("/{id}/behavior-dna")
    public ResponseEntity<Map<String, Object>> getBehaviorDna(@PathVariable String id) {
        Entity entity = entityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found: " + id));

        List<BehaviorEvent> events = behaviorEventRepository.findByEntityIdOrderByTimestampAsc(id);
        List<StabilitySnapshot> snapshots = stabilitySnapshotRepository.findByEntityIdOrderByTimestampDesc(id);

        Map<String, Object> dna = new LinkedHashMap<>();
        dna.put("entityId", entity.getId());
        dna.put("entityType", entity.getEntityType());
        dna.put("totalEvents", events.size());
        dna.put("complexityFactor", computeComplexityFactor(events));
        dna.put("timestamp", System.currentTimeMillis());

        // Top locations
        Map<String, Long> locationFreq = events.stream()
                .filter(e -> e.getLocation() != null)
                .collect(Collectors.groupingBy(BehaviorEvent::getLocation, Collectors.counting()));
        dna.put("topLocations", getTopN(locationFreq, 5));

        // Top resources  
        Map<String, Long> resourceFreq = events.stream()
                .filter(e -> e.getResource() != null)
                .collect(Collectors.groupingBy(BehaviorEvent::getResource, Collectors.counting()));
        dna.put("topResources", getTopN(resourceFreq, 5));

        // Top actions
        Map<String, Long> actionFreq = events.stream()
                .filter(e -> e.getAction() != null)
                .collect(Collectors.groupingBy(BehaviorEvent::getAction, Collectors.counting()));
        dna.put("topActions", getTopN(actionFreq, 5));

        // Latency stats
        DoubleSummaryStatistics latencyStats = events.stream()
                .filter(e -> e.getLatency() != null)
                .mapToDouble(BehaviorEvent::getLatency)
                .summaryStatistics();
        dna.put("averageLatency", latencyStats.getCount() > 0 ? latencyStats.getAverage() : 0);
        dna.put("maxLatency", latencyStats.getCount() > 0 ? latencyStats.getMax() : 0);
        dna.put("minLatency", latencyStats.getCount() > 0 ? latencyStats.getMin() : 0);
        dna.put("latencyVariance", computeVariance(events.stream()
                .filter(e -> e.getLatency() != null)
                .mapToDouble(BehaviorEvent::getLatency).toArray()));

        // Payload stats
        DoubleSummaryStatistics payloadStats = events.stream()
                .filter(e -> e.getPayloadSize() != null)
                .mapToDouble(BehaviorEvent::getPayloadSize)
                .summaryStatistics();
        dna.put("averagePayloadSize", payloadStats.getCount() > 0 ? payloadStats.getAverage() : 0);
        dna.put("payloadVariance", computeVariance(events.stream()
                .filter(e -> e.getPayloadSize() != null)
                .mapToDouble(BehaviorEvent::getPayloadSize).toArray()));

        // Behavior signature (unique combinations)
        long uniqueActionResourcePairs = events.stream()
                .map(e -> (e.getAction() != null ? e.getAction() : "") + ":" + (e.getResource() != null ? e.getResource() : ""))
                .distinct().count();
        dna.put("behaviorSignature", uniqueActionResourcePairs);

        // Entropy signature from latest snapshot
        if (!snapshots.isEmpty()) {
            StabilitySnapshot latest = snapshots.get(0);
            dna.put("currentStability", latest.getBehavioralStabilityScore());
            dna.put("currentDrift", latest.getDriftVelocity());
            dna.put("entropySignature", latest.getEntropyGrowth());
            dna.put("volatilityState", latest.getVolatilityTrend());
        }

        // Predictability score
        dna.put("predictabilityScore", snapshots.isEmpty() ? 0 :
                snapshots.stream().mapToDouble(StabilitySnapshot::getBehavioralStabilityScore).average().orElse(0));

        return ResponseEntity.ok(dna);
    }

    @GetMapping("/{id}/entropy-evolution")
    public ResponseEntity<List<StabilitySnapshot>> getEntropyEvolution(@PathVariable String id) {
        return ResponseEntity.ok(stabilitySnapshotRepository.findByEntityIdOrderByTimestampDesc(id));
    }

    @GetMapping("/{id}/volatility")
    public ResponseEntity<Map<String, Object>> getVolatility(@PathVariable String id) {
        List<StabilitySnapshot> snapshots = stabilitySnapshotRepository.findByEntityIdOrderByTimestampDesc(id);
        String currentVolatility = snapshots.isEmpty() ? "UNKNOWN" : snapshots.get(0).getVolatilityTrend();

        Map<String, Object> vol = new LinkedHashMap<>();
        vol.put("entityId", id);
        vol.put("currentVolatility", currentVolatility);
        vol.put("recordsAnalyzed", snapshots.size());

        if (!snapshots.isEmpty()) {
            vol.put("volatilityScore", snapshots.stream()
                    .mapToDouble(s -> Math.abs(s.getDriftVelocity())).average().orElse(0));
            vol.put("stabilityTrend", snapshots.size() >= 2 ?
                    (snapshots.get(0).getBehavioralStabilityScore() > snapshots.get(1).getBehavioralStabilityScore() ? "IMPROVING" : "DEGRADING")
                    : "STABLE");

            // Volatility classification
            double avgDrift = snapshots.stream().mapToDouble(s -> Math.abs(s.getDriftVelocity())).average().orElse(0);
            if (avgDrift < 0.3) vol.put("volatilityClassification", "LOW");
            else if (avgDrift < 1.0) vol.put("volatilityClassification", "MODERATE");
            else vol.put("volatilityClassification", "HIGH");
        }

        return ResponseEntity.ok(vol);
    }

    private List<Map<String, Object>> getEntitiesRanked(boolean stableFirst) {
        List<Entity> entities = entityRepository.findAll();
        List<Map<String, Object>> ranked = new ArrayList<>();

        for (Entity e : entities) {
            Optional<StabilitySnapshot> latestOpt = stabilitySnapshotRepository.findFirstByEntityIdOrderByTimestampDesc(e.getId());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", e.getId());
            data.put("entityType", e.getEntityType());

            if (latestOpt.isPresent()) {
                StabilitySnapshot latest = latestOpt.get();
                data.put("stabilityScore", latest.getBehavioralStabilityScore());
                data.put("driftVelocity", latest.getDriftVelocity());
                data.put("entropyGrowth", latest.getEntropyGrowth());
                data.put("volatilityTrend", latest.getVolatilityTrend());
            } else {
                // Entity exists but no snapshots yet - still show it
                data.put("stabilityScore", 50.0);
                data.put("driftVelocity", 0.0);
                data.put("entropyGrowth", 0.0);
                data.put("volatilityTrend", "UNKNOWN");
            }
            ranked.add(data);
        }

        ranked.sort((a, b) -> {
            Double scoreA = (Double) a.get("stabilityScore");
            Double scoreB = (Double) b.get("stabilityScore");
            return stableFirst ? scoreB.compareTo(scoreA) : scoreA.compareTo(scoreB);
        });

        return ranked;
    }

    private List<Map<String, Object>> getTopN(Map<String, Long> freqMap, int n) {
        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private double computeComplexityFactor(List<BehaviorEvent> events) {
        if (events.isEmpty()) return 0.0;
        long uniqueActions = events.stream().map(BehaviorEvent::getAction).filter(Objects::nonNull).distinct().count();
        long uniqueResources = events.stream().map(BehaviorEvent::getResource).filter(Objects::nonNull).distinct().count();
        long uniqueLocations = events.stream().map(BehaviorEvent::getLocation).filter(Objects::nonNull).distinct().count();
        return (uniqueActions * 0.4 + uniqueResources * 0.3 + uniqueLocations * 0.3) * Math.log(events.size() + 1);
    }

    private double computeVariance(double[] values) {
        if (values.length == 0) return 0.0;
        double mean = Arrays.stream(values).average().orElse(0);
        return Arrays.stream(values).map(v -> (v - mean) * (v - mean)).average().orElse(0);
    }
}
