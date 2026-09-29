-- Seeds iniciais de Parâmetros de Sistema
INSERT INTO parametro (chave, valor, descricao, tipo) VALUES
('gastos_mensais_meta', '5000.00', 'Meta mensal de gastos para independência financeira (R$)', 'NUMBER'),
('indexador_padrao', 'IPCA', 'Indexador padrão para simulações de Renda Fixa', 'STRING'),
('cors_origins', 'http://localhost:3004', 'Origens permitidas para requisições CORS', 'STRING'),
('ipca_ultima_atualizacao', '', 'Ano-Mês da última sincronização do IPCA via BCB/SGS', 'STRING'),
('brapi_token', '', 'Token opcional para a API BRAPI', 'STRING'),
('dividendo_meta_mensal', '5000.00', 'Meta mensal de dividendos em R$', 'NUMBER'),
('senha_fatura_btg', '', 'Senha padrão para abertura de faturas do BTG (CPF sem pontos)', 'PASSWORD'),
('senha_arquivo_default', '', 'Senha fallback para abertura de arquivos protegidos', 'PASSWORD')
ON CONFLICT (chave) DO NOTHING;

-- Seeds iniciais de Categorias de Gastos
INSERT INTO categoria (nome, icone, cor, tipo) VALUES
('Alimentação', 'Utensils', '#EF4444', 'DESPESA'),
('Supermercado', 'ShoppingCart', '#F59E0B', 'DESPESA'),
('Transporte', 'Car', '#3B82F6', 'DESPESA'),
('Moradia & Contas', 'Home', '#10B981', 'DESPESA'),
('Saúde & Cuidados', 'HeartPulse', '#EC4899', 'DESPESA'),
('Lazer & Entretenimento', 'Tv', '#8B5CF6', 'DESPESA'),
('Compras', 'ShoppingBag', '#6366F1', 'DESPESA'),
('Investimentos', 'TrendingUp', '#059669', 'DESPESA'),
('Impostos & Taxas', 'Receipt', '#6B7280', 'DESPESA'),
('Transferência', 'ArrowLeftRight', '#0EA5E9', 'DESPESA'),
('Salário & Receitas', 'DollarSign', '#22C55E', 'RECEITA'),
('Outros', 'HelpCircle', '#9CA3AF', 'DESPESA')
ON CONFLICT (nome) DO NOTHING;

-- Seeds de Regras de Categorização Automática por Palavras-chave (BTG e Geral)
INSERT INTO regra_categoria (categoria_id, palavra_chave, prioridade) VALUES
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'REST', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'RESTAURANTE', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'PADARIA', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'PIZZARIA', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'LANCHES', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'MC DONALD', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'BURGER', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'KOPENHAGEN', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'POPEYES', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'CACAO', 1),
((SELECT id FROM categoria WHERE nome = 'Alimentação'), 'MILKSHAKE', 1),

((SELECT id FROM categoria WHERE nome = 'Supermercado'), 'CARREFOUR', 1),
((SELECT id FROM categoria WHERE nome = 'Supermercado'), 'MERCADO', 1),
((SELECT id FROM categoria WHERE nome = 'Supermercado'), 'SUPERMERCADO', 1),
((SELECT id FROM categoria WHERE nome = 'Supermercado'), 'MAGALU', 2),

((SELECT id FROM categoria WHERE nome = 'Transporte'), 'AUTO POSTO', 1),
((SELECT id FROM categoria WHERE nome = 'Transporte'), 'POSTO', 1),
((SELECT id FROM categoria WHERE nome = 'Transporte'), 'ESTACIONAMENTO', 1),
((SELECT id FROM categoria WHERE nome = 'Transporte'), 'UBER', 1),
((SELECT id FROM categoria WHERE nome = 'Transporte'), 'PARKING', 1),
((SELECT id FROM categoria WHERE nome = 'Transporte'), 'SABESP', 1),
((SELECT id FROM categoria WHERE nome = 'Transporte'), 'PEDAGIO', 1),

((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'TELEFONICA', 1),
((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'VIVO', 1),
((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'CLARO', 1),
((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'PORTO SEGURO', 1),
((SELECT id FROM categoria WHERE nome = 'Moradia & Contas'), 'TOKIO MARINE', 1),

((SELECT id FROM categoria WHERE nome = 'Saúde & Cuidados'), 'FARMACIA', 1),
((SELECT id FROM categoria WHERE nome = 'Saúde & Cuidados'), 'DROGARIA', 1),
((SELECT id FROM categoria WHERE nome = 'Saúde & Cuidados'), 'NISSEI', 1),
((SELECT id FROM categoria WHERE nome = 'Saúde & Cuidados'), 'BARBEARIA', 1),

((SELECT id FROM categoria WHERE nome = 'Lazer & Entretenimento'), 'PLAYSTATION', 1),
((SELECT id FROM categoria WHERE nome = 'Lazer & Entretenimento'), 'YOUTUBE', 1),
((SELECT id FROM categoria WHERE nome = 'Lazer & Entretenimento'), 'APPLE', 1),
((SELECT id FROM categoria WHERE nome = 'Lazer & Entretenimento'), 'GOOGLE ONE', 1),
((SELECT id FROM categoria WHERE nome = 'Lazer & Entretenimento'), 'AMAZON', 1)
ON CONFLICT DO NOTHING;
