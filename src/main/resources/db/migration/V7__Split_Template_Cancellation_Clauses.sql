ALTER TABLE templates_contrato
    ADD COLUMN IF NOT EXISTS clausula_cancelamento_contratada TEXT,
    ADD COLUMN IF NOT EXISTS clausula_cancelamento_contratante TEXT;

UPDATE templates_contrato
SET clausula_cancelamento_contratada = COALESCE(
        clausula_cancelamento_contratada,
        'Cancelamento pela contratada: devolucao integral dos valores pagos.'
    ),
    clausula_cancelamento_contratante = COALESCE(
        clausula_cancelamento_contratante,
        'Cancelamento pelo contratante: o sinal nao sera devolvido.'
    );
