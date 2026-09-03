package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.api.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentRiskService {
    PaymentRiskEventResponse ingestRiskEvent(PaymentRiskEventRequest request);
    Page<RiskAlertResponse> getRiskAlerts(String status, Pageable pageable);
    RiskIncidentResponse getIncidentDetails(String incidentId);
    RiskInvestigationResponse addInvestigationNotes(String incidentId, RiskInvestigationRequest request);
    RiskEvidenceResponse getRiskEvidence(String incidentId);
    RiskDecisionResponse submitRiskDecision(String incidentId, RiskDecisionRequest request);
    List<RiskAuditLogResponse> getAuditHistory(String incidentId);
    List<RiskDecisionResponse> getAllDecisions();
    List<RiskAuditLogResponse> getAllAuditLogs();
    RiskReplayResponse executeRiskReplay(String entityId);
    RiskEvaluationMetricsResponse getRiskEvaluationMetrics();
    RiskScenarioSimulationResponse simulateRiskScenario(RiskScenarioSimulationRequest request);
}
