package com.youngone.mediacontrol.risk.domain;

import com.youngone.mediacontrol.policy.domain.MediaType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_events", uniqueConstraints = @UniqueConstraint(name = "uk_risk_agent_event", columnNames = {"agent_id", "event_id"}), indexes = {
    @Index(name = "ix_risk_open_score", columnList = "status,risk_score,occurred_at"),
    @Index(name = "ix_risk_subject_time", columnList = "agent_id,user_id,device_instance_id,occurred_at")
})
public class RiskEvent {
    @Id private UUID id;
    @Column(name = "agent_id", nullable = false) private UUID agentId;
    @Column(name = "event_id", nullable = false, length = 100) private String eventId;
    @Column(name = "payload_hash", nullable = false, length = 64) private String payloadHash;
    @Column(name = "user_id", length = 150) private String userId;
    @Column(name = "device_instance_id", length = 300) private String deviceInstanceId;
    @Column(name = "serial_hash", length = 64) private String serialHash;
    @Enumerated(EnumType.STRING) @Column(name = "media_type", nullable = false, length = 40) private MediaType mediaType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private MediaOperation operation;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EnforcementDecision decision;
    @Column(name = "reason_code", nullable = false, length = 100) private String reasonCode;
    @Column(name = "policy_id") private UUID policyId;
    @Column(name = "policy_version") private Long policyVersion;
    @Column(name = "enforcement_applied", nullable = false) private boolean enforcementApplied;
    @Column(name = "enforcement_result_code", nullable = false, length = 100) private String enforcementResultCode;
    @Column(name = "popup_shown", nullable = false) private boolean popupShown;
    @Column(name = "attempt_count_24h", nullable = false) private int attemptCount24h;
    @Column(name = "risk_score", nullable = false) private int riskScore;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RiskSeverity severity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RiskStatus status;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;
    @Column(name = "handled_by", length = 100) private String handledBy;
    @Column(name = "handled_at") private Instant handledAt;

    protected RiskEvent() {}

    public RiskEvent(UUID id, UUID agentId, String eventId, String payloadHash, String userId,
                     String deviceInstanceId, String serialHash, MediaType mediaType,
                     MediaOperation operation, EnforcementDecision decision, String reasonCode,
                     UUID policyId, Long policyVersion, boolean enforcementApplied, String enforcementResultCode,
                     boolean popupShown, int attemptCount24h,
                     int riskScore, RiskSeverity severity, Instant occurredAt, Instant receivedAt) {
        this.id = id; this.agentId = agentId; this.eventId = eventId; this.payloadHash = payloadHash;
        this.userId = userId; this.deviceInstanceId = deviceInstanceId; this.serialHash = serialHash;
        this.mediaType = mediaType; this.operation = operation; this.decision = decision;
        this.reasonCode = reasonCode; this.policyId = policyId; this.policyVersion = policyVersion;
        this.enforcementApplied = enforcementApplied; this.enforcementResultCode = enforcementResultCode;
        this.popupShown = popupShown; this.attemptCount24h = attemptCount24h; this.riskScore = riskScore;
        this.severity = severity; this.status = RiskStatus.OPEN; this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
    }

    public void acknowledge(String actor, Instant at) { status = RiskStatus.ACKNOWLEDGED; handledBy = actor; handledAt = at; }
    public void requestAppeal() { status = RiskStatus.APPEAL_PENDING; }
    public boolean samePayload(String hash) { return payloadHash.equals(hash); }
    public UUID getId() { return id; }
    public UUID getAgentId() { return agentId; }
    public String getDeviceInstanceId() { return deviceInstanceId; }
    public String getSerialHash() { return serialHash; }
    public MediaType getMediaType() { return mediaType; }
    public MediaOperation getOperation() { return operation; }
    public EnforcementDecision getDecision() { return decision; }
    public String getReasonCode() { return reasonCode; }
    public UUID getPolicyId() { return policyId; }
    public Long getPolicyVersion() { return policyVersion; }
    public boolean isEnforcementApplied() { return enforcementApplied; }
    public String getEnforcementResultCode() { return enforcementResultCode; }
    public boolean isPopupShown() { return popupShown; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getUserId() { return userId; }
    public int getRiskScore() { return riskScore; }
    public int getAttemptCount24h() { return attemptCount24h; }
    public RiskSeverity getSeverity() { return severity; }
    public RiskStatus getStatus() { return status; }
}