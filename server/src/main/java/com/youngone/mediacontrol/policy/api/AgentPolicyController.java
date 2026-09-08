package com.youngone.mediacontrol.policy.api;
import com.youngone.mediacontrol.agent.application.AgentService;import com.youngone.mediacontrol.policy.application.PolicyService;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.UUID;
@RestController @RequestMapping("/api/v1/agents/{agentId}/policy")public class AgentPolicyController{
 private final AgentService agents;private final PolicyService policies;public AgentPolicyController(AgentService a,PolicyService p){agents=a;policies=p;}
 @GetMapping ResponseEntity<PolicyModels.Envelope>active(@PathVariable UUID agentId,@RequestHeader("X-Agent-Key")String key,@RequestHeader(value="If-None-Match",required=false)String ifNoneMatch){agents.authenticate(agentId,key);var body=policies.resolveForAgent(agentId);String etag="\"policy-"+body.policyVersion()+"-"+body.payloadHash()+"\"";if(etag.equals(ifNoneMatch))return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();return ResponseEntity.ok().eTag(etag).cacheControl(CacheControl.noCache()).body(body);}
}
