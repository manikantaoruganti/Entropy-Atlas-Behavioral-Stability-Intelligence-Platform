package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "risk_decisions", indexes = {
    @Index(name = "idx_risk_decisions_risk_id", columnList = "paymentRiskId"),
    @Index(name = "idx_risk_decisions_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String paymentRiskId;
    private String decision;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_decision_defensive_actions", joinColumns = @JoinColumn(name = "decision_id"))
    @Column(name = "defensive_action")
    private List<String> defensiveActions;

    private String actorId;
    private String reason;
    private Instant timestamp;
}
