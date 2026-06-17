package com.entropyatlas.entropyatlas.api.dto;

import com.entropyatlas.entropyatlas.domain.DriftExplanation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Schema(description = "Response DTO for a drift explanation")
public class DriftExplanationResponse {
    @Schema(description = "Unique identifier of the drift explanation", example = "exp-a1b2c3d4")
    private String id;

    @Schema(description = "Unique identifier of the entity", example = "user-123")
    private String entityId;

    @Schema(description = "Timestamp when the drift was detected/explained", example = "2023-10-27T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "Summary of the drift explanation", example = "Behavioral drift detected. Key contributing factors include: Timing Entropy (+24.0), Location Entropy (+19.0).")
    private String explanationSummary;

    @Schema(description = "Contribution of each behavior dimension to the drift", example = "{\"Timing Entropy\": 24.0, \"Location Entropy\": 19.0, \"Resource Affinity Drift\": 17.0}")
    private Map<String, Double> dimensionContributions;

    public static DriftExplanationResponse fromDomain(DriftExplanation explanation) {
        return DriftExplanationResponse.builder()
                .id(explanation.getId())
                .entityId(explanation.getEntityId())
                .timestamp(explanation.getTimestamp())
                .explanationSummary(explanation.getExplanationSummary())
                .dimensionContributions(explanation.getDimensionContributions())
                .build();
    }
}
