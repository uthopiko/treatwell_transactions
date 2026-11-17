# Frontend Testing Session — Conversation Summary

## Purpose

Record of the AI-assisted testing session for the transaction aggregator frontend: the prompts used, the decisions taken, the tests delivered, and the reasoning that can be reused in the follow-up interview.

## Context

- React 18, TypeScript, Vite 6, TanStack Query v5, CSS Modules.
- There was no test tooling in the repository at the start.
- Application logic lives in two pure modules, `balanceUtils.ts` and `transactionCardUtils.ts`. The components are mostly wiring; `TransactionDetails` has the richest behaviour.
- Two upcoming tasks shaped the strategy: Task 1 (pagination) changes the shape of the data, and Task 4 (accessibility) changes the semantics the component tests must target.
- Companion document for the backend: `docs/testing-session.md`.

## Prompt log

### 1. Initial direction

> Now I want to plan (don't apply) a strategy for testing for frontend. I want to test mainly the behaviour and domain, not the design.

Outcome: inspected the whole frontend and proposed a five-layer strategy: domain unit tests, API-wire unit tests, hook tests, component behaviour tests, and one end-to-end flow test. Tooling proposal: Vitest, jsdom, Testing Library, user-event, jest-dom and MSW. Three scope questions were asked and answered:

- Tooling: Vitest + RTL + jest-dom + MSW.
- Component sequencing: a thin net now, deepened after Task 4.
- Two suspected balance rules: pin current behaviour and flag them as open questions.

### 2. Naming

> should_ snake_case names don't follow is only fro Java

Outcome: confirmed. `should_` + snake_case stays backend-only; the frontend uses `describe` per unit and plain present-tense `it` sentences. Other Java-only rules were dropped too: `SoftAssertions`, Mockito helpers, JUnit annotations. The rules that carry over are: one rule tested once at the lowest useful level, object mothers plus fixtures, no magic values, no comments in test code.

### 3. Scaffolding

> continue with step 1

Outcome: added the tooling and the shared harness — `vitest` config inside `vite.config.ts` (jsdom, `css: false`, `restoreMocks`, `TZ=Europe/Dublin`), `src/test/setup.ts`, `src/test/render.tsx`, `TestFixtures`, `TransactionMother`, and the `test` / `test:watch` scripts. A throwaway smoke test proved the harness and was then removed, leaving the suite intentionally empty but green.

### 4. Domain tests

> Yes, please

Outcome: added `balanceUtils.test.ts` and `transactionCardUtils.test.ts`. A mutation check removed the PAYIN negation and failed two tests, proving they are not vacuous. Currency and date assertions were pinned as literals after verifying the `Intl` output under the configured timezone.

### 5. The filter-list test

> This test is not better in filtertabs component?

Outcome: agreed, and went further. `getBalanceFilterItems()` returns a static array, so the unit test compared it to a copy of itself. It was deleted, and `FilterTabs` now owns the rule as behaviour: clicking each tab emits that tab's own filter, parameterised over all five. Rendered tab order is deliberately not asserted, because it is presentation and would need role queries that only exist after Task 4.

### 6. Why components are still tested

> First: Why don't you test components?

Outcome: clarified that components are tested, but only for what nothing else can observe — fallbacks, conditional rows, click forwarding, loading/selection lifecycle, keyboard close and listener cleanup. Domain rules stay in unit tests because a component test cannot enumerate a type × status matrix cheaply, and re-asserting the maths through the DOM is the "test the design" trap. The user then chose to keep the order: wire tests next, components after the accessibility work.

### 7. Wire tests

> 1

Outcome: added `api/transactions.test.ts` and `useTransactions.test.tsx`. The URL is asserted through `URL`/`searchParams` so the incidental trailing `?` is not pinned. A real trap was found: with `restoreMocks` enabled, a module-scope `vi.spyOn(globalThis, 'fetch')` is detached before each test and the real `fetch` runs; the spy is created in `beforeEach` instead. A follow-up mutation (`sales → PAYIN`, dropping the `toDate` param) failed exactly four tests.

### 8. Non-empty fixtures

> I changed test to look if the transaction data has the length of the mocked value. Fits for you? Why not test with not empty list¿

Outcome: the added assertion compared an empty array's length to an empty array's length, so it could not tell a surfaced response from a default. The fixture became a non-empty list built from the mothers, the matrix kept asserting only the request, and one separate test asserts the full payload with `toEqual` instead of `.length`.

### 9. Why keep the payload assertion

> But maybe can be added transformation in the future, in this way we ensure it

Outcome: agreed. The payload test is the hook's output contract: a future `select` or flattening step fails it and forces the change to be deliberate. It guards the contract, not the correctness of a transformation, so transformation rules need their own tests at their own level.

### 10. The response helper

> Why is needed function responseWith ... I don't understand well the as unknown as Response??

Outcome: explained that the helper builds the stubbed response in one place, and that `as unknown as Response` is a double assertion used because the three-property object does not overlap enough with the real type. The cast was replaced with a real `Response` (`new Response(JSON.stringify(body), …)`), which keeps type checking on and exercises real JSON serialisation.

### 11. This document

> Can you resume this with prompts in /docs like the other testing in java?

Outcome: this summary.

## Key decisions

### Behaviour and domain only

- No snapshot tests, no CSS module or class-name assertions, no inline-style assertions.
- CSS is disabled in the test config so styling cannot leak into assertions.
- Icons are not asserted; `getStatusClass` is not tested because it only feeds CSS.
- Exhaustive `getStatusLabel` is not tested because the `Record` is already enforced by TypeScript.

### Test each rule once, at the lowest level that can observe it

- Domain maths owns grouping, balance impact, closing balances and formatting.
- The API client owns URL shape, parameter inclusion, parsing, abort signal and error status.
- The hook owns the `TransactionFilter` → backend `type` mapping, refetch on filter change, and the output contract.
- Components own wiring, conditional rendering and lifecycle.
- Components do not re-test the domain; unit tests do not assert markup.

### Naming is language-specific

- `should_` + snake_case is backend-only.
- Frontend tests read as behaviour sentences: `it('counts a failed payout as zero balance impact')`.

### Static data is not behaviour

- The `getBalanceFilterItems` unit test restated its own implementation and was removed.
- The same rule now lives in `FilterTabs` as a click behaviour, parameterised over all five filters.

### Fixes are pinned, suspicions are not invented

- No production code was changed during the session.
- The PAYIN sign and the terminal payout statuses are pinned as current behaviour, with names that make the rule explicit, so a deliberate change forces a test update.
- The newest-first assumption of `computeClosingBalances` is pinned by the closing-balance test, since pagination will stress it.

### Empty fixtures hide broken assertions

- The hook fixture uses two real rows from the mothers.
- Payload assertions use `toEqual`; `.length` cannot detect dropped or renamed fields.

### Mock the smallest surface

- `fetch` is a spy; the response is a real `Response`.
- MSW is reserved for the single flow test, where the request itself is observed on the wire.

### Mutation checks

- Every completed layer was verified by temporarily breaking the production rule and confirming the expected tests fail, then reverting.
- This proved sensitivity for the PAYIN negation, the filter→type mapping and the `toDate` parameter.

## Final test suite

| Layer | File | Tests |
|---|---|---|
| Domain unit | `src/components/BalanceOverview/balanceUtils.test.ts` | 19 |
| Domain unit | `src/components/TransactionCard/transactionCardUtils.test.ts` | 12 |
| Component | `src/components/FilterTabs/FilterTabs.test.tsx` | 5 |
| Wire | `src/api/transactions.test.ts` | 7 |
| Hook | `src/hooks/useTransactions.test.tsx` | 7 |

Result:

```text
Test Files  5 passed (5)
Tests  50 passed (50)
```

Support files:

- `frontend/src/test/setup.ts`
- `frontend/src/test/render.tsx`
- `frontend/src/test/fixtures/TestFixtures.ts`
- `frontend/src/test/object_mothers/TransactionMother.ts`
- `frontend/vite.config.ts` (`test` block)

## Conventions codified in `AGENTS.md`

- A `## Frontend testing` section was added next to the backend one.
- Frontend naming has no `should_` prefix and no snake_case.
- One rule tested once, at the lowest useful level.
- Object mothers and `TestFixtures` for test data; no inline fixtures.
- No CSS, style or snapshot assertions.
- `renderWithClient` for anything that reads queries; `user-event` over `fireEvent`.
- Time pinned with `vi.setSystemTime(fixedNow())` for date-dependent rules.
- Skipped regressions name the open bug and the condition for enabling them.

## Open items

- Write the component behaviour tests for `TransactionCard`, `TransactionsList`, `TransactionDetails` and `BalanceOverview` after the Task 4 accessibility refactor, then migrate `FilterTabs` from text queries to role queries.
- Add the single MSW flow test: load, change filter, assert the request carries `type=ORDER`, open details.
- Add pagination coverage when Task 1 lands. `useInfiniteQuery` will change the hook's data shape, so the payload contract test should fail and be updated deliberately.
- Confirm the PAYIN balance sign with product. The code currently treats it as money leaving the account, and a test now states that.
- Confirm whether non-`FAILED` terminal payouts (`CANCELLED`, `REFUNDED`, `REVERSED`) should affect the balance. The code currently subtracts them, and tests now state that.
- `BalanceOverview` has no error or empty state, so there is nothing to test yet; add coverage when Task 4 introduces them.
- Decide whether rendered tab order is a product rule worth asserting; it is currently untested because it is presentation.
- No coverage threshold is configured; coverage is not a goal for this exercise.

## Commands

```bash
cd frontend && npm test
cd frontend && npm run test:watch
cd frontend && npm test -- src/components/BalanceOverview/balanceUtils.test.ts
```
