package com.youngone.mediacontrol.agent.api;
import com.youngone.mediacontrol.agent.application.AgentUninstallService;import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;import java.util.UUID;
@RestController @RequestMapping("/api/v1/admin/agents")
public class AgentUninstallController{
 private final AgentUninstallService service;public AgentUninstallController(AgentUninstallService service){this.service=service;}
 @PostMapping("/{agentId}/uninstall-tokens")public AgentUninstallModels.IssueResponse issue(@PathVariable UUID agentId,@Valid @RequestBody AgentUninstallModels.IssueRequest request,@RequestHeader("X-Admin-Id")String actor){return service.issue(agentId,request,actor);}
}
