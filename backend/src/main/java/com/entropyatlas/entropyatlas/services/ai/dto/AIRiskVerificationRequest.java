package com.entropyatlas.entropyatlas.services.ai.dto;

import java.util.Map;

public class AIRiskVerificationRequest {
    private String entityId;
    private double riskScore;
    private String riskLevel;
    private String riskType;
    private Map<String, Double> entropySignals;
    private Map<String, Double> driftSignals;
    private Map<String, Double> stabilitySignals;
    private Map<String, Double> baselineComparison;
    private double amount;
    private String location;
    private String deviceId;
    private String paymentMethod;
    private Map<String, String> metadata;

    // Getters and setters
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskType() { return riskType; }
    public void setRiskType(String riskType) { this.riskType = riskType; }
    public Map<String, Double> getEntropySignals() { return entropySignals; }
    public void setEntropySignals(Map<String, Double> entropySignals) { this.entropySignals = entropySignals; }
    public Map<String, Double> getDriftSignals() { return driftSignals; }
    public void setDriftSignals(Map<String, Double> driftSignals) { this.driftSignals = driftSignals; }
    public Map<String, Double> getStabilitySignals() { return stabilitySignals; }
    public void setStabilitySignals(Map<String, Double> stabilitySignals) { this.stabilitySignals = stabilitySignals; }
    public Map<String, Double> getBaselineComparison() { return baselineComparison; }
    public void setBaselineComparison(Map<String, Double> baselineComparison) { this.baselineComparison = baselineComparison; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    // Convenience: typed getRiskScore as Double for nullable usage
    public Double getRiskScoreBoxed() { return riskScore; }
}
