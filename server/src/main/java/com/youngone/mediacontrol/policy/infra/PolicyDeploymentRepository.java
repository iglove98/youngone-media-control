package com.youngone.mediacontrol.policy.infra;
import com.youngone.mediacontrol.policy.domain.*;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface PolicyDeploymentRepository extends JpaRepository<PolicyDeployment,UUID>{Optional<PolicyDeployment>findFirstByStatusInOrderByStartedAtDesc(Collection<DeploymentStatus>statuses);}
