# 🏗️ Clean Architecture - Folder Structure

This document describes the folder structure and organization of the Transaction Stream Processor project, following Clean Architecture principles.

## 📁 Folder Structure

```
src/main/java/io/rafaalberto/transaction_stream_processor/
├── domain/                    # Core business logic (innermost layer)
│   ├── model/                 # Domain entities and value objects
│   ├── service/               # Domain services (pure business logic)
│   └── exception/             # Domain-specific exceptions
│
├── application/               # Use cases and application logic
│   ├── usecase/              # Use case implementations
│   ├── port/                 # Ports (interfaces for adapters)
│   │   ├── input/            # Input ports (use case interfaces)
│   │   └── output/           # Output ports (repository, messaging interfaces)
│   └── dto/                  # Application DTOs (if needed)
│
├── infrastructure/            # External concerns (outer layer)
│   ├── persistence/          # Database implementations
│   │   ├── entity/           # JPA entities (if using JPA)
│   │   ├── repository/       # Repository implementations
│   │   └── mapper/           # Entity-Domain mappers
│   ├── messaging/            # Kafka producers/consumers
│   │   ├── producer/         # Event producers
│   │   ├── consumer/         # Event consumers
│   │   └── config/           # Kafka configuration
│   └── config/               # Infrastructure configuration
│
└── adapter/                   # Interface adapters
    ├── api/                   # REST controllers
    │   ├── controller/        # REST endpoints
    │   ├── dto/               # Request/Response DTOs
    │   └── mapper/            # DTO-Domain mappers
    └── messaging/             # Message adapters (if separate from infrastructure)
```

## 🎯 Layer Descriptions

### 1. `domain/` - Core Business Logic (Innermost Layer)

**Purpose:** Contains the core business logic and rules that are completely independent of frameworks, databases, and external systems.

**Why this name:**
- **`domain/`**: Represents the business domain - the heart of your application
- **`model/`**: Contains domain entities and value objects that represent core business concepts
- **`service/`**: Contains domain services - business logic that doesn't naturally fit within a single entity
- **`exception/`**: Domain-specific exceptions that represent business rule violations

**Key Principles:**
- No dependencies on external frameworks
- Pure Java business logic
- Framework-agnostic code
- Contains the most stable and reusable code

**Example contents:**
- `Transaction` entity
- `TransactionStatus` value object
- `TransactionValidator` domain service
- `InvalidTransactionException` domain exception

---

### 2. `application/` - Use Cases and Application Logic

**Purpose:** Orchestrates use cases and defines the application's behavior. Acts as a bridge between the domain and infrastructure layers.

**Why this name:**
- **`application/`**: Represents the application layer that orchestrates business workflows
- **`usecase/`**: Contains use case implementations (e.g., `ProcessTransactionUseCase`, `IngestTransactionUseCase`)
- **`port/`**: Defines interfaces (ports) that the application needs
  - **`input/`**: Input ports - interfaces that use cases implement (driven by adapters)
  - **`output/`**: Output ports - interfaces for repositories, messaging, etc. (implemented by infrastructure)
- **`dto/`**: Application-level DTOs if needed (though prefer domain objects when possible)

**Key Principles:**
- Depends only on `domain/`
- Defines contracts (ports) that infrastructure must implement
- Contains orchestration logic, not business rules
- Business rules stay in `domain/`

**Example contents:**
- `ProcessTransactionUseCase`
- `TransactionRepository` (output port interface)
- `EventPublisher` (output port interface)
- `TransactionUseCase` (input port interface)

---

### 3. `infrastructure/` - External Concerns (Outer Layer)

**Purpose:** Implements all external dependencies and framework-specific code. This is where you interact with databases, message brokers, file systems, etc.

**Why this name:**
- **`infrastructure/`**: Represents the technical infrastructure layer
- **`persistence/`**: Database-related implementations
  - **`entity/`**: JPA entities (database representation, separate from domain entities)
  - **`repository/`**: Concrete repository implementations (implements output ports from `application/port/output/`)
  - **`mapper/`**: Converts between JPA entities and domain entities
