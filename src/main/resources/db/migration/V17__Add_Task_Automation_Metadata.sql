ALTER TABLE tasks ADD COLUMN automation_source VARCHAR(80);
ALTER TABLE tasks ADD COLUMN automation_contract_id BIGINT;
ALTER TABLE tasks ADD COLUMN automation_key VARCHAR(120);

ALTER TABLE tasks ADD CONSTRAINT uk_tasks_automation_contract_key UNIQUE (automation_contract_id, automation_key);

CREATE INDEX idx_tasks_automation_contract ON tasks(automation_contract_id);
