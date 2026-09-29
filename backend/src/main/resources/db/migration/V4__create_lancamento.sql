CREATE TABLE lancamento (
    id BIGSERIAL PRIMARY KEY,
    conta_bancaria_id BIGINT REFERENCES conta_bancaria(id) ON DELETE SET NULL,
    categoria_id BIGINT REFERENCES categoria(id) ON DELETE SET NULL,
    data_lancamento TIMESTAMP NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    tipo VARCHAR(30) NOT NULL, -- DEBITO_CONTA, PIX_ENVIADO, PIX_RECEBIDO, COMPRA_VISTA, PARCELADO, etc.
    origem VARCHAR(50) NOT NULL, -- BTG_FATURA, BTG_EXTRATO, MANUAL
    codigo_autorizacao VARCHAR(100),
    parcela_atual INT,
    total_parcelas INT,
    status_categorizacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE', -- AUTO, MANUAL, PENDENTE
    observacao TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lancamento_data ON lancamento(data_lancamento);
CREATE INDEX idx_lancamento_conta ON lancamento(conta_bancaria_id);
CREATE INDEX idx_lancamento_categoria ON lancamento(categoria_id);
