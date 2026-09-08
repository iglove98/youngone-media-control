package com.youngone.mediacontrol.agent.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="agents",uniqueConstraints=@UniqueConstraint(name="uk_agents_installation_id",columnNames="installation_id"))
public class Agent {
 @Id private UUID id;
 @Column(name="installation_id",nullable=false,updatable=false,length=100)private String installationId;
 @Column(nullable=false)private String hostname;
 @Column(name="agent_version",nullable=false,length=50)private String agentVersion;
 @Column(name="os_version",nullable=false)private String osVersion;
 @Column(name="ip_address",length=45)private String ipAddress;
 @Column(nullable=false,length=20)private String status;
 @Column(name="credential_hash",nullable=false,length=64)private String credentialHash;
 @Enumerated(EnumType.STRING)@Column(name="environment_type",nullable=false,length=30)private EnvironmentType environmentType;
 @Column(name="device_evidence_hash",length=64)private String deviceEvidenceHash;
 @Column(name="tpm_ek_hash",length=64)private String tpmEkHash;
 @Column(name="vdi_provider_id",length=150)private String vdiProviderId;
 @Column(name="organization_device_id",length=100)private String organizationDeviceId;
 @Column(name="boot_session_id",nullable=false,length=100)private String bootSessionId;
 @Enumerated(EnumType.STRING)@Column(name="connection_status",nullable=false,length=20)private ConnectionStatus connectionStatus;
 @Enumerated(EnumType.STRING)@Column(name="control_status",nullable=false,length=20)private ControlStatus controlStatus;
 @Enumerated(EnumType.STRING)@Column(name="security_status",nullable=false,length=30)private SecurityStatus securityStatus;
 @Enumerated(EnumType.STRING)@Column(name="identity_disposition",nullable=false,length=30)private IdentityDisposition identityDisposition;
 @Column(nullable=false)private boolean ephemeral;
 @Column(name="registered_at",nullable=false)private Instant registeredAt;
 @Column(name="last_seen_at",nullable=false)private Instant lastSeenAt;
 @Version private long version;
 protected Agent(){}
 public Agent(UUID id,String installationId,String hostname,String agentVersion,String osVersion,String ipAddress,String credentialHash,Instant now){this(id,installationId,hostname,agentVersion,osVersion,ipAddress,credentialHash,EnvironmentType.PHYSICAL,"legacy",null,null,null,"legacy-boot",IdentityDisposition.NEW,now);}
 public Agent(UUID id,String installationId,String hostname,String agentVersion,String osVersion,String ipAddress,String credentialHash,EnvironmentType environmentType,String deviceEvidenceHash,String tpmEkHash,String vdiProviderId,String organizationDeviceId,String bootSessionId,IdentityDisposition disposition,Instant now){
  this.id=id;this.installationId=installationId;this.hostname=hostname;this.agentVersion=agentVersion;this.osVersion=osVersion;this.ipAddress=ipAddress;this.credentialHash=credentialHash;this.environmentType=environmentType;this.deviceEvidenceHash=deviceEvidenceHash;this.tpmEkHash=tpmEkHash;this.vdiProviderId=vdiProviderId;this.organizationDeviceId=organizationDeviceId;this.bootSessionId=bootSessionId;this.identityDisposition=disposition;this.ephemeral=environmentType==EnvironmentType.NON_PERSISTENT_VDI;this.connectionStatus=ConnectionStatus.ONLINE;this.controlStatus=ControlStatus.UNKNOWN;this.securityStatus=disposition==IdentityDisposition.REINSTALL_CANDIDATE?SecurityStatus.QUARANTINED:SecurityStatus.NORMAL;this.status=connectionStatus.name();this.registeredAt=now;this.lastSeenAt=now;
 }
 public void heartbeat(String av,String os,String ip,Instant now){heartbeat(av,os,ip,controlStatus,now);}
 public void heartbeat(String av,String os,String ip,ControlStatus control,Instant now){agentVersion=av;osVersion=os;ipAddress=ip;controlStatus=control;lastSeenAt=now;connectionStatus=ConnectionStatus.ONLINE;status=connectionStatus.name();}
 public void refreshIdentity(String bootSession,String credentialHash,String av,String os,String ip,Instant now){this.bootSessionId=bootSession;this.credentialHash=credentialHash;heartbeat(av,os,ip,now);this.identityDisposition=IdentityDisposition.EXISTING;}
 public void markDuplicateSuspected(){securityStatus=SecurityStatus.DUPLICATE_SUSPECTED;identityDisposition=IdentityDisposition.DUPLICATE_REJECTED;}
 public void evaluateConnection(Instant now,long heartbeatSeconds,long staleSeconds){long age=java.time.Duration.between(lastSeenAt,now).getSeconds();connectionStatus=age<=heartbeatSeconds*2?ConnectionStatus.ONLINE:age<=heartbeatSeconds*3?ConnectionStatus.DEGRADED:age<staleSeconds?ConnectionStatus.OFFLINE:ConnectionStatus.STALE;status=connectionStatus.name();}
 public void rotateCredential(String hash){credentialHash=hash;}public boolean hasCredentialHash(String hash){return credentialHash.equals(hash);}
 public UUID getId(){return id;}public String getInstallationId(){return installationId;}public String getStatus(){return status;}public Instant getRegisteredAt(){return registeredAt;}public Instant getLastSeenAt(){return lastSeenAt;}public String getDeviceEvidenceHash(){return deviceEvidenceHash;}public String getBootSessionId(){return bootSessionId;}public EnvironmentType getEnvironmentType(){return environmentType;}public ConnectionStatus getConnectionStatus(){return connectionStatus;}public ControlStatus getControlStatus(){return controlStatus;}public SecurityStatus getSecurityStatus(){return securityStatus;}public IdentityDisposition getIdentityDisposition(){return identityDisposition;}public boolean isEphemeral(){return ephemeral;}
}
