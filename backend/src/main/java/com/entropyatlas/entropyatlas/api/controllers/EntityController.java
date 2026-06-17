package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.api.dto.BehaviorEventResponse;
import com.entropyatlas.entropyatlas.api.dto.DriftExplanationResponse;
import com.entropyatlas.entropyatlas.api.dto.EntityResponse;
import com.entropyatlas.entropyatlas.api.dto.StabilitySnapshotResponse;
import com.entropyatlas.entropyatlas.domain.Entity;
import com.entropyatlas.entropyatlas.exceptions.ResourceNotFoundException;
import com.entropyatlas.entropyatlas.repositories.BehaviorEventRepository;
import com.entropyatlas.entropyatlas.repositories.DriftExplanationRepository;
import com.entropyatlas.entropyatlas.repositories.EntityRepository;
import com.entropyatlas.entropyatlas.repositories.StabilitySnapshotRepository;
import com.entropyatlas.entropyatlas.services.MetricsService;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
@Tag(name = "Entity Management", description = "API for retrieving entity information and their behavioral stability data")
public class EntityController {

    private final EntityRepository entityRepository;
    private final StabilitySnapshotRepository stabilitySnapshotRepository;
    private final BehaviorEventRepository behaviorEventRepository;
    private final DriftExplanationRepository driftExplanationRepository;
    private final MetricsService metricsService;

    @GetMapping
    @Operation(summary = "Get all entities",
            description = "Retrieves a paginated list of all entities.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved entities",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EntityResponse.class))))
            })
    public ResponseEntity<Page<EntityResponse>> getAllEntities(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<EntityResponse> entities = entityRepository.findAll(pageable)
                    .map(EntityResponse::fromDomain);
            return ResponseEntity.ok(entities);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/entities");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID",
            description = "Retrieves detailed information for a specific entity.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved entity",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<EntityResponse> getEntityById(
            @Parameter(description = "ID of the entity to retrieve", example = "user-123") @PathVariable String id) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            Entity entity = entityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));
            return ResponseEntity.ok(EntityResponse.fromDomain(entity));
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/entities/{id}");
        }
    }

    @GetMapping("/{id}/stability")
    @Operation(summary = "Get stability timeline for an entity",
            description = "Retrieves a list of historical stability snapshots for a given entity, ordered by timestamp.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved stability snapshots",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StabilitySnapshotResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<List<StabilitySnapshotResponse>> getEntityStabilityTimeline(
            @Parameter(description = "ID of the entity", example = "user-123") @PathVariable String id) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            // Ensure entity exists
            entityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));

            List<StabilitySnapshotResponse> snapshots = stabilitySnapshotRepository.findByEntityIdOrderByTimestampDesc(id)
                    .stream()
                    .map(StabilitySnapshotResponse::fromDomain)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(snapshots);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/entities/{id}/stability");
        }
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get behavior event timeline for an entity",
            description = "Retrieves a list of historical behavior events for a given entity, ordered by timestamp.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved behavior events",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BehaviorEventResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<List<BehaviorEventResponse>> getEntityBehaviorTimeline(
            @Parameter(description = "ID of the entity", example = "user-123") @PathVariable String id) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            // Ensure entity exists
            entityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));

            List<BehaviorEventResponse> events = behaviorEventRepository.findByEntityIdOrderByTimestampAsc(id)
                    .stream()
                    .map(BehaviorEventResponse::fromDomain)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(events);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/entities/{id}/timeline");
        }
    }

    @GetMapping("/{id}/explanations")
    @Operation(summary = "Get drift explanations for an entity",
            description = "Retrieves a list of historical drift explanations for a given entity, ordered by timestamp.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved drift explanations",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DriftExplanationResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "Entity not found")
            })
    public ResponseEntity<List<DriftExplanationResponse>> getDriftExplanations(
            @Parameter(description = "ID of the entity", example = "user-123") @PathVariable String id) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            // Ensure entity exists
            entityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));

            List<DriftExplanationResponse> explanations = driftExplanationRepository.findByEntityIdOrderByTimestampDesc(id)
                    .stream()
                    .map(DriftExplanationResponse::fromDomain)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(explanations);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/entities/{id}/explanations");
        }
    }
}
