package com.entropyatlas.entropyatlas.api.dto;

import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Schema(description = "Response DTO for a behavior event")
public class BehaviorEventResponse {
    @Schema(description = "Unique identifier of the event", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String eventId;

    @Schema(description = "Unique identifier of the entity performing the behavior", example = "user-123")
    private String entityId;

    @Schema(description = "Type of the entity (e.g., USER, SERVICE, DEVICE)", example = "USER")
    private String entityType;

    @Schema(description = "Timestamp of the event", example = "2023-10-27T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "Geographical location or IP address of the event", example = "US-NYC")
    private String location;

    @Schema(description = "Resource involved in the event", example = "/api/v1/data")
    private String resource;

    @Schema(description = "Action performed", example = "LOGIN")
    private String action;

    @Schema(description = "Latency of the action in milliseconds", example = "150")
    private Long latency;

    @Schema(description = "Size of the payload involved in the action in bytes", example = "1024")
    private Long payloadSize;

    @Schema(description = "Additional metadata for the event", example = "{\"browser\": \"Chrome\", \"os\": \"macOS\"}")
    private Map<String, String> metadata;

    public static BehaviorEventResponse fromDomain(BehaviorEvent event) {
        return BehaviorEventResponse.builder()
                .eventId(event.getEventId())
                .entityId(event.getEntityId())
                .entityType(event.getEntityType().name())
                .timestamp(event.getTimestamp())
                .location(event.getLocation())
                .resource(event.getResource())
                .action(event.getAction())
                .latency(event.getLatency())
                .payloadSize(event.getPayloadSize())
                .metadata(event.getMetadata())
                .build();
    }
}
