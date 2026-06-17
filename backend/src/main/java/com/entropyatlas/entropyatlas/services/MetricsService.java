package com.entropyatlas.entropyatlas.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    private Counter eventsIngestedTotal;
    private Counter entropyCalculationsTotal;
    private Counter driftDetectionsTotal;
    private Counter stabilityUpdatesTotal;
    private Counter replayRequestsTotal;

    @PostConstruct
    public void init() {
        eventsIngestedTotal = Counter.builder("events_ingested_total")
                .description("Total number of behavior events ingested")
                .register(meterRegistry);

        entropyCalculationsTotal = Counter.builder("entropy_calculations_total")
                .description("Total number of entropy calculations performed")
                .register(meterRegistry);

        driftDetectionsTotal = Counter.builder("drift_detections_total")
                .description("Total number of drift detections performed")
                .register(meterRegistry);

        stabilityUpdatesTotal = Counter.builder("stability_updates_total")
                .description("Total number of stability score updates")
                .register(meterRegistry);

        replayRequestsTotal = Counter.builder("replay_requests_total")
                .description("Total number of replay requests initiated")
                .register(meterRegistry);
    }

    public void incrementEventsIngested() {
        eventsIngestedTotal.increment();
    }

    public void incrementEntropyCalculations() {
        entropyCalculationsTotal.increment();
    }

    public void incrementDriftDetections() {
        driftDetectionsTotal.increment();
    }

    public void incrementStabilityUpdates() {
        stabilityUpdatesTotal.increment();
    }

    public void incrementReplayRequests() {
        replayRequestsTotal.increment();
    }

    public Timer.Sample startApiLatencyTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopApiLatencyTimer(Timer.Sample sample, String apiPath) {
        sample.stop(Timer.builder("api_latency_ms")
                .description("API request latency in milliseconds")
                .publishPercentiles(0.5, 0.9, 0.99)
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .tag("path", apiPath)
                .register(meterRegistry));
    }
}
