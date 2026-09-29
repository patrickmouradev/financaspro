CREATE TABLE ativo (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(150) NOT NULL,
    tipo VARCHAR(30) NOT NULL, -- ACAO, FII, RENDA_FIXA
    setor VARCHAR(100),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE operacao (
    id BIGSERIAL PRIMARY KEY,
    ativo_id BIGINT NOT NULL REFERENCES ativo(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL, -- COMPRA, VENDA
    data_operacao DATE NOT NULL,
    quantidade NUMERIC(15, 6) NOT NULL,
    preco_unitario NUMERIC(15, 4) NOT NULL,
    taxas NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    observacao TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_operacao_ativo_data ON operacao(ativo_id, data_operacao);
