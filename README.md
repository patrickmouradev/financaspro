# 💰 FinançasPro — Sistema Completo de Gestão Financeira e Investimentos

Sistema corporativo completo para gestão de finanças pessoais, controle de faturas/extratos bancários BTG, acompanhamento de carteira de ações/FIIs, metas de renda passiva mensal e calculadora comparativa de renda fixa.

---

## 🛠️ Tecnologias e Arquitetura

- **Backend:** Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA · Flyway Migrations · Apache POI (Encryption/Decrypt) · iText 7 PDF · Maven
- **Frontend:** Next.js 14 (App Router) · TypeScript · Tailwind CSS · Recharts · Zustand · Axios · TanStack Query
- **Banco de Dados:** PostgreSQL 15 (`192.168.15.10:5432` — database `financaspro`)
- **Infraestrutura:** Docker · Docker Compose · Jenkins CI/CD · Portainer
- **Armazenamento de Arquivos:** `C:\ARQUIVOS_SITES\FINANCASPRO` no host (mapeado para `/app/uploads` no container)

---

## 📌 Mapeamento de Portas e Serviços

| Serviço | Ambiente HML | Ambiente PRD |
|---|---|---|
| **Backend (Spring Boot)** | `http://localhost:8087` | `http://localhost:8088` |
| **Frontend (Next.js)** | `http://localhost:3004` | `http://localhost:3003` |
| **PostgreSQL** | `192.168.15.10:5432` (db: `financaspro`) | `192.168.15.10:5432` (db: `financaspro`) |

---

## 💡 Recursos e Módulos Principais

1. **Módulo Extrato & Gastos:**
   - Parsing automático de Fatura BTG `.xlsx` criptografada por senha (CPF armazenado em `parametro`).
   - Parsing automático de Extrato Conta Corrente BTG `.xls`.
   - Motor de categorização inteligente por palavra-chave configurável.
   - Relatórios em PDF e Excel formatados.

2. **Módulo Investimentos (Ações):**
   - Gestão de posição e operações de COMPRA / VENDA.
   - Cálculo exato de Preço Médio considerando taxas de corretagem e B3.
   - Integração com a API BRAPI para cotação em tempo real.

3. **Módulo FIIs & Metas de Rendimento:**
   - Controle de dividendos recebidos por cota e total.
   - Projeção automática de cotas faltantes e aporte estimado para atingir a meta mensal desejada (ex: R$ 1.000,00/mês).

4. **Módulo Renda Fixa & Indicadores:**
   - Calculadora interativa de rendimento (Prefixado, Pós-CDI, IPCA+).
   - Tabela regressiva automática de Imposto de Renda (22,5% a 15,0%) e isenções (LCI/LCA).
   - Sincronizador agendado (Scheduler) com a API de Dados Abertos do Banco Central do Brasil (SGS).

---

## 🚀 Como Executar Localmente

### 1. Backend Spring Boot (Java 21)
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
cd backend
..\maven\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run
```

### 2. Frontend Next.js 14
```powershell
cd frontend
npm install
npm run dev
```

---

## 📋 Acompanhamento de Tarefas e Histórico

O status detalhado de todas as 7 fases e micro tarefas concluídas pode ser consultado no arquivo [TASKS.md](file:///C:/GIT/financaspro/TASKS.md).
