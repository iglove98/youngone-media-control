package com.youngone.mediacontrol.policy.application;
import com.youngone.mediacontrol.policy.domain.*;import org.junit.jupiter.api.Test;import java.time.Instant;import java.util.UUID;import static org.assertj.core.api.Assertions.*;
class PolicyWorkflowTest{
 @Test void creatorCannotApproveOwnPolicy(){Policy p=policy("writer");assertThatThrownBy(()->p.approve("writer",Instant.now())).isInstanceOf(IllegalArgumentException.class);}
 @Test void differentAdministratorCanApprove(){Policy p=policy("writer");p.approve("approver",Instant.now());assertThat(p.getStatus()).isEqualTo(PolicyStatus.APPROVED);assertThat(p.getApprovedBy()).isEqualTo("approver");}
 @Test void canaryBucketIsDeterministic(){PolicyDeployment d=new PolicyDeployment(UUID.randomUUID(),UUID.randomUUID(),null,10,"admin",Instant.now());UUID agent=UUID.randomUUID();assertThat(d.includes(agent)).isEqualTo(d.includes(agent));}
 private Policy policy(String creator){return new Policy(UUID.randomUUID(),"p",1,"{}","hash","sig","key",Instant.now(),null,creator,Instant.now());}
}
