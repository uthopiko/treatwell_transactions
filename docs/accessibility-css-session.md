# Accessibility & CSS Review — Session Summary

## Purpose

Record of the accessibility and CSS review session for the transaction aggregator take-home: what was measured, what was changed, why, how it was verified, and the WCAG reference needed to explain it in the follow-up interview or an exam.

Target conformance level: **WCAG 2.2 AA**.

## Context

- The filter tabs work (tablist/tabpanel, roving tabindex, arrow keys) had been lost in an uncommitted change and was recovered from a dangling stash commit.
- A strict CSS review followed, measured against WCAG thresholds and maintenance criteria.
- The review produced eight small phases, each independently verifiable.

## WCAG in one paragraph

WCAG is a checklist of success criteria grouped in three conformance levels: **A**, **AA** and **AAA**. AA is the normal professional target and includes every A criterion. There is no score and no percentage: a page conforms to AA only when every A and AA criterion passes on the evaluated pages and states. “Pass AA” is never a property of a colour; it is a property of a specific element role against a specific criterion.

### Thresholds used in this review

| Criterion | Level | Applies to | Threshold |
|---|---|---|---|
| 1.4.3 Contrast (Minimum) | AA | normal text (< 24px, or < 18.7px bold) | 4.5:1 |
| 1.4.3 Contrast (Minimum) | AA | large text (>= 24px, or >= 18.7px bold) | 3:1 |
| 1.4.11 Non-text Contrast | AA | borders, icons, focus rings, control outlines | 3:1 |
| 1.4.6 Contrast (Enhanced) | AAA | normal text | 7:1 |
| 1.4.6 Contrast (Enhanced) | AAA | large text | 4.5:1 |
| 2.5.8 Target Size (Minimum) | AA | click/tap targets | 24x24 CSS px |
| 2.5.5 Target Size (Enhanced) | AAA | click/tap targets | 44x44 CSS px |
| 2.1.1 Keyboard | A | anything interactive | usable with keyboard only |
| 4.1.2 Name, Role, Value | A | custom widgets | exposed to assistive technology |
| 2.3.3 Animation from Interactions | AAA | interactive motion | can be disabled |

`prefers-reduced-motion` maps to 2.3.3, which is AAA upstream. It was treated as best practice, not as an AA blocker.

## How a contrast ratio is calculated

### Formula

```text
1. channel = hexByte / 255
2. linearise:
     channel <= 0.03928 ? channel / 12.92
                        : ((channel + 0.055) / 1.055) ^ 2.4
3. luminance L = 0.2126*R + 0.7152*G + 0.0722*B
4. ratio = (L_lighter + 0.05) / (L_darker + 0.05)
```

The ratio is symmetric, ranges from 1 (identical colours) to 21 (black on white), and must not be rounded before comparing.

### Worked example: `#999999` on `#ffffff`

```text
C = 153/255 = 0.6
C' = ((0.6 + 0.055) / 1.055) ^ 2.4 = 0.3186
L_text = 0.3186
L_white = 1.0
ratio = (1.0 + 0.05) / (0.3186 + 0.05) = 2.85
```

Verdict: 2.85 < 4.5, so it fails 1.4.3 at AA for normal text.

### Worked example: white on `#4a90d9`

```text
R = 74/255  = 0.2902 -> R' = 0.0685
G = 144/255 = 0.5647 -> G' = 0.2789
B = 217/255 = 0.8510 -> B' = 0.6939
L = 0.2126*0.0685 + 0.7152*0.2789 + 0.0722*0.6939 = 0.2641
ratio = (1.0 + 0.05) / (0.2641 + 0.05) = 3.34
```

Verdict: 3.34 fails 4.5 for 14px text, but passes 3:1 for large text and for non-text elements.

### Tooling

| Tool | What it provides |
|---|---|
| Chrome DevTools colour picker | Built-in contrast ratio with AA/AAA indicators for the inspected element |
| axe DevTools (Deque) | Page scan listing element, measured ratio and required ratio |
| WAVE (WebAIM) | Inline contrast errors on the rendered page |
| Accessibility Insights for Web (Microsoft) | Fast Pass and structured manual audit |
| WebAIM Contrast Checker | Ratio and pass/fail for two arbitrary colours |
| Lighthouse | Automated accessibility audit of the page |

Automated tools find candidates, not conformance. Keyboard, focus order, zoom, reflow and screen-reader checks are manual.

## Findings and fixes

