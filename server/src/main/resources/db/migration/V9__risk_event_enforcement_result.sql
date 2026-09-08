ALTER TABLE risk_events
    ADD COLUMN enforcement_applied BOOLEAN NOT NULL DEFAULT FALSE AFTER policy_version,
    ADD COLUMN enforcement_result_code VARCHAR(100) NOT NULL DEFAULT 'LEGACY_UNKNOWN' AFTER enforcement_applied;