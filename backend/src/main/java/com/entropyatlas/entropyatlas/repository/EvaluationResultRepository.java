package com.entropyatlas.entropyatlas.repository;

import com.entropyatlas.entropyatlas.model.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for EvaluationResult entities.
 */
@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, UUID> {
    /**
     * Returns the most recent evaluation result based on evaluationTimestamp.
     */
    Optional<EvaluationResult> findTopByOrderByEvaluationTimestampDesc();
}
