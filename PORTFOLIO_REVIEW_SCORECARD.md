# Portfolio Review: Transaction Stream Processor

**Purpose:** International hiring process — codebase as portfolio.  
**Context:** Event-driven transaction processor (Spring Boot 4, Java 21, Kafka, PostgreSQL).  
**Note:** Controller advice is exercised via the resource layer (e.g. `TransactionResourceTest` with `@WebMvcTest`); acceptance tests cover end-to-end flows.

---

## Scorecard (grades)

| # | Topic | Score |
|---|--------|--------|
| 1 | Overengineering | **8 / 10** |
| 2 | Tests | **8 / 10** |
| 3 | SOLID | **8 / 10** |
| 4 | Design patterns | **10 / 10** |
| 5 | Clean code | **8 / 10** |
| 6 | Clean architecture | **10 / 10** |

**Overall:** **8.6 / 10**

*Scale: 0–10 (10 = excellent, 8 = very good, 6 = good, 4 = needs improvement, 2 = poor, 0 = critical gaps).*

---

## Scorecard Summary (detailed)

| Topic | Score (0–10) | Summary |
|-------|---------------|---------|
| **Overengineering** | 8/10 | Slight extra layering; otherwise well-scoped. |
| **Tests** | 8/10 | Strong pyramid: unit, integration, acceptance; advice via resource. |
| **SOLID** | 8/10 | Ports/adapters, SRP, DIP clear; minor LSP/ISP notes. |
| **Design patterns** | 10/10 | Repository, use case, DTO, exception mapping, event pub/sub. |
| **Clean code** | 8/10 | Readable, consistent; a few naming/edge-case fixes. |
| **Clean architecture** | 10/10 | Clear boundaries, dependency rule, framework-free core. |
| **Critical / must-fix** | — | 2 critical, 2 important (see below). |

---

## 1. Overengineering — 8/10

**What’s good**

- Single bounded context (transactions), no unnecessary abstractions.
- Domain is rich but focused: `Transaction`, `Money`, `TransactionID`, value objects, factory methods (`create` / `restore`).
- One repository port, two event publisher ports — justified by different event types.
- No speculative “future-proof” layers or interfaces.

**Where it might feel “heavy”**

- **Resource → Controller → Use case:** The HTTP stack has three steps: `TransactionResource` → `CreateTransactionController` / `GetTransactionByIdController` → use case. Controllers only map request → command and result → response. For a hiring reviewer, this is a **plus**: it shows you can separate HTTP from application and keep use cases free of web types. Not overengineered for a portfolio.
- **Two publisher interfaces** (`TransactionEventPublisher`, `TransactionProcessedEventPublisher`): Reasonable for distinct topics and semantics; could be one interface with a generic event type if you wanted to simplify, but current design is acceptable.

**Verdict:** Slight extra layering (Resource + Controller) is justified and demonstrates clean boundaries. **8/10**.

---

## 2. Tests — 8/10

**Structure**

- **Unit:** Domain (`Transaction`, `TransactionID`, `Money`), use cases (Create, GetById, Process), HTTP controllers, `TransactionEntityMapper`, **and** `TransactionResource` (with `@WebMvcTest`).
- **Integration:** HTTP + DB, Kafka consumer (processing, idempotency, DLQ), create-transaction idempotency. Testcontainers for Postgres and Kafka.
- **Acceptance:** Full flow: POST transaction → Kafka → GET returns PROCESSED; plus invalid payload → 400.

**Controller advice**

- There is **no** dedicated unit test for `AppExceptionHandler`.
- Advice **is** tested via:
  - **Resource layer:** `TransactionResourceTest` (`@WebMvcTest(TransactionResource.class)`) — validation (amount, currency, type, externalReference, invalid JSON) → 400 and `ErrorResponse`; `TransactionNotFoundException` → 404; invalid UUID path → 400.
  - **Integration / acceptance:** e.g. zero amount → 400; invalid payload → 400.
- For a portfolio, this is **acceptable and common**: the behavior that matters (status codes and error body shape) is covered at the boundary where the advice is applied. Adding a small `AppExceptionHandlerTest` would strengthen the story (“we also unit-test the handler in isolation”) but is not mandatory.

