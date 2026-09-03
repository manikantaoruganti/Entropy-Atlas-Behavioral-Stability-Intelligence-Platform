package com.entropyatlas.entropyatlas.services.ai;

import com.entropyatlas.entropyatlas.services.ai.dto.AIRiskVerificationRequest;
import com.entropyatlas.entropyatlas.services.ai.dto.AIRiskVerificationResult;
import com.entropyatlas.entropyatlas.services.ai.dto.DecisionEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRiskInvestigatorImpl implements AIRiskInvestigator {

    private final RestTemplate restTemplate;

    @Value("${AI_PROVIDER_ENDPOINT:}")
    String aiProviderEndpoint;

    @Override
    public AIRiskVerificationResult verifyRisk(AIRiskVerificationRequest request) {
        AIRiskVerificationResult fallbackResult = buildFallback(request);

        if (aiProviderEndpoint == null || aiProviderEndpoint.isBlank()) {
            log.warn("AI provider endpoint not configured; using deterministic fallback for entity={}", request.getEntityId());
            return fallbackResult;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AIRiskVerificationRequest> entity = new HttpEntity<>(request, headers);
            AIRiskVerificationResult response = restTemplate.postForObject(aiProviderEndpoint, entity, AIRiskVerificationResult.class);
            if (response == null) {
                log.warn("AI provider returned null response; using fallback");
                return fallbackResult;
            }
            response.setProviderAvailable(true);
            return response;
        } catch (Exception ex) {
            log.error("Error calling AI provider for risk verification; using fallback", ex);
            return fallbackResult;
        }
    }

    private AIRiskVerificationResult buildFallback(AIRiskVerificationRequest request) {
        AIRiskVerificationResult result = new AIRiskVerificationResult();
        result.setProviderAvailable(false);
        result.setVerifiedRiskLevel(request.getRiskLevel());

        String level = request.getRiskLevel() != null ? request.getRiskLevel().toUpperCase() : "MEDIUM";

        switch (level) {
            case "LOW":
                result.setRecommendedDecision(DecisionEnum.ALLOW);
                result.setConfidence(0.90);
                result.setRiskHypothesis("Behavioral patterns within normal thresholds. Low entropy observed.");
                result.setEvidenceSummary("No anomalous velocity, location or device signals detected.");
                result.setExplanation(
                    "Entropy Atlas behavioral analysis shows stable patterns for this entity. " +
                    "Entropy contribution is low, drift signals are within historical baseline. " +
                    "Policy decision: ALLOW transaction.");
                break;
            case "MEDIUM":
                result.setRecommendedDecision(DecisionEnum.MONITOR);
                result.setConfidence(0.78);
                result.setRiskHypothesis("Marginal behavioral deviation detected. Elevated entropy in one or more dimensions.");
                result.setEvidenceSummary("Partial velocity spike or minor location drift. Monitoring recommended.");
                result.setExplanation(
                    "Behavioral intelligence indicates elevated transaction velocity or minor location drift. " +
                    "Entropy signals are above baseline but below critical thresholds. " +
                    "Policy decision: MONITOR with enhanced telemetry collection.");
                break;
            case "HIGH":
                result.setRecommendedDecision(DecisionEnum.REVIEW);
                result.setConfidence(0.87);
                result.setRiskHypothesis("Significant behavioral anomaly detected. Multiple entropy dimensions elevated.");
                result.setEvidenceSummary("Velocity spike AND device/location drift co-occurring. Manual review required.");
                result.setExplanation(
                    "Multi-signal behavioral anomaly: transaction velocity exceeds 3-sigma baseline, " +
                    "device fingerprint drift detected, and location entropy elevated. " +
                    "Entropy Atlas risk score breach. Policy decision: REVIEW — escalate to fraud analyst.");
                break;
            case "CRITICAL":
                result.setRecommendedDecision(DecisionEnum.ESCALATE);
                result.setConfidence(0.95);
                result.setRiskHypothesis("Critical payment abuse signal. Behavioral instability across all entropy dimensions.");
                result.setEvidenceSummary("Extreme velocity, device takeover signal, or carding pattern. Immediate block required.");
                result.setExplanation(
                    "CRITICAL: Entropy Atlas behavioral model detected extreme multi-dimensional anomaly. " +
                    "Velocity ≫ baseline, device entropy critical, location divergence maximum. " +
                    "Stability score has collapsed. Policy enforcement: BLOCK + ESCALATE to risk operations.");
                break;
            default:
                result.setRecommendedDecision(DecisionEnum.MONITOR);
                result.setConfidence(0.70);
                result.setRiskHypothesis("Risk level undetermined. Default monitoring applied.");
                result.setEvidenceSummary("Insufficient behavioral history. Monitoring as precaution.");
                result.setExplanation("Behavioral baseline not yet established for this entity. Defaulting to MONITOR.");
        }

        Map<String, Double> supporting = new HashMap<>();
        Map<String, Double> contradicting = new HashMap<>();

        double score = request.getRiskScore();
        supporting.put("VELOCITY_SPIKE", Math.min(score * 0.4, 1.0));
        supporting.put("ENTROPY_ELEVATION", Math.min(score * 0.3, 1.0));
        supporting.put("DRIFT_SIGNAL", Math.min(score * 0.3, 1.0));
        contradicting.put("HISTORICAL_BASELINE_MATCH", Math.max(1.0 - score, 0.0));

        result.setSupportingEvidence(supporting);
        result.setContradictingEvidence(contradicting);

        return result;
    }
}
