package com.youngone.mediacontrol.agent.infra;
import com.youngone.mediacontrol.agent.domain.AgentIdentityConflict;import org.springframework.data.jpa.repository.JpaRepository;
public interface AgentIdentityConflictRepository extends JpaRepository<AgentIdentityConflict,Long>{}