| Phase | Finding | Evidence | Change |
|---|---|---|---|
| 1 | Muted loading text failed AA | `#999` on white = 2.85 vs 4.5 | `--color-text-muted` -> `#767676` (4.54) |
| 1 | Active page button text failed AA | white on `#4a90d9` = 3.34 vs 4.5 | `--color-accent-fill` / `--color-on-accent` (5.30) |
| 1 | Page-button hover text failed AA | `#4a90d9` on white = 3.34 vs 4.5 | `--color-accent-text` (5.30) |
| 1 | Cancelled badge failed AA | `#e65100` on `#fff3e0` = 3.46 vs 4.5 | `#a33b00` (6.02) |
| 1 | Control boundaries failed non-text contrast | `#e0e0e0` on white = 1.32 vs 3.0 | `--color-control-border: #8a8a8a` (3.45) |
| 1 | Active tab border on selected tint | `#4a90d9` on `#e3f2fd` = 2.93 vs 3.0 | `--color-accent-fill` (4.64) |
| 2 | Clickable card had no affordance | no cursor, no hover | `cursor: pointer`, hover background |
| 2 | Focus ring clipped by scroll container | `overflow-x: auto` + `outline-offset: 2px` | `padding: 2px` on `.tabs` |
| 3 | Page content touched the viewport edge | `.app` had no inline padding | `padding-inline: var(--spacing-md)` |
| 3 | Drawer content touched the panel edge | computed padding = 0 | `padding` on `.header`/`.body` |
| 4 | Badge rules duplicated and drifted | 7 statuses x 2 files | Single `StatusBadge` with `data-status` |
| 5 | Vertical rhythm was accidental | 8px on heading, 0 between the rest | `gap: var(--spacing-md)` on `.container` |
| 6 | Typography/motion/z-index were magic numbers | 8 raw font sizes, `z-index: 100`, `120ms` | Token scale added, values replaced |
| 6 | Dead tokens | 5 tokens with zero usages | Removed |
| 6 | Transition ignored reduced motion | unconditional `transition` | `prefers-reduced-motion` override |
| 7 | Accent token overloaded | text, borders, fills, and surface-as-text | `fill` / `text` / `on-accent` split |
| 8 | Money columns misaligned | proportional digits | `font-variant-numeric: tabular-nums` |
| 8 | Many pages overflowed one row | `.pages` did not wrap | `flex-wrap: wrap` |
| 8 | Pill radius was a hardcoded number | `20px` / `999px` | `--radius-pill: 999px` |
| 8 | Blunt global reset | `* { margin: 0; padding: 0 }` | Targeted reset + `font: inherit` on controls |

## Key decisions and rationale

### Semantic colour tokens

`--color-accent` is decoration only (borders, focus rings). Filled backgrounds use `--color-accent-fill`, accent-coloured text uses `--color-accent-text`, and text placed on a fill uses `--color-on-accent`. The raw value `--color-accent-strong` is kept as an alias source so a brand change is one edit.

`--color-surface` is never used as a text colour. Using a background token as a foreground was the reason the active page button failed contrast.

### One badge component

`StatusBadge` owns the status labels and colours, keyed by `data-status`. The seven duplicated status rules in the card and the drawer were deleted, along with the stringly-typed `styles[statusClass]` lookup and the now-unused `getStatusLabel` / `getStatusClass` helpers.

### One owner for spacing

`.container` owns the vertical rhythm with `gap`. Components no longer add their own margins, so new children inherit the spacing automatically.

### Tokens for every repeated decision

Typography (`--text-*`, `--weight-*`, `--leading-*`), motion (`--duration-fast`, `--ease-standard`) and layering (`--z-overlay`) are tokenised. Sizes are in `rem`, so they respect the user's browser font size.

### Targeted reset instead of a universal one

`box-sizing` applies to everything, margins are zeroed only where needed (`body`, `p`), and form controls inherit the font. This removed three repeated `font-family` declarations and avoids the side effects of a blanket margin/padding reset. The trade-off: any new `ul`, `h1` or `figure` needs its own margin reset.

## Files changed

| File | Change |
|---|---|
| `frontend/src/App.css` | Contrast tokens, accent split, type/motion/z tokens, pill radius, page gutter, reset |
| `frontend/src/components/BalanceOverview/BalanceOverview.tsx` | Tabpanel semantics without tabindex, panel wrapper |
| `frontend/src/components/BalanceOverview/BalanceOverview.module.css` | Container gap, heading typography |
| `frontend/src/components/FilterTabs/FilterTabs.tsx` | Tablist/tab semantics, roving tabindex, arrow/Home/End navigation |
| `frontend/src/components/FilterTabs/FilterTabs.module.css` | Control border, focus ring, active state, reduced motion, pill radius |
| `frontend/src/components/TransactionCard/TransactionCard.tsx` | Uses `StatusBadge` |
| `frontend/src/components/TransactionCard/TransactionCard.module.css` | Padding, gap, hover/cursor, tabular numbers, badge rules removed |
| `frontend/src/components/TransactionCard/transactionCardUtils.ts` | Badge label helpers removed |
| `frontend/src/components/TransactionDetails/TransactionDetails.tsx` | Uses `StatusBadge` |
| `frontend/src/components/TransactionDetails/TransactionDetails.module.css` | Header/body padding, close-button affordance, z-index token, badge rules removed |
| `frontend/src/components/TransactionsList/TransactionsList.module.css` | Header padding, tabular totals |
| `frontend/src/components/Paginator/Paginator.module.css` | Control border, accent tokens, wrapping page list |
| `frontend/src/components/StatusBadge/StatusBadge.tsx` | New shared badge component |
| `frontend/src/components/StatusBadge/StatusBadge.module.css` | New single source of badge colours |

