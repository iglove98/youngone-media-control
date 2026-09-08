package com.youngone.mediacontrol.risk.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_appeals")
public class RiskAppeal {
    @Id private UUID id;
    @Column(name = "risk_event_id", nullable = false) private UUID riskEventId;
    @Column(name = "user_id", nullable = false, length = 150) private String userId;
    @Column(nullable = false, length = 2000) private String statement;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected RiskAppeal() {}
    public RiskAppeal(UUID id, UUID riskEventId, String userId, String statement, Instant createdAt) {
        this.id = id; this.riskEventId = riskEventId; this.userId = userId;
        this.statement = statement; this.createdAt = createdAt;
    }
}