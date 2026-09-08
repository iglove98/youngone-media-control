CREATE TABLE agent_uninstall_tokens(
 id UUID NOT NULL,
 agent_id UUID NOT NULL,
 nonce VARCHAR(100) NOT NULL,
 payload_json LONGTEXT NOT NULL,
 payload_hash CHAR(64) NOT NULL,
 signature TEXT NOT NULL,
 signing_key_id VARCHAR(100) NOT NULL,
 status VARCHAR(20) NOT NULL,
 reason VARCHAR(500) NOT NULL,
 issued_by VARCHAR(150) NOT NULL,
 issued_at TIMESTAMP(6) NOT NULL,
 expires_at TIMESTAMP(6) NOT NULL,
 consumed_at TIMESTAMP(6) NULL,
 PRIMARY KEY(id),
 UNIQUE(nonce),
 INDEX ix_agent_uninstall_tokens_agent_time(agent_id,issued_at),
 FOREIGN KEY(agent_id) REFERENCES agents(id)
);
