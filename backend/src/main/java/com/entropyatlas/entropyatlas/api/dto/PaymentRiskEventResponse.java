package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload after ingesting a payment event for risk analysis")
public class PaymentRiskEventResponse {

    @Schema(description = "Unique ID generated for the ingested event", example = "evt_9b1a03f")
    private String eventId;

    @Schema(description = "The ID of the entity associated with the event", example = "user-982")
    private String entityId;

    @Schema(description = "The correlation ID for tracing the ingestion flow", example = "d927a-883a-11ee")
    private String correlationId;

    @Schema(description = "Timestamp when the event was received by the system", example = "2026-08-20T12:00:01Z")
    private Instant receivedAt;

    @Schema(description = "Status of the ingestion and processing", example = "PROCESSED")
    private String status;
}
