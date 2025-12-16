# 🏗️ Clean Architecture - Folder Structure

This document describes the folder structure and organization of the Transaction Stream Processor project, following Clean Architecture principles.

## 📁 Folder Structure

```
src/main/java/io/rafaalberto/transactionstreamprocessor/
├── domain/                        # Core business logic (innermost layer)
│   ├── entity/                    # Domain entities and value objects
│   └── exception/                 # Domain-specific exceptions
│   └── service/                   # (planned) Domain services
│
├── application/                   # Use cases and application logic
│   ├── usecases/                  # Use case implementations
│   ├── port/                      # (planned) Ports (interfaces for adapters)
│   │   ├── input/                 # (planned) Input ports (use case interfaces)
│   │   └── output/                # (planned) Output ports (repository, messaging interfaces)
│   └── dto/                       # (planned) Application DTOs (if needed)
│
└── infrastructure/                # External concerns (outer layer)
    ├── http/                      # HTTP/REST adapters
    │   ├── controller/            # HTTP controllers
    │   ├── request/               # Request DTOs
    │   └── response/              # Response DTOs
    ├── persistence/               # (planned) Database implementations
    │   ├── entity/                # (planned) JPA entities (if using JPA)
    │   ├── repository/            # (planned) Repository implementations
    │   └── mapper/                # (planned) Entity-Domain mappers
    ├── messaging/                 # (planned) Kafka producers/consumers
    │   ├── producer/              # (planned) Event producers
    │   ├── consumer/              # (planned) Event consumers
    │   └── config/                # (planned) Kafka configuration
    └── config/                    # (planned) Infrastructure configuration
```

## 🎯 Layer Descriptions

### 1. `domain/` - Core Business Logic (Innermost Layer)

**Purpose:** Contains the core business logic and rules that are completely independent of frameworks, databases, and external systems.

**Why this name:**
- **`domain/`**: Represents the business domain - the heart of your application
- **`entity/`**: Contains domain entities and value objects that represent core business concepts
- **`service/`**: (Planned) Domain services - business logic that doesn't naturally fit within a single entity
- **`exception/`**: Domain-specific exceptions that represent business rule violations

**Key Principles:**
- No dependencies on external frameworks
- Pure Java business logic
- Framework-agnostic code
- Contains the most stable and reusable code

**Example contents (implemented):**
- `Transaction` entity
- `TransactionID` value object
- `InvalidTransactionException` domain exception

**Example contents (planned):**
- Additional domain services (e.g., `TransactionValidator`)

---

### 2. `application/` - Use Cases and Application Logic

**Purpose:** Orchestrates use cases and defines the application's behavior. Acts as a bridge between the domain and infrastructure layers.

**Why this name:**
- **`application/`**: Represents the application layer that orchestrates business workflows
- **`usecases/`**: Contains use case implementations (e.g., `CreateTransactionUseCase`)
- **`port/`**: (Planned) Defines interfaces (ports) that the application needs
  - **`input/`**: (Planned) Input ports - interfaces that use cases implement (driven by adapters)
  - **`output/`**: (Planned) Output ports - interfaces for repositories, messaging, etc. (implemented by infrastructure)
- **`dto/`**: (Planned) Application-level DTOs if needed (though prefer domain objects when possible)

**Key Principles:**
- Depends only on `domain/`
- Contains orchestration logic, not business rules
- Business rules stay in `domain/`

**Example contents (implemented):**
- `CreateTransactionUseCase`
- `CreateTransactionCommand`

**Example contents (planned):**
- `TransactionRepository` (output port interface)
- `EventPublisher` (output port interface)
- Input port interfaces (e.g., `CreateTransactionUseCase` as an interface)

---

### 3. `infrastructure/` - External Concerns (Outer Layer)

**Purpose:** Implements all external dependencies and framework-specific code. This is where you interact with databases, message brokers, HTTP interfaces, file systems, etc.

**Why this name:**
- **`infrastructure/`**: Represents the technical infrastructure layer
- **`http/`**: HTTP/REST adapters (interface adapters for web APIs)
  - **`controller/`**: HTTP controllers (ready to be annotated with Spring `@RestController` later)
  - **`request/`**: Request DTOs for API contracts
  - **`response/`**: Response DTOs for API contracts
- **`persistence/`**: (Planned) Database-related implementations
  - **`entity/`**: (Planned) JPA entities (database representation, separate from domain entities)
  - **`repository/`**: (Planned) Concrete repository implementations (implements output ports from `application/port/output/`)
  - **`mapper/`**: (Planned) Converts between JPA entities and domain entities
- **`messaging/`**: (Planned) Kafka-related implementations
  - **`producer/`**: (Planned) Event producers that publish to Kafka
  - **`consumer/`**: (Planned) Event consumers that process Kafka messages
  - **`config/`**: (Planned) Kafka configuration classes
- **`config/`**: (Planned) Infrastructure configuration (database config, connection pools, etc.)

**Key Principles (current state):**
- Only the `http/` layer is implemented
- HTTP controllers are thin and delegate to use cases
- HTTP DTOs live in `request/` and `response/`
- Business logic stays in `domain/` and `application/`

**Key Principles (planned):**
- Persistence and messaging implementations will live under `persistence/` and `messaging/`
- Infrastructure will implement ports defined in `application/port/output/`
- Infrastructure can depend on frameworks (Spring, JPA, Kafka)

**Example contents (implemented):**
- `TransactionController` (HTTP entrypoint)
- `CreateTransactionRequest`, `TransactionResponse` (HTTP DTOs)

**Example contents (planned):**
- `JpaTransactionRepository` (implements `TransactionRepository` port)
- `KafkaTransactionProducer` (implements `EventPublisher` port)
- `TransactionEntity` (JPA entity)
- `TransactionEntityMapper`

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

## 🔄 Data Flow Example

### Ingesting a Transaction (current implementation):

1. **`infrastructure/http/controller/TransactionController`**
   - Receives an HTTP-like request (no framework wiring yet)
   - Maps `CreateTransactionRequest` to `CreateTransactionCommand`
   - Calls use case from `application/usecases/`

2. **`application/usecases/CreateTransactionUseCase`**
   - Orchestrates the transaction creation workflow
   - Creates domain entity (which validates business rules)
   - Returns the `Transaction` to the controller

3. **`domain/entity/Transaction`**
   - Validates transaction business rules during construction
   - Throws domain exceptions if invalid

4. **`infrastructure/http/controller/TransactionController`**
   - Maps the `Transaction` to `TransactionResponse`
   - Returns the response DTO

### Ingesting a Transaction (planned full flow):

1. **`infrastructure/http/controller/TransactionController`**
   - Receives HTTP POST request (via Spring MVC)
   - Maps `CreateTransactionRequest` to `CreateTransactionCommand`
   - Calls use case from `application/usecases/`

2. **`application/usecases/CreateTransactionUseCase`**
   - Orchestrates the transaction creation workflow
   - Uses domain entities and services to enforce business rules
   - Calls output ports (repository, event publisher)

3. **`domain/entity/Transaction`**
   - Validates transaction business rules during construction
   - Throws domain exceptions if invalid

4. **`infrastructure/persistence/repository/JpaTransactionRepository`**
   - (Planned) Implements `TransactionRepository` port (from `application/port/output/`)
   - (Planned) Persists transaction to database

5. **`infrastructure/messaging/producer/KafkaTransactionProducer`**
   - (Planned) Implements `EventPublisher` port (from `application/port/output/`)
   - (Planned) Publishes event to Kafka topic

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
