package com.youngone.mediacontrol.agent.infra;
import com.youngone.mediacontrol.agent.domain.AgentEventReceipt;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface AgentEventReceiptRepository extends JpaRepository<AgentEventReceipt,Long>{Optional<AgentEventReceipt> findByAgentIdAndEventId(UUID agentId,String eventId);}
