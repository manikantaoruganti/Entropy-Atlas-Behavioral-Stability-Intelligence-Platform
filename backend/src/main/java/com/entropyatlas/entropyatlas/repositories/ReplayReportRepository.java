package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.ReplayReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplayReportRepository extends JpaRepository<ReplayReport, String> {
    List<ReplayReport> findByEntityIdOrderByReportGeneratedAtDesc(String entityId);
}
