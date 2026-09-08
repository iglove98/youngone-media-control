package com.youngone.mediacontrol.agent.api;
import com.youngone.mediacontrol.agent.domain.*;import java.time.Instant;import java.util.UUID;
public record AgentResponse(UUID agentId,String installationId,String status,ConnectionStatus connectionStatus,ControlStatus controlStatus,SecurityStatus securityStatus,IdentityDisposition identityDisposition,boolean ephemeral,Instant registeredAt,Instant lastSeenAt,String agentKey,boolean duplicate){
 public static AgentResponse registered(Agent a,String key){return of(a,key,false);}public static AgentResponse heartbeat(Agent a,boolean duplicate){return of(a,null,duplicate);}private static AgentResponse of(Agent a,String key,boolean duplicate){return new AgentResponse(a.getId(),a.getInstallationId(),a.getStatus(),a.getConnectionStatus(),a.getControlStatus(),a.getSecurityStatus(),a.getIdentityDisposition(),a.isEphemeral(),a.getRegisteredAt(),a.getLastSeenAt(),key,duplicate);}
}
