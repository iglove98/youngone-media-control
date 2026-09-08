package com.youngone.mediacontrol.agent.application;
import com.youngone.mediacontrol.agent.infra.AgentRepository;import org.springframework.beans.factory.annotation.Value;import org.springframework.scheduling.annotation.Scheduled;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.time.Instant;
@Service public class AgentStatusMonitor{
 private final AgentRepository agents;private final long heartbeatSeconds;private final long staleSeconds;
 public AgentStatusMonitor(AgentRepository agents,@Value("${youngone.agent.heartbeat-seconds:60}")long heartbeatSeconds,@Value("${youngone.agent.stale-seconds:2592000}")long staleSeconds){this.agents=agents;this.heartbeatSeconds=heartbeatSeconds;this.staleSeconds=staleSeconds;}
 @Scheduled(fixedDelayString="${youngone.agent.status-scan-ms:60000}")@Transactional public void refresh(){Instant now=Instant.now();agents.findAll().forEach(a->a.evaluateConnection(now,heartbeatSeconds,staleSeconds));}
}
