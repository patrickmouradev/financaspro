-- Migration V12: Tabela para Extrato da Conta Investimento
CREATE TABLE IF NOT EXISTS lancamento_investimento (
    id BIGSERIAL PRIMARY KEY,
    conta_bancaria_id BIGINT REFERENCES conta_bancaria(id),
    data_movimento TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- DIVIDENDO, RENDIMENTO, COMPRA, VENDA, VENCIMENTO, CUPOM, IMPOSTO_TAXA, TRANSFERENCIA, OUTROS
    ticker VARCHAR(20),
    valor NUMERIC(15, 2) NOT NULL,
    saldo_resultante NUMERIC(15, 2),
    origem VARCHAR(50) NOT NULL DEFAULT 'BTG_INVESTIMENTO',
    observacao TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_lancamento_inv_data ON lancamento_investimento(data_movimento);
CREATE INDEX IF NOT EXISTS idx_lancamento_inv_ticker ON lancamento_investimento(ticker);
