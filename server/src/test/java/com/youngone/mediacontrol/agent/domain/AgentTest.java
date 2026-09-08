package com.youngone.mediacontrol.agent.domain;
import org.junit.jupiter.api.Test;import java.time.Instant;import java.util.UUID;import static org.assertj.core.api.Assertions.assertThat;
class AgentTest{
 @Test void heartbeatKeepsAgentOnline(){Agent a=agent(Instant.now());a.heartbeat("2","win","127.0.0.2",Instant.now());assertThat(a.getConnectionStatus()).isEqualTo(ConnectionStatus.ONLINE);}
 @Test void derivesDegradedOfflineAndStaleFromLastSeen(){Instant seen=Instant.parse("2026-01-01T00:00:00Z");Agent a=agent(seen);a.evaluateConnection(seen.plusSeconds(150),60,2592000);assertThat(a.getConnectionStatus()).isEqualTo(ConnectionStatus.DEGRADED);a.evaluateConnection(seen.plusSeconds(181),60,2592000);assertThat(a.getConnectionStatus()).isEqualTo(ConnectionStatus.OFFLINE);a.evaluateConnection(seen.plusSeconds(2592000),60,2592000);assertThat(a.getConnectionStatus()).isEqualTo(ConnectionStatus.STALE);}
 private Agent agent(Instant now){return new Agent(UUID.randomUUID(),"i1","pc","1","win","127.0.0.1","hash",now);}
}
