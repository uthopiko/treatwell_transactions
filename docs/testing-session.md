# Testing Session — Conversation Summary

## Purpose

Record of the AI-assisted testing session for the transaction aggregator take-home: the prompts used, the decisions taken, the tests delivered, and the reasoning that can be reused in the follow-up interview.

## Context

- Backend: Spring Boot 3.3, Java 21 target, JPA, H2.
- The `Transaction` entity is anemic: fields, getters and setters only.
- The goal was a safety net before refactoring: pagination, the webhook upsert bug, and frontend work.
- There were no tests in the repository at the start.

## Prompt log

### 1. Initial direction

> I want to ensure my code to start making a refactor. Act as developer, and start thinking on how to test the code. From my perspective, I'll test with unit test, domain but it's quite anemic, so maybe there is no way to unit test it. Maybe, makes only sense to unit test service

Outcome: inspected the codebase and proposed a layered strategy. Confirmed the domain is not worth unit testing, but noted that the deliberate bug lives in the repository query, so unit-testing the service with a mocked repository cannot catch it.

### 2. Scope correction

> But you create tests...I want only to plan the tests...First which classes would you cover by unit testing?

Outcome: stopped implementation, reverted to planning, and listed the unit-test candidates:

- `TransactionEntryDto` — pure mapping.
- `TransactionService` — parsing, create-vs-update branch, field mapping.
- Not `Transaction`, enums, behaviour-free records, or controllers.

### 3. First implementation slice

> We will start with backend and unit testing. Create a unit test for Service and Dto

Outcome: added `spring-boot-starter-test`, created `TransactionServiceTest` with a mocked repository and `TransactionEntryDtoTest`.

### 4. Test data

> Please use objectmothers and test-fixtures

Outcome: introduced `TestFixtures`, `TransactionMother`, and `WebhookEventMother`, then removed duplicated fixture-building from the tests.

### 5. Naming

> Please use for tests should_ prefix and snake_case

Outcome: renamed every test method, for example `should_create_new_transaction_when_source_id_is_unknown`.

### 6. Assertions

> Add SoftAssertions to tests

Outcome: added `SoftAssertionsExtension` and `SoftAssertions` parameters to multi-assertion tests. Single assertions, exception assertions and Mockito `verify` stayed hard.

### 7. Comments

> Please don't use comments.

Outcome: removed comments and Javadoc from test code.

### 8. Constants

> And don't use magic strings or magic numbers

Outcome: moved values into `TestFixtures` or private constants, used `TransactionType.*.name()`, and replaced `getArgument(0)` with `returnsFirstArg()`.

### 9. Agent conventions

> Those best practises can you put in AGENTS.md in order to reuse by the agents?

Outcome: created `AGENTS.md` with the working agreement, test levels, naming, fixtures, constants, assertions, comments rules and a review checklist.

### 10. Integration tests

> Ok, now we'll start with the integration tests

Outcome: proposed three layers before writing code: repository query rules, webhook identity, and HTTP contract.

### 11. Order

> The order doesn't matter, but maybe http first

Outcome: created `TransactionsApiIntegrationTest` with `@SpringBootTest` and `MockMvc`, isolated H2, per-test seeding, and one disabled bug regression.

### 12. The bug

> After DataJpaTest, please can yo said me: Which is the bug?

Outcome: added `TransactionRepositoryIntegrationTest` and explained the bug: the upsert lookup uses `sourceId` alone and ignores `transactionType`.

### 13. More Spring Data coverage

> Ok, now continue with SpringData tests

Outcome: extended the repository suite with `findBySourceId`, null `fulfilmentDate` handling and JPA lifecycle generation; added a `@DataJpaTest` service-plus-repository suite.

### 14. Redundancy question

> Maybe with the tests in controller are not needed SpringDataJpa Tests?

Outcome: explained that the layers are not redundant, but that the controller suite duplicated part of the query matrix. Recommended keeping the query matrix at the data layer and the protocol concerns at the HTTP layer.

### 15. Trim

> yes apply

Outcome: removed four duplicated HTTP tests and the duplicate disabled service-data bug test. Query coverage stayed at the data layer.

### 16. Codify the agreements

> Agreements to agents.md

Outcome: added test ownership, integration setup and bug regression rules to `AGENTS.md`.

