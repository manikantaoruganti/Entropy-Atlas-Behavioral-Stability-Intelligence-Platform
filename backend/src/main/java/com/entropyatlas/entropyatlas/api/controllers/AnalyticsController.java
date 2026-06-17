package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.repositories.*;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final DriftExplanationRepository driftExplanationRepository;

    @GetMapping("/entropy")
    public ResponseEntity<Map<String, Object>> getEntropyAnalytics() {
        List<StabilitySnapshot> snapshots = stabilitySnapshotRepository.findAll();
        double avgEntropy = snapshots.stream()
                .mapToDouble(StabilitySnapshot::getEntropyGrowth)
                .average()
                .orElse(0.0);

        Map<String, Object> response = new HashMap<>();
        response.put("averageEntropyGrowth", avgEntropy);
        response.put("totalCalculations", snapshots.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/drift")
    public ResponseEntity<List<?>> getDriftAnalytics() {
        return ResponseEntity.ok(driftExplanationRepository.findAll());
    }

    @GetMapping("/volatility")
    public ResponseEntity<Map<String, Object>> getVolatilityAnalytics() {
        List<StabilitySnapshot> snapshots = stabilitySnapshotRepository.findAll();
        Map<String, Long> trendCounts = snapshots.stream()
                .collect(Collectors.groupingBy(StabilitySnapshot::getVolatilityTrend, Collectors.counting()));

        Map<String, Object> response = new HashMap<>();
        response.put("volatilityTrends", trendCounts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trends")
    public ResponseEntity<List<StabilitySnapshot>> getTrends() {
        return ResponseEntity.ok(stabilitySnapshotRepository.findAll());
    }

    @GetMapping("/distribution")
    public ResponseEntity<Map<String, Object>> getDistribution() {
        List<StabilitySnapshot> snapshots = stabilitySnapshotRepository.findAll();
        int[] ranges = new int[5]; // 0-20, 21-40, 41-60, 61-80, 81-100
        for (StabilitySnapshot snap : snapshots) {
            double score = snap.getBehavioralStabilityScore();
            if (score <= 20) ranges[0]++;
            else if (score <= 40) ranges[1]++;
            else if (score <= 60) ranges[2]++;
            else if (score <= 80) ranges[3]++;
            else ranges[4]++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("distribution", ranges);
        return ResponseEntity.ok(response);
    }
}
