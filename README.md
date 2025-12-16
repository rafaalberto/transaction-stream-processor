# 🚀 Transaction Stream Processor

[![CI](https://img.shields.io/github/actions/workflow/status/rafaalberto/transaction-stream-processor/ci.yml?label=CI&logo=githubactions&logoColor=white)](https://github.com/rafaalberto/transaction-stream-processor/actions/workflows/ci.yml)
[![Quality Gate](https://img.shields.io/github/actions/workflow/status/rafaalberto/transaction-stream-processor/ci.yml?label=Quality%20Gate&logo=checkmarx&logoColor=white)](https://github.com/rafaalberto/transaction-stream-processor/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Build](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 Overview

**Transaction Stream Processor** is an event-driven microservice designed to ingest, validate, and process financial transactions in real time.  
It demonstrates modern distributed-systems concepts used in fintech environments, including:

- **Clean Architecture** & Domain-Driven Design (DDD) principles
- Framework-agnostic domain and application layers
- Event-driven architecture (planned)
- Apache Kafka producers and consumers (planned)
- Transaction persistence (planned)
- Dead-letter queue (DLQ) handling (planned)
- Outbox-style *"persist + publish"* atomic workflow (planned)

This repository is part of my public portfolio and reflects practical experience gained from real-world financial systems.

> 🛠 **Note:** This project is a **work in progress**. Currently implementing the core architecture with no-framework code. Spring Boot, Kafka, and persistence layers will be added next.

---

## 🎯 Purpose of the Service

This service focuses specifically on **transaction ingestion and processing**, not balance calculations.  
Its core responsibilities include:

### ✅ **Currently Implemented**

**1. Domain Layer (Core Business Logic)**
- `Transaction` entity with business rules validation
- `TransactionID` value object
- Domain exceptions (`InvalidTransactionException`)
- Framework-agnostic, pure Java business logic

**2. Application Layer (Use Cases)**
- `CreateTransactionUseCase` - orchestrates transaction creation
- Command objects (`CreateTransactionCommand`)
- Clean separation from infrastructure concerns

**3. Infrastructure Layer (HTTP Adapters)**
- `TransactionController` - HTTP controller structure (ready for Spring integration)
- Request/Response DTOs (`CreateTransactionRequest`, `TransactionResponse`)
- No framework dependencies in business logic

### 🔄 **Planned Features**

**1. Ingesting external transaction requests**
A REST API (with Spring Boot) will receive incoming transactions and publish them to the Kafka topic `TRANSACTIONS.EVENTS`.

**2. Processing events asynchronously**
A processing module will consume Kafka events, execute validation rules, and determine the transaction's outcome.

**3. Persisting and publishing results atomically**
A dedicated handler will perform:
- Transaction persistence (via repository pattern)
- Publication of a new event `TRANSACTIONS.PROCESSED`

This ensures downstream services can react (Fraud, Notification, Audit, etc.).

**4. Handling invalid data safely**
Invalid transactions will be routed to **`TRANSACTIONS.EVENTS.DLQ`** for later investigation.

---

## 🧩 High-Level Architecture

Place your exported diagram in the folder `docs/diagram.png` and reference it like this:

![Architecture Diagram](docs/diagram.jpg)

---

## 🏗 Project Structure

The project follows **Clean Architecture** principles with clear layer separation:

### **✅ Implemented Layers**

**1. Domain Layer** (`domain/`)
- Core business entities and value objects
- Business rules and validation
- Domain exceptions
- Zero framework dependencies

**2. Application Layer** (`application/`)
- Use case implementations
- Command objects
- Port interfaces (to be implemented)
- Orchestrates domain logic

**3. Infrastructure Layer** (`infrastructure/`)
- HTTP adapters (controllers, DTOs)
- Ready for Spring Boot integration
- Will include persistence and messaging adapters

### **🔄 Planned Modules**

**1. Transaction Ingress Module**
- Spring Boot REST API integration
- Kafka event publishing
- Ensures decoupling between API and processing logic

**2. Transaction Processing Module**
- Kafka event consumers
- Validation rules execution
- Use case orchestration
- Transaction persistence
- Processed event publishing
- DLQ handling for invalid transactions

**3. Downstream Consumers (External)**
These are not part of the service but illustrate event propagation:

- **Notification Service**
- **Fraud Detection**
- **Audit Trail Processor**

For detailed architecture documentation, see [ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 🛠 Tech Stack

### ✅ **Currently Used**
- **Java 21** - Modern Java features
- **Gradle** - Build tool
- **JUnit 5** - Unit testing
- **AssertJ** - Fluent assertions
- **Mockito** - Mocking framework
- **Spotless** - Code formatting
- **Checkstyle** - Static code analysis

### 🔄 **Planned**
- **Spring Boot** - Framework integration (dependencies added, integration pending)
- **Apache Kafka** - Event streaming
- **JPA / Database** - Transaction persistence
- **Docker / Docker Compose** - Containerization
- **Testcontainers** - Integration testing

---

# 🧪 Running Tests

```
./gradlew test
./gradlew integrationTest
```

---

# 🔍 Code Quality — Quick Reference

This project includes a lightweight, production-style quality pipeline designed to keep the codebase clean, safe, and maintainable.

## 🚀 Why This Matters
Linting enforces:

- Consistent formatting
- Safe coding patterns
- Reduced complexity
- High maintainability

These practices reflect standards used in fintech and high-availability backend systems.

## ⚙️ Tools
- **Spotless** — formatting (Google Java Style)
- **Checkstyle** — static analysis & best practices
- **EditorConfig** — editor-agnostic consistency

## 🧭 Most Important Commands

**Format code (auto-fix)**
```
./gradlew spotlessApply
```

**Verify formatting**
```
./gradlew spotlessCheck
```

**Run static analysis**
```
./gradlew checkstyleMain checkstyleTest
```

**Full quality gate (CI equivalent)**
```
./gradlew check
```

## 📊 Reports
- Checkstyle: `build/reports/checkstyle/checkstyle.html`
- Tests: `build/reports/tests/`

---

## 📌 Roadmap (WIP)

### ✅ **Completed**
- [x] Clean Architecture structure
- [x] Domain layer with entities and value objects
- [x] Application layer with use cases
- [x] Infrastructure HTTP layer structure
- [x] Unit tests for domain and application layers
- [x] Code quality tools (Spotless, Checkstyle)

### 🔄 **In Progress / Planned**
- [ ] Create output ports (`application/port/output/`)
- [ ] Integrate Spring Boot with HTTP controllers
- [ ] Implement repository pattern for persistence
- [ ] Define Kafka topics and schemas
- [ ] Implement Kafka producers and consumers
- [ ] Add idempotency strategy
- [ ] Add DLQ consumer
- [ ] Add integration tests with Testcontainers

---

## 🤝 Contributing

This repo is part of my professional portfolio but contributions (issues, suggestions, discussions) are welcome.

---

## 📄 License

MIT License — feel free to use this project for learning and inspiration.

---

## ⭐ If you like this project…

Please give it a **star** on GitHub — it helps a lot! 🌟
