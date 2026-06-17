package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "stability_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StabilitySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String entityId;
    private Instant timestamp;
    private Double behavioralStabilityScore; // 0-100
    private Double instabilityIndex; // 0-100
    private Double entropyGrowth;
    private Double driftVelocity;
    private String volatilityTrend; // e.g., "INCREASING", "DECREASING", "STABLE"
}