## Verification

### Contrast before/after

| Pair | Before | After |
|---|---|---|
| Muted text on white | 2.85 | 4.54 |
| White on accent fill | 3.34 | 5.30 |
| Accent text on white | 3.34 | 5.30 |
| Cancelled badge text | 3.46 | 6.02 |
| Control border on white | 1.32 | 3.45 |
| Active tab border on selected tint | 2.93 | 4.64 |

### Automated checks

```text
Test Files  8 passed (8)
Tests       79 passed (79)

tsc && vite build  -> success
```

- No unused custom properties remain (script over `:root`).
- No `color: var(--color-surface)` remains (surface is never a text colour).
- No raw `font-size`, `font-weight`, `z-index` or transition duration remains in component CSS.

### Reference values used in this session

| Pair | Ratio |
|---|---|
| `#000000` on white | 21.00 |
| `#333333` on white | 12.63 |
| `#666666` on white | 5.74 |
| `#767676` on white | 4.54 |
| `#999999` on white | 2.85 |
| `#cccccc` on white | 1.61 |
| `#e0e0e0` on white | 1.32 |
| `#8a8a8a` on white | 3.45 |
| `#8a8a8a` on `#f5f5f5` | 3.17 |
| white on `#4a90d9` | 3.34 |
| white on `#2f6db3` | 5.30 |
| `#a33b00` on `#fff3e0` | 6.02 |

## Known gaps

These were identified and deliberately left out of scope.

- **Transaction rows are clickable `<div>`s.** They are unreachable by keyboard and have no announced role, which fails 2.1.1 (A) and 4.1.2 (A). Because those are Level A, the page is **not** AA-conformant yet, regardless of the contrast fixes. The fix is semantic: render a `<button>` and remove the panel workaround.
- **The drawer close control is a `<span>`** with the same keyboard/semantics problem.
- **Page buttons are about 30px tall.** They pass 2.5.8 (24x24) but miss the 44x44 recommendation.
- **The accent layer is partially split.** `--color-accent-strong` still serves as the alias source for both fill and text; a future palette change may require separating them.
- **Automated conformance is not established.** No axe/Lighthouse run was added to CI.

## How to explain each change

One sentence per finding:

> Measured value X against criterion Y at level Z; the threshold is T; changed the token/rule; re-measured X'; tests still pass.

Example:

> The muted loading text measured 2.85:1. SC 1.4.3 at AA requires 4.5:1 for 14px text. I changed `--color-text-muted` to `#767676`, which measures 4.54:1. The same token feeds the only muted text in the app, so one change fixed it.

## Exam cheat sheet

```text
channel = hexByte / 255
linear  = channel <= 0.03928 ? channel/12.92 : ((channel+0.055)/1.055)^2.4
L       = 0.2126 R + 0.7152 G + 0.0722 B
ratio   = (L_light + 0.05) / (L_dark + 0.05)

AA normal text 4.5 | AA large text 3.0 | AA non-text 3.0
AAA normal 7.0     | AAA large 4.5
large text = >= 24px, or >= 18.7px bold
```

Common mistakes:

1. Averaging RGB channels instead of using weighted luminance.
2. Skipping gamma linearisation.
3. Rounding before comparison (4.499 fails 4.5).
4. Applying the 3:1 threshold to small text.
5. Omitting the `+0.05` on both sides.
6. Forgetting to composite `rgba()` colours over their real background.

### Practice questions

1. `#666` text (14px) on white. Does it pass AA? AAA?
2. White 14px text on `#4a90d9`. AA? What if it were 24px?
3. `#e0e0e0` border on white. Does it pass 1.4.11 at AA?
4. `#8a8a8a` border on `#f5f5f5`. Does it pass 1.4.11 at AA?
5. A ratio of 3.7 for 20px bold text. AA? AAA?
6. A ratio of 4.8 for 12px text. AA? AAA?
7. A ratio of 6.9 for 14px text. AA? AAA?
8. Which criterion and level cover a focus ring?
9. A loading message at 14px in `#999` on white: which criterion fails?
10. The transaction row is a clickable `<div>`: which level is at risk and why?

### Answers

1. 5.74. Passes AA (4.5). Fails AAA (7.0).
2. 3.34. Fails AA normal text (4.5). At 24px it is large text, so 3.34 passes the 3:1 AA threshold.
3. 1.32. Fails: 3:1 is required for control boundaries.
4. 3.17. Passes the 3:1 non-text threshold.
5. 20px bold is large text (>= 18.7px bold). AA passes (3.0). AAA fails (needs 4.5).
6. 4.8 passes AA (4.5). Fails AAA (7.0).
7. 6.9 fails AAA (7.0) and passes AA (4.5). No rounding: 6.9 < 7.0.
8. SC 1.4.11 Non-text Contrast, level AA, threshold 3:1.
9. SC 1.4.3 Contrast (Minimum), level AA: 2.85 < 4.5.
10. SC 2.1.1 Keyboard and 4.1.2 Name/Role/Value, both level A. A single A failure means the page cannot conform at AA.
