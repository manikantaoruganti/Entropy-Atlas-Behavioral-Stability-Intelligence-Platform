package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.StabilitySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StabilitySnapshotRepository extends JpaRepository<StabilitySnapshot, String> {
    List<StabilitySnapshot> findByEntityIdOrderByTimestampDesc(String entityId);
    Optional<StabilitySnapshot> findFirstByEntityIdOrderByTimestampDesc(String entityId);
    List<StabilitySnapshot> findByEntityIdAndTimestampBetweenOrderByTimestampAsc(String entityId, Instant start, Instant end);
}
