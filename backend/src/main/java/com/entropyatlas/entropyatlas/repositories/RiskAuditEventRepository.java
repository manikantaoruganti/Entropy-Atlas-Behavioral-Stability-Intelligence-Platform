package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.RiskAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAuditEventRepository extends JpaRepository<RiskAuditEvent, String> {
    List<RiskAuditEvent> findByPaymentRiskIdOrderByTimestampDesc(String paymentRiskId);
}
