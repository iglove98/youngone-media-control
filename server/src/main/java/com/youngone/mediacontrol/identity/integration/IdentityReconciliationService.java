package com.youngone.mediacontrol.identity.integration;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class IdentityReconciliationService {
    public Decision decide(NormalizedIdentity incoming, ExistingIdentity existingByExternalId,
                           List<ExistingIdentity> existingByImmutableKey) {
        if (incoming.externalObjectId() == null || incoming.externalObjectId().isBlank())
            return new Decision(Disposition.REJECTED, null, "MISSING_EXTERNAL_OBJECT_ID");
        if (incoming.immutablePersonKey() == null || incoming.immutablePersonKey().isBlank())
            return new Decision(Disposition.REVIEW_REQUIRED, null, "MISSING_IMMUTABLE_PERSON_KEY");
        if (existingByExternalId != null) {
            if (!Objects.equals(existingByExternalId.immutablePersonKey(), incoming.immutablePersonKey()))
                return new Decision(Disposition.REVIEW_REQUIRED, existingByExternalId.personId(), "EXTERNAL_ID_REBOUND");
            return new Decision(Disposition.UPDATE_EXISTING, existingByExternalId.personId(), "EXTERNAL_ID_MATCH");
        }
        var candidates = existingByImmutableKey == null ? List.<ExistingIdentity>of() : existingByImmutableKey;
        if (candidates.size() > 1)
            return new Decision(Disposition.REVIEW_REQUIRED, null, "DUPLICATE_IMMUTABLE_PERSON_KEY");
        if (candidates.size() == 1)
            return new Decision(Disposition.LINK_EXTERNAL_IDENTITY, candidates.getFirst().personId(), "IMMUTABLE_KEY_MATCH");
        return new Decision(Disposition.CREATE_PERSON, null, "NEW_IMMUTABLE_KEY");
    }
    public enum Disposition { CREATE_PERSON, UPDATE_EXISTING, LINK_EXTERNAL_IDENTITY, REVIEW_REQUIRED, REJECTED }
    public record ExistingIdentity(UUID personId, String immutablePersonKey, EmploymentStatus status) {}
    public record Decision(Disposition disposition, UUID personId, String reasonCode) {}
}