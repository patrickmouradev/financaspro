CREATE TABLE simulacao_salva (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo_ativo VARCHAR(30) NOT NULL, -- PREFIXADO, POS_CDI, IPCA_MAIS
    valor_investido NUMERIC(15, 2) NOT NULL,
    taxa NUMERIC(10, 4) NOT NULL,
    indexador VARCHAR(20),
    resultado_json TEXT NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
