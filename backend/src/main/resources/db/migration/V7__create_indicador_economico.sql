CREATE TABLE indicador_economico (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL, -- IPCA, CDI, SELIC
    ano_mes VARCHAR(7) NOT NULL, -- YYYY-MM
    valor_percentual NUMERIC(10, 4) NOT NULL,
    fonte VARCHAR(50) NOT NULL DEFAULT 'BCB_SGS',
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_indicador_tipo_anomes UNIQUE (tipo, ano_mes)
);

CREATE INDEX idx_indicador_tipo_anomes ON indicador_economico(tipo, ano_mes);
