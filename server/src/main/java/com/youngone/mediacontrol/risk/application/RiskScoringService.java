package com.youngone.mediacontrol.risk.application;

import com.youngone.mediacontrol.risk.domain.*;
import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {
    public Score score(EnforcementDecision decision, MediaOperation operation, long previousAttempts24h) {
        int base = switch (decision) {
            case BLOCK -> 40;
            case READ_ONLY -> operation == MediaOperation.WRITE ? 30 : 15;
            case DETECT_ONLY -> 20;
            case ALLOW -> 5;
        };
        int repetition = (int) Math.min(50, previousAttempts24h * 10);
        int score = Math.min(100, base + repetition);
        RiskSeverity severity = score >= 80 ? RiskSeverity.CRITICAL
            : score >= 60 ? RiskSeverity.HIGH
            : score >= 30 ? RiskSeverity.MEDIUM : RiskSeverity.LOW;
        return new Score(score, severity, Math.toIntExact(Math.min(Integer.MAX_VALUE, previousAttempts24h + 1)));
    }
    public record Score(int value, RiskSeverity severity, int attemptCount24h) {}
}