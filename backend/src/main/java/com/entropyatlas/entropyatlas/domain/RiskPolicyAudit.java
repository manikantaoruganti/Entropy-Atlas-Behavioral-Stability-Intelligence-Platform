package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "risk_policy_audit",
        indexes = {@Index(name = "idx_risk_policy_audit_correlation", columnList = "correlationId"),
                   @Index(name = "idx_risk_policy_audit_entity", columnList = "entityId"),
                   @Index(name = "idx_risk_policy_audit_timestamp", columnList = "timestamp")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskPolicyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String correlationId;
    private String entityId;
    private String transactionId;
    private Double riskScore;
    private String riskLevel;
    private String aiVerificationResult;
    private String recommendedDecision;
    private String policyDecision;
    private String finalDecision;
    private String reason;
    private Instant timestamp;
}
