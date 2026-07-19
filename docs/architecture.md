# Trade Lab — Architectural Blueprint

This document outlines the software architecture of **Trade Lab**. It provides a guide on how the current Android application is built and details how the codebase is strategically structured to support future expansion to **iOS** and **Web** with maximum code reuse.

---

## 1. Architectural Philosophy & Core Tenets

The architecture of Trade Lab is built on three core pillars:
1.  **Offline-First & Local-First:** All transactional execution, account balances, historical performance tracking, and user profile data are computed and stored on-device. This guarantees instant UI response times and total data privacy.
2.  **Unidirectional Data Flow (UDF):** The user interface is a pure reflection of the underlying database state. UI actions trigger events; events update the database; database updates flow back up to the UI.
3.  **Strict Separation of Concerns (Clean Architecture):** Business logic is decoupled from platform frameworks (Android SDK, Jetpack Compose). This decoupling is what makes cross-platform portability possible.

---

## 2. Current Architecture (Android MVVM / Clean Architecture)

The current app utilizes a localized Clean MVVM structure divided into three distinct layers:

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│   [Jetpack Compose UI Screen & Canvas Vector Charts]   │
└───────────────────────────▲────────────────────────────┘
                            │ (State Observables via Flows)
┌───────────────────────────▼────────────────────────────┐
│                       VIEW MODEL                       │
│    [TradingViewModel (manages reactive screen state)]  │
└───────────────────────────▲────────────────────────────┘
                            │ (Calls clean business actions)
┌───────────────────────────▼────────────────────────────┐
│                    REPOSITORY LAYER                    │
│    [TradingRepository (validates trades & balances)]   │
└───────────────────────────▲────────────────────────────┘
                            │ (Reads/Writes Data Entities)
┌───────────────────────────▼────────────────────────────┐
│                   DATA PERSISTENCE LAYER               │
│ [Room SQLite database (Entities, DAOs, Migrations)]    │
└────────────────────────────────────────────────────────┘

### D. Billing & Monetization Layer (`com.ashwathai.tradelab.billing`)
*   **BillingManager:** A lifecycle-aware component that interfaces with the **Google Play Billing SDK**.
    *   Handles connection to the Google Play Store.
    *   Queries for available subscription products (e.g., `tradelab_pro_monthly`).
    *   Launches the official Google Play purchase sheet.
    *   Acknowledges purchases to prevent automatic refunds.
    *   **Hybrid Logic:** Seamlessly switches between a high-fidelity simulation in Debug mode and the real SDK in Release mode.
