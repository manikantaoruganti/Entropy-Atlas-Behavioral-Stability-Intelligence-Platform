package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "replay_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String entityId;
    private Instant replayStartTime;
    private Instant replayEndTime;
    private Instant reportGeneratedAt;
    private String status; // e.g., "COMPLETED", "FAILED"
    private String comparisonSummary; // e.g., "Rebuilt state matches stored state for 95% of metrics."
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(
    name = "replay_report_details",
    joinColumns = @JoinColumn(name = "report_id")
)
@Column(name = "detail_message")
private List<String> details;
    // @ElementCollection
    // @CollectionTable(name = "replay_report_details", joinColumns = @JoinColumn(name = "report_id"))
    // @Column(name = "detail_message")
    // private List<String> details; // List of messages about the replay process and findings
}
