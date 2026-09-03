package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO representing the execution summary of a forensic risk replay")
public class RiskReplayResponse {

    @Schema(description = "ID of the entity replayed", example = "user-982")
    private String entityId;

    @Schema(description = "The number of historical events re-evaluated by the engine", example = "45")
    private int eventsProcessedCount;

    @Schema(description = "Indicates whether the replayed scores diverged from original stored scores", example = "true")
    private Boolean divergenceDetected;

    @Schema(description = "The average risk score recorded in the original run", example = "0.74")
    private Double previousAvgScore;

    @Schema(description = "The average risk score calculated during this replay session", example = "0.88")
    private Double newAvgScore;

    @Schema(description = "Timestamp when the replay session completed", example = "2026-08-20T12:25:00Z")
    private Instant timestamp;
}
