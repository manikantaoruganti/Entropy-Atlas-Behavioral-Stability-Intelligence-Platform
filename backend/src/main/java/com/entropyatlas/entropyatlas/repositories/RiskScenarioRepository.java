package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.RiskScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskScenarioRepository extends JpaRepository<RiskScenario, String> {
    List<RiskScenario> findByEntityId(String entityId);
    List<RiskScenario> findByStatus(String status);
}
