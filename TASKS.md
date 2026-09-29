# 📋 FinançasPro — Acompanhamento de Tarefas (Checklist)

> Documento dinâmico de controle de progresso. Cada micro tarefa concluída é marcada com `[x]` e commitada individualmente no repositório.

---

## 🏗️ FASE 1 — Fundação do Projeto

### 1.1 — Setup do repositório e estrutura inicial
- [x] 1.1.1 Inicializar repositório Git e criar `.gitignore` na raiz
- [x] 1.1.2 Criar `.env.example` raiz com todas as variáveis documentadas
- [x] 1.1.3 Criar `README.md` inicial com visão geral e instruções de execução

### 1.2 — Setup do Backend Spring Boot
- [x] 1.2.1 Criar estrutura Maven `backend/` com Java 21 e `pom.xml` completo (Spring Boot 3, Security, JPA, PostgreSQL, Flyway, JWT, Apache POI, iText, Actuator)
- [x] 1.2.2 Criar `backend/src/main/resources/application.yaml` (base sem segredos)
- [x] 1.2.3 Criar `backend/src/main/resources/application-hml.yaml`
- [x] 1.2.4 Criar `backend/src/main/resources/application-prd.yaml`
- [x] 1.2.5 Criar `backend/.env.example`
- [x] 1.2.6 Criar classe principal `FinancasproApplication.java` com anotações básicas

### 1.3 — Banco de Dados e Migrations
- [x] 1.3.1 Criar migration `V1__create_usuario.sql`
- [x] 1.3.2 Criar migration `V2__create_conta_bancaria.sql`
- [x] 1.3.3 Criar migration `V3__create_categoria_regra.sql`
- [x] 1.3.4 Criar migration `V4__create_lancamento.sql`
- [x] 1.3.5 Criar migration `V5__create_ativo_operacao.sql`
- [x] 1.3.6 Criar migration `V6__create_fii_dividendo.sql`
- [x] 1.3.7 Criar migration `V7__create_indicador_economico.sql`
- [x] 1.3.8 Criar migration `V8__create_simulacao_salva.sql`
- [x] 1.3.9 Criar migration `V9__create_parametro.sql`
- [x] 1.3.10 Criar migration `V10__seed_categorias_e_parametros.sql` (dados iniciais e senhas de arquivo)
- [x] 1.3.11 Validar execução das migrations Flyway via Maven (`mvn clean test-compile`)

### 1.4 — Package Utils
- [x] 1.4.1 Criar `utils/DateUtils.java` (anoMesAtual, calcularDiasEntre, formatarDataBr, primeiroEUltimoDiaMes)
- [x] 1.4.2 Criar `utils/MoedaUtils.java` (formatarReal, arredondar2Casas, somaBigDecimal)
- [x] 1.4.3 Criar `utils/PercentualUtils.java` (calcularVariacao, calcularPercentual, arredondar6Casas)
- [x] 1.4.4 Criar `utils/CsvExcelParser.java` (parsearArquivo, detectarFormato, mapearColunas, abrirComSenha com POIFS/EncryptionInfo)
- [x] 1.4.5 Criar `utils/PdfGenerator.java` (gerarRelatorio)
- [x] 1.4.6 Criar `utils/ExcelGenerator.java` (gerarPlanilha)

### 1.5 — Autenticação JWT
- [x] 1.5.1 Criar entity `Usuario.java` (id, nome, email, senhaHash, ativo)
- [x] 1.5.2 Criar `UsuarioRepository.java`
- [x] 1.5.3 Criar `AuthService.java` (autenticar, gerarToken, validarToken)
- [x] 1.5.4 Criar `AuthController.java` (endpoint POST /api/auth/login)
- [x] 1.5.5 Criar `JwtFilter.java` (OncePerRequestFilter)
- [x] 1.5.6 Criar `SecurityConfig.java` (filtro JWT, endpoints públicos)
- [x] 1.5.7 Criar DTOs `LoginRequestDTO.java` e `TokenResponseDTO.java` e `UsuarioBuilder.java`

### 1.6 — Módulo Parâmetros de Sistema
- [x] 1.6.1 Criar entity `Parametro.java` (id, chave, valor, descricao, tipo)
- [x] 1.6.2 Criar `ParametroBuilder.java`
- [x] 1.6.3 Criar `ParametroRepository.java`
- [x] 1.6.4 Criar `ParametroService.java` (buscarPorChave, atualizar, listarTodos)
- [x] 1.6.5 Criar `ParametroController.java` (GET /api/parametros, PUT /api/parametros/{chave})
- [x] 1.6.6 Criar `ParametroDTO.java`

