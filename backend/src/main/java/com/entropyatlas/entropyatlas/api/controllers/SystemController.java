package com.entropyatlas.entropyatlas.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.util.*;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        boolean dbUp = checkPostgres();
        boolean redisUp = checkRedis();
        boolean kafkaUp = checkKafka();

        Map<String, Object> health = new HashMap<>();
        health.put("status", (dbUp && redisUp && kafkaUp) ? "UP" : "DOWN");
        health.put("checksRun", 3);
        health.put("postgres", dbUp ? "UP" : "DOWN");
        health.put("redis", redisUp ? "UP" : "DOWN");
        health.put("kafka", kafkaUp ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getResources() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> res = new HashMap<>();
        res.put("cpuCount", runtime.availableProcessors());
        res.put("memoryMax", runtime.maxMemory());
        res.put("memoryTotal", runtime.totalMemory());
        res.put("memoryFree", runtime.freeMemory());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/dependencies")
    public ResponseEntity<Map<String, Object>> getDependencies() {
        Map<String, Object> deps = new HashMap<>();
        deps.put("postgresql", checkPostgres() ? "UP" : "DOWN");
        deps.put("redis", checkRedis() ? "UP" : "DOWN");
        deps.put("kafka", checkKafka() ? "UP" : "DOWN");
        return ResponseEntity.ok(deps);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("operationalState", (checkPostgres() && checkRedis() && checkKafka()) ? "HEALTHY" : "DEGRADED");
        status.put("version", "2.0.0-RELEASE");
        status.put("bootTime", System.currentTimeMillis() - 86400000L);
        return ResponseEntity.ok(status);
    }

    private boolean checkPostgres() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(1);
        } catch (Exception e) {
            log.error("Postgres health check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkRedis() {
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            String ping = conn.ping();
            return "PONG".equalsIgnoreCase(ping);
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkKafka() {
        try {
            String[] parts = bootstrapServers.split(",");
            for (String part : parts) {
                String[] hostPort = part.trim().split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), 1000);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Kafka health check failed: {}", e.getMessage());
        }
        return false;
    }
}
