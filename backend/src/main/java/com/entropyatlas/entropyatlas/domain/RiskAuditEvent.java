package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "risk_audit_events", indexes = {
    @Index(name = "idx_risk_audit_events_risk_id", columnList = "paymentRiskId"),
    @Index(name = "idx_risk_audit_events_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String paymentRiskId;
    private String actionType;
    
    @Column(length = 2048)
    private String details;
    
    private String actorId;
    private Instant timestamp;
}
