package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import com.entropyatlas.entropyatlas.domain.ReplayReport;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import com.entropyatlas.entropyatlas.repositories.BehaviorEventRepository;
import com.entropyatlas.entropyatlas.repositories.ReplayReportRepository;
import com.entropyatlas.entropyatlas.repositories.StabilitySnapshotRepository;
import com.entropyatlas.entropyatlas.repositories.DriftExplanationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplayEngineService {

    private final BehaviorEventRepository behaviorEventRepository;
    private final ReplayReportRepository replayReportRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final DriftExplanationRepository driftExplanationRepository;
    private final BehavioralIntelligencePipeline intelligencePipeline;
    private final MetricsService metricsService;

    @Transactional
    public ReplayReport replayEntityHistory(String entityId) {
        Instant replayStartTime = Instant.now();
        List<String> details = new ArrayList<>();
        details.add("Starting replay for entity: " + entityId);
        metricsService.incrementReplayRequests();

        List<BehaviorEvent> events = behaviorEventRepository.findByEntityIdOrderByTimestampAsc(entityId);
        if (events.isEmpty()) {
            details.add("No behavior events found for entity: " + entityId);
            return createReplayReport(entityId, replayStartTime, Instant.now(), "COMPLETED", "No events to replay.", details);
        }

        details.add(String.format("Found %d historical events to replay.", events.size()));

        // Clear existing snapshots and drift explanations for clean rebuild
        List<StabilitySnapshot> existingSnapshots = stabilitySnapshotRepository.findByEntityIdOrderByTimestampDesc(entityId);
        int previousSnapshotCount = existingSnapshots.size();
        List<com.entropyatlas.entropyatlas.domain.DriftExplanation> existingExplanations = driftExplanationRepository.findByEntityIdOrderByTimestampDesc(entityId);
        driftExplanationRepository.deleteAll(existingExplanations);
        stabilitySnapshotRepository.deleteAll(existingSnapshots);
        details.add(String.format("Clearing %d existing stability snapshots and %d drift explanations for clean rebuild.", previousSnapshotCount, existingExplanations.size()));

        List<StabilitySnapshot> rebuiltSnapshots = new ArrayList<>();

        // Re-process each event through the intelligence pipeline
        for (int i = 0; i < events.size(); i++) {
            BehaviorEvent event = events.get(i);
            try {
                StabilitySnapshot snapshot = intelligencePipeline.processEvent(event);
                if (snapshot != null) {
                    rebuiltSnapshots.add(snapshot);
                    if (i % 10 == 0 || i == events.size() - 1) {
                        details.add(String.format("[%d/%d] Event %s: Stability=%.2f, Drift=%.4f",
                                i + 1, events.size(), event.getEventId(),
                                snapshot.getBehavioralStabilityScore(), snapshot.getDriftVelocity()));
                    }
                }
            } catch (Exception e) {
                log.error("Error during replay for event {}: {}", event.getEventId(), e.getMessage());
                details.add(String.format("Error processing event %s: %s", event.getEventId(), e.getMessage()));
            }
        }

        // Generate comparison summary
        double avgStability = rebuiltSnapshots.stream()
                .mapToDouble(StabilitySnapshot::getBehavioralStabilityScore)
                .average().orElse(0);
        double avgDrift = rebuiltSnapshots.stream()
                .mapToDouble(s -> Math.abs(s.getDriftVelocity()))
                .average().orElse(0);

        String comparisonSummary = String.format(
                "Successfully replayed %d events. Generated %d stability snapshots. " +
                "Average stability: %.2f%%. Average drift velocity: %.4f. " +
                "Previous snapshot count: %d.",
                events.size(), rebuiltSnapshots.size(), avgStability, avgDrift, previousSnapshotCount
        );
        details.add(comparisonSummary);
        details.add("Replay completed successfully at " + Instant.now());

        return createReplayReport(entityId, replayStartTime, Instant.now(), "COMPLETED", comparisonSummary, details);
    }

    private ReplayReport createReplayReport(String entityId, Instant startTime, Instant endTime, String status, String summary, List<String> details) {
        ReplayReport report = new ReplayReport(
                UUID.randomUUID().toString(),
                entityId,
                startTime,
                endTime,
                Instant.now(),
                status,
                summary,
                details
        );
        return replayReportRepository.save(report);
    }
}
