package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO containing the applied decision and bounded defensive actions triggered")
public class RiskDecisionResponse {

    @Schema(description = "Unique ID of the decision log", example = "dec_103a11")
    private String decisionId;

    @Schema(description = "ID of the incident the decision resolves", example = "inc_992f8b")
    private String incidentId;

    @Schema(description = "The applied decision action", example = "BLOCK_CARD")
    private String decision;

    @Schema(description = "List of bounded defensive infrastructure actions executed automatically", example = "[\"BLOCK_TEMPORARY_PAYOUTS\", \"REVOKE_API_SESSION_KEY\"]")
    private List<String> defensiveActionsTriggered;

    @Schema(description = "Timestamp when the decision was applied", example = "2026-08-20T12:20:00Z")
    private Instant timestamp;

    @Schema(description = "Correlation ID of the transaction")
    private String correlationId;

    @Schema(description = "Entity ID associated with the transaction")
    private String entityId;

    @Schema(description = "Risk policy applied")
    private String policyApplied;

    @Schema(description = "Calculated risk level")
    private String riskLevel;
}
