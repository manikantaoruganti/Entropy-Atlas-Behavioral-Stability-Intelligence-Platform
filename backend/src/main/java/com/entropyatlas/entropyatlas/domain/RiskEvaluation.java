package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "risk_evaluations", indexes = {
    @Index(name = "idx_risk_evaluations_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Double falsePositiveRate;
    private Double precisionValue;
    private Double recall;
    private BigDecimal lossPreventedAmount;
    private BigDecimal totalFraudVolumeDetected;
    private Instant timestamp;
}
