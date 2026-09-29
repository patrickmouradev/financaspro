# 💰 FinançasPro

Sistema completo de controle financeiro pessoal, gestão de investimentos, calculadora de renda fixa e acompanhamento de meta de independência financeira com FIIs.

## 🛠️ Tecnogias e Arquitetura

- **Backend:** Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA · Flyway · Apache POI · iText 7 · Maven
- **Frontend:** Next.js 14 (App Router) · TypeScript · Tailwind CSS v4 · Recharts · Zustand · Axios · TanStack Query
- **Banco de Dados:** PostgreSQL 15 compartilhado (`192.168.15.10:5432` — database `financaspro`)
- **Infraestrutura:** Docker · Docker Compose · Jenkins CI/CD · Portainer

---

## 📌 Mapeamento de Portas do Servidor

| Serviço | HML | PRD |
|---|---|---|
| Backend (Spring Boot) | `:8087` | `:8088` |
| Frontend (Next.js) | `:3004` | `:3003` |
| PostgreSQL | `192.168.15.10:5432` | `192.168.15.10:5432` |

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- JDK 21+ instalado
- Node.js 20+ instalado
- Banco de Dados PostgreSQL rodando em `192.168.15.10:5432` com a database `financaspro` criada

### Passos
1. **Configuração de Ambiente:**
   ```bash
   cp .env.example .env
   ```
2. **Executar Backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. **Executar Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## 📋 Acompanhamento de Tarefas

O status do desenvolvimento por micro tarefas pode ser acompanhado no arquivo [TASKS.md](file:///C:/GIT/financaspro/TASKS.md).
