package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO representing a single action or state change in the risk audit trail")
public class RiskAuditLogResponse {

    @Schema(description = "Unique ID of the audit log record", example = "aud_01b8e")
    private String auditId;

    @Schema(description = "ID of the associated incident", example = "inc_992f8b")
    private String incidentId;

    @Schema(description = "The type of action logged (e.g. INGESTION, ANALYSIS, DECISION_APPLIED, INVESTIGATION_NOTE)", example = "DECISION_APPLIED")
    private String actionType;

    @Schema(description = "Detail message containing state variables or context details", example = "Decision BLOCK_CARD applied by actor analyst_12. Defensive actions initiated: BLOCK_TEMPORARY_PAYOUTS.")
    private String details;

    @Schema(description = "The identity of the actor or process that triggered the audit event", example = "analyst_12")
    private String actorId;

    @Schema(description = "Timestamp when the audit event occurred", example = "2026-08-20T12:20:00Z")
    private Instant timestamp;

    @Schema(description = "Correlation ID of the transaction")
    private String correlationId;

    @Schema(description = "Entity ID associated with the transaction")
    private String entityId;

    @Schema(description = "Decision action applied")
    private String decision;

    @Schema(description = "Risk policy applied")
    private String policyApplied;

    @Schema(description = "Calculated risk level")
    private String riskLevel;
}