```

### A. Data Persistence Layer (`com.ashwathai.tradelab.data`)
*   **Entities (`Entities.kt`):** Pure data models mapped to SQLite tables.
    *   `UserProfileEntity`: Tracks cash balances, real-world target budgets, and psychological profile configurations.
    *   `HoldingEntity`: Tracks shares held, average purchase price, and ticker codes.
    *   `TransactionEntity`: Historic logs of executions (BUY/SELL) with timestamps and execution prices.
    *   `WatchlistEntity`: User-curated stocks marked for quick monitoring.
    *   `StockPriceEntity`: Simulates current asset market rates, high/low parameters, and fluctuation spreads.
*   **DAOs (`Daos.kt`):** Type-safe interfaces containing SQL queries for Room database compilation.
*   **AppDatabase (`AppDatabase.kt`):** Coordinates SQLite file-handle instantiation and manages destructive/non-destructive migrations.

### B. Business Logic & Domain Layer (`com.ashwathai.tradelab.data.TradingRepository`)
*   Acts as a mediator between the database layer and presentation view models.
*   Enforces transaction invariants. E.g., when executing a buy order:
    1.  Reads user cash balance.
    2.  Calculates total order cost (`Shares * Price`).
    3.  Throws error if `Cash < Total Cost` (enforcing zero-slippage budget rules).
    4.  Updates cash balance, adds/updates holding shares, and writes execution logs in a single SQL Transaction block.

### C. Presentation Layer (`com.ashwathai.tradelab.ui` & `MainActivity.kt`)
*   **TradingViewModel:** Emits unified UI state flows using Kotlin `StateFlow`. Exposes functions for buying, selling, resetting profile configurations, and simulating market tick price shifts.
*   **Jetpack Compose Views:** Lightweight, stateless layouts that observe the ViewModel states. Native **Android Canvas DrawScopes** are used to draw beautiful high-contrast charts directly from pricing data streams.

---

## 3. Multi-Platform Evolution Strategy (The KMP Roadmap)

To expand to **iOS** and **Web** while retaining 70-80% of our business logic, we will transition the project to **Kotlin Multiplatform (KMP)**.

### Target KMP Architecture Diagram

```
       ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
       │   Android App    │   │     iOS App      │   │     Web App      │
       │ (Jetpack Compose)│   │  (SwiftUI / KMP) │   │ (React / Compose)│
       └─────────┬────────┘   └────────┬─────────┘   └────────┬─────────┘
                 │                     │                      │
  ==============─┼─────────────────────┼──────────────────────┼==============
  SHARED MODULE  │                     │                      │
                 ▼                     ▼                      ▼
       ┌────────────────────────────────────────────────────────────────┐
       │                  SHARED PRESENTATION INTERFACES                │
       │         [Common ViewModels & StateFlow State Machines]        │
       └───────────────────────────────┬────────────────────────────────┘
                                       │
       ┌────────────────────────────────────────────────────────────────┐
       │                     SHARED DOMAIN REPOSITORIES                 │
       │         [Transaction Validation, Sizing Algorithms, Quizzes]   │
       └───────────────────────────────┬────────────────────────────────┘
                                       │
       ┌────────────────────────────────────────────────────────────────┐
       │                      SHARED DATA PERSISTENCE                   │
       │      [Room KMP (SQLCipher local database) / Ktor API Client]   │
       └────────────────────────────────────────────────────────────────┘
