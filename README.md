# MiniBank API 🏦

A banking REST API built with **Java 21, Spring Boot and PostgreSQL**. It supports creating accounts, deposits, withdrawals and transfers between accounts, with validation, safe money handling and automated tests.

## Features

- Create accounts with auto-generated account numbers (`MB` + 10 digits)
- Deposit, withdraw and transfer money
- Business rules: no overdrafts, no zero or negative amounts, at most 2 decimal places, no transfers to the same account
- **Atomic transfers**: `@Transactional` makes a transfer all-or-nothing, so money is never lost halfway
- **Optimistic locking** (`@Version`): two requests can't overwrite the same balance at the same time
- Money stored as `BigDecimal` / `NUMERIC(19,2)`, never `double`, to avoid rounding errors
- Clear JSON error responses (RFC 9457 problem details) with the right HTTP status codes
- Unit tests (JUnit 5 + Mockito) and a CI pipeline on GitHub Actions
- Runs with Docker Compose

## Tech stack

| Area | Tools |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot (Web MVC, Data JPA, Validation, Actuator) |
| Database | PostgreSQL 16 (H2 in-memory for tests) |
| Testing | JUnit 5, Mockito |
| DevOps | Docker, Docker Compose, GitHub Actions |
| Build | Maven |

## Project structure

```
src/main/java/com/devaryan/minibank
├── MinibankApplication.java     # entry point
├── controller/                  # REST endpoints
├── service/                     # business logic and transactions
├── repository/                  # database access (Spring Data JPA)
├── entity/                      # JPA entities (database tables)
├── dto/                         # request and response objects
└── exception/                   # custom exceptions and global error handler
```

## Running locally

**Requirements:** Java 21 and Docker.

```bash
# 1. Start PostgreSQL (on port 5433)
docker compose up -d db

# 2. Run the app
./mvnw spring-boot:run          # Windows: .\mvnw spring-boot:run

# 3. Check it's up
curl http://localhost:8080/actuator/health
```

Or run everything (database + app) in Docker:

```bash
docker compose up --build
```

Database settings can be overridden with the environment variables `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

## Running tests

```bash
./mvnw test
```

Tests use an in-memory H2 database, so PostgreSQL isn't needed.

## API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/accounts` | Create an account |
| `GET` | `/api/accounts` | List all accounts |
| `GET` | `/api/accounts/{accountNumber}` | Get one account |
| `POST` | `/api/accounts/{accountNumber}/deposit` | Deposit money |
| `POST` | `/api/accounts/{accountNumber}/withdraw` | Withdraw money |
| `POST` | `/api/transfers` | Transfer between accounts |

### Examples

**Create an account**

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountHolder": "Dev Aryan"}'
```

```json
{
  "accountNumber": "MB4821937465",
  "accountHolder": "Dev Aryan",
  "balance": 0,
  "createdAt": "2026-11-06T08:30:00Z"
}
```

**Deposit**

```bash
curl -X POST http://localhost:8080/api/accounts/MB4821937465/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000}'
```

**Transfer**

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{"fromAccount": "MB4821937465", "toAccount": "MB1029384756", "amount": 1500}'
```

### Errors

| Status | When |
|---|---|
| `400 Bad Request` | Invalid input, insufficient funds, invalid amount |
| `404 Not Found` | Account doesn't exist |
| `409 Conflict` | The account was changed by another request at the same time |

Example:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Insufficient funds in account MB1029384756"
}
```

## Author

**Dev Aryan**
