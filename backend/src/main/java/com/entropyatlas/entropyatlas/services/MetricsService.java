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

    private Counter riskEventsTotal;
    private Counter riskHighTotal;
    private Counter riskCriticalTotal;
    private Counter riskReviewTotal;
    private Counter riskDecisionsTotal;
    private Counter riskPolicyBlocksTotal;
    private Counter riskAiVerificationTotal;
    private Counter riskAiFallbackTotal;

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

        riskEventsTotal = Counter.builder("risk_events_total")
                .description("Total number of risk events ingested")
                .register(meterRegistry);

        riskHighTotal = Counter.builder("risk_high_total")
                .description("Total number of high risk events")
                .register(meterRegistry);

        riskCriticalTotal = Counter.builder("risk_critical_total")
                .description("Total number of critical risk events")
                .register(meterRegistry);

        riskReviewTotal = Counter.builder("risk_review_total")
                .description("Total number of events sent to review")
                .register(meterRegistry);

        riskDecisionsTotal = Counter.builder("risk_decisions_total")
                .description("Total number of risk decisions applied")
                .register(meterRegistry);

        riskPolicyBlocksTotal = Counter.builder("risk_policy_blocks_total")
                .description("Total number of policy blocks triggered")
                .register(meterRegistry);

        riskAiVerificationTotal = Counter.builder("risk_ai_verification_total")
                .description("Total number of AI verifications performed")
                .register(meterRegistry);

        riskAiFallbackTotal = Counter.builder("risk_ai_fallback_total")
                .description("Total number of AI fallback activations")
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

    public void incrementRiskEvents() {
        riskEventsTotal.increment();
    }

    public void incrementRiskHigh() {
        riskHighTotal.increment();
    }

    public void incrementRiskCritical() {
        riskCriticalTotal.increment();
    }

    public void incrementRiskReview() {
        riskReviewTotal.increment();
    }

    public void incrementRiskDecisions() {
        riskDecisionsTotal.increment();
    }

    public void incrementRiskPolicyBlocks() {
        riskPolicyBlocksTotal.increment();
    }

    public void incrementRiskAiVerification() {
        riskAiVerificationTotal.increment();
    }

    public void incrementRiskAiFallback() {
        riskAiFallbackTotal.increment();
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
