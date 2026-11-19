INSERT INTO templates_contrato (nome, tipo_evento, caminho_arquivo, versao)
SELECT 'Template padrão', 'GERAL', 'uploads/templates/template_teste.docx', '1'
WHERE NOT EXISTS (
    SELECT 1 FROM templates_contrato WHERE caminho_arquivo = 'uploads/templates/template_teste.docx'
);

UPDATE contracts
SET template_id = (
    SELECT id
    FROM templates_contrato
    WHERE caminho_arquivo = 'uploads/templates/template_teste.docx'
    ORDER BY id
    LIMIT 1
)
WHERE template_id IS NULL;

ALTER TABLE clients
    ALTER COLUMN nome SET NOT NULL,
    ALTER COLUMN cpf_cnpj SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN telefone SET NOT NULL;

ALTER TABLE events
    ALTER COLUMN cliente_id SET NOT NULL,
    ALTER COLUMN tipo_evento SET NOT NULL,
    ALTER COLUMN data SET NOT NULL,
    ALTER COLUMN local SET NOT NULL,
    ALTER COLUMN valor SET NOT NULL;

ALTER TABLE contracts
    ALTER COLUMN event_id SET NOT NULL,
    ALTER COLUMN template_id SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_clients_cpf_cnpj ON clients (cpf_cnpj);
