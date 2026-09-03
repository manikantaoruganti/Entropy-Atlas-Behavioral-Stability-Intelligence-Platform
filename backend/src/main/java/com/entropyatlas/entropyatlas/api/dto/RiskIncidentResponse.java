package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO containing detailed case files for a risk incident")
public class RiskIncidentResponse {

    @Schema(description = "Unique ID of the incident", example = "inc_992f8b")
    private String incidentId;

    @Schema(description = "ID of the entity flagged", example = "user-982")
    private String entityId;

    @Schema(description = "Current computed risk score", example = "0.94")
    private Double alertScore;

    @Schema(description = "The type of payment abuse detected", example = "CARDING_ATTACK")
    private String scenarioType;

    @Schema(description = "Severity category", example = "CRITICAL")
    private String severity;

    @Schema(description = "Current operational status of the incident", example = "UNDER_REVIEW")
    private String status;

    @Schema(description = "Historical investigation notes logged by analysts", example = "Flagged due to sudden IP changes and velocity spikes.")
    private String investigationNotes;

    @Schema(description = "The ID of the last investigator who updated the incident", example = "analyst_12")
    private String lastUpdatedBy;

    @Schema(description = "Timestamp when the incident was last modified", example = "2026-08-20T12:10:00Z")
    private Instant updatedAt;
}
