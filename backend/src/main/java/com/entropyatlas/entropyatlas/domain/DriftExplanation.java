package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Map;

@jakarta.persistence.Entity
@Table(name = "drift_explanations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriftExplanation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String entityId;
    private Instant timestamp;
    private String snapshotId; // Reference to StabilitySnapshot
    private String explanationSummary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "drift_explanation_contributions", joinColumns = @JoinColumn(name = "explanation_id"))
    @MapKeyColumn(name = "dimension")
    @Column(name = "contribution_score")
    private Map<String, Double> dimensionContributions; // e.g., "Timing Entropy": 24.0
}
