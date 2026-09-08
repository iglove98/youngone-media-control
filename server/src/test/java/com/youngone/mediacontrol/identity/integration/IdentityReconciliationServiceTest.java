package com.youngone.mediacontrol.identity.integration;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class IdentityReconciliationServiceTest {
    private final IdentityReconciliationService service = new IdentityReconciliationService();
    @Test void externalIdReboundRequiresReview() {
        var incoming = identity("external-1", "person-new");
        var existing = new IdentityReconciliationService.ExistingIdentity(UUID.randomUUID(), "person-old", EmploymentStatus.ACTIVE);
        assertThat(service.decide(incoming, existing, List.of()).disposition())
            .isEqualTo(IdentityReconciliationService.Disposition.REVIEW_REQUIRED);
    }
    @Test void uniqueImmutableKeyLinksAnotherSource() {
        UUID personId = UUID.randomUUID();
        var existing = new IdentityReconciliationService.ExistingIdentity(personId, "person-1", EmploymentStatus.ACTIVE);
        var decision = service.decide(identity("external-2", "person-1"), null, List.of(existing));
        assertThat(decision.disposition()).isEqualTo(IdentityReconciliationService.Disposition.LINK_EXTERNAL_IDENTITY);
        assertThat(decision.personId()).isEqualTo(personId);
    }
    private NormalizedIdentity identity(String externalId, String immutableKey) {
        return new NormalizedIdentity(externalId, immutableKey, null, null, null, null, null, "User",
            EmploymentStatus.ACTIVE, null, null, Set.of(), null, null, "1", "hash");
    }
}