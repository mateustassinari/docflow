CREATE UNIQUE INDEX IF NOT EXISTS uk_contracts_event_id ON contracts (event_id);

CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts (status);

CREATE INDEX IF NOT EXISTS idx_contracts_autentique_id ON contracts (autentique_id);
