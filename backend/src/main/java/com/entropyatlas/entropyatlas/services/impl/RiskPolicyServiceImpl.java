package com.entropyatlas.entropyatlas.services.impl;

import com.entropyatlas.entropyatlas.config.RiskPolicyProperties;
import com.entropyatlas.entropyatlas.domain.RiskPolicyAudit;
import com.entropyatlas.entropyatlas.repositories.RiskPolicyAuditRepository;
import com.entropyatlas.entropyatlas.services.RiskPolicyService;
import com.entropyatlas.entropyatlas.services.ai.dto.AIRiskVerificationResult;
import com.entropyatlas.entropyatlas.services.ai.dto.DecisionEnum;
import com.entropyatlas.entropyatlas.domain.PaymentRisk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Deterministic policy engine that validates AI recommendations against configurable safety constraints.
 * It also creates an immutable audit record for every evaluation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskPolicyServiceImpl implements RiskPolicyService {

    private final RiskPolicyProperties properties;
    private final RiskPolicyAuditRepository auditRepository;
    private final Clock clock = Clock.systemUTC(); // can be overridden in tests via reflection if needed

    @Override
    public DecisionEnum evaluatePolicy(AIRiskVerificationResult aiResult, PaymentRisk paymentRisk) {
        boolean blocked = false;
        StringBuilder reasonBuilder = new StringBuilder();

        // 1. AI provider availability
        if (!aiResult.isProviderAvailable() && properties.isBlockOnAiUnavailable()) {
            blocked = true;
            reasonBuilder.append("AI provider unavailable; ");
        }

        // 2. Confidence threshold
        if (!blocked && aiResult.getConfidence() < properties.getConfidenceThreshold()) {
            blocked = true;
            reasonBuilder.append("Confidence below threshold; ");
        }

        // 3. Unsafe recommendation check
        if (!blocked) {
            DecisionEnum rec = aiResult.getRecommendedDecision();
            if (rec == null) {
                blocked = true;
                reasonBuilder.append("AI recommendation is missing or invalid; ");
            } else if (paymentRisk.getRiskScore() >= properties.getRiskScoreThreshold() && rec == DecisionEnum.ALLOW) {
                blocked = true;
                reasonBuilder.append("AI recommended ALLOW for risk score above threshold; ");
            } else if ("CRITICAL".equalsIgnoreCase(paymentRisk.getRiskLevel()) && (rec == DecisionEnum.ALLOW || rec == DecisionEnum.MONITOR)) {
                blocked = true;
                reasonBuilder.append("AI recommended unsafe decision for CRITICAL risk; ");
            }
        }

        // 4. Intervention frequency limit
        if (!blocked) {
            Instant windowStart = Instant.now(clock).minusSeconds(properties.getFrequencyWindowSeconds());
            long recentCount = auditRepository.countByEntityIdAndTimestampAfter(paymentRisk.getEntityId(), windowStart);
            if (recentCount >= properties.getMaxInterventionFrequency()) {
                blocked = true;
                reasonBuilder.append("Intervention frequency limit exceeded; ");
            }
        }

        // 5. Cool‑down period
        if (!blocked) {
            RiskPolicyAudit latest = auditRepository.findFirstByEntityIdOrderByTimestampDesc(paymentRisk.getEntityId());
            if (latest != null) {
                long secondsSinceLast = Duration.between(latest.getTimestamp(), Instant.now(clock)).getSeconds();
                if (secondsSinceLast < properties.getCooldownSeconds()) {
                    blocked = true;
                    reasonBuilder.append("Cooldown period active; ");
                }
            }
        }

        DecisionEnum finalDecision;
        if (blocked) {
            // safe fallback decision from config
            finalDecision = DecisionEnum.valueOf(properties.getSafeFallbackDecision());
        } else {
            // Use AI recommended decision if present, otherwise fallback
            if (aiResult.getRecommendedDecision() != null) {
                finalDecision = aiResult.getRecommendedDecision();
            } else {
                finalDecision = DecisionEnum.valueOf(properties.getSafeFallbackDecision());
            }
        }

        // Persist immutable audit record
        RiskPolicyAudit audit = new RiskPolicyAudit();
        audit.setCorrelationId(paymentRisk.getCorrelationId());
        audit.setEntityId(paymentRisk.getEntityId());
        audit.setTransactionId(paymentRisk.getTransactionId());
        audit.setRiskScore(paymentRisk.getRiskScore());
        audit.setRiskLevel(paymentRisk.getRiskLevel());
        audit.setAiVerificationResult(aiResult.getVerifiedRiskLevel());
        audit.setRecommendedDecision(aiResult.getRecommendedDecision() != null ? aiResult.getRecommendedDecision().name() : "UNDETERMINED");
        audit.setPolicyDecision(finalDecision.name());
        audit.setFinalDecision(finalDecision.name());
        audit.setReason(reasonBuilder.toString().isEmpty() ? "COMPLIANT" : reasonBuilder.toString());
        audit.setTimestamp(Instant.now(clock));
        auditRepository.save(audit);

        log.info("Policy evaluation for entity {} resulted in {} (blocked={} reason='{}')",
                paymentRisk.getEntityId(), finalDecision, blocked, reasonBuilder.toString());
        return finalDecision;
    }
}
