package com.entropyatlas.entropyatlas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistent record of a single risk‑evaluation run.
 */
@Entity
public class EvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String datasetVersion;
    private Instant evaluationTimestamp;
    private String modelVersion; // e.g., git commit hash or semantic version

    private long truePositives;
    private long trueNegatives;
    private long falsePositives;
    private long falseNegatives;
    private double precision;
    private double recall;
    private double f1Score;
    private double falsePositiveRate;
    private double falseNegativeRate;
    private double falsePositiveCost;
    private double falseNegativeCost;
    private double avgDetectionLatencyMs;

    // Getters and setters (generated)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDatasetVersion() { return datasetVersion; }
    public void setDatasetVersion(String datasetVersion) { this.datasetVersion = datasetVersion; }
    public Instant getEvaluationTimestamp() { return evaluationTimestamp; }
    public void setEvaluationTimestamp(Instant evaluationTimestamp) { this.evaluationTimestamp = evaluationTimestamp; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public long getTruePositives() { return truePositives; }
    public void setTruePositives(long truePositives) { this.truePositives = truePositives; }
    public long getTrueNegatives() { return trueNegatives; }
    public void setTrueNegatives(long trueNegatives) { this.trueNegatives = trueNegatives; }
    public long getFalsePositives() { return falsePositives; }
    public void setFalsePositives(long falsePositives) { this.falsePositives = falsePositives; }
    public long getFalseNegatives() { return falseNegatives; }
    public void setFalseNegatives(long falseNegatives) { this.falseNegatives = falseNegatives; }
    public double getPrecision() { return precision; }
    public void setPrecision(double precision) { this.precision = precision; }
    public double getRecall() { return recall; }
    public void setRecall(double recall) { this.recall = recall; }
    public double getF1Score() { return f1Score; }
    public void setF1Score(double f1Score) { this.f1Score = f1Score; }
    public double getFalsePositiveRate() { return falsePositiveRate; }
    public void setFalsePositiveRate(double falsePositiveRate) { this.falsePositiveRate = falsePositiveRate; }
    public double getFalseNegativeRate() { return falseNegativeRate; }
    public void setFalseNegativeRate(double falseNegativeRate) { this.falseNegativeRate = falseNegativeRate; }
    public double getFalsePositiveCost() { return falsePositiveCost; }
    public void setFalsePositiveCost(double falsePositiveCost) { this.falsePositiveCost = falsePositiveCost; }
    public double getFalseNegativeCost() { return falseNegativeCost; }
    public void setFalseNegativeCost(double falseNegativeCost) { this.falseNegativeCost = falseNegativeCost; }
    public double getAvgDetectionLatencyMs() { return avgDetectionLatencyMs; }
    public void setAvgDetectionLatencyMs(double avgDetectionLatencyMs) { this.avgDetectionLatencyMs = avgDetectionLatencyMs; }
}
