package com.youngone.mediacontrol.identity.integration;

import java.util.List;

public record HrChangePage(List<NormalizedIdentity> identities, String nextCursor, boolean hasMore) {
    public HrChangePage { identities = List.copyOf(identities); }
}