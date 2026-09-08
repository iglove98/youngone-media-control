package com.youngone.mediacontrol.risk.api;

import com.youngone.mediacontrol.agent.application.AgentService;
import com.youngone.mediacontrol.risk.application.RiskEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents/{agentId}/risk-events")
public class AgentRiskController {
    private final AgentService agents;
    private final RiskEventService risks;
    public AgentRiskController(AgentService agents, RiskEventService risks) { this.agents = agents; this.risks = risks; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public RiskModels.Response create(@PathVariable UUID agentId, @RequestHeader("X-Agent-Key") String key,
                                      @Valid @RequestBody RiskModels.Create request) {
        agents.authenticate(agentId, key); return risks.record(agentId, request);
    }
    @PostMapping("/{eventId}/appeals") @ResponseStatus(HttpStatus.CREATED)
    public void appeal(@PathVariable UUID agentId, @PathVariable UUID eventId,
                       @RequestHeader("X-Agent-Key") String key, @Valid @RequestBody RiskModels.Appeal request) {
        agents.authenticate(agentId, key); risks.appeal(agentId, eventId, request);
    }
}