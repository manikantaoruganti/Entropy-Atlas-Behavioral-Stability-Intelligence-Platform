package com.entropyatlas.entropyatlas.api.dto;

import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Response DTO for an entity's stability snapshot")
public class StabilitySnapshotResponse {
    @Schema(description = "Unique identifier of the stability snapshot", example = "snap-a1b2c3d4")
    private String id;

    @Schema(description = "Unique identifier of the entity", example = "user-123")
    private String entityId;

    @Schema(description = "Timestamp of the snapshot", example = "2023-10-27T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "Behavioral Stability Score (0-100, higher is more stable)", example = "85.5")
    private Double behavioralStabilityScore;

    @Schema(description = "Instability Index (0-100, higher is more unstable)", example = "14.5")
    private Double instabilityIndex;

    @Schema(description = "Rate of entropy growth", example = "0.12")
    private Double entropyGrowth;

    @Schema(description = "Velocity of behavioral drift", example = "5.3")
    private Double driftVelocity;

    @Schema(description = "Trend of volatility (e.g., INCREASING, DECREASING, STABLE)", example = "STABLE")
    private String volatilityTrend;

    public static StabilitySnapshotResponse fromDomain(StabilitySnapshot snapshot) {
        return StabilitySnapshotResponse.builder()
                .id(snapshot.getId())
                .entityId(snapshot.getEntityId())
                .timestamp(snapshot.getTimestamp())
                .behavioralStabilityScore(snapshot.getBehavioralStabilityScore())
                .instabilityIndex(snapshot.getInstabilityIndex())
                .entropyGrowth(snapshot.getEntropyGrowth())
                .driftVelocity(snapshot.getDriftVelocity())
                .volatilityTrend(snapshot.getVolatilityTrend())
                .build();
    }
}
