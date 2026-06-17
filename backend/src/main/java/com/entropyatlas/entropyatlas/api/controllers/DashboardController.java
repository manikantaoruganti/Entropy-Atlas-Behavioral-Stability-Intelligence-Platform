package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.repositories.*;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EntityRepository entityRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final DriftExplanationRepository driftExplanationRepository;
    private final BehaviorEventRepository behaviorEventRepository;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        long totalEntities = entityRepository.count();
        long totalEvents = behaviorEventRepository.count();
        long totalDrifts = driftExplanationRepository.count();

        List<StabilitySnapshot> snapshots = stabilitySnapshotRepository.findAll();
        double avgStability = 0.0;
        double avgEntropy = 0.0;
        long unstableCount = 0;

        if (!snapshots.isEmpty()) {
            double sumStability = 0;
            double sumEntropy = 0;
            for (StabilitySnapshot snap : snapshots) {
                sumStability += snap.getBehavioralStabilityScore();
                sumEntropy += snap.getEntropyGrowth();
                if (snap.getBehavioralStabilityScore() < 70) {
                    unstableCount++;
                }
            }
            avgStability = sumStability / snapshots.size();
            avgEntropy = sumEntropy / snapshots.size();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalEntities", totalEntities);
        response.put("totalEvents", totalEvents);
        response.put("totalDrifts", totalDrifts);
        response.put("averageStability", avgStability);
        response.put("averageEntropy", avgEntropy);
        response.put("unstableEntities", unstableCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "HEALTHY");
        health.put("checksPassed", true);
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/activity")
    public ResponseEntity<List<?>> getActivity() {
        return ResponseEntity.ok(behaviorEventRepository.findTop50ByOrderByTimestampDesc());
    }
}
