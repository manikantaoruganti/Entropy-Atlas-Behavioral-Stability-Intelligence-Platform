package com.entropyatlas.entropyatlas.api.dto;

import com.entropyatlas.entropyatlas.domain.ReplayReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Schema(description = "Response DTO for a replay report")
public class ReplayReportResponse {
    @Schema(description = "Unique identifier of the replay report", example = "rep-a1b2c3d4")
    private String id;

    @Schema(description = "Unique identifier of the entity", example = "user-123")
    private String entityId;

    @Schema(description = "Start time of the replay process", example = "2023-10-27T09:00:00Z")
    private Instant replayStartTime;

    @Schema(description = "End time of the replay process", example = "2023-10-27T09:05:00Z")
    private Instant replayEndTime;

    @Schema(description = "Timestamp when the report was generated", example = "2023-10-27T09:05:01Z")
    private Instant reportGeneratedAt;

    @Schema(description = "Status of the replay (e.g., COMPLETED, FAILED)", example = "COMPLETED")
    private String status;

    @Schema(description = "Summary of the replay comparison", example = "Successfully replayed 150 events. Rebuilt 10 stability snapshots.")
    private String comparisonSummary;

    @Schema(description = "Detailed messages about the replay process and findings", example = "[\"Starting replay for entity: user-123\", \"Processed event e1: Stability Score 85.0\"]")
    private List<String> details;

    public static ReplayReportResponse fromDomain(ReplayReport report) {
        return ReplayReportResponse.builder()
                .id(report.getId())
                .entityId(report.getEntityId())
                .replayStartTime(report.getReplayStartTime())
                .replayEndTime(report.getReplayEndTime())
                .reportGeneratedAt(report.getReportGeneratedAt())
                .status(report.getStatus())
                .comparisonSummary(report.getComparisonSummary())
                .details(report.getDetails())
                .build();
    }
}
