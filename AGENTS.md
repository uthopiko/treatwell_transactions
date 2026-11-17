# AGENTS.md

## Working agreement

- Inspect the existing code and tests before changing anything.
- Plan the test scope with the user before creating files.
- Do not add tests beyond the requested layer or scope.
- Prefer the smallest change that satisfies the request.
- Remove duplicate coverage when a lower layer already owns the rule.

## Backend testing

### Stack and commands

- Java 21, Spring Boot 3.3, JUnit 5, AssertJ, Mockito.
- Run all tests: `cd backend && ./mvnw test`
- Run one class: `cd backend && ./mvnw test -Dtest=TransactionServiceTest`

### Test targets

- Unit test `TransactionEntryDto` and `TransactionService`.
- Do not unit test the anemic `Transaction` entity, enums, or records without behaviour.
- Do not test getters and setters.
- Use `@DataJpaTest` for `TransactionRepository` and service-plus-repository persistence.
- Use `@SpringBootTest` with `MockMvc` for the HTTP contract.

### Test levels

- Pure unit tests: `TransactionService` with a mocked `TransactionRepository`; `TransactionEntryDto` mapping. No Spring context.
- Persistence and query rules: `@DataJpaTest` with the real repository, because filters, ordering and uniqueness live in JPQL.
- HTTP contract: `@SpringBootTest` with `MockMvc`, limited to request/response shape and status codes.
- Never mock the repository when the rule under test is the query itself.

### Test ownership

- Test each rule at the lowest level that can observe it reliably.
- `@DataJpaTest` owns the query matrix: filters, ordering, tie-breaks, null handling, lookups and persistence lifecycle.
- `@SpringBootTest` owns the HTTP contract: parameter binding, status codes, headers, JSON shape, routing and end-to-end wiring.
- Keep one combined-filter HTTP test as proof that filters are wired through; leave the full matrix to the data layer.
- Do not duplicate the same rule across layers. Remove an HTTP test when a `@DataJpaTest` already proves the rule.

### Integration test setup

- Put shared integration properties in `backend/src/test/java/com/challenge/transactions/fixtures/IntegrationTestProperties.java`.
- Do not load `data.sql`; disable SQL init and let Hibernate create the schema.
- Seed each test through the repository using an object mother, with a unique `withId(...)` per persisted row.
- `@DataJpaTest` is transactional by default; add `@Transactional` to `@SpringBootTest` with `MockMvc` tests.
- Use a dedicated in-memory database name for full-stack tests.

### Bug regressions

- Write the regression test before the fix.
- Keep it `@Disabled` with a reason while the bug is open, so the suite stays green; enabling it is the definition of done.
- Prove the bug once at the user-visible HTTP level and once at the lowest level that explains it. Do not repeat it at every layer.

### Test method naming

- Use the `should_` prefix and snake_case.
- Describe behaviour, not the implementation.

```java
@Test
void should_create_new_transaction_when_source_id_is_unknown() {
}
```

Do not use camelCase, `testFoo`, or `given_when_then` naming.

### Test data

- Use object mothers in `backend/src/test/java/com/challenge/transactions/object_mothers/`.
- Use shared values in `backend/src/test/java/com/challenge/transactions/fixtures/TestFixtures.java`.
- Use canonical factory methods for common cases and the builder for variations.

```java
Transaction order = TransactionMother.order();

Transaction cancelled = TransactionMother.transaction()
        .withStatus(TransactionStatus.CANCELLED)
        .build();

WebhookEventDto event = WebhookEventMother.event()
        .withDescription(UPDATED_DESCRIPTION)
        .build();
```

Do not construct entities or DTOs inline in a test when a mother exists.

### Constants

- Do not use magic strings or magic numbers.
- Put shared values in `TestFixtures`; put test-specific values in `private static final` constants in the test class.
- Use enum constants instead of hard-coded wire values.

```java
softly.assertThat(dto.type()).isEqualTo(TransactionType.ORDER.name());
```

- Avoid literal indexes. Prefer Mockito helpers such as `returnsFirstArg()` over `getArgument(0)`.

### Assertions

- Use AssertJ.
- Use `SoftAssertionsExtension` and a `SoftAssertions` parameter when a test has more than one assertion, so all mismatches are reported together.

```java
@ExtendWith(SoftAssertionsExtension.class)
class TransactionEntryDtoTest {

    @Test
    void should_map_every_exposed_field(SoftAssertions softly) {
        softly.assertThat(dto.id()).isEqualTo(TransactionMother.ID);
        softly.assertThat(dto.type()).isEqualTo(TransactionType.ORDER.name());
    }
}
```

- Use hard assertions for single assertions and exception assertions.
- Keep Mockito `verify(...)` calls hard.

### Comments

- Do not add comments or Javadoc to test code.
- Test method names and object mother method names must carry the intent.

### Review checklist

- Test methods use `should_` and snake_case.
- Test data comes from an object mother and `TestFixtures`.
- No magic strings or numbers outside constant declarations.
- Multi-assertion tests use `SoftAssertions`.
- No rule is tested twice at different layers.
- Integration tests use `IntegrationTestProperties` and per-test seeds.
- Disabled tests name the open bug and the condition for enabling them.
- No comments or Javadoc in test code.
- `./mvnw test` passes.