```

### Strategic Step-by-Step Evolution Path

#### Step 1: Establish the `:shared` Module (✅ Completed & Configured)
We have successfully established and configured the multiplatform shared library module (`:shared`) containing multi-platform target compilations:
*   **Gradle Build Setup:** Registered the Kotlin Multiplatform plugin globally, added the `:shared` module configuration in `settings.gradle.kts`, and successfully resolved AGP compatibility settings inside `gradle.properties`.
*   **Cross-Platform Compilation:** Structured `:shared` to support both native JVM Android (`androidMain`) and web-based WebAssembly (`wasmJsMain`), alongside the standard shared `commonMain`.
*   **Proof of Concept:** Implemented a compiler-verified shared `Platform` abstraction demonstrating `expect`/`actual` targets:
    *   `commonMain/`: Declares `expect fun getPlatform(): Platform`
    *   `androidMain/`: Yields `"Android " + SDK_INT`
    *   `wasmJsMain/`: Yields `"WebAssembly (Browser)"`
*   **Status:** Successfully compiles and builds green within the Android Gradle Toolchain (`compile_applet` passed).

#### Step 2: Database Portability (Room KMP)
Starting in **Room 2.7.0**, Room fully supports Kotlin Multiplatform.
*   The data entities (`Entities.kt`) and DAOs (`Daos.kt`) will be moved directly into `:shared/src/commonMain/kotlin`.
*   A factory helper class utilizing Kotlin’s `expect`/`actual` declarations will open database connections natively on each target platform:
    *   **Android:** Opens via Android's database context.
    *   **iOS:** Instantiates the database via SQLite driver wrappers on iOS.
    *   **Web:** Room KMP supports Kotlin/Wasm and Kotlin/JS target compilation. It utilizes SQLite compiled to WebAssembly (Wasm) combined with the Origin Private File System (OPFS) or IndexedDB for persistent storage directly in modern web browsers, providing identical SQL/DAO semantics as the native app.

#### Step 3: Cloud Synchronization & User Registration Sync Architecture
To maintain the offline-first experience while allowing seamless transitions to the web, the app employs a **Synchronization Engine**:
*   **Local-First Write:** All user trades, watchlists, and profile states write immediately to the local Room KMP SQLite instance. This maintains instant responsiveness.
*   **Asynchronous Sync Syncing:** A background sync manager monitors network availability. When connected, it serializes local changes (via JSON using `kotlinx.serialization`) and syncs them to a centralized database (such as Google Cloud Firestore or a custom REST API).
*   **Idempotent Merging:** The backend uses timestamp-based conflict resolution to merge incoming transactions. When a user registers or logs in on a new Web platform, the sync manager pulls the latest remote snapshot, resolves any conflicts with local offline trades, and writes the unified dataset back to the local WebAssembly Room instance. This ensures users have access to all of their data on any device without latency.

#### Step 4: Domain & Business Logic Migration
*   Move `TradingRepository` to `commonMain`. 
*   Since the repository performs calculation tasks, arithmetic, and database state updates, it is 100% portable pure Kotlin code and requires zero platform-specific edits.

#### Step 4: Native Presentation Layer Binding
*   **Android Presentation:** Keeps the existing Jetpack Compose UI, now observing the `:shared` ViewModels.
*   **iOS Presentation (Two Options):**
    *   *Option A (Maximum Reuse - Compose Multiplatform):* Build the iOS UI in Compose. The exact same Compose UI files in the shared module render natively on iOS.
    *   *Option B (Premium Platform Polish - SwiftUI):* Export the shared ViewModels to Swift using objective-C/Swift framework boundaries. Build the views natively in SwiftUI observing the shared StateFlow streams.
*   **Web Presentation:**
    *   Compile the shared Kotlin code to Kotlin/Wasm to render the UI on an HTML5 canvas, or use Kotlin/JS wrappers to hook into a React/Tailwind frontend.

#### Step 5: UI Layer Modularization & Whitelabel Layout
To simplify future whitelabel configurations, the UI layer under `com.ashwathai.tradelab.ui` is organized into standalone feature modules:
*   `com.ashwathai.tradelab.ui.common`: Reusable, brand-neutral components (buttons, headers, dialogs, disclaimers).
*   `com.ashwathai.tradelab.ui.portfolio`: Screens and graphics tracking portfolio holdings and historic performances.
*   `com.ashwathai.tradelab.ui.charts`: Native canvas-based technical charting structures.
*   `com.ashwathai.tradelab.ui.watchlist`: Stock lookup, watchlist management, and default order sheets.
*   `com.ashwathai.tradelab.ui.academy`: Gamified quiz components and financial lesson modules.
*   `com.ashwathai.tradelab.ui.derivatives`: Advanced options chain tables, Greeks metrics, and option tickets.
*   `com.ashwathai.tradelab.ui.commodities`: Indian MCX commodities desk UI.
*   `com.ashwathai.tradelab.ui.profile`: Subscription triggers, profile setups, and psychological profiling flows.
*   `com.ashwathai.tradelab.ui.diagnostics`: AI-driven auditing pages.

*Whitelabel Toggle:* Feature toggles are centralized inside a standard configuration manager (`WhitelabelConfig.kt`), allowing instant feature exclusions and customer branding customizations compile-time or runtime.

---

## 4. Architectural Best Practices for Future Agents

When adding new code or editing existing structures, you **must** adhere to these architectural rules:
1.  **Framework Quarantine:** Keep Kotlin files in `com.ashwathai.tradelab.data` completely free of references to Android system frameworks (e.g., `android.content.Context`, Android Views). Use constructor injection for dependencies.
2.  **Explicit Coroutine Scopes:** Always leverage structured concurrency. Perform database transactions inside the repository layer using `withContext(Dispatchers.IO)` to prevent UI freezing.
3.  **No Direct SQLite Calls:** All database transactions must go through a Room DAO. Write clean SQL statements using uppercase SQL keywords (e.g., `SELECT`, `INSERT`, `WHERE`) to keep queries clean and understandable.
4.  **Compose State Decoupling:** Composable screens must remain state-agnostic. Pass simple values (e.g., standard numbers, strings, or data classes) and lambdas for actions down to layout elements. Keep business logic and state evaluation nested in the ViewModels.
