# 🚀 Transaction Stream Processor

[![CI](https://img.shields.io/github/actions/workflow/status/rafaalberto/transaction-stream-processor/ci.yml?label=CI&logo=githubactions&logoColor=white)](https://github.com/rafaalberto/transaction-stream-processor/actions/workflows/ci.yml)
[![Quality Gate](https://img.shields.io/github/actions/workflow/status/rafaalberto/transaction-stream-processor/ci.yml?label=Quality%20Gate&logo=checkmarx&logoColor=white)](https://github.com/rafaalberto/transaction-stream-processor/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Build](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 Overview

**Transaction Stream Processor** is an event-driven backend service designed to ingest, validate, persist, and process financial transactions.

This project is intentionally built as a **portfolio-grade system**, focusing on **architecture, correctness, and scalability**, rather than CRUD scaffolding.

It demonstrates real-world backend engineering practices commonly used in fintech and distributed systems, including clean boundaries, strong validation, explicit use cases, and production-style testing.

> ⚠️ This project is a **work in progress by design**. Kafka-based event streaming and asynchronous processing are planned next.

---

## 🎯 Service Responsibility

This service is responsible for **transaction ingestion and lifecycle tracking**, not balance calculation.

### What it does today
- Accepts transaction creation requests via HTTP
- Validates input at API and domain boundaries
- Persists transactions in PostgreSQL
- Allows querying transactions by ID
- Exposes consistent and user-friendly error responses

### What it will do next
- Publish transaction events to Kafka
- Process transactions asynchronously
- Handle failures using DLQ patterns
- Guarantee atomic persist + publish using the Outbox pattern

---

## 🧠 Architectural Rationale

This project was designed to demonstrate how a backend system can evolve incrementally while preserving architectural integrity.

The core follows Clean Architecture principles, with a strict separation between domain, application, and infrastructure layers. The domain and use cases are completely framework-agnostic, allowing infrastructure concerns (HTTP, persistence, messaging) to change without impacting business rules.

Instead of introducing Kafka and persistence upfront, the system was intentionally built in stages:
- First, by modeling the domain and its invariants.
- Then, by introducing persistence with PostgreSQL and Flyway.
- And only afterwards, preparing the system for event-driven processing with Kafka.

This approach mirrors real-world systems, where architecture must support continuous change rather than assume perfect requirements from day one.

The goal is not to showcase as many technologies as possible, but to demonstrate conscious architectural decisions, low coupling, and a sustainable path for future evolution.

## ⚖️ Design Trade-offs

Some design decisions were made deliberately to balance simplicity, clarity, and future extensibility:

- **No premature abstractions**  
  The code avoids generic mappers, base repositories, or overly flexible interfaces until there is a real need. This keeps the codebase easy to read and reason about.

- **Use cases as concrete classes**  
  Application use cases are implemented as concrete classes instead of interfaces, reducing indirection and improving readability. Abstractions are introduced only when multiple implementations become necessary.

- **Incremental persistence strategy**  
  The project started with in-memory implementations and evolved to PostgreSQL using JPA and Flyway. This allowed the domain and application layers to stabilize before introducing infrastructure complexity.

- **Test strategy aligned with evolution stage**  
  The project combines pure unit tests for domain and application logic with slice tests for HTTP controllers. Full integration tests are introduced once infrastructure components become relevant.

These trade-offs favor long-term maintainability and clear intent over architectural perfection or pattern-heavy designs.

---

## 🧱 Architecture Overview

The project follows **Clean Architecture**, enforcing strict dependency rules:

```
domain → application → infrastructure
```

- **Domain**: pure business logic, no framework dependencies  
- **Application**: use cases and orchestration  
- **Infrastructure**: HTTP, persistence, messaging, configuration  

An architecture diagram is available at:

```
docs/diagram.jpg
```

---
## 🧪 Testing Strategy

The project uses multiple testing layers to ensure correctness and confidence:

- **Domain tests** — pure business rules
- **Use case tests** — orchestration and behavior
- **Resource tests (`@WebMvcTest`)** — HTTP contract, validation, error handling
- **Integration tests** — real PostgreSQL using Testcontainers

Run tests locally:

```bash
./gradlew test
./gradlew integrationTest
```

---

## 🏗 Project Structure

```
src/main/java
├─ domain
│ └─ transaction
│   ├─ Transaction
│   ├─ TransactionID
│   ├─ Money
│   ├─ Currency
│   ├─ TransactionStatus
│   └─ exception
│
├─ application
│ ├─ usecases
│ │ ├─ CreateTransactionUseCase
│ │ └─ GetTransactionByIdUseCase
│ └─ repository
│   └─ TransactionRepository
│
└─ infrastructure
  ├─ http
  │ ├─ resource
  │ ├─ controller
  │ ├─ request
  │ ├─ response
  │ └─ advice
  └─ persistence
    ├─ jpa
    └─ flyway
```

---

## 🛠 Tech Stack

### Currently Used
- **Java 21**
- **Spring Boot 4**
- **Gradle**
- **PostgreSQL**
- **Spring Data JPA**
- **Flyway**
- **Docker & Docker Compose**
- **JUnit 5**
- **Mockito**
- **AssertJ**
- **Testcontainers**
- **Spotless**
- **Checkstyle**

### Planned
- **Apache Kafka**
- **Dead Letter Queue (DLQ)**
- **Outbox Pattern**

---

## 🧑‍💻 Running Locally (Development Mode)

For local development, the application is typically run directly from the IDE
(IntelliJ, VS Code, etc.), while infrastructure dependencies are provided via Docker.

### Infrastructure (PostgreSQL + Kafka)

Start only the required infrastructure services:

```bash
docker compose -f docker-compose-dev.yml up

SPRING_PROFILES_ACTIVE=local,kafka ./gradlew bootRun
```

---
## 🐳 Running with Docker Compose

The application can be fully started locally using Docker Compose.

### Prerequisites
- Docker
- Docker Compose
- Available ports:
  - `8081` for the application
  - `5433` for PostgreSQL

### Start services

```bash
docker compose up --build
```

This will:
- Start PostgreSQL on port `5433`
- Build and start the application on port `8081`
- Execute Flyway migrations automatically

### Stop services

```bash
docker compose down
```

Remove volumes (reset database):

```bash
docker compose down -v
```

---

## 🔌 API Examples

### Create a transaction

```bash
curl -X POST http://localhost:8081/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100,
    "currency": "BRL",
    "type": "CREDIT",
    "occurredAt": "2025-03-23T11:00:00Z",
    "externalReference": "account-service::123"
  }'
```

### Get transaction by ID
Use the `id` returned by the create transaction endpoint.
```bash
curl -X GET http://localhost:8081/transactions/{transactionId}
```

---

## 🔍 Code Quality

Code quality is treated as a first-class concern.

### Tools
- **Spotless** — code formatting
- **Checkstyle** — static analysis
- **EditorConfig** — editor consistency

### Useful commands

```bash
./gradlew spotlessApply
./gradlew check
```

Reports are available under:

```bash
build/reports/
```

---

## 🗺 Roadmap

### ✅ Completed
- Clean Architecture foundation
- Domain modeling with invariants
- Explicit use cases
- REST API with validation and error handling
- PostgreSQL persistence
- Flyway migrations
- Docker Compose setup
- Unit and integration tests
- Code quality tooling
- Kafka topic design
- Event publishing on transaction creation

### 🔄 Next Steps
- DLQ handling
- Outbox pattern implementation

---

## 📄 License

MIT License — free to use for learning and inspiration.

---

## ⭐ If you find this project useful or interesting, consider starring the repository.
