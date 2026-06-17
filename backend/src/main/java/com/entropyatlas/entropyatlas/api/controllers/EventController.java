package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.api.dto.BehaviorEventRequest;
import com.entropyatlas.entropyatlas.api.dto.BehaviorEventResponse;
import com.entropyatlas.entropyatlas.services.EventIngestionService;
import com.entropyatlas.entropyatlas.services.MetricsService;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Ingestion", description = "API for ingesting behavior events into Entropy Atlas")
public class EventController {

    private final EventIngestionService eventIngestionService;
    private final MetricsService metricsService;

    @PostMapping
    @Operation(summary = "Ingest a new behavior event",
            description = "Receives a behavior event and publishes it to Kafka for processing, also persists it to the database.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Event ingested successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BehaviorEventResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid event data")
            })
    public ResponseEntity<BehaviorEventResponse> ingestEvent(@Valid @RequestBody BehaviorEventRequest request) {
        Timer.Sample sample = metricsService.startApiLatencyTimer();
        try {
            BehaviorEventResponse response = BehaviorEventResponse.fromDomain(eventIngestionService.ingestEvent(request));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } finally {
            metricsService.stopApiLatencyTimer(sample, "/api/v1/events");
        }
    }
}