**Gaps / improvements**

- **Awaitility:** Used in acceptance and integration tests but not declared in `build.gradle.kts`. Add explicitly so the build is reproducible:  
  `testImplementation("org.awaitility:awaitility:4.2.0")` (or current version).
- **ProcessTransactionUseCase** when transaction not found: Integration/acceptance cover the happy path; unit tests cover use case with mocks. Optional: one integration test that asserts 404 when GET is called for a non-existent ID (if not already covered).
- **CreateTransactionUseCase** “duplicate but not found” path: The `catch (DuplicateTransactionException)` uses `findByExternalReference(...).orElseThrow(() -> new IllegalStateException(...))`. If that ever runs, the API would return 500 (no handler for `IllegalStateException`). Consider handling that case explicitly (e.g. map to a domain/application exception that the advice turns into 409 Conflict) and add a test.

**Verdict:** Strong test pyramid, clear separation of unit / integration / acceptance, and controller advice covered at resource level. **8/10**.

---

## 3. SOLID — 8/10

- **S — Single Responsibility:** Use cases do one thing; controllers only map HTTP ↔ application; repository and publishers have clear roles. `AppExceptionHandler` only maps exceptions to HTTP. Good.
- **O — Open/Closed:** Domain and use cases depend on abstractions (repository, publishers); new persistence or messaging can be added without changing core logic. Good.
- **D — Dependency Inversion:** Application layer defines ports (`TransactionRepository`, `TransactionEventPublisher`, `TransactionProcessedEventPublisher`); infrastructure implements them. Use cases and config are explicit about dependencies. Good.
- **L — Liskov Substitution:** Adapters (JPA repository, Kafka publishers) are substitutable for their ports. No obvious violations.
- **I — Interface Segregation:** Ports are focused (repository: save/find; publishers: publish). Two separate publisher interfaces are a bit finer than one “event publisher” — acceptable and arguably better for ISP.

**Minor**

- `CreateTransactionUseCase.execute` catches `DuplicateTransactionException` and then calls `transactionRepository.findByExternalReference(...)`. That couples the use case to “repository can find by external reference.” It’s consistent with the current port; just be aware that any new implementation of the port must support that contract.

**Verdict:** Clear DIP and SRP; OCP and LSP respected. **8/10**.

---

## 4. Design patterns — 10/10

| Pattern | Usage |
|--------|--------|
| **Repository** | Port in application layer, JPA adapter; entity ↔ domain mapping in mapper. |
| **Use case / application service** | Stateless classes, no Spring in core; wired in `UseCaseConfig`. |
| **DTO / request–response** | Records for requests and responses; validation on input DTOs. |
| **Thin controller** | Controllers translate HTTP ↔ commands/responses only. |
| **REST resource** | Single `@RestController` delegating to controllers. |
| **Event publisher (port)** | Two publisher ports, Kafka adapters. |
| **Exception mapping** | `@RestControllerAdvice` → `ErrorResponse` for validation, not found, invalid transaction, malformed JSON. |
| **Factory methods** | `Transaction.create`, `Transaction.restore`; `TransactionID.random()`, `TransactionID.from(String)`. |

**Verdict:** Patterns are used consistently and appropriately. **10/10**.

---

## 5. Clean code — 8/10

**Strengths**

- Consistent naming (commands, events, requests, responses).
- Small, focused methods (e.g. in `AppExceptionHandler`, use case private helpers).
- Immutable domain and value objects where it matters.
- Validation in one place (domain + Bean Validation at boundary).

**Improvements**

- **CreateTransactionUseCase:** The `catch (DuplicateTransactionException ex)` could return `transactionRepository.findByExternalReference(...).orElseThrow(...)` without assigning to a variable if you only need to throw; current code is already clear.
- **ProcessTransactionUseCase:** `Optional<Transaction>` for “already processed” is a good semantic; the consumer’s `ifPresent` is clear.
- **AppExceptionHandler:** `Collectors.joining` could be replaced with `String.join(", ", ...)` or keep as-is; both are fine. Consider extracting a small helper for the `InvalidFormatException` branch to keep the handler method shorter.
- **TransactionResource** line 42: `new GetTransactionByIdRequest(id).toTransactionId()` — parsing happens in the resource layer; if `TransactionID.from(id)` throws `InvalidTransactionException`, the advice correctly returns 400. Clean.

