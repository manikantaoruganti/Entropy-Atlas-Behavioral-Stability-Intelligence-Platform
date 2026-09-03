package com.entropyatlas.entropyatlas.services.ai.dto;

import com.entropyatlas.entropyatlas.services.ai.dto.DecisionEnum;

import java.util.Map;

public class AIRiskVerificationResult {
    private String verifiedRiskLevel;
    private double confidence;
    private String riskHypothesis;
    private String evidenceSummary;
    private Map<String, Double> supportingEvidence;
    private Map<String, Double> contradictingEvidence;
    private DecisionEnum recommendedDecision;
    private String explanation;
    private boolean providerAvailable;

    // Getters and setters
    public String getVerifiedRiskLevel() { return verifiedRiskLevel; }
    public void setVerifiedRiskLevel(String verifiedRiskLevel) { this.verifiedRiskLevel = verifiedRiskLevel; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getRiskHypothesis() { return riskHypothesis; }
    public void setRiskHypothesis(String riskHypothesis) { this.riskHypothesis = riskHypothesis; }
    public String getEvidenceSummary() { return evidenceSummary; }
    public void setEvidenceSummary(String evidenceSummary) { this.evidenceSummary = evidenceSummary; }
    public Map<String, Double> getSupportingEvidence() { return supportingEvidence; }
    public void setSupportingEvidence(Map<String, Double> supportingEvidence) { this.supportingEvidence = supportingEvidence; }
    public Map<String, Double> getContradictingEvidence() { return contradictingEvidence; }
    public void setContradictingEvidence(Map<String, Double> contradictingEvidence) { this.contradictingEvidence = contradictingEvidence; }
    public DecisionEnum getRecommendedDecision() { return recommendedDecision; }
    public void setRecommendedDecision(DecisionEnum recommendedDecision) { this.recommendedDecision = recommendedDecision; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public boolean isProviderAvailable() { return providerAvailable; }
    public void setProviderAvailable(boolean providerAvailable) { this.providerAvailable = providerAvailable; }
}
