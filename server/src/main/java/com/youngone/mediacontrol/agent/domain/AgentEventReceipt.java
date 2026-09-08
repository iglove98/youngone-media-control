package com.youngone.mediacontrol.agent.domain;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="agent_event_receipts",uniqueConstraints=@UniqueConstraint(name="uk_agent_event_receipts",columnNames={"agent_id","event_id"}))
public class AgentEventReceipt{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;
 @Column(name="agent_id",nullable=false)private java.util.UUID agentId;
 @Column(name="event_id",nullable=false,length=100)private String eventId;
 @Column(name="event_type",nullable=false,length=40)private String eventType;
 @Column(name="payload_hash",nullable=false,length=64)private String payloadHash;
 @Column(name="received_at",nullable=false)private Instant receivedAt;
 protected AgentEventReceipt(){}public AgentEventReceipt(java.util.UUID agentId,String eventId,String payloadHash,Instant at){this.agentId=agentId;this.eventId=eventId;this.eventType="HEARTBEAT";this.payloadHash=payloadHash;this.receivedAt=at;}public boolean samePayload(String hash){return payloadHash.equals(hash);}
}
