ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS prazo_data_fim DATE;

CREATE INDEX IF NOT EXISTS idx_tasks_period_end ON tasks(prazo_data_fim);
