package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO containing explainability evidence and feature attribution for the risk alert")
public class RiskEvidenceResponse {

    @Schema(description = "ID of the associated incident", example = "inc_992f8b")
    private String incidentId;

    @Schema(description = "ID of the flagged entity", example = "user-982")
    private String entityId;

    @Schema(description = "SHAP feature attributions showing dimensional contribution to the risk score", example = "{\"amount\": 0.45, \"location\": 0.25, \"velocity\": 0.20}")
    private Map<String, Double> shapValues;

    @Schema(description = "List of risk rules triggered by the transaction", example = "[\"IP_GEOGRAPHIC_VELOCITY_SPIKE\", \"HIGH_AMOUNT_UNUSUAL_HOUR\"]")
    private List<String> triggeredRules;

    @Schema(description = "Raw anomaly divergence score", example = "8.85")
    private Double anomalyScore;
}
