package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "risk_scenarios", indexes = {
    @Index(name = "idx_risk_scenarios_entity", columnList = "entityId"),
    @Index(name = "idx_risk_scenarios_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String entityId;
    private String scenarioType;
    private int eventRate;
    private int durationSeconds;
    private int expectedEvents;
    private String status;
    private Instant timestamp;
}
