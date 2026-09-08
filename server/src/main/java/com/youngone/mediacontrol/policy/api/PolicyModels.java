package com.youngone.mediacontrol.policy.api;

import com.youngone.mediacontrol.policy.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class PolicyModels {
    private PolicyModels() {}
    public record Rule(
        @NotNull MediaType mediaType,
        @Size(max = 4) String vendorId,
        @Size(max = 4) String productId,
        @Pattern(regexp = "[0-9a-fA-F]{64}") String serialHash,
        @NotNull PolicyAction readAction,
        @NotNull PolicyAction writeAction,
        @NotNull PolicyAction executeAction,
        @NotNull PolicyAction expiredOfflineAction,
        @NotNull OfflineBehavior offlineBehavior,
        @PositiveOrZero long offlineGraceSeconds,
        @Size(max = 200) String reasonCode) {
        public String selectorKey() {
            return mediaType + "|" + normalize(vendorId) + "|" + normalize(productId) + "|" + normalize(serialHash);
        }
        private static String normalize(String value) { return value == null ? "*" : value.toUpperCase(Locale.ROOT); }
    }
    public record Create(@NotBlank @Size(max = 150) String name, @NotNull Instant effectiveAt,
                         Instant expiresAt, @NotEmpty List<@Valid Rule> rules) {}
    public record AssignmentCreate(@NotNull UUID policyId, @NotNull AssignmentTargetType targetType,
                                   @Size(max = 150) String targetId, @Min(0) @Max(10000) int priority) {}
    public record DeploymentCreate(@NotNull UUID policyId, @Min(1) @Max(100) int rolloutPercentage) {}
    public record Payload(long schemaVersion, long policyVersion, List<Rule> rules, Instant effectiveAt, Instant expiresAt) {}
    public record Envelope(UUID policyId, long policyVersion, String payloadJson, String payloadHash, String signature,
                           String signingKeyId, String publicKey, Instant effectiveAt, Instant expiresAt) {}
    public record DeploymentResponse(UUID deploymentId, UUID policyId, UUID previousPolicyId,
                                     int rolloutPercentage, DeploymentStatus status) {}
}