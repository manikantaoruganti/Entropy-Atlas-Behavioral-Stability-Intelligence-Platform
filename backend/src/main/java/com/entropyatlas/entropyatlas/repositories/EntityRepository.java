package com.entropyatlas.entropyatlas.repositories;

import com.entropyatlas.entropyatlas.domain.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntityRepository extends JpaRepository<Entity, String> {
    Optional<Entity> findById(String id);
}
