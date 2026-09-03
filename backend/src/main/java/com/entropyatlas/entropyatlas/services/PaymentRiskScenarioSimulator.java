package com.entropyatlas.entropyatlas.services;

import com.entropyatlas.entropyatlas.api.dto.PaymentRiskEventRequest;
import com.entropyatlas.entropyatlas.api.dto.StartScenarioRequest;
import com.entropyatlas.entropyatlas.api.dto.StartScenarioResponse;
import com.entropyatlas.entropyatlas.api.dto.PaymentRiskScenario;
import com.entropyatlas.entropyatlas.config.KafkaConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Service that deterministically generates synthetic payment‑risk events for a given scenario
 * and publishes them to the {@link KafkaConfig#PAYMENT_RISK_EVENTS_TOPIC}.
 *
 * The generator uses a fixed {@link Random} seed derived from the scenario name and a UUID
 * to guarantee reproducibility across runs.
 */
@Service
public class PaymentRiskScenarioSimulator {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentRiskScenarioSimulator(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public StartScenarioResponse simulate(StartScenarioRequest request) throws JsonProcessingException {
        UUID scenarioId = UUID.randomUUID();
        PaymentRiskScenario scenario = request.getScenario();
        int eventCount = request.getEventCount() != null ? request.getEventCount() : defaultEventCount(scenario);
        String groundTruth = scenario == PaymentRiskScenario.NORMAL_PAYMENT_TRAFFIC ? "NORMAL" : "RISK";

        // deterministic seed: combine scenario name hash and UUID hash
        long seed = ((long) scenario.name().hashCode() << 32) ^ scenarioId.getMostSignificantBits();
        Random rng = new Random(seed);
        List<PaymentRiskEventRequest> events = generateEvents(scenario, eventCount, rng, groundTruth);
        // publish
        for (PaymentRiskEventRequest ev : events) {
            String json = objectMapper.writeValueAsString(ev);
            // use eventId as key if present, else UUID random
            String key = ev.getEntityId();
            kafkaTemplate.send(new ProducerRecord<>(KafkaConfig.PAYMENT_RISK_EVENTS_TOPIC, key, json));
        }
        // Build response
        StartScenarioResponse response = new StartScenarioResponse();
        response.setScenarioId(scenarioId);
        response.setScenarioType(scenario);
        response.setEventCount(events.size());
        response.setExpectedGroundTruth(groundTruth);
        response.setStartTime(Instant.now());
        return response;
    }

    private int defaultEventCount(PaymentRiskScenario scenario) {
        // simple defaults – can be tuned later
        switch (scenario) {
            case NORMAL_PAYMENT_TRAFFIC: return 1000;
            case VELOCITY_SPIKE: return 1500;
            case GEO_DRIFT: return 1200;
            case DEVICE_DRIFT: return 1200;
            case AMOUNT_ANOMALY: return 1300;
            case PAYMENT_METHOD_SHIFT: return 1300;
            case FAILURE_CLUSTER: return 1100;
            case COORDINATED_PAYMENT_ABUSE: return 1400;
            case AI_SERVICE_FAILURE: return 1000;
            case POLICY_BLOCK: return 1000;
            default: return 1000;
        }
    }

    private List<PaymentRiskEventRequest> generateEvents(PaymentRiskScenario scenario,
                                                       int count,
                                                       Random rng,
                                                       String groundTruth) {
        List<PaymentRiskEventRequest> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PaymentRiskEventRequest ev = new PaymentRiskEventRequest();
            ev.setEntityId("entity-" + rng.nextInt(1000));
            ev.setAmount(java.math.BigDecimal.valueOf(rng.nextDouble() * 500 + 10));
            ev.setCurrency("INR");
            ev.setAction("CHARGE");
            ev.setDeviceId("dev-" + rng.nextInt(5000));
            ev.setLocation("loc-" + rng.nextInt(200));
            ev.setResource("/v1/payments");
            ev.setTimestamp(Instant.now().plusMillis(rng.nextInt(5000)));
            ev.setMetadata(null);
            ev.setGroundTruth(groundTruth);
            // Apply scenario‑specific tweaks
            applyScenarioTweaks(ev, scenario, i, rng);
            list.add(ev);
        }
        return list;
    }

    private void applyScenarioTweaks(PaymentRiskEventRequest ev,
                                     PaymentRiskScenario scenario,
                                     int index,
                                     Random rng) {
        switch (scenario) {
            case VELOCITY_SPIKE:
                // concentrate many events in a short time window (first 200 events)
                if (index < 200) {
                    ev.setTimestamp(ev.getTimestamp().plusMillis(rng.nextInt(100)));
                }
                break;
            case GEO_DRIFT:
                // after halfway, shift location to a new range
                if (index > 0 && index > ev.getEntityId().length()) {
                    ev.setLocation("remote-" + rng.nextInt(50));
                }
                break;
            case DEVICE_DRIFT:
                // introduce a batch of new device ids after 30% of events
                if (index > (int) (0.3 * ev.getEntityId().length())) {
                    ev.setDeviceId("newdev-" + rng.nextInt(1000));
                }
                break;
            case AMOUNT_ANOMALY:
                // inflate amount for 5% of events
                if (rng.nextDouble() < 0.05) {
                    ev.setAmount(ev.getAmount().multiply(java.math.BigDecimal.valueOf(10)));
                }
                break;
            case PAYMENT_METHOD_SHIFT:
                // alternate between CHARGE and REFUND after 500 events
                if (index > 500) {
                    ev.setAction(rng.nextBoolean() ? "REFUND" : "CHARGE");
                }
                break;
            case FAILURE_CLUSTER:
                // mark some events as failed via metadata flag
                if (rng.nextDouble() < 0.03) {
                    ev.setMetadata(java.util.Collections.singletonMap("status", "FAILED"));
                }
                break;
            case COORDINATED_PAYMENT_ABUSE:
                // share a correlation id among groups of 10 events
                if (index % 10 == 0) {
                    ev.setMetadata(java.util.Collections.singletonMap("correlationId", UUID.randomUUID().toString()));
                }
                break;
            case AI_SERVICE_FAILURE:
                // indicate AI service unavailable via metadata
                ev.setMetadata(java.util.Collections.singletonMap("aiServiceUnavailable", "true"));
                break;
            case POLICY_BLOCK:
                // exceed a made‑up max amount threshold
                ev.setAmount(java.math.BigDecimal.valueOf(10000.0));
                break;
            default:
                // NORMAL_PAYMENT_TRAFFIC – nothing extra
                break;
        }
    }
}
