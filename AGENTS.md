# Authoritative Agent Context & Handover (`AGENTS.md`)

Welcome, Agent! This document is your single source of truth for understanding the **Trade Lab** project from Ashwath AI. Reading this file gives you full context of the project architecture, features, current progress, and next steps without needing to sift through thousands of lines of code.

---

## 1. Project High-Level Overview
**Trade Lab** is an Android-first, realistic, offline-first paper trading simulator designed for beginners and young investors (16–18 years old). It aims to teach realistic position sizing, risk management, and market mechanics using virtual budgets denominated in Indian Rupees (₹) and US Dollars ($).

*   **Key Philosophy:** Prevent reckless "fantasy high-roller" behaviors by starting with realistic budgets (e.g., ₹10,000) and enforcing disciplined position sizing. Users "earn" more virtual capital by completing educational quizzes ("Learn-to-Earn").
*   **Company & Brand:** Developed by **Ashwath AI** (Package ID: `com.ashwathai.TradeLab`).
*   **Aesthetic Theme:** Premium high-contrast **Sophisticated Dark** theme with neon highlights and custom live vector charts.

---

## 2. Directory Structure & Key Code Entry Points

To avoid searching blindly, here is exactly where the core logic resides:

```
├── AGENTS.md                                # This authoritative instruction file
├── docs/                                    # Documentation directory
│   ├── PRODUCT_STRATEGY_AND_PRD.md          # Original product requirements document
│   ├── VISION_INTERVIEW_LOGS.md             # Qualitative interview logs & strategy notes
│   ├── architecture.md                      # Comprehensive multi-platform architecture spec
│   └── epics_and_sprints.md                 # Product roadmap, epics, sprints, and tasks
├── app/                                     # Android Application Module
│   ├── src/main/java/com/ashwathai/tradelab/ # Core Kotlin code
│   │   ├── MainActivity.kt                  # Entry point (holds the Jetpack Compose navigation & views)
│   │   ├── data/                            # Local database and persistence layer
│   │   │   ├── AppDatabase.kt               # Room database definition
│   │   │   ├── Daos.kt                      # Room DAOs (User, Holdings, Transactions, Watchlist, StockPrices)
│   │   │   ├── Entities.kt                  # SQLite table definitions
│   │   │   └── TradingRepository.kt         # Data-access repository managing trade executions
│   │   └── ui/                              # User Interface
│   │       ├── TradingViewModel.kt          # UI state machine and business logic
│   │       ├── di/                          # Hilt Dependency Injection Modules
│   │       └── theme/                       # Color, Type, and Theme configurations
│   └── src/test/                            # Local JVM & Robolectric Tests
│       └── java/com/ashwathai/tradelab/      # Tests verifying UI states and robustness
```

---

## 3. What Has Been Done (Phase 1 & 2 Foundations)
The **Architectural Modernization** has been successfully verified:
1.  **Local Database Schema (Room):** Structured local tables initialized with mock Indian equity data (e.g., RELIANCE, TCS, INFOSYS, HDFCBANK). Version 9 supports persistent UI preferences.
2.  **Hilt Dependency Injection:** Fully decoupled the app using Hilt. All components use constructor injection, resolving static singleton bottlenecks.
3.  **Steered Anchored Simulation:** Implemented a realistic price engine where stocks "wiggle" locally but gravitate (5% vector) toward real-world Yahoo Finance anchors.
4.  **Indian Market Realism:** Simulation respects NSE/BSE holidays and strictly stops wiggling at 3:30 PM IST (MCX 11:30 PM).
5.  **Ultra-Dense UI & Swipe Navigation:**
    *   `HorizontalPager` integration allowing fluid swipes between tabs.
    *   Collapsible search lens and "vanishing" tickers to maximize data density.
6.  **Psychological Profiler:** A 60-second questionnaire on the Profile screen that aligns virtual capital sizes to real-world budgets to build realistic trading habits.
7.  **Production Auth & Diagnostics:** Firebase Auth (Google/Phone) with robust Logcat-based diagnostics for production environment configuration.
8.  **Viral Sharing & Professional Mechanics (Epic 16):**
    *   Integrated dynamic performance-based "GenZ" sharing hooks for social media snapshots.
    *   Added institutional-grade product toggles (CNC/MIS for Equity, NRML/MIS for F&O) with educational tooltips.
    *   Standardized T+1 settlement visibility and square-off logic for derivatives.

---

## 4. Current Work & Next Milestones

We have successfully completed **Phase 1 (Foundations)** and the **Pre-Launch Polish**. The app is now at **v1.2.0 (Build 4)** and ready for production distribution.

### What is Active Right Now
*   **Release Distribution:** Deploying signed APKs and AABs to internal testing tracks.
*   **Multi-Platform Planning:** Scoping the migration of core logic to Kotlin Multiplatform (KMP) for iOS and Web support (deferred until milestone achievement).

### Parallelizable Engineering Streams
To maximize efficiency, multiple agents or contributors can work on these distinct tracks in parallel:

*   **Track A (UI/UX & Charts - Frontend):** Adding indicators like **Moving Averages (SMA/EMA)** and **RSI** directly onto the custom vector charts (`MainActivity.kt` canvas logic).
*   **Track B (Order Management - Core Backend):** Extending the `TradingRepository` and database entities to support **Limit** and **Stop-Loss** orders.
*   **Track C (Academy & Gamification - Business Logic):** Designing the "Learn-to-Earn" questionnaire schemas, lesson databases, and virtual capital unlocking triggers.

---

## 5. Architectural & Implementation Strategy

For a deep dive into how the system is organized and how it will expand to iOS/Web, please read:
👉 **[`docs/architecture.md`](docs/architecture.md)**

For the full breakdown of upcoming epics, sprints, and detailed task boards, please read:
👉 **[`docs/epics_and_sprints.md`](docs/epics_and_sprints.md)**

---

## 6. Authoritative Document Iteration Process

To maintain continuous alignment and prevent code-spec drift, all agents and developers must adhere to the following iteration loop:

```
  ┌─────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐
  │  Step 1: Update Spec    │ ───> │  Step 2: User Approval  │ ───> │  Step 3: Implement Code │
  │ Edit epics/architecture │      │ Confirm plan in chat    │      │ Write Kotlin & Tests    │
  └─────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘
```

1.  **Spec-First Modification:** Before implementing a new feature or modifying existing interfaces, update the target epic/sprint task in `docs/epics_and_sprints.md` or the design details in `docs/architecture.md` to show the proposed design.
2.  **Review & Handshake:** Present the spec changes to the user for feedback. 
3.  **Surgical Execution:** Once agreed, implement the feature, write corresponding unit/integration tests, and mark the task as complete (`[x]`) in the documentation.
4.  **Mandatory Testing Rule:** Always add or update corresponding unit tests whenever new functionality is implemented or modified. Every PR/change should include verification logic.
5.  **Background Tasks Rule:** Infinite loops or periodic background tasks must NEVER be placed in a `ViewModel`'s `init` block. Move these to an explicit `startBackgroundTasks()` function called by the Activity. This prevents the "Never-Idle" deadlock that hangs Android tests.
6.  **Versioning & Release Policy:** NEVER automatically bump version codes or numbers in `app/build.gradle.kts`. Always ask the user for explicit approval before performing a version bump or updating `CHANGELOG.md` and `RELEASES.md`.
7.  **No Dead-Ends:** Never add non-functional UI placeholders. Every visual affordance must connect to an active feature or remain omitted.