**Verdict:** Readable and consistent; a few small refactors would make it even clearer. **8/10**.

---

## 6. Clean architecture — 10/10

- **Dependency rule:** Dependencies point inward. Domain has no dependencies on outer layers. Application depends only on domain and defines ports; infrastructure and HTTP depend on application (and domain).
- **Domain:** Entities and value objects; domain exceptions. No frameworks.
- **Application:** Use cases, commands, events, repository and publisher ports. No Spring or HTTP types in use case code.
- **Infrastructure:** HTTP (resource, controllers, DTOs, exception handler), JPA (entity, repository adapter, mapper), Kafka (publisher adapters, consumer). Configuration (`UseCaseConfig`, etc.) lives in infrastructure and wires adapters to ports.
- **Presentation:** Effectively the HTTP slice inside infrastructure; no separate “presentation” module, which is fine for this size.

**Verdict:** Clear layers and dependency direction; framework-free core. **10/10**.

---

## 7. Critical and important points to change

### Critical

1. **Awaitility dependency**  
   Acceptance and integration tests use `org.awaitility.Awaitility` but it is not declared in `build.gradle.kts`. It may work via a transitive dependency today but could break with version upgrades.  
   **Action:** Add in `build.gradle.kts`:  
   `testImplementation("org.awaitility:awaitility")` (with a fixed version, e.g. `4.2.0`, or use the BOM if available).

2. **Unhandled `IllegalStateException` in CreateTransactionUseCase**  
   In the duplicate path you do:  
   `return transactionRepository.findByExternalReference(command.externalReference()).orElseThrow(() -> new IllegalStateException("Duplicate reported but transaction not found"));`  
   There is no `@ExceptionHandler` for `IllegalStateException`, so the API would return **500** for that (rare) case.  
   **Action:** Either (a) introduce an application exception (e.g. `InconsistentStateException`) and handle it in `AppExceptionHandler` with 409 or 500 and a clear message, or (b) document that this is a defensive path and add a handler that returns 409 Conflict with a generic message. Prefer (a) and add a unit test for “duplicate reported but not found.”

### Important (not blocking, but recommended)

3. **Save-then-publish ordering and failure**  
   `CreateTransactionUseCase` saves the transaction and then publishes the event. If the publish fails, the transaction remains in the database without a corresponding event (consumer won’t process it). For a portfolio, you can either:  
   - Add a short comment in code or README that you’re aware of this (at-least-once / eventual consistency), or  
   - Mention “transactional outbox” or “saga” as a possible evolution in the README.  
   No code change strictly required for the review, but showing awareness is a plus.

4. **Optional: unit test for AppExceptionHandler**  
   You already cover advice behavior via `TransactionResourceTest` and integration/acceptance tests. For a portfolio, one small `AppExceptionHandlerTest` (e.g. one test per exception type returning the expected `ErrorResponse` and status) would signal that you care about testing the handler in isolation. Not critical.

---

## Final summary for recruiters / hiring managers

- **Architecture:** Clean Architecture with clear domain, application (use cases + ports), and infrastructure. Dependency rule is respected; core is framework-free.
- **Testing:** Unit tests for domain and use cases; unit tests for HTTP at the resource layer (including controller advice behavior); integration tests with Testcontainers; acceptance test for the main flow. Controller advice is intentionally tested via the resource layer.
- **Design:** Repository and event publisher patterns, thin controllers, centralized exception mapping, consistent use of DTOs and validation.
- **Code quality:** Readable, consistent style (Checkstyle + Spotless), Java 21, and appropriate use of records and immutability.

**Recommended before sharing as portfolio:** Fix the two critical items (Awaitility in `build.gradle.kts`, and handling of the “duplicate but not found” path so it never results in an unhandled 500). Optionally add an explicit test for that edge case and, if you like, a minimal `AppExceptionHandlerTest`.

Overall, this is a **strong portfolio project** for an international hiring process: it demonstrates clean architecture, testing discipline, and design patterns without overengineering.
