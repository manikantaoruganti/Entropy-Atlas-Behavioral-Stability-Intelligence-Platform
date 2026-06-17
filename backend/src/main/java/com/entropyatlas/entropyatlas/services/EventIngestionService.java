package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.api.dto.BehaviorEventRequest;
import com.entropyatlas.entropyatlas.config.KafkaConfig;
import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import com.entropyatlas.entropyatlas.domain.Entity;
import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import com.entropyatlas.entropyatlas.repositories.BehaviorEventRepository;
import com.entropyatlas.entropyatlas.repositories.EntityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionService {

    private final BehaviorEventRepository behaviorEventRepository;
    private final EntityRepository entityRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final BehavioralIntelligencePipeline intelligencePipeline;
    private final MetricsService metricsService;

    @Transactional
    public BehaviorEvent ingestEvent(BehaviorEventRequest request) {
        // 1. Persist the raw event
        BehaviorEvent event = new BehaviorEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEntityId(request.getEntityId());
        event.setEntityType(Entity.EntityType.valueOf(request.getEntityType()));
        event.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now());
        event.setLocation(request.getLocation());
        event.setResource(request.getResource());
        event.setAction(request.getAction());
        event.setLatency(request.getLatency());
        event.setPayloadSize(request.getPayloadSize());
        event.setMetadata(request.getMetadata());

        BehaviorEvent savedEvent = behaviorEventRepository.save(event);
        metricsService.incrementEventsIngested();
        log.info("Ingested behavior event: {} for entity: {}", savedEvent.getEventId(), savedEvent.getEntityId());

        // 2. Ensure entity exists or create it
        Optional<Entity> existingEntity = entityRepository.findById(request.getEntityId());
        if (existingEntity.isEmpty()) {
            Entity newEntity = new Entity(request.getEntityId(), Entity.EntityType.valueOf(request.getEntityType()), Instant.now(), Instant.now());
            entityRepository.save(newEntity);
            log.info("Created new entity: {}", newEntity.getId());
        } else {
            Entity entity = existingEntity.get();
            entity.setUpdatedAt(Instant.now());
            entityRepository.save(entity);
        }

        // 3. Publish event to Kafka for stream processing
        try {
            String eventJson = objectMapper.writeValueAsString(savedEvent);
            kafkaTemplate.send(KafkaConfig.BEHAVIOR_EVENTS_TOPIC, savedEvent.getEntityId(), eventJson);
            log.debug("Published event {} to Kafka topic {}", savedEvent.getEventId(), KafkaConfig.BEHAVIOR_EVENTS_TOPIC);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize behavior event to JSON: {}", e.getMessage());
        }

        // 4. *** CRITICAL: Run the full intelligence pipeline synchronously ***
        // This ensures stability snapshots, drift explanations, and all computed
        // intelligence is immediately available in the database after ingestion.
        try {
            StabilitySnapshot snapshot = intelligencePipeline.processEvent(savedEvent);
            if (snapshot != null) {
                log.info("Intelligence pipeline completed for entity {}: stability={}, drift={}",
                        savedEvent.getEntityId(), snapshot.getBehavioralStabilityScore(), snapshot.getDriftVelocity());
            }
        } catch (Exception e) {
            log.error("Intelligence pipeline failed for event {}: {}", savedEvent.getEventId(), e.getMessage(), e);
            // Event is still persisted, pipeline failure is non-fatal
        }

        return savedEvent;
    }
}
