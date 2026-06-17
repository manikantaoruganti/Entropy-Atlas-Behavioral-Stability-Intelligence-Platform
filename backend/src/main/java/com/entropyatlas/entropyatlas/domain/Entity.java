package com.entropyatlas.entropyatlas.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "entities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Convenience constructor used by the data generator.
     */
    public Entity(String id, EntityType entityType) {
        this.id = id;
        this.entityType = entityType;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public enum EntityType {
        USER,
        SERVICE,
        DEVICE,
        MERCHANT,
        REGION,
        API_CLIENT,
        DATACENTER,
        PARTNER
    }
}
// package com.entropyatlas.entropyatlas.domain;

// import jakarta.persistence.*;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// import java.time.Instant;

// @jakarta.persistence.Entity
// @Table(name = "entities")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class Entity {
//     @Id
//     private String id;

//     @Enumerated(EnumType.STRING)
//     private EntityType entityType;

//     private Instant createdAt;
//     private Instant updatedAt;

//     public enum EntityType {
//         USER, SERVICE, DEVICE, MERCHANT, REGION, API_CLIENT, DATACENTER, PARTNER
//     }
// }
