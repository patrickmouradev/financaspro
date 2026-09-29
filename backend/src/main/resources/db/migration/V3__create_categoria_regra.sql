CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    icone VARCHAR(50),
    cor VARCHAR(20),
    tipo VARCHAR(20) NOT NULL DEFAULT 'DESPESA' -- DESPESA, RECEITA
);

CREATE TABLE regra_categoria (
    id BIGSERIAL PRIMARY KEY,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id) ON DELETE CASCADE,
    palavra_chave VARCHAR(100) NOT NULL,
    prioridade INT NOT NULL DEFAULT 1,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_regra_palavra_chave ON regra_categoria(palavra_chave);
