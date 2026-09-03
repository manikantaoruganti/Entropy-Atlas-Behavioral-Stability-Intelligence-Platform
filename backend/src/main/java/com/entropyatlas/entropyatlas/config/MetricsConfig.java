package com.entropyatlas.entropyatlas.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * Central configuration for custom Prometheus metrics used in the Razorpay risk workflow.
 * The metrics are defined as beans so they can be injected wherever needed.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Counter riskEventsTotal(MeterRegistry registry) {
        return Counter.builder("risk_events_total")
                .description("Total number of risk events processed")
                .register(registry);
    }

    @Bean
    public Counter riskHighTotal(MeterRegistry registry) {
        return Counter.builder("risk_high_total")
                .description("Total high‑risk events detected")
                .register(registry);
    }

    @Bean
    public Counter riskCriticalTotal(MeterRegistry registry) {
        return Counter.builder("risk_critical_total")
                .description("Total critical‑risk events detected")
                .register(registry);
    }

    @Bean
    public Counter riskReviewTotal(MeterRegistry registry) {
        return Counter.builder("risk_review_total")
                .description("Total events routed for manual review")
                .register(registry);
    }

    @Bean
    public Counter riskDecisionsTotal(MeterRegistry registry) {
        return Counter.builder("risk_decisions_total")
                .description("Total risk decisions made (allow/monitor/…)")
                .register(registry);
    }

    @Bean
    public Counter riskPolicyBlocksTotal(MeterRegistry registry) {
        return Counter.builder("risk_policy_blocks_total")
                .description("Total events blocked by policy engine")
                .register(registry);
    }

    @Bean
    public Counter riskAiVerificationTotal(MeterRegistry registry) {
        return Counter.builder("risk_ai_verification_total")
                .description("Total events verified by AI service")
                .register(registry);
    }

    @Bean
    public Counter riskAiFallbackTotal(MeterRegistry registry) {
        return Counter.builder("risk_ai_fallback_total")
                .description("Total events where AI fallback was used")
                .register(registry);
    }

    @Bean
    public Timer riskDetectionLatency(MeterRegistry registry) {
        return Timer.builder("risk_detection_latency")
                .description("Latency between event ingestion and risk decision (ms)")
                .publishPercentileHistogram()
                .register(registry);
    }

    @Bean
    public Timer riskAiLatency(MeterRegistry registry) {
        return Timer.builder("risk_ai_latency")
                .description("Latency of AI verification service (ms)")
                .publishPercentileHistogram()
                .register(registry);
    }

    @Bean
    public Counter riskEvaluationRunsTotal(MeterRegistry registry) {
        return Counter.builder("risk_evaluation_runs_total")
                .description("Total number of risk evaluation runs performed")
                .register(registry);
    }

    @Bean
    public Counter riskFalsePositiveTotal(MeterRegistry registry) {
        return Counter.builder("risk_false_positive_total")
                .description("Cumulative false‑positive count across evaluations")
                .register(registry);
    }

    @Bean
    public Counter riskFalseNegativeTotal(MeterRegistry registry) {
        return Counter.builder("risk_false_negative_total")
                .description("Cumulative false‑negative count across evaluations")
                .register(registry);
    }
}
