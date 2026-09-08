package com.youngone.mediacontrol.risk.api;

import com.youngone.mediacontrol.risk.application.RiskEventService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/risk-events")
public class AdminRiskController {
    private final RiskEventService risks;
    public AdminRiskController(RiskEventService risks) { this.risks = risks; }
    @GetMapping public List<RiskModels.AdminResponse> active() { return risks.active(); }
    @PostMapping("/{eventId}/acknowledge") public void acknowledge(@PathVariable UUID eventId,
        @RequestHeader("X-Admin-Id") String actor) { risks.acknowledge(eventId, actor); }
}