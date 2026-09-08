package com.youngone.mediacontrol.policy.infra;
import com.youngone.mediacontrol.policy.domain.*;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface PolicyAssignmentRepository extends JpaRepository<PolicyAssignment,UUID>{List<PolicyAssignment>findByEnabledTrueAndTargetTypeAndTargetId(AssignmentTargetType type,String targetId);List<PolicyAssignment>findByEnabledTrueAndTargetType(AssignmentTargetType type);}
