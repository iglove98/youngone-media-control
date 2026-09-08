package com.youngone.mediacontrol.risk.application;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.youngone.mediacontrol.risk.api.RiskModels;
import com.youngone.mediacontrol.risk.domain.*;
import com.youngone.mediacontrol.risk.infra.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@Service
public class RiskEventService {
    private final RiskEventRepository events;
    private final RiskAppealRepository appeals;
    private final IntegrationOutboxRepository outbox;
    private final RiskScoringService scoring;
    private final ObjectMapper objectMapper;
    public RiskEventService(RiskEventRepository events, RiskAppealRepository appeals,
                            IntegrationOutboxRepository outbox, RiskScoringService scoring, ObjectMapper objectMapper) {
        this.events = events; this.appeals = appeals; this.outbox = outbox;
        this.scoring = scoring; this.objectMapper = objectMapper;
    }

    @Transactional
    public RiskModels.Response record(UUID agentId, RiskModels.Create request) {
        var existing = events.findByAgentIdAndEventId(agentId, request.eventId());
        if (existing.isPresent()) {
            if (!existing.get().samePayload(request.payloadHash()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "eventId payload mismatch");
            return response(existing.get(), true);
        }
        Instant now = Instant.now();
        long previous = events.countByAgentIdAndUserIdAndDeviceInstanceIdAndOccurredAtAfter(
            agentId, request.userId(), request.deviceInstanceId(), now.minus(Duration.ofHours(24)));
        var score = scoring.score(request.decision(), request.operation(), previous);
        var event = events.save(new RiskEvent(UUID.randomUUID(), agentId, request.eventId(), request.payloadHash(),
            request.userId(), request.deviceInstanceId(), request.serialHash(), request.mediaType(), request.operation(),
            request.decision(), request.reasonCode(), request.policyId(), request.policyVersion(), request.enforcementApplied(),
            request.enforcementResultCode(), request.popupShown(),
            score.attemptCount24h(), score.value(), score.severity(), request.occurredAt(), now));
        try { outbox.save(new IntegrationOutbox(event.getId(), objectMapper.writeValueAsString(new RiskModels.Outbound(event.getId(), agentId, score.value(), score.severity(), score.attemptCount24h(), request)), now)); }
        catch (JacksonException e) { throw new IllegalStateException("Cannot serialize risk event", e); }
        return response(event, false);
    }

    @Transactional
    public void appeal(UUID agentId, UUID eventId, RiskModels.Appeal request) {
        RiskEvent event = get(eventId);
        if (!event.getAgentId().equals(agentId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Risk event does not belong to agent");
        if (event.getUserId() != null && !event.getUserId().equals(request.userId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appeal user does not match event user");
        appeals.save(new RiskAppeal(UUID.randomUUID(), eventId, request.userId(), request.statement(), Instant.now()));
        event.requestAppeal();
    }

    @Transactional
    public void acknowledge(UUID eventId, String actor) { get(eventId).acknowledge(actor, Instant.now()); }
    @Transactional(readOnly = true)
    public List<RiskModels.AdminResponse> active() {
        return events.findTop100ByStatusInOrderByRiskScoreDescOccurredAtDesc(List.of(RiskStatus.OPEN, RiskStatus.APPEAL_PENDING)).stream()
            .map(e -> new RiskModels.AdminResponse(e.getId(), e.getAgentId(), e.getUserId(), e.getDeviceInstanceId(),
                e.getSerialHash(), e.getMediaType(), e.getOperation(), e.getDecision(), e.getReasonCode(), e.getPolicyId(),
                e.getPolicyVersion(), e.isEnforcementApplied(), e.getEnforcementResultCode(), e.isPopupShown(), e.getRiskScore(), e.getSeverity(), e.getAttemptCount24h(),
                e.getStatus(), e.getOccurredAt())).toList();
    }
    private RiskEvent get(UUID id) { return events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Risk event not found")); }
    private RiskModels.Response response(RiskEvent e, boolean duplicate) {
        return new RiskModels.Response(e.getId(), e.getRiskScore(), e.getSeverity(), e.getAttemptCount24h(),
            e.getStatus(), duplicate, e.getSeverity() == RiskSeverity.HIGH || e.getSeverity() == RiskSeverity.CRITICAL);
    }
}