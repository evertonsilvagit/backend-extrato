# Project Context

## Purpose
Backend API for "Extrato" - a personal finance management application. The system allows users to:
- Track bank account transactions (credits and debits)
- Manage recurring bills/expenses (contas) with payment schedules
- Track income entries (entradas) with tax rates
- Manage debts (dívidas) grouped by category
- Generate account statements (extratos) with balance calculations

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 4.0.0
- **Web**: Spring MVC (REST API)
- **Persistence**: Spring Data JPA with Hibernate
- **Database**: MySQL (production)
- **Migrations**: Flyway (manual initialization via `FlywayInitializer`)
- **Build Tool**: Gradle
- **Code Generation**: Lombok (for boilerplate reduction)
- **Testing**: JUnit 5, Spring Boot Test, Testcontainers (MySQL), WebTestClient

## Project Conventions

### Code Style
- Package structure: `br.com.everton.backendextrato`
- Layers: `controller`, `service`, `repository`, `model`, `dto`, `config`
- Entity classes use explicit getters/setters (not Lombok for entities)
- DTOs use Java Records
- Portuguese naming for domain concepts (e.g., `Lancamento`, `Conta`, `Entrada`, `Divida`)
- API paths use Portuguese names (e.g., `/api/extratos`, `/api/contas`, `/api/entradas`, `/api/dividas`)

### Architecture Patterns
- Layered architecture: Controller → Service → Repository
- Constructor-based dependency injection
- DTOs for API request/response, entities for persistence
- Transactional services with `@Transactional` annotations
- Repository pattern with Spring Data JPA interfaces

### Testing Strategy
- Integration tests using `@SpringBootTest` with random port
- Testcontainers for MySQL in integration tests
- WebTestClient for HTTP testing
- H2 in-memory database available for test runtime
- Test naming: `*IntegrationTest.java`, `*SmokeTest.java`

### Git Workflow
- Main branch: `main`
- Commit message style: conventional commits (e.g., `feat:`, `fix:`, `chore:`, `build:`)
- Portuguese descriptions are acceptable in commits

## Domain Context
Key domain entities:
- **Lancamento** (Transaction): Individual credit/debit entry with date, type, value, description, category, linked to an account
- **Conta** (Bill/Expense): Recurring expense with payment day and validity months
- **Entrada** (Income): Income source with tax rate and monthly occurrences
- **EntradaMes**: Links income entries to specific months
- **Divida** (Debt): Debt record with description, value, and group category
- **Tipo** (Type): Enum with `CREDITO` and `DEBITO` values

Financial calculations:
- Saldo (Balance) = sum of credits minus sum of debits
- Saldo anterior (Previous balance) = balance before the query period
- Saldo atual (Current balance) = previous balance + period transactions

## Important Constraints
- `contaId` is required for transaction queries
- Pagination defaults: page 0, size 20
- Values use `BigDecimal` with precision 19, scale 2
- Dates use `LocalDate` in UTC timezone
- CORS configured for frontend at `https://frontend-extrato.vercel.app`
- Flyway auto-run is disabled; uses manual `FlywayInitializer`

## External Dependencies
- **Database**: MySQL hosted at `sql10.freesqldatabase.com` (free tier)
- **Frontend**: Deployed on Vercel at `https://frontend-extrato.vercel.app`
- Server runs on port 8083
