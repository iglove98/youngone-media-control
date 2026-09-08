package com.youngone.mediacontrol.agent.domain;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="agent_identity_conflicts")public class AgentIdentityConflict{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(name="agent_id",nullable=false)private UUID agentId;@Column(name="installation_id",nullable=false,length=100)private String installationId;@Column(name="existing_evidence_hash",length=64)private String existingEvidenceHash;@Column(name="incoming_evidence_hash",length=64)private String incomingEvidenceHash;@Column(name="incoming_boot_session_id",length=100)private String incomingBootSessionId;@Column(name="detected_at",nullable=false)private Instant detectedAt;
 protected AgentIdentityConflict(){}public AgentIdentityConflict(UUID agentId,String installationId,String existing,String incoming,String boot,Instant at){this.agentId=agentId;this.installationId=installationId;this.existingEvidenceHash=existing;this.incomingEvidenceHash=incoming;this.incomingBootSessionId=boot;this.detectedAt=at;}
}
