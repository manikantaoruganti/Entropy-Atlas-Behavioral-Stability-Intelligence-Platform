package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.services.ai.dto.AIRiskVerificationResult;
import com.entropyatlas.entropyatlas.domain.PaymentRisk;
import com.entropyatlas.entropyatlas.services.ai.dto.DecisionEnum;

/**
 * Service responsible for enforcing deterministic risk‑policy constraints on AI recommendations.
 * It validates the AI output against configurable thresholds, frequency limits, cooldowns, etc.
 * The service also creates an immutable audit record for every decision evaluation.
 */
public interface RiskPolicyService {
    /**
     * Evaluate the policy for a given AI verification result and the associated payment risk.
     *
     * @param aiResult    The result returned by the AI risk investigator.
     * @param paymentRisk The persisted {@link PaymentRisk} entity containing deterministic scoring data.
     * @return The final defensive {@link DecisionEnum} that should be applied (may be a safe fallback).
     */
    DecisionEnum evaluatePolicy(AIRiskVerificationResult aiResult, PaymentRisk paymentRisk);
}
