# My approach

I started with tests because I wanted to change the feature without breaking behaviour. The first question was not “how many tests?”, but “where does each rule belong?”, so the suite stays small and a failure points to the layer that owns the rule.

The use of IA is documented in /docs, there are the prompts that I use. For example for accesibility, I use first two add-ons and then I used IA to review other parts. Also I used for generate tests in baby steps.

## Testing

The domain is anemic: `Transaction` only has fields and accessors. Unit-testing it would test the compiler, so I focused on the places with real behaviour: the DTO mapping and the webhook writer, which parses the event, decides whether to create or update, and copies the fields. Those tests use a mocked repository.

The rules that depend on the database — filters, ordering, tie-breaks, null dates and the upsert identity — are tested against a real repository in a data-layer test. Mocking the repository there would hide exactly the rule under test. The HTTP layer is covered separately with request and response tests for parameters, status codes and JSON shape; I did not repeat the query matrix there.

On the frontend I followed the same idea: pure calculations as unit tests, the API client and hooks separately, components for rendering and interaction, and one flow test with MSW to prove the wiring. For data I used object mothers and shared fixtures. I considered Instancio and Faker, but for this size they add configuration and hide the values each assertion depends on. The frontend suite ended at 83 tests across 9 files.

## Frontend review

I fixed the dependency vulnerabilities I found. I also found that a failed transactions request looked exactly like an empty list: the component only knew about loading. It now recognises the error and shows an accessible alert.

The card showed the gross amount, while the balance uses the net. That mismatch is confusing when there is a commission, so the card now shows gross, net and fee. The detail panel already exposed them.

`TransactionCard` would be better named `TransactionListItem`, but I did not rename it because it touches imports, tests and styles without changing behaviour. It is a cleanup for later.

I changed accesibility tags, and some colors that don't comply AA. I used some add-ons to review this. Also I changed design to be more UX-friendly. I remove some unused css-properties.

## Net amount

`amount` is the gross value of the movement, `fee_amount` is the marketplace commission, `net_amount` is what actually affects the supplier balance, and `tax_amount` is separate. Today the writer simply copies whatever the webhook sends.

I would move the derivation to the backend: calculate net as amount minus fee, persist it as a snapshot, and stop trusting the net received from the source. A missing fee can be treated as zero, values are rounded to two decimals, and the table can enforce the relationship. This also gives the balance a stable value to sum.

Before implementing it, I would settle three questions with product: whether tax also reduces the net, whether a missing fee is zero or a rejected payload, and whether `netAmount` stays in the webhook contract for compatibility or disappears.

## Balance

The frontend balance is page- and filter-scoped: it is wrong on page 2 and becomes a subtotal under filters.

In this case we need a conversation with product in order the clarify which is the expected behaviour.

We think that the balance recalculation must be in the backend side, but...We have some decisions to take, for example, we can calculate balance (in reads or in writes) in balance, but what about UX/UI? 

If you either return all rows, or you paginate  balance will work perfect because you returned balance from the API. But, what about filters?With filters the balance wouldn't be accurate. 

Also from architecture side we have to decide calculate balance on reads, or persisting a balance per transaction. This makes reads cheap, but requires recalculating later rows on writes and backdated upserts, serialising concurrent writes, and backfilling; filtered balances are not viable.

For this iteration I kept the page-scoped accumulation, shown only with the `All` filter, with the closing balance, the day change and `Opening balance` for the first group. 

## Transactions without a date

The database, the entity, the webhook and the DTO all allow `fulfilmentDate` to be null; only the frontend assumed it was always present, which produced `Invalid Date`. I implemented a temporary “No date” group but removed it on the assumption that the API will not send null for now.

This is a product decision: if an undated transaction is valid, where should it appear and should it affect the balance? If it is not valid, the webhook should reject it and the column should be `NOT NULL`. I would not invent a fallback date; the API does not currently expose a created date that could be used honestly.
