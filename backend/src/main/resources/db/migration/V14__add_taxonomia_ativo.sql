-- Migration V14: Adiciona colunas de Taxonomia Detalhada (Categoria, Classe, Indexador) na tabela de Ativos

ALTER TABLE ativo ADD COLUMN IF NOT EXISTS categoria_nome VARCHAR(100);
ALTER TABLE ativo ADD COLUMN IF NOT EXISTS classe VARCHAR(100);
ALTER TABLE ativo ADD COLUMN IF NOT EXISTS indexador VARCHAR(50);
ALTER TABLE ativo ADD COLUMN IF NOT EXISTS taxa_adicional NUMERIC(10, 4);

-- Seeds / Atualizações de Ativos com a nova taxonomia do mercado financeiro
INSERT INTO ativo (ticker, nome, tipo, categoria_nome, classe, indexador, taxa_adicional) VALUES
('XPML11', 'XP Malls FII', 'FII', 'Fundos Imobiliários', 'Tijolo', 'IPCA', 0.00),
('BTLG11', 'BTG Pactual Logística FII', 'FII', 'Fundos Imobiliários', 'Tijolo', 'IPCA', 0.00),
('MXRF11', 'Maxi Renda FII', 'FII', 'Fundos Imobiliários', 'Papel', 'CDI', 0.00),
('VISC11', 'Vinci Shopping Centers FII', 'FII', 'Fundos Imobiliários', 'Tijolo', 'IPCA', 0.00),
('CDB_BTG_CDI', 'CDB BTG 100% CDI', 'RENDA_FIXA', 'Renda Fixa', 'CDB', 'CDI', 100.00),
('CDB_BTG_IPCA', 'CDB BTG IPCA + 6.5%', 'RENDA_FIXA', 'Renda Fixa', 'CDB', 'IPCA', 6.50),
('TESOURO_SELIC', 'Tesouro Selic 2029', 'RENDA_FIXA', 'Renda Fixa', 'Tesouro Selic', 'SELIC', 0.00),
('CRA_JBS', 'CRA JBS 100% CDI', 'RENDA_FIXA', 'Renda Fixa', 'CRA', 'CDI', 100.00),
('CRI_KINEA', 'CRI Kinea IPCA + 7%', 'RENDA_FIXA', 'Renda Fixa', 'CRI', 'IPCA', 7.00)
ON CONFLICT (ticker) DO UPDATE SET
    categoria_nome = EXCLUDED.categoria_nome,
    classe = EXCLUDED.classe,
    indexador = EXCLUDED.indexador,
    taxa_adicional = EXCLUDED.taxa_adicional;
