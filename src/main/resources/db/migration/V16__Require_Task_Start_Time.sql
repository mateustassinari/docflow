UPDATE tasks
SET prazo_horario = TIME '09:00'
WHERE prazo_horario IS NULL;

UPDATE tasks
SET prazo_horario_fim = TIME '10:00'
WHERE prazo_horario_fim IS NULL;

UPDATE tasks
SET all_day = FALSE
WHERE all_day = TRUE;

ALTER TABLE tasks
    ALTER COLUMN prazo_horario SET NOT NULL;

ALTER TABLE tasks
    ALTER COLUMN all_day SET DEFAULT FALSE;
