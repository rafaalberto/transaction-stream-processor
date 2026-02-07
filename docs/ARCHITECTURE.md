# 🏗️ Clean Architecture - Folder Structure

This document describes the folder structure and organization of the Transaction Stream Processor project, following Clean Architecture principles.

## 📁 Folder Structure

```
src/main/java/io/rafaalberto/transactionstreamprocessor/
├── domain/                        # Core business logic (innermost layer)
│   └── transaction/
│       ├── Transaction            # Domain entity
│       ├── TransactionID          # Value object
│       ├── Money                   # Value object
│       ├── Currency                # Value object
│       ├── TransactionStatus       # Value object
│       ├── TransactionType         # Value object
│       └── exception/              # Domain-specific exceptions
│
├── application/                   # Use cases and application logic
│   ├── events/                    # Application events (DTOs for messaging)
│   ├── publisher/                 # Output ports (event publishers)
│   ├── repository/                # Output port (persistence)
│   └── usecases/                  # Use case implementations + commands
│
└── infrastructure/                # External concerns (outer layer)
    ├── config/                    # Spring and infrastructure configuration
    ├── http/                      # HTTP/REST adapters
    │   ├── controller/            # Controllers (delegate to use cases)
    │   ├── exception/             # Global exception handling
    │   ├── request/               # Request DTOs
    │   ├── resource/              # REST entrypoints
    │   └── response/              # Response DTOs
    ├── messaging/                 # Kafka producers and consumers
    │   ├── consumer/              # Kafka listeners
    │   └── publisher/             # Kafka producer implementations
    └── persistence/               # Database implementations
        └── jpa/                   # JPA entities, repository, mapper
```

## 🎯 Layer Descriptions

### 1. `domain/` - Core Business Logic (Innermost Layer)

**Purpose:** Contains the core business logic and rules that are completely independent of frameworks, databases, and external systems.

**Structure:**
- **`domain/transaction/`**: Holds the transaction aggregate: entity, value objects, and domain exceptions in one place.
- **`exception/`**: Domain-specific exceptions that represent business rule violations (`InvalidTransactionException`, `TransactionNotFoundException`).

**Key Principles:**
- No dependencies on external frameworks
- Pure Java business logic
- Framework-agnostic code
- Contains the most stable and reusable code

**Example contents (implemented):**
- `Transaction` entity (with `create`, `restore`, `process`)
- Value objects: `TransactionID`, `Money`, `Currency`, `TransactionStatus`, `TransactionType`
- Domain exceptions: `InvalidTransactionException`, `TransactionNotFoundException`

---

### 2. `application/` - Use Cases and Application Logic

**Purpose:** Orchestrates use cases and defines the application's behavior. Acts as a bridge between the domain and infrastructure layers. Defines ports (interfaces) that infrastructure implements.

**Structure:**
- **`events/`**: Application events used for messaging (`TransactionCreatedEvent`, `TransactionProcessedEvent`).
- **`publisher/`**: Output ports for publishing events (`TransactionEventPublisher`, `TransactionProcessedEventPublisher`).
- **`repository/`**: Output port for persistence (`TransactionRepository`).
- **`usecases/`**: Use case implementations and their commands (e.g., `CreateTransactionUseCase`, `CreateTransactionCommand`, `GetTransactionByIdUseCase`, `ProcessTransactionUseCase`).

**Key Principles:**
- Depends only on `domain/`
- Contains orchestration logic, not business rules
- Business rules stay in `domain/`
- Ports are interfaces; infrastructure provides implementations

**Example contents (implemented):**
- Use cases: `CreateTransactionUseCase`, `GetTransactionByIdUseCase`, `ProcessTransactionUseCase`
- Commands: `CreateTransactionCommand`
- Ports: `TransactionRepository`, `TransactionEventPublisher`, `TransactionProcessedEventPublisher`
- Events: `TransactionCreatedEvent`, `TransactionProcessedEvent`

---

### 3. `infrastructure/` - External Concerns (Outer Layer)

**Purpose:** Implements all external dependencies and framework-specific code. This is where you interact with databases, message brokers, HTTP interfaces, etc.

**Structure:**
- **`config/`**: Spring and infrastructure configuration (use cases, HTTP, JPA, Kafka topics and consumer config).
- **`http/`**: HTTP/REST adapters
  - **`resource/`**: REST entrypoints (`TransactionResource`)
  - **`controller/`**: Controllers that map request/response and delegate to use cases
  - **`request/`**, **`response/`**: Request and response DTOs
  - **`exception/`**: Global exception handling (`AppExceptionHandler`, `ErrorResponse`)
