ALTER TABLE policies ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'MIGRATED';
ALTER TABLE policies ADD COLUMN approved_by VARCHAR(100);
ALTER TABLE policies ADD COLUMN approved_at TIMESTAMP(6);
CREATE TABLE policy_assignments(id UUID NOT NULL,policy_id UUID NOT NULL,target_type VARCHAR(20) NOT NULL,target_id VARCHAR(150),priority INT NOT NULL,enabled BOOLEAN NOT NULL,created_by VARCHAR(100) NOT NULL,created_at TIMESTAMP(6) NOT NULL,PRIMARY KEY(id),FOREIGN KEY(policy_id) REFERENCES policies(id));
CREATE INDEX ix_assignment_target ON policy_assignments(target_type,target_id,enabled);
CREATE TABLE policy_deployments(id UUID NOT NULL,policy_id UUID NOT NULL,previous_policy_id UUID,rollout_percentage INT NOT NULL,status VARCHAR(20) NOT NULL,started_by VARCHAR(100) NOT NULL,started_at TIMESTAMP(6) NOT NULL,ended_at TIMESTAMP(6),PRIMARY KEY(id),FOREIGN KEY(policy_id) REFERENCES policies(id),FOREIGN KEY(previous_policy_id) REFERENCES policies(id));
CREATE INDEX ix_deployment_status_started ON policy_deployments(status,started_at);
