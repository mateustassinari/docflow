ALTER TABLE events ADD COLUMN custo_total NUMERIC(10, 2);

CREATE TABLE service_type_cost_defaults (
    tipo_servico   VARCHAR(100) PRIMARY KEY,
    custo_default  NUMERIC(10, 2) NOT NULL DEFAULT 0
);

INSERT INTO service_type_cost_defaults (tipo_servico, custo_default) VALUES
    ('PINTURA_AO_VIVO', 0),
    ('AQUARELA_AO_VIVO', 0),
    ('ENCOMENDA_ARTISTICA', 0),
    ('IDENTIDADE_VISUAL', 0);
