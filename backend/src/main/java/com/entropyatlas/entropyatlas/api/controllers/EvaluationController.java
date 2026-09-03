package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.model.EvaluationResult;
import com.entropyatlas.entropyatlas.repository.EvaluationResultRepository;
import com.entropyatlas.entropyatlas.services.RiskEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the latest risk‑evaluation results.
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationResultRepository repository;
    private final RiskEvaluationService evaluationService;

    @Autowired
    public EvaluationController(EvaluationResultRepository repository, RiskEvaluationService evaluationService) {
        this.repository = repository;
        this.evaluationService = evaluationService;
    }

    /**
     * Returns the most recent evaluation result, or runs a new evaluation if none exist.
     */
    @GetMapping("/latest")
    public ResponseEntity<EvaluationResult> getLatest() {
        return repository.findTopByOrderByEvaluationTimestampDesc()
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    EvaluationResult result = evaluationService.runEvaluation();
                    return ResponseEntity.ok(result);
                });
    }
}
