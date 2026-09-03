package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.RiskPolicyAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RiskPolicyAuditRepository extends JpaRepository<RiskPolicyAudit, String> {
    // Count interventions within a time window for frequency limit
    long countByEntityIdAndTimestampAfter(String entityId, Instant after);

    // Find most recent audit record for an entity
    RiskPolicyAudit findFirstByEntityIdOrderByTimestampDesc(String entityId);
}
