package com.entropyatlas.entropyatlas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the risk evaluation pipeline.
 */
@Component
@ConfigurationProperties(prefix = "evaluation")
public class EvaluationProperties {

    /** Dataset version identifier (e.g., v1, v2). */
    private String datasetVersion = "v1";

    /** Cost associated with a false positive (default $0.01). */
    private double falsePositiveCost = 0.01;

    /** Cost associated with a false negative (default $1.00). */
    private double falseNegativeCost = 1.00;

    /** Whether to run the evaluation automatically on application startup. */
    private boolean runOnStartup = false;

    // getters and setters
    public String getDatasetVersion() { return datasetVersion; }
    public void setDatasetVersion(String datasetVersion) { this.datasetVersion = datasetVersion; }
    public double getFalsePositiveCost() { return falsePositiveCost; }
    public void setFalsePositiveCost(double falsePositiveCost) { this.falsePositiveCost = falsePositiveCost; }
    public double getFalseNegativeCost() { return falseNegativeCost; }
    public void setFalseNegativeCost(double falseNegativeCost) { this.falseNegativeCost = falseNegativeCost; }
    public boolean isRunOnStartup() { return runOnStartup; }
    public void setRunOnStartup(boolean runOnStartup) { this.runOnStartup = runOnStartup; }
}
