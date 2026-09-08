package com.youngone.mediacontrol.agent.infra;
import com.youngone.mediacontrol.agent.domain.*;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface AgentRepository extends JpaRepository<Agent,UUID>{Optional<Agent> findByInstallationId(String id);Optional<Agent> findFirstByDeviceEvidenceHashAndEnvironmentType(String hash,EnvironmentType type);}
