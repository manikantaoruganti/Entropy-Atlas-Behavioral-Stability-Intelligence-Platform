package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "payment_risks", indexes = {
    @Index(name = "idx_payment_risks_entity", columnList = "entityId"),
    @Index(name = "idx_payment_risks_timestamp", columnList = "timestamp"),
    @Index(name = "idx_payment_risks_transaction", columnList = "transactionId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String transactionId;
    private String entityId;
    private String riskType; // e.g. PAYMENT_ABUSE / FRAUD_SPIKE
    private Double riskScore;
    private String riskLevel;
    private Double confidence;
    private String aiVerificationResult;
    private String recommendedDecision;
    private String actualPolicyDecision;
    private String aiExplanation;
    private String status;
    private String correlationId;
    private Instant timestamp;
}
