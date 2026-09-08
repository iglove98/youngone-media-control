package com.youngone.mediacontrol.agent.application;
import com.youngone.mediacontrol.agent.domain.*;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;
class IdentityDecisionServiceTest{private final IdentityDecisionService service=new IdentityDecisionService();
 @Test void rejectsSameInstallationOnDifferentStrongEvidence(){assertThat(service.decide(EnvironmentType.PHYSICAL,true,false,false)).isEqualTo(IdentityDisposition.DUPLICATE_REJECTED);}
 @Test void classifiesReinstallWhenEvidenceAlreadyExists(){assertThat(service.decide(EnvironmentType.PHYSICAL,false,true,false)).isEqualTo(IdentityDisposition.REINSTALL_CANDIDATE);}
 @Test void nonPersistentVdiIsAlwaysEphemeralWhenNew(){assertThat(service.decide(EnvironmentType.NON_PERSISTENT_VDI,false,true,false)).isEqualTo(IdentityDisposition.EPHEMERAL_NEW);}
}
