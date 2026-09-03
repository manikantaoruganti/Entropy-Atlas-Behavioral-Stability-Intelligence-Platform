package com.entropyatlas.entropyatlas.api.controllers;

import com.entropyatlas.entropyatlas.api.dto.StartScenarioRequest;
import com.entropyatlas.entropyatlas.api.dto.StartScenarioResponse;
import com.entropyatlas.entropyatlas.services.PaymentRiskScenarioSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to trigger deterministic payment‑risk scenario simulations.
 */
@RestController
@RequestMapping({"/api/scenario", "/api/v1/scenario", "/api/v1/risk/scenario"})
public class PaymentRiskScenarioController {

    private final PaymentRiskScenarioSimulator simulator;

    @Autowired
    public PaymentRiskScenarioController(PaymentRiskScenarioSimulator simulator) {
        this.simulator = simulator;
    }

    @PostMapping("/start")
    public ResponseEntity<StartScenarioResponse> startScenario(@RequestBody StartScenarioRequest request) throws Exception {
        StartScenarioResponse response = simulator.simulate(request);
        return ResponseEntity.ok(response);
    }
}
