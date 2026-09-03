package com.entropyatlas.entropyatlas.streams;

import com.entropyatlas.entropyatlas.services.PaymentRiskService;
import com.entropyatlas.entropyatlas.api.dto.PaymentRiskEventRequest;
import com.entropyatlas.entropyatlas.api.dto.PaymentRiskEventResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Processor that integrates the deterministic payment‑risk workflow into the Kafka Streams pipeline.
 * It ensures idempotent processing by tracking processed correlation IDs in a state store.
 */
@RequiredArgsConstructor
@Slf4j
public class PaymentRiskProcessor implements ValueTransformerWithKey<String, PaymentRiskEventRequest, PaymentRiskEventResponse> {

    private final PaymentRiskService paymentRiskService;
    private final ObjectMapper objectMapper;

    private KeyValueStore<String, Boolean> processedStore;

    @Override
    public void init(ProcessorContext context) {
        // Retrieve the state store defined in the topology
        this.processedStore = (KeyValueStore<String, Boolean>) context.getStateStore("processed-payments-store");
    }

    @Override
    public PaymentRiskEventResponse transform(String readOnlyKey, PaymentRiskEventRequest value) {
        try {
            String correlationId = value.getCorrelationId();
            if (correlationId == null) {
                // Generate a correlation ID if missing (ensures dedup logic works)
                correlationId = java.util.UUID.randomUUID().toString();
                value.setCorrelationId(correlationId);
            }
            // Idempotency check
            if (processedStore.get(correlationId) != null) {
                log.debug("Skipping already processed payment risk event with correlationId {}", correlationId);
                return null; // duplicate – drop
            }
            // Invoke the existing deterministic risk workflow
            PaymentRiskEventResponse response = paymentRiskService.ingestRiskEvent(value);
            // Record as processed
            processedStore.put(correlationId, true);
            return response;
        } catch (Exception e) {
            log.error("Error processing payment risk event: {}", e.getMessage(), e);
            // Do not let the exception kill the stream – return a minimal failure response
            PaymentRiskEventResponse failure = new PaymentRiskEventResponse();
            failure.setEventId(value != null ? value.getTransactionId() : null);
            failure.setEntityId(value != null ? value.getEntityId() : null);
            failure.setCorrelationId(value != null ? value.getCorrelationId() : null);
            failure.setReceivedAt(java.time.Instant.now());
            failure.setStatus("FAILED");
            return failure;
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}
