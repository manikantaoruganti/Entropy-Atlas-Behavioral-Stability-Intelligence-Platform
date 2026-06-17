package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Schema(description = "Request DTO for ingesting a new behavior event")
public class BehaviorEventRequest {
    @NotBlank
    @Schema(description = "Unique identifier of the entity performing the behavior", example = "user-123")
    private String entityId;

    @NotBlank
    @Schema(description = "Type of the entity (e.g., USER, SERVICE, DEVICE)", example = "USER")
    private String entityType;

    @Schema(description = "Timestamp of the event (defaults to now if not provided)", example = "2023-10-27T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "Geographical location or IP address of the event", example = "US-NYC")
    private String location;

    @Schema(description = "Resource involved in the event (e.g., API endpoint, database table)", example = "/api/v1/data")
    private String resource;

    @NotBlank
    @Schema(description = "Action performed (e.g., LOGIN, PURCHASE, READ, WRITE)", example = "LOGIN")
    private String action;

    @Schema(description = "Latency of the action in milliseconds", example = "150")
    private Long latency;

    @Schema(description = "Size of the payload involved in the action in bytes", example = "1024")
    private Long payloadSize;

    @Schema(description = "Additional metadata for the event", example = "{\"browser\": \"Chrome\", \"os\": \"macOS\"}")
    private Map<String, String> metadata;
}
