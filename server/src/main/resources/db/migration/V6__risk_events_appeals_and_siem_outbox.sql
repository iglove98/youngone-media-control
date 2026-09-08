CREATE TABLE risk_events (
    id UUID NOT NULL, agent_id UUID NOT NULL, event_id VARCHAR(100) NOT NULL, payload_hash VARCHAR(64) NOT NULL,
    user_id VARCHAR(150), device_instance_id VARCHAR(300), serial_hash VARCHAR(64), media_type VARCHAR(40) NOT NULL,
    operation VARCHAR(20) NOT NULL, decision VARCHAR(20) NOT NULL, reason_code VARCHAR(100) NOT NULL,
    policy_id UUID, policy_version BIGINT, popup_shown BOOLEAN NOT NULL, attempt_count_24h INT NOT NULL,
    risk_score INT NOT NULL, severity VARCHAR(20) NOT NULL, status VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL, received_at TIMESTAMP(6) NOT NULL, handled_by VARCHAR(100), handled_at TIMESTAMP(6),
    PRIMARY KEY (id), CONSTRAINT uk_risk_agent_event UNIQUE (agent_id, event_id), FOREIGN KEY (agent_id) REFERENCES agents(id)
);
CREATE INDEX ix_risk_open_score ON risk_events(status, risk_score, occurred_at);
CREATE INDEX ix_risk_subject_time ON risk_events(agent_id, user_id, device_instance_id, occurred_at);
CREATE TABLE risk_appeals (
    id UUID NOT NULL, risk_event_id UUID NOT NULL, user_id VARCHAR(150) NOT NULL, statement VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL, PRIMARY KEY (id), FOREIGN KEY (risk_event_id) REFERENCES risk_events(id)
);
CREATE INDEX ix_risk_appeal_event ON risk_appeals(risk_event_id, created_at);
CREATE TABLE integration_outbox (
    id UUID NOT NULL, destination VARCHAR(50) NOT NULL, event_id UUID NOT NULL, event_type VARCHAR(100) NOT NULL,
    payload_json LONGTEXT NOT NULL, status VARCHAR(20) NOT NULL, attempt_count INT NOT NULL,
    next_attempt_at TIMESTAMP(6) NOT NULL, created_at TIMESTAMP(6) NOT NULL, sent_at TIMESTAMP(6), last_error VARCHAR(1000),
    PRIMARY KEY (id), CONSTRAINT uk_outbox_destination_event UNIQUE (destination, event_id)
);
CREATE INDEX ix_outbox_dispatch ON integration_outbox(status, next_attempt_at);