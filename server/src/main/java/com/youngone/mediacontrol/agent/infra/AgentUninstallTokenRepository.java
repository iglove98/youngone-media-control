package com.youngone.mediacontrol.agent.infra;
import com.youngone.mediacontrol.agent.domain.AgentUninstallToken;import org.springframework.data.jpa.repository.JpaRepository;import java.util.UUID;
public interface AgentUninstallTokenRepository extends JpaRepository<AgentUninstallToken,UUID>{}
