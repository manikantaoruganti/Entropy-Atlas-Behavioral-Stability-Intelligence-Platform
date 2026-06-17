package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Map;

@jakarta.persistence.Entity
@Table(name = "behavior_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorEvent {
    @Id
    private String eventId;
    private String entityId;

    @Enumerated(EnumType.STRING)
    private Entity.EntityType entityType;

    private Instant timestamp;
    private String location;
    private String resource;
    private String action;
    private Long latency;
    private Long payloadSize;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "behavior_event_metadata", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata;
}
