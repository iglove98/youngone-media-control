package com.youngone.mediacontrol.risk.infra;
import com.youngone.mediacontrol.risk.domain.IntegrationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface IntegrationOutboxRepository extends JpaRepository<IntegrationOutbox, UUID> {}