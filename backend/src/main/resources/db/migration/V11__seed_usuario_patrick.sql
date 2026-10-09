-- Migration V11: Seed do usuário principal Patrick Moura com senha 140908 (BCrypt)
INSERT INTO usuario (nome, email, senha_hash, ativo)
VALUES ('Patrick Moura', 'patrickmoura@gmail.com', '$2a$10$yWpUMO6UKLVZGEWFpIIJG.4b6ml2065LfvxCf3G/ue/iIBcY1u2J2', true)
ON CONFLICT (email) DO UPDATE
SET senha_hash = EXCLUDED.senha_hash,
    nome = EXCLUDED.nome,
    ativo = true;
