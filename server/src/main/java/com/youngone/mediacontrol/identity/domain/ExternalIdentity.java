package com.youngone.mediacontrol.identity.domain;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="external_identities")public class ExternalIdentity{
 @Id private UUID id;@Column(name="source_id",nullable=false)private UUID sourceId;@Column(name="person_id",nullable=false)private UUID personId;
 @Column(name="external_object_id",nullable=false,length=300)private String externalObjectId;@Column(name="immutable_person_key",nullable=false,length=200)private String immutablePersonKey;
 @Column(name="source_version",length=200)private String sourceVersion;@Column(name="payload_hash",nullable=false,length=64)private String payloadHash;
 @Column(name="first_seen_at",nullable=false)private Instant firstSeenAt;@Column(name="last_seen_at",nullable=false)private Instant lastSeenAt;
 protected ExternalIdentity(){}public ExternalIdentity(UUID sourceId,UUID personId,String externalId,String immutableKey,String sourceVersion,String hash,Instant now){id=UUID.randomUUID();this.sourceId=sourceId;this.personId=personId;externalObjectId=externalId;immutablePersonKey=immutableKey;this.sourceVersion=sourceVersion;payloadHash=hash;firstSeenAt=now;lastSeenAt=now;}
 public void touch(String version,String hash,Instant now){sourceVersion=version;payloadHash=hash;lastSeenAt=now;}public UUID getPersonId(){return personId;}public String getImmutablePersonKey(){return immutablePersonKey;}public String getPayloadHash(){return payloadHash;}
}