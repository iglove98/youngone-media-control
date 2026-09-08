package com.youngone.mediacontrol.risk.application;

import com.youngone.mediacontrol.risk.domain.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringServiceTest {
    private final RiskScoringService scoring = new RiskScoringService();
    @Test void repeatedBlockedAttemptsEscalateToCritical() {
        var first = scoring.score(EnforcementDecision.BLOCK, MediaOperation.WRITE, 0);
        var repeated = scoring.score(EnforcementDecision.BLOCK, MediaOperation.WRITE, 4);
        assertThat(first.value()).isEqualTo(40);
        assertThat(repeated.value()).isEqualTo(80);
        assertThat(repeated.severity()).isEqualTo(RiskSeverity.CRITICAL);
        assertThat(repeated.attemptCount24h()).isEqualTo(5);
    }
    @Test void riskScoreNeverExceedsOneHundred() {
        assertThat(scoring.score(EnforcementDecision.BLOCK, MediaOperation.WRITE, 100).value()).isEqualTo(90);
    }
}