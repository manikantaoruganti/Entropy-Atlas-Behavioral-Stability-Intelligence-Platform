package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.DriftExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriftExplanationRepository extends JpaRepository<DriftExplanation, String> {
    List<DriftExplanation> findByEntityIdOrderByTimestampDesc(String entityId);
}
