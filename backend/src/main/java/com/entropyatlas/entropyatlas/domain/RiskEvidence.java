package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@jakarta.persistence.Entity
@Table(name = "risk_evidence", indexes = {
    @Index(name = "idx_risk_evidence_risk_id", columnList = "paymentRiskId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String paymentRiskId;
    private Double anomalyScore;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_evidence_shap_values", joinColumns = @JoinColumn(name = "evidence_id"))
    @MapKeyColumn(name = "feature_dimension")
    @Column(name = "attribution_score")
    private Map<String, Double> shapValues;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_evidence_triggered_rules", joinColumns = @JoinColumn(name = "evidence_id"))
    @Column(name = "rule_name")
    private List<String> triggeredRules;
}
