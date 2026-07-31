# Authoritative Agent Context & Handover (`AGENTS.md`)

Welcome, Agent! This document is your single source of truth for understanding the **Trade Lab** project from Ashwath AI. Reading this file gives you full context of the project architecture, features, current progress, and next steps without needing to sift through thousands of lines of code.

---

## 1. Project High-Level Overview
**Trade Lab** is an Android-first, realistic, offline-first paper trading simulator designed for beginners and retail practitioners (16–35+ years old). It aims to teach realistic position sizing, risk management, and market mechanics using virtual budgets denominated in Indian Rupees (₹) and US Dollars ($).

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
9.  **Track B — Institutional Order Management (Epic 18):**
    *   Implemented 5x leverage for Intraday (MIS) trades.
    *   Built an **Auto-Liquidation Engine** that triggers a "Margin Call" square-off if equity falls below 50% maintenance.
    *   Implemented **Trailing Stop-Loss** logic that dynamically adjusts trigger prices to lock in profits.
    *   Integrated multi-leg **Bracket Orders (OCO)** into the primary Trade Desk.
10. **Track C — Social Discipline & Precision Ledger (Epic 19):**
    *   Implemented a proprietary **Discipline Score (0-100)** algorithm quantifying retail maturity.
    *   Added dynamic **Discipline Badges** (e.g., "Sizing Master", "Patience King") to the user profile.
    *   Upgraded the **Global Leaderboard** with multi-sort (Wealth vs. Maturity) and 100-user capacity.
    *   Integrated a high-precision **Tradebook (Ledger)** tracking every virtual penny to 4 decimal places.

---

## 4. Current Work & Next Milestones

We have successfully completed **Phase 1 (Foundations)**, the **Pre-Launch Polish**, and the **Social Arena**. The app is now at **v1.3.0 (Build 4)** and ready for production distribution.

### What is Active Right Now (Post-Launch & Multi-Platform Prep)
*   **Track A (UI/UX & Charts):** native **Candlestick** rendering and institutional overlays are stable. Future work includes Volume Profiles and advanced Fibonacci tools.
*   **Multi-Platform Migration:** Initiating the move of core entities and repositories to Kotlin Multiplatform (KMP) to support iOS.
*   **Web Evolution:** Transitioning the web sandbox to a professional **Next.js** application with full state parity (Epic 20).

### Parallelizable Engineering Streams
Multiple contributors can work on these tracks in parallel to meet the launch deadline:
*   **Track A:** `docs/epics_and_sprints.md#Epic 17`
*   **Track B:** `docs/epics_and_sprints.md#Epic 18`
*   **Track C:** `docs/epics_and_sprints.md#Epic 19`

---

## 5. Architectural & Implementation Strategy

For a deep dive into how the system is organized and how it will expand to iOS/Web, please read:
👉 **[`docs/architecture.md`](docs/architecture.md)**

For the full breakdown of upcoming epics, sprints, and detailed task boards, please read:
👉 **[`docs/epics_and_sprints.md`](docs/epics_and_sprints.md)**

---

## 6. Authoritative Document Iteration Process

> **Agent Workflow:** Before starting any task, agents MUST read [`agent-skills.md`](./agent-skills.md) and invoke the appropriate skill from [`skills/`](./skills/).

To maintain continuous alignment and prevent code-spec drift, all agents and developers must adhere to the following iteration loop:

```
  ┌─────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐
  │  Step 1: Select Branch  │ ───> │  Step 2: Update Spec    │ ───> │  Step 3: User Approval  │ ───> │  Step 4: Implement Code │
  │ Checkout develop/website│      │ Edit epics/architecture │      │ Confirm plan in chat    │      │ Write Kotlin & Tests    │
  └─────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘
```

0.  **Branch Discipline:** You MUST work on the correct branch based on the task type:
    *   **`website` branch**: All changes to the static website (`website/` directory).
    *   **`develop` branch**: All Android App engineering, refactoring, and feature work.
    *   **`main` branch**: Reserved for stable production releases only. NEVER implement features directly on `main`.
1.  **Spec-First Modification:** Before implementing a new feature or modifying existing interfaces, update the target epic/sprint task in `docs/epics_and_sprints.md` or the design details in `docs/architecture.md` to show the proposed design.
2.  **Review & Handshake:** Present the spec changes to the user for feedback. 
3.  **Surgical Execution:** Once agreed, implement the feature, write corresponding unit/integration tests, and mark the task as complete (`[x]`) in the documentation.
4.  **Mandatory Testing Rule:** Always add or update corresponding unit tests whenever new functionality is implemented or modified. Every PR/change should include verification logic.
5.  **Background Tasks Rule:** Infinite loops or periodic background tasks must NEVER be placed in a `ViewModel`'s `init` block. Move these to an explicit `startBackgroundTasks()` function called by the Activity. This prevents the "Never-Idle" deadlock that hangs Android tests.
6.  **Versioning & Release Policy:** NEVER automatically bump version codes or numbers in `app/build.gradle.kts`. Always ask the user for explicit approval before performing a version bump. **`RELEASES.md` and `CHANGELOG.md` MUST ONLY be updated when the project is ready for release and ONLY after explicit user confirmation.**
7.  **No Dead-Ends:** Never add non-functional UI placeholders. Every visual affordance must connect to an active feature or remain omitted.
8.  **Authoritative Manual Verification:** Every feature implementation must include an update to the [Authoritative Manual Verification Protocol](file:///C:/Users/Atul/AppData/Local/Google/AndroidStudio2026.1.2/projects/tradelab.55ea813f/.artifacts/b97c14b5-a8e9-4d28-9834-c46165db831b/MANUAL_VERIFICATION_PROTOCOL.artifact.md) or the checklist to ensure functional integrity can be verified by testers.
