package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.RiskEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskEvaluationRepository extends JpaRepository<RiskEvaluation, String> {
    List<RiskEvaluation> findAllByOrderByTimestampDesc();
}
