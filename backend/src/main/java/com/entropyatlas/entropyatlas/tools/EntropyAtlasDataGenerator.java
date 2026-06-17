package com.entropyatlas.entropyatlas.tools;

import com.entropyatlas.entropyatlas.domain.BehaviorEvent;
import com.entropyatlas.entropyatlas.domain.Entity;
import com.entropyatlas.entropyatlas.repositories.BehaviorEventRepository;
import com.entropyatlas.entropyatlas.repositories.EntityRepository;
import com.entropyatlas.entropyatlas.services.ReplayEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntropyAtlasDataGenerator implements CommandLineRunner {

    private final EntityRepository entityRepository;
    private final BehaviorEventRepository behaviorEventRepository;
    private final ReplayEngineService replayEngineService;

    private static final List<String> LOCATIONS = Arrays.asList(
            "US-EAST", "US-WEST", "EU-WEST", "EU-CENTRAL", "AP-SOUTH",
            "AP-EAST", "SINGAPORE", "LONDON", "MUMBAI", "TOKYO"
    );

    private static final List<String> ACTIONS = Arrays.asList(
            "LOGIN", "LOGOUT", "READ", "WRITE", "QUERY", "PROCESS_PAYMENT",
            "SETTLEMENT", "REFUND", "AUTHORIZE", "CAPTURE", "SYNC", "EXPORT",
            "IMPORT", "STREAM", "NOTIFY"
    );

    private static final List<String> RESOURCES = Arrays.asList(
            "/payments/process", "/payments/refund", "/auth/login", "/auth/refresh",
            "/ledger/entries", "/merchant/profile", "/orders/create", "/orders/update",
            "/reports/export", "/notifications/send"
    );

    private static final List<Long> PAYLOADS = Arrays.asList(512L, 1024L, 2048L, 4096L, 8192L, 16384L);

    @Override
    public void run(String... args) throws Exception {
        if (Arrays.asList(args).contains("--generate-data")) {
            log.info("=========================================================");
            log.info("STARTING PRODUCTION-GRADE BEHAVIORAL DATA GENERATION");
            log.info("=========================================================");

            behaviorEventRepository.deleteAll();
            entityRepository.deleteAll();

            List<Entity> generatedEntities = generateEntities();
            log.info("Generated {} unique entities.", generatedEntities.size());

            int totalEvents = 0;
            Random random = new Random(42); // Deterministic seed for consistency

            for (Entity entity : generatedEntities) {
                String primaryLocation = LOCATIONS.get(random.nextInt(LOCATIONS.size()));
                String primaryAction = ACTIONS.get(random.nextInt(ACTIONS.size()));
                String primaryResource = RESOURCES.get(random.nextInt(RESOURCES.size()));
                Long primaryPayload = PAYLOADS.get(random.nextInt(PAYLOADS.size()));

                int eventsForEntity = 70 + random.nextInt(40); // 70 to 110 events per entity
                Instant now = Instant.now();

                List<BehaviorEvent> eventsToSave = new ArrayList<>();

                for (int i = 0; i < eventsForEntity; i++) {
                    // Spread events chronologically over 90 days
                    long offsetDays = 90 - (90L * i / eventsForEntity);
                    Instant timestamp = now.minus(Duration.ofDays(offsetDays))
                            .plus(Duration.ofMinutes(random.nextInt(60 * 24))); // Randomize time within the day

                    int phase = determinePhase(offsetDays);

                    BehaviorEvent event = new BehaviorEvent();
                    event.setEventId(UUID.randomUUID().toString());
                    event.setEntityId(entity.getId());
                    event.setEntityType(entity.getEntityType());
                    event.setTimestamp(timestamp);

                    // Phase Evolution Logic
                    if (phase == 1) { // Stable
                        event.setLocation(random.nextDouble() < 0.90 ? primaryLocation : randomItem(LOCATIONS, random));
                        event.setAction(random.nextDouble() < 0.90 ? primaryAction : randomItem(ACTIONS, random));
                        event.setResource(random.nextDouble() < 0.90 ? primaryResource : randomItem(RESOURCES, random));
                        event.setPayloadSize(random.nextDouble() < 0.90 ? primaryPayload : randomItem(PAYLOADS, random));
                        event.setLatency(80L + random.nextInt(70));
                    } else if (phase == 2) { // Minor Variations
                        event.setLocation(random.nextDouble() < 0.70 ? primaryLocation : randomItem(LOCATIONS, random));
                        event.setAction(random.nextDouble() < 0.70 ? primaryAction : randomItem(ACTIONS, random));
                        event.setResource(random.nextDouble() < 0.70 ? primaryResource : randomItem(RESOURCES, random));
                        event.setPayloadSize(random.nextDouble() < 0.70 ? primaryPayload : randomItem(PAYLOADS, random));
                        event.setLatency(150L + random.nextInt(100));
                    } else if (phase == 3) { // Drift Begins
                        event.setLocation(random.nextDouble() < 0.50 ? primaryLocation : randomItem(LOCATIONS, random));
                        event.setAction(random.nextDouble() < 0.50 ? primaryAction : randomItem(ACTIONS, random));
                        event.setResource(random.nextDouble() < 0.50 ? primaryResource : randomItem(RESOURCES, random));
                        event.setPayloadSize(random.nextDouble() < 0.50 ? primaryPayload : randomItem(PAYLOADS, random));
                        event.setLatency(250L + random.nextInt(250));
                    } else if (phase == 4) { // High Entropy
                        event.setLocation(randomItem(LOCATIONS, random));
                        event.setAction(randomItem(ACTIONS, random));
                        event.setResource(randomItem(RESOURCES, random));
                        event.setPayloadSize(randomItem(PAYLOADS, random));
                        event.setLatency(800L + random.nextInt(2200));
                    } else { // Phase 5 - Recovery
                        event.setLocation(random.nextDouble() < 0.90 ? primaryLocation : randomItem(LOCATIONS, random));
                        event.setAction(random.nextDouble() < 0.90 ? primaryAction : randomItem(ACTIONS, random));
                        event.setResource(random.nextDouble() < 0.90 ? primaryResource : randomItem(RESOURCES, random));
                        event.setPayloadSize(random.nextDouble() < 0.90 ? primaryPayload : randomItem(PAYLOADS, random));
                        event.setLatency(80L + random.nextInt(70));
                    }

                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("phase", "PHASE_" + phase);
                    metadata.put("client_version", "v" + (1 + random.nextInt(3)) + ".0");
                    event.setMetadata(metadata);

                    eventsToSave.add(event);
                }

                // Sort chronologically before saving
                eventsToSave.sort(Comparator.comparing(BehaviorEvent::getTimestamp));
                behaviorEventRepository.saveAll(eventsToSave);
                totalEvents += eventsToSave.size();
            }

            log.info("Successfully generated {} events across 90 days.", totalEvents);
            log.info("Initiating Replay Engine orchestration for {} entities...", generatedEntities.size());

            // Run intelligence pipeline
            for (Entity entity : generatedEntities) {
                replayEngineService.replayEntityHistory(entity.getId());
            }

            log.info("=========================================================");
            log.info("BEHAVIORAL DATA GENERATION COMPLETE.");
            log.info("All stability snapshots and drift explanations have been populated.");
            log.info("=========================================================");
        }
    }

    private int determinePhase(long offsetDays) {
        if (offsetDays >= 70) return 1; // Days 90-70
        if (offsetDays >= 50) return 2; // Days 70-50
        if (offsetDays >= 30) return 3; // Days 50-30
        if (offsetDays >= 10) return 4; // Days 30-10
        return 5;                       // Days 10-0
    }

    private <T> T randomItem(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }

    private List<Entity> generateEntities() {
        List<Entity> entities = new ArrayList<>();

        String[] services = {"payment-service", "auth-service", "ledger-engine", "merchant-gateway",
                "settlement-service", "notification-engine", "analytics-engine", "entropy-engine",
                "drift-engine", "replay-engine"};
        for (String s : services) {
            entities.add(new Entity(s, Entity.EntityType.SERVICE));
        }

        String[] clients = {"mobile-app", "web-portal", "merchant-dashboard", "admin-console",
                "amazon", "flipkart", "uber", "netflix", "spotify", "airbnb"};
        for (String c : clients) {
            entities.add(new Entity(c, Entity.EntityType.API_CLIENT));
        }

        String[] partners = {"visa-network", "mastercard-network", "paypal-gateway", "razorpay-gateway", "stripe-gateway"};
        for (String p : partners) {
            entities.add(new Entity(p, Entity.EntityType.PARTNER));
        }

        String[] regions = {"us-east", "us-west", "eu-west", "ap-south"};
        for (String r : regions) {
            entities.add(new Entity(r, Entity.EntityType.REGION));
        }

        for (int i = 1; i <= 20; i++) {
            entities.add(new Entity("merchant-" + i, Entity.EntityType.MERCHANT));
        }

        for (int i = 1; i <= 10; i++) {
            entities.add(new Entity("device-" + i, Entity.EntityType.DEVICE));
        }

        return entityRepository.saveAll(entities);
    }
}
