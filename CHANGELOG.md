# Change Log
All notable changes to the TradeLab project will be documented in this file. This project adheres to a spec-first iteration cycle.

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
