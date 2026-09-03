package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

/**
 * Response returned after a scenario start request.
 */
@Data
@Schema(description = "Response for a started deterministic payment‑risk scenario")
public class StartScenarioResponse {

    @Schema(description = "Unique identifier for the scenario execution", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    private UUID scenarioId;

    @Schema(description = "The scenario type that was started", example = "VELOCITY_SPIKE")
    private PaymentRiskScenario scenarioType;

    @Schema(description = "Total number of events generated for this run", example = "1200")
    private int eventCount;

    @Schema(description = "Expected ground‑truth label for the scenario (NORMAL or RISK)", example = "RISK")
    private String expectedGroundTruth;

    @Schema(description = "ISO‑8601 timestamp when the scenario started", example = "2026-08-20T12:34:56Z")
    private Instant startTime;
}
