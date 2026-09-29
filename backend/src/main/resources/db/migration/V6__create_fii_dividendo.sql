CREATE TABLE fii (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(150) NOT NULL,
    segmento VARCHAR(100),
    quantidade_cotas NUMERIC(15, 6) NOT NULL DEFAULT 0,
    preco_medio NUMERIC(15, 4) NOT NULL DEFAULT 0.00,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dividendo (
    id BIGSERIAL PRIMARY KEY,
    fii_id BIGINT NOT NULL REFERENCES fii(id) ON DELETE CASCADE,
    data_pagamento DATE NOT NULL,
    valor_por_cota NUMERIC(15, 4) NOT NULL,
    quantidade_cotas NUMERIC(15, 6) NOT NULL,
    valor_total NUMERIC(15, 2) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dividendo_fii_data ON dividendo(fii_id, data_pagamento);
