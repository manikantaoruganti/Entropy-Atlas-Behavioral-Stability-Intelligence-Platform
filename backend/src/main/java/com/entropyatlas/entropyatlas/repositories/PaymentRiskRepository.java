package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.PaymentRisk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRiskRepository extends JpaRepository<PaymentRisk, String> {
    Optional<PaymentRisk> findByTransactionId(String transactionId);
    Optional<PaymentRisk> findByCorrelationId(String correlationId);
    Page<PaymentRisk> findByEntityId(String entityId, Pageable pageable);
    Page<PaymentRisk> findByStatus(String status, Pageable pageable);
    List<PaymentRisk> findByEntityIdOrderByTimestampDesc(String entityId);
}
