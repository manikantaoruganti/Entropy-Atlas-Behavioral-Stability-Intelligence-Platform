package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO containing detail and status of the initiated simulation task")
public class RiskScenarioSimulationResponse {

    @Schema(description = "Unique ID generated for the simulation task", example = "sim_9103f")
    private String simulationId;

    @Schema(description = "The target entity ID for simulation", example = "user-982")
    private String entityId;

    @Schema(description = "The type of simulated scenario", example = "FRAUD_SPIKE")
    private String scenarioType;

    @Schema(description = "Ingestion rate used for the simulation (events per second)", example = "5")
    private int eventRate;

    @Schema(description = "Total number of simulated events expected to be generated", example = "150")
    private int expectedEvents;

    @Schema(description = "Current status of the simulation task", example = "RUNNING")
    private String status;
}
