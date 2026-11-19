ALTER TABLE events
    ADD COLUMN sinal_recebido            BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN data_recebimento_sinal    DATE,
    ADD COLUMN restante_recebido         BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN data_recebimento_restante DATE,
    ADD COLUMN observacao_pagamento      TEXT;
