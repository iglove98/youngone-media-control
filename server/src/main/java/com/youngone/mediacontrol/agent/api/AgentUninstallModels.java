package com.youngone.mediacontrol.agent.api;
import jakarta.validation.constraints.*;import java.time.Instant;import java.util.UUID;
public final class AgentUninstallModels{
 private AgentUninstallModels(){}
 public record IssueRequest(@NotBlank @Size(max=500)String reason,@Min(1)@Max(60)int validMinutes){}
 public record TokenPayload(int schemaVersion,UUID agentId,String installationId,String nonce,Instant issuedAt,Instant expiresAt,String reason,String approvedBy){}
 public record TokenEnvelope(String payloadJson,String payloadHash,String signature,String signingKeyId){}
 public record IssueResponse(UUID tokenId,UUID agentId,String installationId,Instant issuedAt,Instant expiresAt,String status,TokenEnvelope token){}
}
