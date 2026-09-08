package com.youngone.mediacontrol.risk.infra;
import com.youngone.mediacontrol.risk.domain.RiskAppeal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface RiskAppealRepository extends JpaRepository<RiskAppeal, UUID> {}