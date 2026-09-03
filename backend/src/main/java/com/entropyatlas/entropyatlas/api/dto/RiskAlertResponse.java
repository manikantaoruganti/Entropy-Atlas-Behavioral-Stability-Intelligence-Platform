package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO representing a risk alert for payment abuse or fraud-spike detection")
public class RiskAlertResponse {

    @Schema(description = "Unique identifier of the risk alert", example = "alt_882f71")
    private String alertId;

    @Schema(description = "The ID of the entity flagged by the alert", example = "user-982")
    private String entityId;

    @Schema(description = "The payment abuse threat scenario type", example = "FRAUD_SPIKE")
    private String scenarioType;

    @Schema(description = "Alert severity category", example = "HIGH")
    private String severity;

    @Schema(description = "Computed risk score between 0.0 (no risk) and 1.0 (maximum risk)", example = "0.94")
    private Double score;

    @Schema(description = "The active status of the alert", example = "ACTIVE")
    private String status;

    @Schema(description = "Timestamp when the alert was triggered", example = "2026-08-20T12:05:00Z")
    private Instant createdAt;

    // Added fields for frontend compatibility
    private Double riskScore;
    private String riskLevel;
    private Instant timestamp;
    private java.math.BigDecimal amount;
    private String currency;
    private String deviceId;
    private String location;
    private String resource;
    private String action;
    private Double entropyContribution;
    private Double driftContribution;
    private String baselineBehavior;
    private String currentBehavior;
    private Double aiConfidence;
    private String aiExplanation;
    private String recommendedDecision;
    private String policyDecision;
    private String correlationId;
}
