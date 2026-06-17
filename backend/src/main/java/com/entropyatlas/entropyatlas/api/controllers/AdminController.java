package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.api.dto.ReplayReportResponse;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import com.entropyatlas.entropyatlas.exceptions.ResourceNotFoundException;
import com.entropyatlas.entropyatlas.repositories.DriftExplanationRepository;
import com.entropyatlas.entropyatlas.repositories.EntityRepository;
import com.entropyatlas.entropyatlas.repositories.ReplayReportRepository;
import com.entropyatlas.entropyatlas.repositories.StabilitySnapshotRepository;
import com.entropyatlas.entropyatlas.services.MetricsService;
import com.entropyatlas.entropyatlas.services.ReplayEngineService;

import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Administrative APIs for managing Entropy Atlas, including replay and rebuild functions")
public class AdminController {

    private final ReplayEngineService replayEngineService;
    private final ReplayReportRepository replayReportRepository;
    private final EntityRepository entityRepository;
    private final DriftExplanationRepository driftExplanationRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final MetricsService metricsService;

    @PostMapping("/replay/{entityId}")
    @Operation(summary = "Initiate replay of entity history",
            description = "Triggers a replay of all historical behavior events for a given entity, reconstructing its stability evolution.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Replay initiated successfully",
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReplayReportResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<ReplayReportResponse> replayEntity(
            @Parameter(description = "ID of the entity to replay", example = "user-123") @PathVariable String entityId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            entityRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + entityId));

            metricsService.incrementReplayRequests();
            ReplayReportResponse report = ReplayReportResponse.fromDomain(replayEngineService.replayEntityHistory(entityId));
            return new ResponseEntity<>(report, HttpStatus.ACCEPTED);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/admin/replay/{entityId}");
        }
    }

    @GetMapping("/replay-reports/{entityId}")
    @Operation(summary = "Get replay reports for an entity",
            description = "Retrieves a list of all replay reports for a given entity.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved replay reports",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReplayReportResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<List<ReplayReportResponse>> getReplayReports(
            @Parameter(description = "ID of the entity to get reports for", example = "user-123") @PathVariable String entityId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            entityRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + entityId));

            List<ReplayReportResponse> reports = replayReportRepository.findByEntityIdOrderByReportGeneratedAtDesc(entityId)
                    .stream()
                    .map(ReplayReportResponse::fromDomain)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reports);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/admin/replay-reports/{entityId}");
        }
    }

    @PostMapping("/rebuild/{entityId}")
    @Operation(summary = "Initiate rebuild of entity state",
            description = "Clears the derived intelligence state (snapshots and explanations) for an entity and re-processes all historical behavior events to rebuild its behavior DNA.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Rebuild initiated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReplayReportResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<ReplayReportResponse> rebuildEntity(
            @Parameter(description = "ID of the entity to rebuild", example = "user-123") @PathVariable String entityId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            entityRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + entityId));

            ReplayReportResponse report = ReplayReportResponse.fromDomain(replayEngineService.replayEntityHistory(entityId));
            return new ResponseEntity<>(report, HttpStatus.ACCEPTED);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/admin/rebuild/{entityId}");
        }
    }

    @GetMapping("/drift-report/{entityId}")
    @Operation(summary = "Get comprehensive drift report for an entity",
            description = "Generates a detailed drift report for an entity, combining latest explanations, stability score, and historical snapshot summaries.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Drift report generated successfully"),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<java.util.Map<String, Object>> getDriftReport(
            @Parameter(description = "ID of the entity to get drift report for", example = "user-123") @PathVariable String entityId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            entityRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + entityId));

            List<StabilitySnapshot> snapshots = stabilitySnapshotRepository.findByEntityIdOrderByTimestampDesc(entityId);
            List<com.entropyatlas.entropyatlas.domain.DriftExplanation> explanations = driftExplanationRepository.findByEntityIdOrderByTimestampDesc(entityId);

            java.util.Map<String, Object> report = new java.util.LinkedHashMap<>();
            report.put("entityId", entityId);
            report.put("generatedAt", java.time.Instant.now());
            
            if (!snapshots.isEmpty()) {
                StabilitySnapshot latest = snapshots.get(0);
                report.put("currentStabilityScore", latest.getBehavioralStabilityScore());
                report.put("currentDriftVelocity", latest.getDriftVelocity());
                report.put("volatilityTrend", latest.getVolatilityTrend());
            } else {
                report.put("currentStabilityScore", 100.0);
                report.put("currentDriftVelocity", 0.0);
                report.put("volatilityTrend", "STABLE");
            }
            
            report.put("totalExplanationsCount", explanations.size());
            report.put("latestExplanations", explanations.stream().limit(5).collect(Collectors.toList()));
            report.put("historicalSnapshotsCount", snapshots.size());
            report.put("historicalStabilitySummary", snapshots.stream().limit(10).map(s -> {
                java.util.Map<String, Object> sm = new java.util.LinkedHashMap<>();
                sm.put("timestamp", s.getTimestamp());
                sm.put("stabilityScore", s.getBehavioralStabilityScore());
                sm.put("driftVelocity", s.getDriftVelocity());
                return sm;
            }).collect(Collectors.toList()));

            return ResponseEntity.ok(report);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/admin/drift-report/{entityId}");
        }
    }
}
