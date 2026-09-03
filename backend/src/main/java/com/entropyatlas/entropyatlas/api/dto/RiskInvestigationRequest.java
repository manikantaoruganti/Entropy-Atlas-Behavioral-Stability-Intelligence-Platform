package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request payload to update risk investigation notes and statuses")
public class RiskInvestigationRequest {

    @NotBlank(message = "Notes are required for investigation updates")
    @Schema(description = "Detailed investigation logs or comments added by the analyst", example = "Checked user logs, verified device fingerprint. Confirmed carding attempt.")
    private String notes;

    @NotBlank(message = "Investigator ID is required")
    @Schema(description = "The ID of the analyst updating the case file", example = "analyst_12")
    private String investigatorId;

    @NotBlank(message = "Target operational status is required")
    @Schema(description = "New workflow status for the incident (e.g. UNDER_REVIEW, RESOLVED_FALSE_POSITIVE, ESCALATED)", example = "RESOLVED_FALSE_POSITIVE")
    private String status;
}
