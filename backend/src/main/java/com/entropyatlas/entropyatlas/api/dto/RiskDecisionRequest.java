package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request payload to apply a risk resolution/mitigation decision")
public class RiskDecisionRequest {

    @NotBlank(message = "Decision action is required")
    @Schema(description = "Decision action chosen by the analyst or automated system (e.g. BLOCK_CARD, RESTRICT_USER, MARK_SAFE)", example = "BLOCK_CARD")
    private String decision;

    @NotBlank(message = "Reason description is required")
    @Schema(description = "Business reason justifying the decision action", example = "Confirmed fraudulent transactions from unrecognized IP locations.")
    private String reason;

    @NotBlank(message = "Actor/Analyst ID is required")
    @Schema(description = "ID of the entity/analyst making this decision", example = "analyst_12")
    private String actorId;
}
