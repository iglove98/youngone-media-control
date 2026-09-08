package com.youngone.mediacontrol.risk.infra;

import com.youngone.mediacontrol.risk.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.*;

public interface RiskEventRepository extends JpaRepository<RiskEvent, UUID> {
    Optional<RiskEvent> findByAgentIdAndEventId(UUID agentId, String eventId);
    long countByAgentIdAndUserIdAndDeviceInstanceIdAndOccurredAtAfter(UUID agentId, String userId, String deviceInstanceId, Instant since);
    List<RiskEvent> findTop100ByStatusInOrderByRiskScoreDescOccurredAtDesc(Collection<RiskStatus> statuses);
}