- **`messaging/`**: Kafka-related implementations
  - **`producer/`**: Event producers that publish to Kafka
  - **`consumer/`**: Event consumers that process Kafka messages
  - **`config/`**: Kafka configuration classes
- **`config/`**: Infrastructure configuration (database config, connection pools, etc.)

**Key Principles:**
- Implements interfaces defined in `application/port/output/`
- Can depend on frameworks (Spring, JPA, Kafka)
- Handles all technical concerns
- Isolated from business logic

**Example contents:**
- `JpaTransactionRepository` (implements `TransactionRepository` port)
- `KafkaTransactionProducer` (implements `EventPublisher` port)
- `TransactionEntity` (JPA entity)
- `TransactionEntityMapper`

---

### 4. `adapter/` - Interface Adapters

**Purpose:** Adapts external interfaces (like REST APIs) to the application layer. Translates between external formats and application models.

**Why this name:**
- **`adapter/`**: Implements the Adapter pattern - adapts external interfaces to application needs
- **`api/`**: REST API adapters
  - **`controller/`**: REST controllers (Spring `@RestController`)
  - **`dto/`**: Request/Response DTOs for API contracts
  - **`mapper/`**: Converts between API DTOs and domain/application models
- **`messaging/`**: Message adapters (if you want to separate message handling from infrastructure)

**Key Principles:**
- Translates external formats (JSON, HTTP) to domain/application models
- Implements input ports from `application/port/input/`
- Handles HTTP concerns (validation, serialization)
- Thin layer - delegates to use cases

**Example contents:**
- `TransactionController` (REST endpoints)
- `TransactionRequestDto`, `TransactionResponseDto`
- `TransactionDtoMapper`

---

## 🔄 Dependency Flow

The dependency rule in Clean Architecture states that **dependencies should point inward**:

```
adapter → application → domain
infrastructure → application → domain
```

**Rules:**
- ✅ `adapter/` can depend on `application/` and `domain/`
- ✅ `infrastructure/` can depend on `application/` and `domain/`
- ✅ `application/` can depend on `domain/`
- ❌ `domain/` **cannot** depend on anything else
- ❌ `application/` **cannot** depend on `adapter/` or `infrastructure/`
- ❌ `adapter/` and `infrastructure/` **cannot** depend on each other

This ensures that:
- Business logic remains independent and testable
- You can swap out frameworks without changing business logic
- The domain layer is the most stable and reusable

---

## 🔄 Data Flow Example

### Ingesting a Transaction:

1. **`adapter/api/controller/TransactionController`**
   - Receives HTTP POST request
   - Maps `TransactionRequestDto` to domain model
   - Calls use case from `application/usecase/`

2. **`application/usecase/IngestTransactionUseCase`**
   - Orchestrates the ingestion workflow
   - Uses domain services for validation
   - Calls output ports (repository, event publisher)

3. **`domain/service/TransactionValidator`**
   - Validates transaction business rules
   - Throws domain exceptions if invalid

4. **`infrastructure/persistence/repository/JpaTransactionRepository`**
   - Implements `TransactionRepository` port
   - Persists transaction to database

5. **`infrastructure/messaging/producer/KafkaTransactionProducer`**
   - Implements `EventPublisher` port
   - Publishes event to Kafka topic

---

## 📝 Alternative Naming Conventions

If you prefer different terminology, here are some alternatives:

- **`usecase/`** → `service/` (if you prefer "service" for use cases)
- **`port/`** → `interface/` or `contract/`
- **`infrastructure/`** → `adapter/` (if you want to merge infrastructure and adapters)
- **`adapter/`** → `presentation/` or `interface/`

The important thing is consistency and clarity within your team.

---

## 🎓 Benefits of This Structure

1. **Testability**: Domain and application layers can be tested without frameworks
2. **Maintainability**: Clear separation of concerns makes code easier to understand
3. **Flexibility**: Easy to swap databases, message brokers, or frameworks
4. **Scalability**: Each layer can evolve independently
5. **Team Collaboration**: Different teams can work on different layers with minimal conflicts

---

## 📚 References

This structure follows:
- **Clean Architecture** by Robert C. Martin
- **Hexagonal Architecture** (Ports and Adapters)
- **Domain-Driven Design** principles

For more information, see:
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
