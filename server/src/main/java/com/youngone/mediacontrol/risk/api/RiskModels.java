package com.youngone.mediacontrol.risk.api;

import com.youngone.mediacontrol.policy.domain.MediaType;
import com.youngone.mediacontrol.risk.domain.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class RiskModels {
    private RiskModels() {}
    public record Create(
        @NotBlank @Size(max = 100) String eventId,
        @Pattern(regexp = "[0-9a-fA-F]{64}") String payloadHash,
        @Size(max = 150) String userId,
        @Size(max = 300) String deviceInstanceId,
        @Pattern(regexp = "[0-9a-fA-F]{64}") String serialHash,
        @NotNull MediaType mediaType,
        @NotNull MediaOperation operation,
        @NotNull EnforcementDecision decision,
        @NotBlank @Size(max = 100) String reasonCode,
        UUID policyId,
        @PositiveOrZero Long policyVersion,
        boolean enforcementApplied,
        @NotBlank @Size(max = 100) String enforcementResultCode,
        boolean popupShown,
        @NotNull Instant occurredAt) {}
    public record Response(UUID riskEventId, int riskScore, RiskSeverity severity, int attemptCount24h,
                           RiskStatus status, boolean duplicate, boolean appealRecommended) {}
    public record Outbound(UUID riskEventId, UUID agentId, int riskScore, RiskSeverity severity,
                           int attemptCount24h, Create source) {}
    public record AdminResponse(UUID riskEventId, UUID agentId, String userId, String deviceInstanceId,
                                String serialHash, MediaType mediaType, MediaOperation operation,
                                EnforcementDecision decision, String reasonCode, UUID policyId,
                                Long policyVersion, boolean enforcementApplied, String enforcementResultCode,
                                boolean popupShown, int riskScore, RiskSeverity severity,
                                int attemptCount24h, RiskStatus status, Instant occurredAt) {}
    public record Appeal(@NotBlank @Size(max = 150) String userId, @NotBlank @Size(max = 2000) String statement) {}
}