### 1.7 — Docker e Jenkins (Fundação)
- [x] 1.7.1 Criar script/instrução de banco `CREATE DATABASE financaspro;` para o PostgreSQL 192.168.15.10
- [x] 1.7.2 Criar script/instrução de pasta `C:\ARQUIVOS_SITES\FINANCASPRO` no servidor
- [x] 1.7.3 Criar `backend/Dockerfile` (multi-stage: build Maven + runtime JRE 21)
- [x] 1.7.4 Criar `docker-compose.yml` na raiz (backend + frontend sem postgres container)
- [x] 1.7.5 Criar `backend/Jenkinsfile` seguindo padrão condomínio
- [x] 1.7.6 Documentar registro de containers no Portainer

---

## 📄 FASE 2 — Módulo Extrato & Gastos

### 2.1 — Entidades e Repositórios
- [x] 2.1.1 Criar entity `ContaBancaria.java` e `ContaBancariaRepository.java`
- [x] 2.1.2 Criar entity `Categoria.java` e `CategoriaRepository.java`
- [x] 2.1.3 Criar entity `RegraCategoria.java` e `RegraCategoriaRepository.java`
- [x] 2.1.4 Criar entity `Lancamento.java` e `LancamentoRepository.java` (queries customizadas)

### 2.2 — Builders
- [x] 2.2.1 Criar `LancamentoBuilder.java`, `ContaBancariaBuilder.java`, `CategoriaBuilder.java`, `RegraCategoriaBuilder.java`

### 2.3 — DTOs
- [x] 2.3.1 Criar `LancamentoDTO.java`, `ImportacaoResultadoDTO.java`, `ResumoMensalDTO.java`, `CategoriaResumoDTO.java`, `ContaBancariaDTO.java`, `CategoriaDTO.java`, `RegraCategoriaDTO.java`

### 2.4 — Services & Parsers Específicos
- [x] 2.4.1 Criar `BtgFaturaParser.java` (parsing da fatura .xlsx com desproteção por senha)
- [x] 2.4.2 Criar `BtgExtratoParser.java` (parsing do extrato conta corrente .xls)
- [x] 2.4.3 Criar `CategorizacaoService.java` (motor de categorização por palavra-chave)
- [x] 2.4.4 Criar `ImportacaoService.java` (processar preview e confirmar importação)
- [x] 2.4.5 Criar `ExtratoService.java` (CRUD e resumos)

### 2.5 — Controllers & Relatórios
- [ ] 2.5.1 Criar `ImportacaoController.java` (endpoints de importação e confirmação)
- [ ] 2.5.2 Criar `ExtratoController.java` (CRUD, resumo mensal, relatórios PDF e Excel)
- [ ] 2.5.3 Criar `CategoriaController.java` (CRUD de categorias e regras)
- [ ] 2.5.4 Implementar exportação em `PdfGenerator.java` e `ExcelGenerator.java`
- [ ] 2.5.5 Testes unitários do Módulo Extrato

---

## 📈 FASE 3 — Módulo Investimentos

### 3.1 — Entidades, Repositórios e DTOs
- [ ] 3.1.1 Criar enum `TipoAtivo.java` (ACAO, FII, RENDA_FIXA)
- [ ] 3.1.2 Criar entity `Ativo.java` e `AtivoRepository.java`
- [ ] 3.1.3 Criar entity `Operacao.java` e `OperacaoRepository.java`
- [ ] 3.1.4 Criar `AtivoBuilder.java` e `OperacaoBuilder.java`
- [ ] 3.1.5 Criar `AtivoDTO.java`, `OperacaoDTO.java`, `RentabilidadeDTO.java`, `CarteiraDTO.java`

### 3.2 — Services & Controllers
- [ ] 3.2.1 Criar `RentabilidadeService.java` (preço médio, cotação BRAPI, ganho/perda)
- [ ] 3.2.2 Criar `InvestimentoService.java` (gestão de carteira e operações)
- [ ] 3.2.3 Criar `InvestimentoController.java`, `OperacaoController.java`, `AtivoController.java`
- [ ] 3.2.4 Testes unitários do Módulo Investimentos

---

## 🔢 FASE 4 — Módulo Renda Fixa

