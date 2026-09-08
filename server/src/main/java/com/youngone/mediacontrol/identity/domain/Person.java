package com.youngone.mediacontrol.identity.domain;

import com.youngone.mediacontrol.identity.integration.*;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="persons") public class Person{
 @Id private UUID id;@Column(name="authoritative_person_key",nullable=false,length=200)private String authoritativePersonKey;
 @Column(name="employee_number",length=100)private String employeeNumber;@Column(name="login_id",length=150)private String loginId;
 @Column(name="windows_sid",length=200)private String windowsSid;@Column(length=254)private String upn;@Column(length=254)private String email;
 @Column(name="display_name",length=200)private String displayName;@Enumerated(EnumType.STRING)@Column(name="employment_status",nullable=false,length=20)private EmploymentStatus status;
 @Column(name="organization_external_id",length=200)private String organizationExternalId;@Column(name="manager_person_key",length=200)private String managerPersonKey;
 @Column(name="effective_from")private Instant effectiveFrom;@Column(name="effective_to")private Instant effectiveTo;
 @Column(name="created_at",nullable=false)private Instant createdAt;@Column(name="updated_at",nullable=false)private Instant updatedAt;
 protected Person(){}public Person(UUID id,NormalizedIdentity n,Instant now){this.id=id;this.authoritativePersonKey=n.immutablePersonKey();this.createdAt=now;apply(n,now);}
 public void apply(NormalizedIdentity n,Instant now){employeeNumber=n.employeeNumber();loginId=n.loginId();windowsSid=n.windowsSid();upn=n.upn();email=n.email();displayName=n.displayName();status=n.employmentStatus();organizationExternalId=n.organizationExternalId();managerPersonKey=n.managerImmutableKey();effectiveFrom=n.effectiveFrom();effectiveTo=n.effectiveTo();updatedAt=now;}
 public UUID getId(){return id;}public String getAuthoritativePersonKey(){return authoritativePersonKey;}public EmploymentStatus getStatus(){return status;}
}