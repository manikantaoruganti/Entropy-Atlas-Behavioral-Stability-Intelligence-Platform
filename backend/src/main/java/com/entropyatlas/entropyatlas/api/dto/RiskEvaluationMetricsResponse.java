package com.entropyatlas.entropyatlas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO containing AI Risk Manager evaluation performance metrics")
public class RiskEvaluationMetricsResponse {

    @Schema(description = "False positive rate of risk detection (0.0 to 1.0)", example = "0.012")
    private Double falsePositiveRate;

    @Schema(description = "Precision of detection decisions (0.0 to 1.0)", example = "0.95")
    private Double precision;

    @Schema(description = "Recall/Detection rate of fraud events (0.0 to 1.0)", example = "0.91")
    private Double recall;

    @Schema(description = "Total volume of financial loss prevented by defensive responses", example = "1250000.00")
    private BigDecimal lossPreventedAmount;

    @Schema(description = "Total amount of fraud volume detected", example = "1380000.00")
    private BigDecimal totalFraudVolumeDetected;
}
