package com.youngone.mediacontrol.risk.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_outbox", uniqueConstraints = @UniqueConstraint(name = "uk_outbox_destination_event", columnNames = {"destination", "event_id"}))
public class IntegrationOutbox {
    @Id private UUID id;
    @Column(nullable = false, length = 50) private String destination;
    @Column(name = "event_id", nullable = false) private UUID eventId;
    @Column(name = "event_type", nullable = false, length = 100) private String eventType;
    @Lob @Column(name = "payload_json", nullable = false, columnDefinition = "LONGTEXT") private String payloadJson;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Column(name = "next_attempt_at", nullable = false) private Instant nextAttemptAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected IntegrationOutbox() {}
    public IntegrationOutbox(UUID eventId, String payloadJson, Instant now) {
        this.id = UUID.randomUUID(); this.destination = "SIEM"; this.eventId = eventId;
        this.eventType = "MEDIA_RISK_EVENT"; this.payloadJson = payloadJson; this.status = "PENDING";
        this.attemptCount = 0; this.nextAttemptAt = now; this.createdAt = now;
    }
}