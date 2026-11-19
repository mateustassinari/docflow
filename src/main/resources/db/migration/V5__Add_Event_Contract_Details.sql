ALTER TABLE events ADD COLUMN horario_inicio TIME;
ALTER TABLE events ADD COLUMN duracao_estimada VARCHAR(100);
ALTER TABLE events ADD COLUMN numero_convidados INTEGER;
ALTER TABLE events ADD COLUMN tecnica VARCHAR(255);
ALTER TABLE events ADD COLUMN tamanho_tela VARCHAR(100);
ALTER TABLE events ADD COLUMN cena_retratada TEXT;
ALTER TABLE events ADD COLUMN entrega_obra VARCHAR(255);
ALTER TABLE events ADD COLUMN valor_sinal NUMERIC(10, 2);
