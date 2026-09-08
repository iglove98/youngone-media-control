package com.youngone.mediacontrol.agent.api;
import com.youngone.mediacontrol.agent.application.AgentService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.net.URI;import java.util.UUID;
@RestController @RequestMapping("/api/v1/agents")public class AgentController{
 private final AgentService service;public AgentController(AgentService s){service=s;}
 @PostMapping("/register")ResponseEntity<AgentResponse> register(@Valid @RequestBody AgentRequests.Register r,HttpServletRequest h){var result=service.register(r,h.getRemoteAddr());var body=AgentResponse.registered(result.agent(),result.key());if(result.conflict())return ResponseEntity.status(HttpStatus.CONFLICT).body(body);return ResponseEntity.created(URI.create("/api/v1/agents/"+body.agentId())).body(body);}
 @PostMapping("/{id}/heartbeat")AgentResponse heartbeat(@PathVariable UUID id,@RequestHeader("X-Agent-Key")String key,@Valid @RequestBody AgentRequests.Heartbeat r,HttpServletRequest h){var result=service.heartbeat(id,key,r,h.getRemoteAddr());return AgentResponse.heartbeat(result.agent(),result.duplicate());}
}
