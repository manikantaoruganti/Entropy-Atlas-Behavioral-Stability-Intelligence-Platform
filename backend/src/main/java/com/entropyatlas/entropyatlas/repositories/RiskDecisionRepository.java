package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.RiskDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskDecisionRepository extends JpaRepository<RiskDecision, String> {
    Optional<RiskDecision> findByPaymentRiskId(String paymentRiskId);
    List<RiskDecision> findByActorId(String actorId);
}
