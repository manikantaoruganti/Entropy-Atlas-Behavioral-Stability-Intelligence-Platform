package com.entropyatlas.entropyatlas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the deterministic risk policy engine.
 *
 * These values can be overridden via application.yml or environment variables
 * (e.g., `RISK_POLICY_MAX_INTERVENTION_FREQUENCY`).
 */
@Component
@ConfigurationProperties(prefix = "risk.policy")
public class RiskPolicyProperties {
    /** Maximum number of interventions allowed per entity within the configured time window */
    private int maxInterventionFrequency = 5;
    /** Time window in seconds for the max intervention frequency count */
    private long frequencyWindowSeconds = 3600; // 1 hour
    /** Minimum confidence required for a decision to be accepted */
    private double confidenceThreshold = 0.75;
    /** Minimum risk score required to trigger an intervention */
    private double riskScoreThreshold = 0.6;
    /** Cool‑down period in seconds after a denied decision before another can be attempted */
    private long cooldownSeconds = 300; // 5 minutes
    /** Flag to enforce a safe fallback when policy blocks */
    private boolean safeFallbackEnabled = true;
    /** Decision to use as safe fallback (defaults to MONITOR) */
    private String safeFallbackDecision = "MONITOR";
    /** Whether to block decisions when the AI provider is unavailable */
    private boolean blockOnAiUnavailable = false;

    // Getters and setters
    public int getMaxInterventionFrequency() { return maxInterventionFrequency; }
    public void setMaxInterventionFrequency(int maxInterventionFrequency) { this.maxInterventionFrequency = maxInterventionFrequency; }
    public long getFrequencyWindowSeconds() { return frequencyWindowSeconds; }
    public void setFrequencyWindowSeconds(long frequencyWindowSeconds) { this.frequencyWindowSeconds = frequencyWindowSeconds; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
    public double getRiskScoreThreshold() { return riskScoreThreshold; }
    public void setRiskScoreThreshold(double riskScoreThreshold) { this.riskScoreThreshold = riskScoreThreshold; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(long cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
    public boolean isSafeFallbackEnabled() { return safeFallbackEnabled; }
    public void setSafeFallbackEnabled(boolean safeFallbackEnabled) { this.safeFallbackEnabled = safeFallbackEnabled; }
    public String getSafeFallbackDecision() { return safeFallbackDecision; }
    public void setSafeFallbackDecision(String safeFallbackDecision) { this.safeFallbackDecision = safeFallbackDecision; }
    public boolean isBlockOnAiUnavailable() { return blockOnAiUnavailable; }
    public void setBlockOnAiUnavailable(boolean blockOnAiUnavailable) { this.blockOnAiUnavailable = blockOnAiUnavailable; }
}
