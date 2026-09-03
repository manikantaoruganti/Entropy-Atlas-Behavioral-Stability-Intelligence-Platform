package com.entropyatlas.entropyatlas.services.ai;

import com.entropyatlas.entropyatlas.services.ai.dto.AIRiskVerificationRequest;
import com.entropyatlas.entropyatlas.services.ai.dto.AIRiskVerificationResult;

public interface AIRiskInvestigator {
    AIRiskVerificationResult verifyRisk(AIRiskVerificationRequest request);
}
