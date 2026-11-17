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

## Frontend testing

### Stack and commands

- Vitest, jsdom, React Testing Library, `@testing-library/user-event`, `@testing-library/jest-dom`, MSW.
- Run all tests: `cd frontend && npm test`
- Watch: `cd frontend && npm run test:watch`
- Run one file: `cd frontend && npm test -- src/components/BalanceOverview/balanceUtils.test.ts`

### Test levels

- Domain unit tests: pure functions in `balanceUtils.ts` and `transactionCardUtils.ts`. No React.
- Wire unit tests: `api/transactions.ts` URL shape, parameters and failure semantics.
- Hook unit tests: `useTransactions` filter-to-type mapping and cache identity.
- Component tests: user interaction, conditional rendering and lifecycle with React Testing Library.
- One flow test: `BalanceOverview` with the real hook and MSW, proving filters reach the API and results render.

### Scope

- Test behaviour and domain rules, not design.
- Never assert CSS module class names, inline styles or snapshots.
- CSS is disabled in the test config so styling cannot leak into assertions.
- Do not test type-only modules, `App`, `main`, third-party internals, or mappings TypeScript already enforces.

### Test ownership

- Test each rule once, at the lowest level that can observe it.
- Domain maths belongs to unit tests; components only prove the wiring.
- Keep one flow test as proof that filters are wired through.

### Test naming

- The `should_` prefix and snake_case are backend-only; do not use them here.
- Use `describe` per unit and plain present-tense behaviour sentences in `it`.

```ts
describe('balanceUtils', () => {
  it('counts a failed payout as zero balance impact', () => {});
});
```

### Test data

- Use `src/test/fixtures/TestFixtures.ts` for shared values.
- Use `src/test/object_mothers/TransactionMother.ts` for entities: `order()`, `payout()`, `payin()`, `other()`, and `transaction()` for variations.
- Do not construct fixtures inline in a test when a mother exists.
- No magic strings or numbers outside constant declarations.

### Rendering

- Use `renderWithClient` from `src/test/render.tsx` for anything that reads queries; it gives each test a fresh `QueryClient`.
- Prefer `user-event` over `fireEvent`.
- Query by role, label or text; never by CSS class.
- Pin time with `vi.setSystemTime(fixedNow())` when a rule depends on the current date.

### Bug regressions

- Write the regression test before the fix.
- Keep it skipped with a reason while the bug is open, so the suite stays green; removing the skip is the definition of done.
- Prove the bug once where the user can observe it.

### Review checklist

- Test names read as behaviour sentences, with no `should_` prefix.
- Test data comes from a mother and `TestFixtures`.
- No CSS, style or snapshot assertions.
- No rule tested twice at different layers.
- Skipped tests name the open bug and the condition for enabling them.
- No comments in test code.
- `npm test` passes.
