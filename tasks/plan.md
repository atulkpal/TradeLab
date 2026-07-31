# Implementation Plan: Launch Pricing — Real Product IDs & ₹49 Early-Bird Promo

## Overview
Ship the real Google Play subscription products and a time-boxed early-launch offer. ₹99 is the list price; everyone who subscribes through 2026-08-31 gets a 7-day free trial + ₹49/mo (50% OFF). At **2026-09-01 00:00 IST** the app automatically switches to offering the ₹99 product. Lock-in (₹49 stays ₹49) is handled natively by Play Billing's per-product pricing; the app only needs to pick the right product for *new* purchases.

## Architecture Decisions
1. **Client-side date-driven product selection** — a pure `SubscriptionConfig` object (no server). Fits the offline-first app; countdown + pricing derive from the wall clock, so the UI flips automatically at the cutoff.
2. **`SubscriptionConfig.activeProductId(now)`** centralizes the switch: before Sep 1 → `tradelab_subs` (₹49); on/after → `trade_lab_subs_99` (₹99). The old placeholder `tradelab_pro_monthly` is removed from the call site.
3. **Reactive price display** — a `rememberLaunchPromo()` composable ticks every second and re-evaluates the promo state, so all price strings (₹49 + strikethrough ₹99 during promo, ₹99 after) update live and the banner auto-hides at cutoff.
4. **Entitlement unchanged** — `BillingManager.queryPurchases()` already scans all SUBS purchases and grants Pro for any active one; premium users never see the paywall again, so no ₹49 customer is ever upsold to ₹99.

## Task List

### Phase 1: Foundation (config + tests)
- [ ] Task 1: Create `billing/SubscriptionConfig.kt`
- [ ] Task 2: Write `SubscriptionConfigTest.kt` (TDD)

### Checkpoint: Foundation
- [ ] Focused tests pass, app compiles

### Phase 2: Wiring + UI
- [ ] Task 3: Wire product selection at `MainActivity.kt:1357`
- [ ] Task 4: Add `rememberLaunchPromo()` + countdown banner (Paywall + Premium Hub)
- [ ] Task 5: Update all pricing/trial strings (7-day + dynamic price)

### Checkpoint: Feature complete
- [ ] Full unit suite passes, release build compiles
- [ ] Manual Verification Protocol section D checklist reviewed

## Risks and Mitigations
| Risk | Impact | Mitigation |
|------|--------|------------|
| Device clock wrong during promo | User sees wrong price | Low — paper trading app; cutoff is cosmetic, Play charges actual product price |
| ₹49 product archived after Sep 1 | Subscribers force-cancelled | Keep `tradelab_subs` live in Play Console for renewals (user action) |
| Product IDs not live in Play Console | Billing flow fails | User must publish both products before release build testing |
| Stale hardcoded ₹99/15-day strings | Inconsistent UX | Centralize strings via `SubscriptionConfig`; grep verified after edit |

## Open Questions
- None — decisions confirmed: cutoff Sep 1 00:00 IST, countdown on Paywall + Premium Hub, ₹49 with strikethrough ₹99 styling.
