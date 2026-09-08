package com.youngone.mediacontrol.policy.infra;
import com.youngone.mediacontrol.policy.domain.*;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface PolicyRepository extends JpaRepository<Policy,UUID>{Optional<Policy> findFirstByStatusOrderByVersionDesc(PolicyStatus status);Optional<Policy> findFirstByOrderByVersionDesc();}
