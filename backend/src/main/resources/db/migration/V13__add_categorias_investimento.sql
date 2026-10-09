-- Migration V13: Adiciona Categorias e Regras Especializadas para Mercado Financeiro / Investimentos

INSERT INTO categoria (nome, icone, cor, tipo) VALUES
('Fundos Imobiliários', 'Building2', '#10B981', 'DESPESA'),
('Ações', 'TrendingUp', '#3B82F6', 'DESPESA'),
('Renda Fixa', 'ShieldCheck', '#F59E0B', 'DESPESA'),
('Fundos de Investimento', 'PieChart', '#8B5CF6', 'DESPESA'),
('Dividendos & Proventos', 'Coins', '#22C55E', 'RECEITA')
ON CONFLICT (nome) DO NOTHING;

-- Regras para Fundos Imobiliários (FIIs)
INSERT INTO regra_categoria (categoria_id, palavra_chave, prioridade) VALUES
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'XPML11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'BTLG11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'MXRF11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'VISC11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'HGLG11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'KNIP11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'CPTS11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'TGAR11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'BCFF11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'VRTA11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'RBRR11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'VGHF11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'PVBI11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'ALZR11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'TRBL11', 10),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'FII', 5),
((SELECT id FROM categoria WHERE nome = 'Fundos Imobiliários'), 'FUNDO IMOBILIARIO', 5)
ON CONFLICT DO NOTHING;

-- Regras para Ações
INSERT INTO regra_categoria (categoria_id, palavra_chave, prioridade) VALUES
((SELECT id FROM categoria WHERE nome = 'Ações'), 'PETR4', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'VALE3', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'WEGE3', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'ITUB4', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'BBAS3', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'RENT3', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'SUZB3', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'B3SA3', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'EGIE3', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'TAEE11', 10),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'COMPRA AÇÃO', 5),
((SELECT id FROM categoria WHERE nome = 'Ações'), 'VENDA AÇÃO', 5)
ON CONFLICT DO NOTHING;

-- Regras para Renda Fixa
INSERT INTO regra_categoria (categoria_id, palavra_chave, prioridade) VALUES
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'CDB', 8),
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'TESOURO', 8),
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'LCI', 8),
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'LCA', 8),
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'CRI', 8),
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'CRA', 8),
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'DEBENTURE', 8),
((SELECT id FROM categoria WHERE nome = 'Renda Fixa'), 'COMPROMISSADA', 8)
ON CONFLICT DO NOTHING;

-- Regras para Fundos de Investimento
INSERT INTO regra_categoria (categoria_id, palavra_chave, prioridade) VALUES
((SELECT id FROM categoria WHERE nome = 'Fundos de Investimento'), 'FIM', 8),
((SELECT id FROM categoria WHERE nome = 'Fundos de Investimento'), 'FIA', 8),
((SELECT id FROM categoria WHERE nome = 'Fundos de Investimento'), 'FIC', 8),
((SELECT id FROM categoria WHERE nome = 'Fundos de Investimento'), 'FUNDO DE INVESTIMENTO', 8),
((SELECT id FROM categoria WHERE nome = 'Fundos de Investimento'), 'MULTIMERCADO', 8)
ON CONFLICT DO NOTHING;

-- Regras para Dividendos & Proventos
INSERT INTO regra_categoria (categoria_id, palavra_chave, prioridade) VALUES
((SELECT id FROM categoria WHERE nome = 'Dividendos & Proventos'), 'DIVIDENDO', 9),
((SELECT id FROM categoria WHERE nome = 'Dividendos & Proventos'), 'RENDIMENTO', 9),
((SELECT id FROM categoria WHERE nome = 'Dividendos & Proventos'), 'PROVENTO', 9),
((SELECT id FROM categoria WHERE nome = 'Dividendos & Proventos'), 'JCP', 9),
((SELECT id FROM categoria WHERE nome = 'Dividendos & Proventos'), 'JUROS S/ CAPITAL', 9)
ON CONFLICT DO NOTHING;