### 4.1 — Entidades, DTOs e Services
- [ ] 4.1.1 Criar entity `IndicadorEconomico.java` e `IndicadorRepository.java`
- [ ] 4.1.2 Criar entity `SimulacaoSalva.java` e `SimulacaoRepository.java`
- [ ] 4.1.3 Criar `SimulacaoBuilder.java`
- [ ] 4.1.4 Criar `SimulacaoRequestDTO.java`, `SimulacaoResultadoDTO.java`, `ComparacaoDTO.java`
- [ ] 4.1.5 Criar `IpcaService.java` (integração BCB/SGS série 10844, CDI série 12, Selic série 432)
- [ ] 4.1.6 Criar `CalculadoraRendaFixaService.java` (Prefixado, Pós-CDI, IPCA+, Ganho Real, Comparador)

### 4.2 — Scheduler, Controllers e Testes
- [ ] 4.2.1 Criar `IpcaScheduler.java` (sincronização mensal automática do IPCA)
- [ ] 4.2.2 Criar `RendaFixaController.java` e `IndicadorController.java`
- [ ] 4.2.3 Testes unitários da Calculadora de Renda Fixa

---

## 🏛️ FASE 5 — Módulo FIIs & Meta

### 5.1 — Entidades, DTOs e Services
- [ ] 5.1.1 Criar entity `Fii.java` e `FiiRepository.java`
- [ ] 5.1.2 Criar entity `Dividendo.java` e `DividendoRepository.java`
- [ ] 5.1.3 Criar `DividendoBuilder.java`
- [ ] 5.1.4 Criar `FiiDTO.java`, `DividendoDTO.java`, `MetaDTO.java`, `ProjecaoMetaDTO.java`
- [ ] 5.1.5 Criar `FiiService.java` (gestão de FIIs e dividendos)
- [ ] 5.1.6 Criar `MetaDividendoService.java` (cálculo de DY médio mensal, projeção de cotas faltantes, progresso da meta)

### 5.2 — Controllers e Testes
- [ ] 5.2.1 Criar `FiiController.java` e `DividendoController.java`
- [ ] 5.2.2 Testes unitários do Módulo FIIs & Meta

---

## ⚛️ FASE 6 — Frontend Next.js 14

### 6.1 — Setup do Projeto Frontend
- [ ] 6.1.1 Criar projeto Next.js 14 em `frontend/` com TypeScript e Tailwind CSS v4
- [ ] 6.1.2 Instalar shadcn/ui, lucide-react, recharts, axios, react-query, zustand, react-hook-form, zod, react-dropzone, tanstack-table
- [ ] 6.1.3 Criar `frontend/src/lib/axios.ts`, `frontend/src/lib/query-client.ts`, `frontend/src/store/auth.store.ts`
- [ ] 6.1.4 Criar `frontend/.env.local.example`

### 6.2 — Components & Layout
- [ ] 6.2.1 Criar página `app/(auth)/login/page.tsx`
- [ ] 6.2.2 Criar `app/(dashboard)/layout.tsx`, `Sidebar.tsx`, `Header.tsx`, `KpiCard.tsx`

### 6.3 — Páginas do Dashboard e Módulos
- [ ] 6.3.1 Criar Dashboard principal `app/(dashboard)/page.tsx` (KPIs + Gráficos Recharts)
- [ ] 6.3.2 Criar telas do Extrato `app/(dashboard)/extrato/page.tsx`, `importar/page.tsx`, `categorias/page.tsx`
- [ ] 6.3.3 Criar telas de Investimentos `app/(dashboard)/investimentos/page.tsx`, `importar/page.tsx`
- [ ] 6.3.4 Criar telas de Renda Fixa `app/(dashboard)/renda-fixa/page.tsx`, `simulacoes/page.tsx`
- [ ] 6.3.5 Criar telas de FIIs `app/(dashboard)/investimentos/fiis/page.tsx`
- [ ] 6.3.6 Criar tela de Configurações `app/(dashboard)/configuracoes/page.tsx`

### 6.4 — Docker Frontend & Pipelines
- [ ] 6.4.1 Criar `frontend/Dockerfile` e `frontend/Jenkinsfile`

---

## ✅ FASE 7 — Finalização, Validação e Deploy

### 7.1 — Testes & QA Final
- [ ] 7.1.1 Teste de integração de ponta a ponta (migração zero, login, importação real BTG, gráficos, exportação PDF/Excel)
- [ ] 7.1.2 Testes de validação de Actuator `/actuator/health`

### 7.2 — Documentação e Deploy PRD
- [ ] 7.2.1 Finalizar `README.md` com instruções de uso, ambiente e credenciais Jenkins
- [ ] 7.2.2 Executar deploy via Jenkins em ambiente PRD e validar no Portainer
