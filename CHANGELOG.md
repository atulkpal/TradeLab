# Change Log
All notable changes to the TradeLab project will be documented in this file. This project adheres to a spec-first iteration cycle.

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
