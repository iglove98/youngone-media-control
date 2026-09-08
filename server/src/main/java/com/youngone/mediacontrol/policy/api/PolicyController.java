package com.youngone.mediacontrol.policy.api;
import com.youngone.mediacontrol.policy.application.PolicyService;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.net.URI;import java.util.UUID;
@RestController @RequestMapping("/api/v1")public class PolicyController{
 private final PolicyService service;public PolicyController(PolicyService s){service=s;}
 @PostMapping("/policies")ResponseEntity<PolicyModels.Envelope>create(@RequestHeader("X-Admin-Id")String actor,@Valid @RequestBody PolicyModels.Create r){var p=service.create(r,actor);return ResponseEntity.created(URI.create("/api/v1/policies/"+p.getId())).body(service.envelope(p));}
 @PostMapping("/policies/{id}/approve")PolicyModels.Envelope approve(@PathVariable UUID id,@RequestHeader("X-Admin-Id")String actor){return service.envelope(service.approve(id,actor));}
 @PostMapping("/policy-assignments")ResponseEntity<Void>assign(@RequestHeader("X-Admin-Id")String actor,@Valid @RequestBody PolicyModels.AssignmentCreate r){service.assign(r,actor);return ResponseEntity.status(HttpStatus.CREATED).build();}
 @PostMapping("/policy-deployments")PolicyModels.DeploymentResponse deploy(@RequestHeader("X-Admin-Id")String actor,@Valid @RequestBody PolicyModels.DeploymentCreate r){var d=service.deploy(r,actor);return new PolicyModels.DeploymentResponse(d.getId(),d.getPolicyId(),d.getPreviousPolicyId(),d.getRolloutPercentage(),d.getStatus());}
 @PostMapping("/policy-deployments/{id}/rollback")PolicyModels.DeploymentResponse rollback(@PathVariable UUID id,@RequestHeader("X-Admin-Id")String actor){var d=service.rollback(id);return new PolicyModels.DeploymentResponse(d.getId(),d.getPolicyId(),d.getPreviousPolicyId(),d.getRolloutPercentage(),d.getStatus());}
}
