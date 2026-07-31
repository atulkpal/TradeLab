# Task List: Launch Pricing — Real Product IDs & ₹49 Early-Bird Promo

## Task 1: Create `billing/SubscriptionConfig.kt`
**Description:** Central config with product IDs, prices, promo window, countdown helpers. Pure object, no Android deps (unit-testable).
**Acceptance criteria:**
- [x] Constants: `PRODUCT_LAUNCH_49 = "tradelab_subs"`, `PRODUCT_REGULAR_99 = "trade_lab_subs_99"`, `FREE_TRIAL_DAYS = 7`, `LAUNCH_PRICE_INR = 49`, `REGULAR_PRICE_INR = 99`
- [x] `PROMO_END_UTC_MS` = 2026-09-01 00:00 IST
- [x] Functions: `isLaunchPromoActive(now)`, `activeProductId(now)`, `displayPrice(now)`, `monthlyPrice(now)`, `promoEndsInMillis(now)`, `countdownLabel(now)`
**Verification:** `:app:testDebugUnitTest`
**Dependencies:** None
**Files:** `app/src/main/java/com/ashwathai/tradelab/billing/SubscriptionConfig.kt`
**Estimated scope:** Small (1 file)

## Task 2: Write `SubscriptionConfigTest.kt` (TDD)
**Description:** Unit tests for promo window, product/price selection, cutoff edge.
**Acceptance criteria:**
- [x] Before 2026-09-01 00:00 IST → `activeProductId() == "tradelab_subs"`, price ₹49
- [x] At/after cutoff → `activeProductId() == "trade_lab_subs_99"`, price ₹99
- [x] `FREE_TRIAL_DAYS == 7`
- [x] Countdown helper returns zero after cutoff
**Verification:** `:app:testDebugUnitTest`
**Dependencies:** Task 1
**Files:** `app/src/test/java/com/ashwathai/tradelab/billing/SubscriptionConfigTest.kt`
**Estimated scope:** Small (1 file)

## Task 3: Wire product selection at `MainActivity.kt:1357`
**Description:** Replace `"tradelab_pro_monthly"` with `SubscriptionConfig.activeProductId()`.
**Acceptance criteria:**
- [x] No occurrence of `tradelab_pro_monthly` remains in `app/src/main`
**Verification:** grep + `:app:compileDebugKotlin`
**Dependencies:** Task 1
**Files:** `app/src/main/java/com/ashwathai/tradelab/MainActivity.kt`
**Estimated scope:** XS (1 line)

## Task 4: Add `rememberLaunchPromo()` + countdown banner
**Description:** Ticking composable + promo banner on Paywall dialog and Premium Hub CTA; auto-hides and flips to ₹99 at cutoff.
**Acceptance criteria:**
- [x] `rememberLaunchPromo()` returns isActive + countdown label, recomposes every second
- [x] Banner visible during promo ("₹49 launch offer ends in DD:HH:MM:SS")
- [x] Banner hidden after cutoff
**Verification:** `:app:compileDebugKotlin`
**Dependencies:** Task 1
**Files:** `app/src/main/java/com/ashwathai/tradelab/MainActivity.kt`, `app/src/main/java/com/ashwathai/tradelab/ui/profile/PremiumHubScreen.kt`
**Estimated scope:** Medium (2-3 files)

## Task 5: Update all pricing/trial strings
**Description:** 15-day → 7-day free trial; hardcoded ₹99 → dynamic price everywhere.
**Acceptance criteria:**
- [x] No "15-Day"/"15-day"/"Day 15" subscription strings remain
- [x] All subscription price strings derive from `SubscriptionConfig`
- [x] Simulated debug dialog shows the same dynamic pricing
**Verification:** grep + `:app:compileDebugKotlin` + `:app:testDebugUnitTest`
**Dependencies:** Task 1
**Files:** `MainActivity.kt`, `TradingViewModel.kt`, `ProfileScreen.kt`, `PremiumHubScreen.kt`, `WatchlistScreen.kt`, `PortfolioScreen.kt`
**Estimated scope:** Medium (6 files, small edits)

## Checkpoint
- [x] All tests pass: `:app:testDebugUnitTest`
- [x] Release path compiles: `:app:compileReleaseKotlin`
- [x] Manual Verification Protocol section D checklist updated (done in docs)
