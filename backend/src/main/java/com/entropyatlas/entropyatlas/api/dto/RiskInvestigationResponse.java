package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO confirming an investigation update")
public class RiskInvestigationResponse {

    @Schema(description = "ID of the incident updated", example = "inc_992f8b")
    private String incidentId;

    @Schema(description = "Updated operational status of the incident", example = "RESOLVED_FALSE_POSITIVE")
    private String status;

    @Schema(description = "The updated investigation notes log", example = "Checked user logs, verified device fingerprint. Confirmed carding attempt.")
    private String notes;

    @Schema(description = "The ID of the analyst who performed the update", example = "analyst_12")
    private String updatedBy;

    @Schema(description = "Timestamp when the update was recorded", example = "2026-08-20T12:15:00Z")
    private Instant updatedAt;
}
