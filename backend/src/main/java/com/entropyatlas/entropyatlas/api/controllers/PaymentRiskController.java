package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.api.dto.*;
import com.entropyatlas.entropyatlas.services.MetricsService;
import com.entropyatlas.entropyatlas.services.PaymentRiskService;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
@Tag(name = "Payment Risk Management", description = "AI Risk Manager endpoints for Payment Abuse and Fraud-Spike detection")
public class PaymentRiskController {

    private final PaymentRiskService paymentRiskService;
    private final MetricsService metricsService;

    @PostMapping("/events")
    @Operation(summary = "Ingest a new payment event for risk analysis",
            description = "Ingests payment transaction metadata and runs real-time behavioral fraud checks.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Event ingested and processed",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentRiskEventResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request validation")
            })
    public ResponseEntity<PaymentRiskEventResponse> ingestRiskEvent(@Valid @RequestBody PaymentRiskEventRequest request) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            PaymentRiskEventResponse response = paymentRiskService.ingestRiskEvent(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/events");
        }
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get list of active risk alerts",
            description = "Retrieves active alerts flagged for potential payment abuse or fraud spikes.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved risk alerts",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RiskAlertResponse.class))))
            })
    public ResponseEntity<Page<RiskAlertResponse>> getRiskAlerts(
            @Parameter(description = "Filter by operational status", example = "ACTIVE") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            if ("createdAt".equals(sortBy)) {
                sortBy = "timestamp";
            }
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<RiskAlertResponse> alerts = paymentRiskService.getRiskAlerts(status, pageable);
            return ResponseEntity.ok(alerts);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/alerts");
        }
    }

    @GetMapping("/incidents/{incidentId}")
    @Operation(summary = "Get detailed case file of an incident",
            description = "Retrieves investigation status, notes, and threat details for a specific incident.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Incident details retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskIncidentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<RiskIncidentResponse> getIncidentDetails(
            @Parameter(description = "Unique identifier of the incident", example = "inc_992f8b") @PathVariable String incidentId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            RiskIncidentResponse incident = paymentRiskService.getIncidentDetails(incidentId);
            return ResponseEntity.ok(incident);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/incidents/{incidentId}");
        }
    }

    @PostMapping("/incidents/{incidentId}/investigate")
    @Operation(summary = "Log analyst investigation notes",
            description = "Logs details, notes, and progress updates for a running manual review of a risk incident.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Investigation file updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskInvestigationResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<RiskInvestigationResponse> addInvestigationNotes(
            @Parameter(description = "Unique ID of the incident", example = "inc_992f8b") @PathVariable String incidentId,
            @Valid @RequestBody RiskInvestigationRequest request) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            RiskInvestigationResponse response = paymentRiskService.addInvestigationNotes(incidentId, request);
            return ResponseEntity.ok(response);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/incidents/{incidentId}/investigate");
        }
    }

    @GetMapping("/incidents/{incidentId}/evidence")
    @Operation(summary = "Retrieve AI explainability evidence",
            description = "Extracts feature attributions, rules, and mathematical evidence justifying a risk alert.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evidence retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskEvidenceResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<RiskEvidenceResponse> getRiskEvidence(
            @Parameter(description = "Unique ID of the incident", example = "inc_992f8b") @PathVariable String incidentId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            RiskEvidenceResponse evidence = paymentRiskService.getRiskEvidence(incidentId);
            return ResponseEntity.ok(evidence);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/incidents/{incidentId}/evidence");
        }
    }

    @PostMapping("/incidents/{incidentId}/decision")
    @Operation(summary = "Log and execute a defensive risk decision",
            description = "Applies a resolution decision to restrict or permit payments, triggering defensive operations.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Decision executed successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskDecisionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<RiskDecisionResponse> submitRiskDecision(
            @Parameter(description = "Unique ID of the incident", example = "inc_992f8b") @PathVariable String incidentId,
            @Valid @RequestBody RiskDecisionRequest request) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            RiskDecisionResponse response = paymentRiskService.submitRiskDecision(incidentId, request);
            return ResponseEntity.ok(response);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/incidents/{incidentId}/decision");
        }
    }

    @GetMapping("/incidents/{incidentId}/audit")
    @Operation(summary = "Get audit trail for an incident",
            description = "Retrieves full historical audit history log for a risk incident.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RiskAuditLogResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<List<RiskAuditLogResponse>> getAuditHistory(
            @Parameter(description = "Unique ID of the incident", example = "inc_992f8b") @PathVariable String incidentId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            List<RiskAuditLogResponse> history = paymentRiskService.getAuditHistory(incidentId);
            return ResponseEntity.ok(history);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/incidents/{incidentId}/audit");
        }
    }

    @GetMapping("/decisions")
    @Operation(summary = "Get list of all risk decisions applied",
            description = "Retrieves complete history of all applied risk decisions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved risk decisions",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RiskDecisionResponse.class))))
            })
    public ResponseEntity<List<RiskDecisionResponse>> getAllDecisions() {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            List<RiskDecisionResponse> response = paymentRiskService.getAllDecisions();
            return ResponseEntity.ok(response);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/decisions");
        }
    }

    @GetMapping("/audit")
    @Operation(summary = "Get global audit trail for all events",
            description = "Retrieves complete global history of all risk audit events.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved global audit logs",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RiskAuditLogResponse.class))))
            })
    public ResponseEntity<List<RiskAuditLogResponse>> getAllAuditLogs() {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            List<RiskAuditLogResponse> response = paymentRiskService.getAllAuditLogs();
            return ResponseEntity.ok(response);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/audit");
        }
    }

    @PostMapping("/replay/{entityId}")
    @Operation(summary = "Execute a transaction risk replay analysis",
            description = "Runs historical transaction sequences of an entity through the updated AI model to check divergence.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Replay sequence completed",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskReplayResponse.class)))
            })
    public ResponseEntity<RiskReplayResponse> executeRiskReplay(
            @Parameter(description = "ID of the entity to replay", example = "user-982") @PathVariable String entityId) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            RiskReplayResponse response = paymentRiskService.executeRiskReplay(entityId);
            return ResponseEntity.ok(response);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/replay/{entityId}");
        }
    }

    @GetMapping("/metrics")
    @Operation(summary = "Retrieve AI Risk Manager performance evaluation metrics",
            description = "Provides key metrics measuring precision, recall, and total volume saved.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskEvaluationMetricsResponse.class)))
            })
    public ResponseEntity<RiskEvaluationMetricsResponse> getRiskEvaluationMetrics() {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            RiskEvaluationMetricsResponse response = paymentRiskService.getRiskEvaluationMetrics();
            return ResponseEntity.ok(response);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/metrics");
        }
    }

    @PostMapping("/simulation")
    @Operation(summary = "Trigger a simulated payment-abuse scenario",
            description = "Generates mock threat activities to benchmark detection and defensive responses.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Simulation scenario triggered",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskScenarioSimulationResponse.class)))
            })
    public ResponseEntity<RiskScenarioSimulationResponse> simulateRiskScenario(@Valid @RequestBody RiskScenarioSimulationRequest request) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            RiskScenarioSimulationResponse response = paymentRiskService.simulateRiskScenario(request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/risk/simulation");
        }
    }
}
