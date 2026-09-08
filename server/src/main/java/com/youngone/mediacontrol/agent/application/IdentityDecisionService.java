package com.youngone.mediacontrol.agent.application;
import com.youngone.mediacontrol.agent.domain.*;import org.springframework.stereotype.Service;
@Service public class IdentityDecisionService{
 public IdentityDisposition decide(EnvironmentType environment,boolean sameInstallation,boolean strongEvidenceMatches,boolean bootSessionMatches){
  if(sameInstallation&&!strongEvidenceMatches)return IdentityDisposition.DUPLICATE_REJECTED;
  if(sameInstallation&&!bootSessionMatches)return IdentityDisposition.EXISTING;
  if(sameInstallation)return IdentityDisposition.EXISTING;
  if(environment==EnvironmentType.NON_PERSISTENT_VDI)return IdentityDisposition.EPHEMERAL_NEW;
  if(strongEvidenceMatches)return IdentityDisposition.REINSTALL_CANDIDATE;
  return IdentityDisposition.NEW;
 }
}
