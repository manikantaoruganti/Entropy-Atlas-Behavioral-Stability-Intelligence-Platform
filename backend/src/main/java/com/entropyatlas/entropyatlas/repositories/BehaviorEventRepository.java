package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BehaviorEventRepository extends JpaRepository<BehaviorEvent, String> {
    List<BehaviorEvent> findByEntityIdOrderByTimestampAsc(String entityId);
    List<BehaviorEvent> findByEntityIdAndTimestampBetweenOrderByTimestampAsc(String entityId, Instant start, Instant end);
    List<BehaviorEvent> findTop50ByOrderByTimestampDesc();
}
