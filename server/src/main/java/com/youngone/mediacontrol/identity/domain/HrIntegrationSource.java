package com.youngone.mediacontrol.identity.domain;

import com.youngone.mediacontrol.identity.integration.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="hr_integration_sources")
public class HrIntegrationSource {
 @Id private UUID id; @Column(nullable=false,length=150)private String name;
 @Enumerated(EnumType.STRING)@Column(name="connector_type",nullable=false,length=40)private HrConnectorType connectorType;
 @Enumerated(EnumType.STRING)@Column(name="sync_mode",nullable=false,length=20)private SyncMode syncMode;
 @Column(name="authority_rank",nullable=false)private int authorityRank;@Column(nullable=false)private boolean enabled;
 @Lob @Column(name="config_json",nullable=false,columnDefinition="LONGTEXT")private String configJson;
 @Column(name="secret_ref",length=300)private String secretRef;@Column(name="created_at",nullable=false)private Instant createdAt;
 @Column(name="updated_at",nullable=false)private Instant updatedAt;@Column(name="created_by",nullable=false,length=100)private String createdBy;
 protected HrIntegrationSource(){}
 public HrIntegrationSource(UUID id,String name,HrConnectorType type,SyncMode mode,int rank,String configJson,String secretRef,String actor,Instant now){this.id=id;this.name=name;this.connectorType=type;this.syncMode=mode;this.authorityRank=rank;this.enabled=true;this.configJson=configJson;this.secretRef=secretRef;this.createdAt=now;this.updatedAt=now;this.createdBy=actor;}
 public UUID getId(){return id;}public HrConnectorType getConnectorType(){return connectorType;}public SyncMode getSyncMode(){return syncMode;}public boolean isEnabled(){return enabled;}
}