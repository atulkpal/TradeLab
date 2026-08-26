# Change Log
All notable changes to the TradeLab project will be documented in this file. This project adheres to a spec-first iteration cycle.

---

## [2.0.2] - Banner Ads, Interstitial Pacing & Watchlist Fixes - 2026-08-26

### Added
- **Banner ads across 10 screens:** `LevelPlayBanner` composable with adaptive size, `sdkReady` guard, try-catch, zero-height collapse, destroy-on-dispose. Wired to Portfolio, Watchlist, Academy, Commodities, F&O, Charts, Profile — all gated `!isPremium`.
- **Paced chapter-complete interstitial:** fires after every 3rd chapter reward completion, 3-min minimum gap, Pro users skip.
- **Post-video bonus reward card:** `LectureScreen` bonus card with `onLaunchBonusAd` wired through `MainActivity`.
- **In-App Update API:** `app-update-ktx:2.1.0`, flexible update prompt on launch, auto-install on resume.
- **Watchlist adaptive tip auto-dismiss:** `CircularProgressIndicator` countdown (5s) with `AnimatedVisibility` fade+slide exit.

### Changed
- **Debug = test ads only:** `AdConfig.USE_TEST_ADS = BuildConfig.DEBUG`; debug builds use LevelPlay test key `25b63cf85` with test ad units. Release builds use production config.
- **AdConfig test key correction:** `TEST_APP_KEY` from `85460dcd` → `25b63cf85` (the old key was paired with different ad units that didn't match the test rewarded/interstitial units).

### Fixed
- **BuySellBottomSheet MIS dialog not appearing:** moved `LocalContext.current` from inside `AlertDialog`'s `confirmButton` composable (resolving to `ContextThemeWrapper`) to `BuySellBottomSheet` function top scope (resolving to `MainActivity`).
- **Watchlist drag-to-dismiss swallowing MIS tab clicks:** moved `pointerInput` drag gesture from Card root modifier to the sliding handle bar only — freed inner clickable modifiers.
- **"Invalid ad unit id" (626) on debug builds:** corrected test ad unit IDs to match LevelPlay test app key `25b63cf85`.

---

## [2.0.1] - Ad Serving Live + Guest Session Fix - 2026-08-26

### Fixed
- **Unity Ads SDK missing from all prior builds (root cause):** unityads-adapter POM declares zero dependencies and does not bundle the SDK; added explicit com.unity3d.ads:unity-ads:4.20.0 and upgraded adapter 4.3.55 -> 5.12.0 (official LevelPlay 9.x pairing). Symptom was NoClassDefFoundError: com.unity3d.ads.IUnityAdsInitializationListener -> zero Unity Ads demand.
- **Guest login regression:** guest sessions now persist across process death (SharedPreferences flag; cleared on logout). Minimize/reopen no longer bounces to the login screen.
- **Rewarded callback hygiene:** stale callbacks cleared at cycle end (prevented spontaneous re-show attempts and double-grant risk); parallelLoad race no longer counted as load failure.

### Changed
- **Fail-closed reward surfaces:** WATCHLIST_CREATE / PORTFOLIO_RESET / PROFILE_LEVERAGE no longer grant rewards when ads fail (guarded by AdFailClosedGuardTest source-scan).
- Release builds strip Log.v/Log.d (ProGuard assumenosideeffects); Log.w/e retained.
- Rewarded unit migrated to rewarded_v2 (349kle4725uh1kfa) after rewarded_main stuck un-provisioned server-side.

### Verified
- Live serving on device (registered test device): rewarded displayed -> reward earned -> closed -> preload (network=ironsourceads test creative, revenue 0.0).
- Unity Ads SDK 4.20.0 initialized with game id 800362159; valid bidding tokens generated per load.

## [2.0.0] - Academy Video Platform & Monetization Migration - 2026-08-25

### Added
- **Remote Video Delivery (Epic 27, LIVE):** `VideoManifestRepository` fetches `videos/manifest.json` from Firebase Storage (24h TTL cache, offline-surviving); 92 polished lectures in production; new batches ship without app releases.
- **Dynamic EN/हिंदी lecture toggle** — renders per-lecture only when a Hindi variant exists; Gujarati/Bengali armed via manifest.
- **Notification framework (Epic 28 groundwork):** FCM data-payload routing (market_alerts / engagement channels), token registration with change-only Firestore sync, local market-open alert at 09:15 IST (weekend + holiday aware, self-rescheduling WorkManager chain).
- **LevelPlay Integration Guide** (`docs/LEVELPLAY_GUIDE.md`) — internal playbook for all Ashwath AI products.
- **Play Store listing 2.0:** ASO-optimized text, 200+ videos messaging, full course catalog.
- **`MarketCalendar`:** single source of truth for NSE/BSE holidays + session hours (shared by trading engine and notifications).

### Changed
- **AdMob → Unity LevelPlay hard cut (Epic 26):** all rewarded surfaces (AI credits, F&O tokens, Commodities unlock, Academy double-reward) run LevelPlay; foreground interstitial; consent metadata v1.
- **Academy UI:** full-screen lecture destination, video-first layout, Shorts-style fullscreen playback, course card v3, gamification chips (streak/XP).
- **VideoCacheManager:** internal cacheDir (FUSE-proof) and clean cache filenames.

### Removed
- AdMob SDK, managers, views, manifest entries — zero gms.ads references remain.
- All fake-ad fallback auto-grants (Portfolio credits, Profile free-unlocks, Academy streams, desk countdowns).

### Fixed
- `%2F` cache-filename bug that broke all remote video playback.
- `legacyAdFormats` init gap causing LevelPlay 1024 no-fill.
- Video pill-switch WebView reload; theme compliance across Academy surfaces.

---

## [1.8.2] - NLM Video Pipeline & Academy Content Release - 2026-08-22

### Added
- **NLM Video Generation Pipeline**:
  - Automated video generation pipeline using Google NotebookLM for 204 Academy lectures
  - 7 Google accounts configured for parallel video generation
  - SHORT format videos (~60-90s) with branded intro/outro
  - Download-once, play-locally model with external cache
  - Firebase Storage integration for video hosting
- **NLM Pipeline Scripts** (`nlm/scripts/`):
  - `extract/` - Lecture extraction from `academy_data_v2.json`
  - `create_notebooks/` - Parallel notebook creation across 7 accounts
  - `generate_videos/` - Batch video generation with quota management
  - `download/` - Video download from NotebookLM
  - `process/` - FFmpeg-based intro/outro concatenation
  - `upload/` - Firebase Storage upload with public URLs
- **Pipeline Documentation** (`nlm/docs/`):
  - `PIPELINE.md` - Full pipeline walkthrough
  - `STATUS_SHEET.md` - Master lecture tracker with current status
  - `SCRIPT_INVENTORY.md` - Complete script reference
  - `QUOTA_MANAGEMENT.md` - Quota limits, backoff strategies
  - `TROUBLESHOOTING.md` - Common issues and fixes
- **Lecture Tracker** (`nlm/tracking/lecture_tracker.csv`):
  - Master CSV tracking all 204 lectures with notebook/video status
  - Auto-generated from audit logs, batch results, and downloaded files

### Changed
- Updated `README.md` with NLM pipeline section and status badges

---

## [1.8.1] - Personalization & Stability Release - 2026-08-04

### Added
- **Hyper-Personalization & Focus Suite (Epic 24)**:
  - Formally launched the **5-mode Theme Engine** (Serious, Vibrant, Terminal, Arcade, Light) with dynamic typography and color palettes.
  - Implemented **Stealth Mode** with privacy-first data blurring for sensitive portfolio metrics.
  - Introduced **Zen Mode** for a distraction-free trading experience, hiding news tickers and movers.
- **Deep Stability Hardening**:
  - **Moshi Codegen Migration**: Replaced `KotlinJsonAdapterFactory` with compile-time code generation to make the app immune to R8 obfuscation/reflection crashes.
  - **In-App Diagnostic Suite**: Added a custom `UncaughtExceptionHandler` that captures fatal traces to SharedPreferences.
  - **Diagnostic Viewer**: Secret long-press gesture on the Profile version footer to view local crash logs.
- **Critical Stability Hotfixes**:
  - Resolved **SQLiteDatabaseCorruptException** via Write-Ahead Logging (WAL).
  - Fixed **NaN Progress Indicator** crash during data loading.
- **Institutional Visual Polish**:
  - Finalized migration to high-performance **VectorDrawables** for all decorative elements.

---

## [1.7.0] - Institutional Power Release - 2026-08-04

### Added
- **Institutional Visualization (Track A)**:
  - Formally launched the **Candlestick Engine** for high-fidelity price action analysis.
  - Added **Technical Indicators**: MACD, Bollinger Bands, and Volume Bars.
  - Implemented **Interactive Zoom** and scaling gestures for stock charts.
- **Institutional Order Management (Track B)**:
  - Implemented **5x Leverage** for Intraday (MIS) equity trades.
  - Added **Bracket Orders (BO)**: Entry + Target + Stop-Loss multi-leg execution.
  - Integrated **Trailing Stop-Loss** logic to lock in profits automatically.
  - Supported **OCO (One Cancels Other)** order logic.
- **UI "Quick Win" Refinement**:
  - Promoted the **Equity Curve** chart to a primary, collapsible element on the Home screen for immediate performance visibility.
- **Build & Release Policy**:
  - Established a formal policy for archiving build artifacts (APKs/AABs) to preserve release history.

### Fixed
- Stabilized Robolectric tests by fixing Firebase initialization crashes.

---

## [1.6.0] - Varsity Academy & Multi-Format Monetization - 2026-08-04

### Added
- **Academy v2 (The Varsity Expansion)**:
  - Transitioned from flat modules to a **6-Course, 68-Chapter** comprehensive curriculum.
  - Implemented a **Multi-Question Knowledge Check** engine with a 60% passing threshold.
  - Added **Mission Reward Engine** with claimable virtual capital and progress persistence.
  - Integrated a **Preview Model** for locked courses (read freely, earn when ready).
- **Multi-Format Monetization**:
  - **App Open Ads**: Implemented a lifecycle-aware manager to show ads when resuming the app from the background.
  - **Blended Native Ads**: Custom-styled ads that blend into the **Watchlist**, **Portfolio**, and **Academy** dashboards.
  - Ad-gate bypass for all formats for **Premium** users.
- **Institutional Trading Desk Polish**:
  - **GTT Persistence**: Added `validUntil` fields ensuring GTT orders survive end-of-day resets.
  - **Quick Exit (Square Off)**: One-tap market exit buttons for all Equity and F&O positions.
  - **Precision Ledger**: Redesigned the tradebook with date-grouping and net-flow analytics.
- **Visual Vector Migration**:
  - Replaced 26 decorative emoji with high-performance **VectorDrawables** across the entire UI.
  - Added build version footer in the Profile screen for easy triage.

### Fixed
- Resolved the **F&O Academic Gate** deadlock by mapping beginner completions to v2 chapter IDs.
- Fixed F&O premium accounting (now tracked as an asset rather than a cash expense).
- Normalized lot sizes and volatility for MCX commodities.

---

## [1.5.0] - Launch Pricing, Real Product IDs & Store-Listing Compliance - 2026-07-31

### Added
- **Launch Pricing (Sprint 16.4)**:
  - Real Google Play subscription products: `tradelab_subs` (₹49 launch) and `trade_lab_subs_99` (₹99 regular), replacing the placeholder `tradelab_pro_monthly`.
  - 7-day free trial on both products (replaces the old 15-day trial).
  - Time-boxed ₹49/mo **Early-Bird promo (50% OFF)** valid through **2026-09-01 00:00 IST**, with an automatic client-side flip to ₹99 after the cutoff.
  - Ticking launch countdown banner on the Paywall and Premium Hub; all subscription pricing text now derives from `SubscriptionConfig`.
- **Store-Listing Compliance (Sprint 16.5)**:
  - Leaderboard identity is now a SHA-256 hash of the user's email — email addresses are never written to or exposed via the public Firestore leaderboard.
  - In-app **Delete Account** option on the Profile screen that links to the Play-compliant account deletion request page.
- **Unit tests**: `SubscriptionConfigTest` (promo window, cutoff, pricing) and `hashUserId` vectors.

### Fixed
- Public Firestore leaderboard no longer exposes user email addresses as document IDs.

## [1.3.0] - Social Maturity & Global Arena - 2026-07-25

### Added
- **Investor Maturity Engine (Track C)**:
  - Implemented a proprietary **0-100 Discipline Score** algorithm that rewards healthy position sizing (max 12% allocation), portfolio diversification (3+ sectors), and long-term holding times.
  - Added dynamic **Discipline Badges** (e.g., "Sizing Master", "Patience King") to the user profile based on quantified behavioral milestones.
- **Global Leaderboard Upgrades**:
  - Introduced a **Competitive Toggle** allowing users to sort the arena by Wealth (XP) or Maturity (Discipline Score).
  - Increased leaderboard capacity to the **top 100 practitioners** globally.
- **Viral Discipline Challenge**:
  - Added a **Share My Score** hook that generates social-friendly competitive messages to challenge friends to beat your risk management stats.
  - Implemented a "Challenge a Friend" invite card in the Academy to drive arena growth.

### Changed
- Refactored `TradingRepository` to integrate the `DisciplineCalculator` and recalculate maturity on every execution.
- Migrated Room database to **Version 20** to support persistent discipline metrics.

---

## [1.2.0] - Pre-Launch Polish & Advanced Realism - 2026-07-23

### Added
- **Viral Sharing Hooks (Sprint 16.1)**: Implemented performance-aware catchy phrases (e.g., "Flipped my capital! 🚀") that appear on shared portfolio cards to encourage social media engagement.
- **Professional Order Toggles (Sprint 16.2)**: Added CNC/MIS (Equity) and NRML/MIS (F&O) product type selectors with educational tooltips, mimicking institutional trading desks.
- **T+1 UI Refinements**: Standardized P/L display and square-off logic for options, including clear "T1" unsettled share visibility.

### Changed
- Updated version code to 4 and version name to 1.2.0 for release preparation.

---

## [1.1.0] - Foundation Modernization & Market Realism - 2026-07-21

### Added
- **Architectural Modernization (Epic 11)**:
  - Migrated the entire codebase to **Hilt Dependency Injection**. All components now utilize constructor injection for better testability and decoupled logic.
  - Upgraded to **Android API 37** (Android 15+) for the latest platform stability and features.
  - Decoupled background simulation loops into `startBackgroundTasks()` to prevent Robolectric/Compose idleness deadlocks, resulting in stable, green builds.
- **Indian Market Realism**:
  - Implemented the **Steered Anchored Simulation** engine: Stock prices now "wiggle" with organic noise but gravitate toward real-world Yahoo Finance anchors.
  - Enforced strict **Market Hour Logic**: Prices for Indian stocks (.NS, .BO) and MCX commodities now strictly stop fluctuating outside of official NSE/BSE session hours (3:30 PM IST cutoff).
  - Integrated a hardcoded **2026-2027 Indian Market Holiday Calendar** (Republic Day, Holi, Independence Day, etc.) to ensure the simulation stays static during national closures.
  - Added a visual **"MARKET CLOSED"** red badge in the Watchlist header when in Live mode during non-trading hours.
- **Ultra-Dense UI & Swipe Navigation**:
  - Replaced static screen switching with a fluid **`HorizontalPager`** implementation, allowing users to swipe horizontally between Portfolio, Watchlist, Commodities, etc.
  - Optimized the **HeaderBar** with a fixed height and centered developer data toggle to prevent vertical jitter during navigation.
  - Implemented a collapsible **Watchlist Search Lens** in the title bar and "vanishing" popular tickers (hiding once 5+ stocks are added) to maximize data density.
- **User Preferences Persistence**:
  - Added `isWatchlistCompactMode` to the Room database schema (Version 9). The app now remembers your choice of "Compact" vs "Classic" view and restores it on startup.
- **Enhanced Firebase Auth Diagnostics**:
  - Added a formal "Sign Out" flow on the Profile screen and improved Logcat diagnostics for Phone and Google authentication setup.

### Changed
- Refactored the monolithic `MainActivity.kt` logic into clean, feature-by-package Kotlin files under `com.ashwathai.tradelab.ui`.

---

## [1.0.0] - MVP Release - 2026-07-15

### Added
- **Local Room Database Schema**: Established SQLite entities for `UserProfile`, `Holdings`, `Transactions`, `Watchlist`, and `StockPrices`.
- **Native Live Charts Engine**: Built a custom drawing canvas rendering live price tick fluctuations as continuous line charts.
- **Unidirectional State Flow Engine**: Centralized portfolio balance evaluation, asset weight calculations, and transactional validations in `TradingViewModel`.
- **Position Sizing Profiler**: Designed a 60-second questionnaire for realistic budget calibration.
