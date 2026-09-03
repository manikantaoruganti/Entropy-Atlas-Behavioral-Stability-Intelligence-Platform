package com.entropyatlas.entropyatlas.tools;

import com.entropyatlas.entropyatlas.api.dto.PaymentRiskEventRequest;
import com.entropyatlas.entropyatlas.api.dto.PaymentRiskScenario;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Generates a deterministic synthetic payment‑risk dataset with explicit ground‑truth labels.
 * The dataset is split into TRAINING, VALIDATION, and HELD‑OUT TEST partitions.
 *
 * The generator uses a fixed seed based on the dataset version to guarantee reproducibility.
 */
public class SyntheticRiskDatasetGenerator {

    private final String datasetVersion;
    private final long seed;
    private final Random rng;

    public SyntheticRiskDatasetGenerator(String datasetVersion) {
        this.datasetVersion = datasetVersion;
        // deterministic seed derived from version string
        this.seed = datasetVersion.hashCode();
        this.rng = new Random(seed);
    }

    /**
     * Generates the full dataset and returns it split into three partitions.
     * @return a List of three lists: [training, validation, heldOutTest]
     */
    public List<List<PaymentRiskEventRequest>> generateDataset() {
        int total = 5000; // sufficiently large
        List<PaymentRiskEventRequest> all = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            PaymentRiskEventRequest ev = new PaymentRiskEventRequest();
            ev.setEntityId("eval-entity-" + rng.nextInt(1000));
            // Amount distribution: legit around 10‑200, abuse higher 300‑2000
            boolean isAbuse = rng.nextDouble() < 0.2; // 20% abuse
            double amount = isAbuse ? 300 + rng.nextDouble() * 1700 : 10 + rng.nextDouble() * 190;
            ev.setAmount(java.math.BigDecimal.valueOf(amount));
            ev.setCurrency("INR");
            ev.setAction("CHARGE");
            ev.setDeviceId("dev-" + rng.nextInt(5000));
            ev.setLocation("loc-" + rng.nextInt(200));
            ev.setResource("/v1/payments");
            ev.setTimestamp(Instant.now().minusSeconds(rng.nextInt(86400)));
            ev.setMetadata(null);
            ev.setGroundTruth(isAbuse ? "RISK" : "NORMAL");
            all.add(ev);
        }
        // Shuffle deterministically
        java.util.Collections.shuffle(all, new Random(seed + 1));
        int trainSize = (int) (total * 0.6);
        int valSize = (int) (total * 0.2);
        List<PaymentRiskEventRequest> training = all.subList(0, trainSize);
        List<PaymentRiskEventRequest> validation = all.subList(trainSize, trainSize + valSize);
        List<PaymentRiskEventRequest> heldOut = all.subList(trainSize + valSize, total);
        List<List<PaymentRiskEventRequest>> partitions = new ArrayList<>(3);
        partitions.add(new ArrayList<>(training));
        partitions.add(new ArrayList<>(validation));
        partitions.add(new ArrayList<>(heldOut));
        return partitions;
    }
}