### 17. This document

> Can you create a document with the resume of our conversation with the prompts?

Outcome: this summary.

## Key decisions

### Test each rule at the lowest level that can observe it

- Unit tests cover pure mapping, parsing and branching.
- `@DataJpaTest` covers filters, ordering, tie-breaks, null handling, lookups and persistence lifecycle.
- `@SpringBootTest` with `MockMvc` covers parameter binding, status codes, headers, JSON shape, routing and end-to-end wiring.
- Duplicate coverage was removed rather than kept "for safety".

### Mocking the repository has a hard limit

A service unit test can verify which repository method is called and how the returned entity is populated. It cannot verify which row the query selects. That is where the deliberate bug lives, so the bug needed real-database and HTTP tests.

### Object mothers plus fixtures

- `TestFixtures` holds canonical values.
- `TransactionMother` and `WebhookEventMother` provide canonical objects and builders.
- Tests override only the attribute under test.
- Integration tests seed through the repository with unique IDs per persisted row.

### Database isolation

- `data.sql` is disabled in tests because its `CURRENT_DATE` rows make assertions date-dependent.
- Hibernate creates the schema in test slices.
- Full-stack tests use a dedicated in-memory database name.
- `@DataJpaTest` is transactional by default; `@SpringBootTest` with `MockMvc` uses `@Transactional` for rollback.

### Disabled bug regressions

The failing regression is committed as `@Disabled` with a reason so the suite stays green. Enabling it is the definition of done. The same bug is not repeated at every layer: one user-visible HTTP test plus one lowest-level explanation.

## The bug

`TransactionService.upsertFromWebhook` looks up the existing transaction with:

```java
repository.findBySourceId(event.sourceId())
```

`sourceId` is only unique within a transaction type. A `PAYOUT` event reusing an `ORDER` source ID finds the order and overwrites it, including `transactionType`. The order becomes a payout and the original record is lost.

Observed failure when the disabled HTTP test is enabled:

```text
JSON path "$.transactions.length()" expected:<5> but was:<4>
```

Suggested fix:

```java
Optional<Transaction> findByTransactionTypeAndSourceId(TransactionType type, String sourceId);
```

Then add a database unique constraint on the identity key and enable the regression test. Decide explicitly whether `supplierId` belongs in that key.

## Final test suite

| Layer | Class | Tests |
|---|---|---|
| Unit | `TransactionEntryDtoTest` | 4 |
| Unit | `TransactionServiceTest` | 10 |
| Spring Data | `TransactionRepositoryIntegrationTest` | 14 |
| Spring Data | `TransactionServiceDataIntegrationTest` | 2 |
| HTTP | `TransactionsApiIntegrationTest` | 6 enabled, 1 disabled |

Result:

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```

Support classes:

- `backend/src/test/java/com/challenge/transactions/fixtures/TestFixtures.java`
- `backend/src/test/java/com/challenge/transactions/fixtures/IntegrationTestProperties.java`
- `backend/src/test/java/com/challenge/transactions/object_mothers/TransactionMother.java`
- `backend/src/test/java/com/challenge/transactions/object_mothers/WebhookEventMother.java`

## Conventions codified in `AGENTS.md`

- `should_` prefix and snake_case for test methods.
- Object mothers and shared fixtures for test data.
- No magic strings or numbers outside constant declarations.
- `SoftAssertions` for multi-assertion tests.
- No comments or Javadoc in test code.
- One rule tested once, at the lowest useful level.
- Shared integration properties and per-test seeding.
- Disabled tests must name the open bug and the condition for enabling them.

## Open items

- Enable the disabled HTTP test and fix the upsert identity.
- Add pagination contracts and tests together with the implementation.
- Decide how to return `400` instead of `500` for unknown webhook enums and malformed dates.
- Consider Testcontainers if production uses a database other than H2.
- Frontend tests were discussed but not part of this session.

## Commands

```bash
cd backend && ./mvnw test
cd backend && ./mvnw test -Dtest=TransactionRepositoryIntegrationTest
cd backend && ./mvnw test -Dtest='TransactionsApiIntegrationTest#should_not_overwrite_order_when_payout_reuses_source_id' -Djunit.jupiter.conditions.deactivate='*'
```