- **`persistence/jpa/`**: Database implementation
  - **`TransactionEntity`**: JPA entity
  - **`JpaTransactionRepository`**: Implements `TransactionRepository`
  - **`TransactionEntityMapper`**: Maps between entity and domain
- **`messaging/`**: Kafka producers and consumers
  - **`publisher/`**: `KafkaTransactionEventPublisher`, `KafkaTransactionProcessedPublisher`, `LogTransactionEventPublisher` (profile-based)
  - **`consumer/`**: `TransactionCreatedEventConsumer` (consumes events, runs `ProcessTransactionUseCase`, publishes processed event)

**Key Principles:**
- HTTP, persistence, and messaging are all implemented.
- REST resources and controllers are thin and delegate to use cases.
- Infrastructure implements the ports defined in `application/` (repository, publishers).
- Infrastructure can depend on frameworks (Spring, JPA, Kafka).

**Example contents (implemented):**
- HTTP: `TransactionResource`, `CreateTransactionController`, `GetTransactionByIdController`, `AppExceptionHandler`, `CreateTransactionRequest`, `TransactionResponse`, `TransactionDetailsResponse`, `ErrorResponse`
- Persistence: `JpaTransactionRepository`, `TransactionEntity`, `TransactionEntityMapper`, `TransactionJpaRepository`
- Messaging: `KafkaTransactionEventPublisher`, `KafkaTransactionProcessedPublisher`, `TransactionCreatedEventConsumer`, `KafkaTopics`, `KafkaConsumerConfig`
- Config: `ApplicationUseCaseConfig`, `HttpControllerConfig`, `JpaPersistenceConfig`

---

## 🔄 Dependency Flow

The dependency rule in Clean Architecture states that **dependencies should point inward**:

```
infrastructure → application → domain
```

**Rules:**
- ✅ `infrastructure/` can depend on `application/` and `domain/`
- ✅ `application/` can depend on `domain/`
- ❌ `domain/` **cannot** depend on anything else
- ❌ `application/` **cannot** depend on `infrastructure/`
- ❌ Different parts of `infrastructure/` (e.g., `http/`, `persistence/`, `messaging/`) **should not** depend on each other

This ensures that:
- Business logic remains independent and testable
- You can swap out frameworks without changing business logic
- The domain layer is the most stable and reusable

---

## 🔄 Data Flow

### Flow A – Creating a transaction (HTTP)

1. **`infrastructure/http/resource/TransactionResource`**
   - Receives HTTP POST at `/transactions`, delegates to `CreateTransactionController`.

2. **`infrastructure/http/controller/CreateTransactionController`**
   - Maps `CreateTransactionRequest` to `CreateTransactionCommand`, calls `CreateTransactionUseCase.execute`.

3. **`application/usecases/CreateTransactionUseCase`**
   - Optionally finds existing by `externalReference` (idempotency).
   - Builds `Transaction` via `Transaction.create(...)` (domain validates).
   - Saves via `TransactionRepository.save` and publishes via `TransactionEventPublisher.publish`.

4. **Infrastructure**
   - **Persistence:** `JpaTransactionRepository` persists the transaction.
   - **Messaging:** When profile `kafka` is active, `KafkaTransactionEventPublisher` publishes `TransactionCreatedEvent` to Kafka.

5. **Back to HTTP**
   - Controller maps `Transaction` to `TransactionResponse`; resource returns 201 with the response body.

### Flow B – Processing a transaction (async, Kafka)

1. **`infrastructure/messaging/consumer/TransactionCreatedEventConsumer`**
   - Consumes `TransactionCreatedEvent` from the Kafka topic (profile `kafka`).

2. **Consumer**
   - Calls `ProcessTransactionUseCase.execute(transactionId)`.

3. **`application/usecases/ProcessTransactionUseCase`**
   - Loads transaction via `TransactionRepository.findById`, calls `transaction.process()` (domain enforces CREATED → PROCESSED), saves the updated transaction.

4. **Consumer**
   - Builds `TransactionProcessedEvent` from the processed transaction and publishes it via `TransactionProcessedEventPublisher` (Kafka implementation).

**Next (planned):** DLQ for consumer failures; Outbox pattern for atomic persist + publish on creation.

---

## 📝 Alternative Naming Conventions

If you prefer different terminology, here are some alternatives:

- **`usecase/`** → `service/` (if you prefer "service" for use cases)
- **`port/`** → `interface/` or `contract/`
- **`infrastructure/http/`** → `adapter/api/` or `presentation/api/` (if you prefer separating adapters from infrastructure)
- **`infrastructure/http/request/`** → `infrastructure/http/dto/` (if you prefer "dto" over "request/response")

The important thing is consistency and clarity within your team. This project uses `infrastructure/http/` to keep all framework-specific code in the infrastructure layer.

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
