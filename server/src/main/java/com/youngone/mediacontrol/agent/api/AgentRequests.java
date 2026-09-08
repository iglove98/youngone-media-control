package com.youngone.mediacontrol.agent.api;
import com.youngone.mediacontrol.agent.domain.ControlStatus;
import com.youngone.mediacontrol.agent.domain.*;import jakarta.validation.constraints.*;
public final class AgentRequests{
 private AgentRequests(){}
 public record Register(@NotBlank String bootstrapToken,@NotBlank @Size(max=100)String installationId,@NotBlank @Size(max=255)String hostname,@NotBlank @Size(max=50)String agentVersion,@NotBlank @Size(max=255)String osVersion,@NotNull EnvironmentType environmentType,@Size(max=64)String deviceEvidenceHash,@Size(max=64)String tpmEkHash,@Size(max=150)String vdiProviderId,@Size(max=100)String organizationDeviceId,@NotBlank @Size(max=100)String bootSessionId){}
 public record Heartbeat(@NotBlank @Size(max=100)String eventId,@NotBlank @Size(max=50)String agentVersion,@NotBlank @Size(max=255)String osVersion,@PositiveOrZero long queueDepth,@Size(max=100)String policyVersion,@NotNull ControlStatus controlStatus){}
}
