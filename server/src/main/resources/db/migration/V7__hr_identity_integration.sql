CREATE TABLE hr_integration_sources (
    id UUID NOT NULL, name VARCHAR(150) NOT NULL, connector_type VARCHAR(40) NOT NULL,
    sync_mode VARCHAR(20) NOT NULL, authority_rank INT NOT NULL, enabled BOOLEAN NOT NULL,
    config_json LONGTEXT NOT NULL, secret_ref VARCHAR(300), schedule_expression VARCHAR(100),
    last_cursor VARCHAR(1000), created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id), CONSTRAINT uk_hr_source_name UNIQUE (name)
);
CREATE TABLE persons (
    id UUID NOT NULL, authoritative_person_key VARCHAR(200) NOT NULL, employee_number VARCHAR(100),
    login_id VARCHAR(150), windows_sid VARCHAR(200), upn VARCHAR(254), email VARCHAR(254), display_name VARCHAR(200),
    employment_status VARCHAR(20) NOT NULL, organization_external_id VARCHAR(200), manager_person_key VARCHAR(200),
    effective_from TIMESTAMP(6), effective_to TIMESTAMP(6), created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id), CONSTRAINT uk_person_authoritative_key UNIQUE (authoritative_person_key)
);
CREATE INDEX ix_person_login ON persons(login_id, windows_sid, upn);
CREATE INDEX ix_person_org_status ON persons(organization_external_id, employment_status);
CREATE TABLE external_identities (
    id UUID NOT NULL, source_id UUID NOT NULL, person_id UUID NOT NULL, external_object_id VARCHAR(300) NOT NULL,
    immutable_person_key VARCHAR(200) NOT NULL, source_version VARCHAR(200), payload_hash VARCHAR(64) NOT NULL,
    first_seen_at TIMESTAMP(6) NOT NULL, last_seen_at TIMESTAMP(6) NOT NULL, disabled_at TIMESTAMP(6),
    PRIMARY KEY (id), CONSTRAINT uk_external_source_object UNIQUE (source_id, external_object_id),
    FOREIGN KEY (source_id) REFERENCES hr_integration_sources(id), FOREIGN KEY (person_id) REFERENCES persons(id)
);
CREATE INDEX ix_external_immutable_key ON external_identities(immutable_person_key, source_id);
CREATE TABLE identity_groups (
    id UUID NOT NULL, source_id UUID NOT NULL, external_group_id VARCHAR(300) NOT NULL, name VARCHAR(200) NOT NULL,
    group_type VARCHAR(30) NOT NULL, updated_at TIMESTAMP(6) NOT NULL, PRIMARY KEY (id),
    CONSTRAINT uk_identity_group_source_external UNIQUE (source_id, external_group_id),
    FOREIGN KEY (source_id) REFERENCES hr_integration_sources(id)
);
CREATE TABLE person_group_memberships (
    person_id UUID NOT NULL, group_id UUID NOT NULL, valid_from TIMESTAMP(6), valid_to TIMESTAMP(6),
    PRIMARY KEY (person_id, group_id), FOREIGN KEY (person_id) REFERENCES persons(id), FOREIGN KEY (group_id) REFERENCES identity_groups(id)
);
CREATE TABLE hr_sync_runs (
    id UUID NOT NULL, source_id UUID NOT NULL, sync_mode VARCHAR(20) NOT NULL, status VARCHAR(20) NOT NULL,
    cursor_before VARCHAR(1000), cursor_after VARCHAR(1000), read_count BIGINT NOT NULL, applied_count BIGINT NOT NULL,
    conflict_count BIGINT NOT NULL, started_at TIMESTAMP(6) NOT NULL, ended_at TIMESTAMP(6), error_summary VARCHAR(2000),
    PRIMARY KEY (id), FOREIGN KEY (source_id) REFERENCES hr_integration_sources(id)
);
CREATE INDEX ix_hr_sync_source_started ON hr_sync_runs(source_id, started_at);
CREATE TABLE hr_identity_conflicts (
    id UUID NOT NULL, source_id UUID NOT NULL, sync_run_id UUID, external_object_id VARCHAR(300),
    immutable_person_key VARCHAR(200), conflict_type VARCHAR(50) NOT NULL, details_json LONGTEXT NOT NULL,
    status VARCHAR(20) NOT NULL, detected_at TIMESTAMP(6) NOT NULL, resolved_by VARCHAR(100), resolved_at TIMESTAMP(6),
    PRIMARY KEY (id), FOREIGN KEY (source_id) REFERENCES hr_integration_sources(id), FOREIGN KEY (sync_run_id) REFERENCES hr_sync_runs(id)
);
CREATE INDEX ix_hr_conflict_open ON hr_identity_conflicts(status, detected_at);