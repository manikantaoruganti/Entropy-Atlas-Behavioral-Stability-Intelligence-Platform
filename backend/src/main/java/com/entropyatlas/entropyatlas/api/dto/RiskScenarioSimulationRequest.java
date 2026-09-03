package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request payload to initiate a simulated payment-risk threat scenario")
public class RiskScenarioSimulationRequest {

    @NotBlank(message = "Entity ID to simulate against is required")
    @Schema(description = "The target entity ID for simulation", example = "user-982")
    private String entityId;

    @NotBlank(message = "Simulation threat scenario type is required")
    @Schema(description = "The type of threat scenario to simulate (e.g. FRAUD_SPIKE, CARDING_ATTACK, VELOCITY_SPIKE)", example = "FRAUD_SPIKE")
    private String scenarioType;

    @Min(value = 1, message = "Event rate must be at least 1 event per second")
    @Schema(description = "Simulation event ingestion frequency (events per second)", example = "5")
    private int eventRate;

    @Min(value = 1, message = "Simulation duration must be at least 1 second")
    @Schema(description = "Duration of simulated attack in seconds", example = "30")
    private int durationSeconds;
}
