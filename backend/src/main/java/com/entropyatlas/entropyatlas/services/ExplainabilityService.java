package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.domain.DriftExplanation;
import com.entropyatlas.entropyatlas.repositories.DriftExplanationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplainabilityService {

    private final DriftExplanationRepository driftExplanationRepository;

    public List<DriftExplanation> getDriftExplanationsForEntity(String entityId) {
        return driftExplanationRepository.findByEntityIdOrderByTimestampDesc(entityId);
    }

    public DriftExplanation saveDriftExplanation(DriftExplanation explanation) {
        return driftExplanationRepository.save(explanation);
    }
}
