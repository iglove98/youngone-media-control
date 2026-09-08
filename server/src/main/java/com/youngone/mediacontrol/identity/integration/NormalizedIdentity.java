package com.youngone.mediacontrol.identity.integration;

import java.time.Instant;
import java.util.*;

public record NormalizedIdentity(
    String externalObjectId,
    String immutablePersonKey,
    String employeeNumber,
    String loginId,
    String windowsSid,
    String upn,
    String email,
    String displayName,
    EmploymentStatus employmentStatus,
    String organizationExternalId,
    String managerImmutableKey,
    Set<String> groupExternalIds,
    Instant effectiveFrom,
    Instant effectiveTo,
    String sourceVersion,
    String payloadHash) {
    public NormalizedIdentity {
        groupExternalIds = groupExternalIds == null ? Set.of() : Set.copyOf(groupExternalIds);
    }
}