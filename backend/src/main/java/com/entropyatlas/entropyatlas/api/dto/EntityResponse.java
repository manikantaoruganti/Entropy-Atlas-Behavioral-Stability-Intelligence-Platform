package com.entropyatlas.entropyatlas.api.dto;

import com.entropyatlas.entropyatlas.domain.Entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Response DTO for an entity")
public class EntityResponse {
    @Schema(description = "Unique identifier of the entity", example = "user-123")
    private String id;

    @Schema(description = "Type of the entity (e.g., USER, SERVICE, DEVICE)", example = "USER")
    private String entityType;

    @Schema(description = "Timestamp when the entity was first created/observed", example = "2023-01-01T00:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the entity was last updated/observed", example = "2023-10-27T10:00:00Z")
    private Instant updatedAt;

    public static EntityResponse fromDomain(Entity entity) {
        return EntityResponse.builder()
                .id(entity.getId())
                .entityType(entity.getEntityType().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
