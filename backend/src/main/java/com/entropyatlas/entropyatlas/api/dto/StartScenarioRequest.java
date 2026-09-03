package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for starting a deterministic payment‑risk scenario.
 */
@Data
@Schema(description = "Start a deterministic payment‑risk scenario for local evaluation")
public class StartScenarioRequest {

    @NotNull(message = "Scenario type must be provided")
    @Schema(description = "One of the predefined payment‑risk scenarios", example = "VELOCITY_SPIKE")
    private PaymentRiskScenario scenario;

    @Schema(description = "Optional override for total number of events to generate (default varies per scenario)", example = "1000")
    private Integer eventCount;
}
