-- Migration V15: Novas Regras de Categorização Automática ("De Para") solicitadas pelo usuário

-- 1. Garante existência da Categoria "Cartão de Crédito"
INSERT INTO categoria (nome, icone, cor, tipo) VALUES
('Cartão de Crédito', 'CreditCard', '#8B5CF6', 'DESPESA')
ON CONFLICT (nome) DO NOTHING;

-- 2. Atualiza ou remove regras conflitantes prévias para SABESP (se existiam em Transporte)
DELETE FROM regra_categoria WHERE palavra_chave = 'SABESP';

-- 3. Insere Novas Regras de Categorização ("De Para") com Alta Prioridade
INSERT INTO regra_categoria (categoria_id, palavra_chave, prioridade) VALUES
((SELECT id FROM categoria WHERE nome = 'Impostos & Taxas'), 'JUROS - LIMITE DA CONTA', 15),
((SELECT id FROM categoria WHERE nome = 'Impostos & Taxas'), 'IOF LIMITE DA CONTA', 15),

((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'TELEFONICA BRAS', 15),
((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'PORTO SEGURO SEGUROS', 15),
((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'SABESP', 15),
((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'CARLOS EDUARDO PEREIRA DA SILVA', 15),

((SELECT id FROM categoria WHERE nome = 'Cartão de Crédito'), 'FATURA DO CARTÃO BTG PACTUAL', 15),
((SELECT id FROM categoria WHERE nome = 'Cartão de Crédito'), 'FATURA DO CARTAO BTG PACTUAL', 15),

((SELECT id FROM categoria WHERE nome = 'Transporte'), 'AMG SERVICOS DE ESTACIONAMENTO', 15)
ON CONFLICT DO NOTHING;
