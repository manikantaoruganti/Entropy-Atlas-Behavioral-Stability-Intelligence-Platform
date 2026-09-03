package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Schema(description = "Request payload for ingesting a payment event for risk analysis")
public class PaymentRiskEventRequest {

    @NotBlank(message = "Entity ID is required")
    @Schema(description = "The ID of the user or entity performing the transaction", example = "user-982")
    private String entityId;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be zero or positive")
    @Schema(description = "Transaction amount in decimal format", example = "499.50")
    private BigDecimal amount;

    @NotBlank(message = "Currency code is required")
    @Schema(description = "ISO 3-letter currency code", example = "INR")
    private String currency;

    @NotBlank(message = "Action is required")
    @Schema(description = "Payment action performed (e.g. CHARGE, REFUND)", example = "CHARGE")
    private String action;

    @NotBlank(message = "Device ID is required")
    @Schema(description = "Unique device fingerprint of the payment initiator", example = "dev_fp_882a1b")
    private String deviceId;

    @Schema(description = "IP address or geographic location of the event", example = "103.21.141.22")
    private String location;

    @Schema(description = "Target API endpoint or payment gateway resource", example = "/v1/payments")
    private String resource;

    @Schema(description = "Transaction execution timestamp (defaults to current server time if omitted)", example = "2026-08-20T12:00:00Z")
    private Instant timestamp;

    @Schema(description = "Additional key-value metadata to feed correlation engines", example = "{\"email\": \"user@example.com\", \"cardNetwork\": \"Visa\"}")
    private Map<String, String> metadata;
    @Schema(description = "Ground truth label for the event (NORMAL or RISK)", example = "RISK")
    private String groundTruth;

    @Schema(description = "The type of entity performing the transaction", example = "USER")
    private String entityType;

    @Schema(description = "The correlation ID for tracing the ingestion flow", example = "d927a-883a-11ee")
    private String correlationId;

    @Schema(description = "Unique ID for the transaction", example = "txn_88a91b")
    private String transactionId;
}
