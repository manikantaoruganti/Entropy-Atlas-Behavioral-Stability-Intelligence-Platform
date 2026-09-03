package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.RiskEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskEvidenceRepository extends JpaRepository<RiskEvidence, String> {
    Optional<RiskEvidence> findByPaymentRiskId(String paymentRiskId);
}
