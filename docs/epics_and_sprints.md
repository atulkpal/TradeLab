# Trade Lab — Epics & Sprints Backlog

This document defines the development roadmap for **Trade Lab**. It breaks down our long-term vision into concrete, manageable **Epics**, **Sprints**, and granular **Tasks**. Use this document to track active progress, coordinate parallel streams, and select your next engineering assignment.

---

## Roadmap Overview

```
 ┌─────────────────────────────────────────────────────────────┐
 │       EPIC 1: Local MVP Persistence & Engine [100% COMPLETE]│
 └──────────────────────────────┬──────────────────────────────┘
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  EPIC 2: Advanced Order Types & Trade Desk   [100% COMPLETE]│
 ├─────────────────────────────────────────────────────────────┤
 │  EPIC 3: Learn-to-Earn Financial Academy     [100% COMPLETE]│
 ├─────────────────────────────────────────────────────────────┤
 │  EPIC 8: On-Demand Ads & Gamified Monetization [100% COMP.] │
 └──────────────────────────────┬──────────────────────────────┘
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  EPIC 4: Technical Charts & Indicators       [100% COMPLETE]│
 ├─────────────────────────────────────────────────────────────┤
 │  EPIC 5: Serverless AI Portfolio Diagnostics [100% COMPLETE]│
 ├─────────────────────────────────────────────────────────────┤
 │  EPIC 7: Multi-Watchlists, Auth Gate & Paywalls [100% COMP.]│
 └──────────────────────────────┬──────────────────────────────┘
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  EPIC 6: Multi-Platform KMP Core Migration   [ACTIVE WORK]  │
 ├─────────────────────────────────────────────────────────────┤
 │  EPIC 9: Simulated F&O Engine & Premium Paywalls [100% COMP.]│
 └──────────────────────────────┬──────────────────────────────┘
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  EPIC 10: Codebase Modularization & Refactoring [100% COMP.]│
 └─────────────────────────────────────────────────────────────┘
```

---

## Epic 1: Local MVP Persistence & Engine (Status: 🟢 Complete)
Build a solid, offline-first client database foundation with responsive Material 3 rendering.

### Sprint 1.1: Local Database Schema & Initialization
*   [x] Create entities for user profile, holdings, transactions, and stocks.
*   [x] Set up Room SQLite database context with structured schema.
*   [x] Implement automatic mock price and initial holdings database seeding.

### Sprint 1.2: Unidirectional Portfolios & Transactions
*   [x] Create centralized `TradingRepository` to validate balances.
*   [x] Implement BUY and SELL trade execution blocks.
*   [x] Expose aggregate calculations (Total Return, Open P/L, Asset distribution).

### Sprint 1.3: Visual Chart Canvas Renderer
*   [x] Design native Android Compose Canvas drawing routine.
*   [x] Map historic prices to high-contrast visual trend lines.
*   [x] Integrate live market fluctuation tick simulator to update graphs instantly.

### Sprint 1.4: Realism Budgets & Cognitive Profiler
*   [x] Implement multi-step questionnaire checking user retail intent.
*   [x] Guide users toward a disciplined starting capital recommendation (₹10,000 to ₹100,000).
*   [x] Implement portfolio reset database queries.

---

## Epic 2: Advanced Order Types & Trade Desk (Status: 🟢 Complete)
Move beyond simple market executions. Implement realistic broker order types, GTT screens, and a futuristic expandable bottom sheet.

### Sprint 2.1: Database Schema Support for Pending & GTT Orders
*   [x] Add `PendingOrderEntity` table to `Entities.kt` to persist unexecuted Limit, Stop-Loss, and GTT (Good-Till-Triggered) orders.
*   [x] Support order status properties (`PENDING`, `EXECUTED`, `CANCELLED`) and order duration types (`DAY`, `GTT`).
*   [x] Define DAOs to query, update, and delete active pending orders based on simulated price levels.

### Sprint 2.2: Futuristic Slide-Up Order & Detail Sheet
*   [x] Replace the static order view with a sleek, interactive Slide-Up Bottom Sheet for Buy/Sell/GTT operations.
*   [x] Add an expandable caret/arrow button on the top-header of the sheet.
*   [x] When tapped, animate the sheet to expand to a gorgeous full-screen stock detailed view containing:
    *   An expanded canvas-drawn historic line chart.
    *   Detailed stock indicators (High/Low, Open, 52-Week Range, Volume, Market Cap).
    *   Educational tabs explaining Market vs. Limit vs. GTT orders.
*   [x] Embed an order configuration widget directly inside the expanded/collapsed sheet with numeric inputs, position sizing advice, and quick-percent buttons (e.g., "25%", "50%", "100%" of buying power).

### Sprint 2.3: Order Matching & GTT Trigger Engine
*   [x] Integrate GTT and Limit pending-order trigger evaluation into the `TradingRepository`.
*   [x] When the Market Tick Simulator changes prices, evaluate all active pending and GTT entries.
*   [x] Automatically execute qualifying orders, log transactions, and notify the user with an in-app visual toast/alert.

---

## Epic 3: Learn-to-Earn Financial Academy (Status: 🟢 Complete)
Reward study habits with additional virtual trading capital.

### Sprint 3.1: Academic Lesson Database
*   [x] Add a `LessonEntity` database table storing financial articles, trading concepts, and risk guidelines.
*   [x] Define lesson categories (e.g., "Position Sizing", "Market Basics", "Order Types").
*   [x] Track user read status and lock/unlock progress flags.

### Sprint 3.2: Interactive Terminology Quizzes
*   [x] Implement a Quiz UI overlay showing multiple-choice questions at the end of each lesson.
*   [x] Provide immediate feedback on correct/incorrect answers with interactive explanations.
*   [x] Store quiz pass/fail histories in the local database.

### Sprint 3.3: Virtual Capital Unlocking Logic
*   [x] Implement the mathematical progression code: passing a quiz unlocks ₹5,000 or ₹10,000 in virtual capital.
*   [x] Add a "Claim Capital" transaction that appends the unlocked amount to the user's current buying power.
*   [x] Design an aesthetic UI celebration overlay with custom animations for claiming rewards.

---

## Epic 4: Technical Charts & Overlays (Status: 🟢 Complete)
Add intermediate indicators and professional metrics to the custom line charts.

### Sprint 4.1: Technical Analysis Algorithms
*   [x] Write pure Kotlin algorithms computing Simple Moving Average (SMA) and Exponential Moving Average (EMA).
*   [x] Implement Relative Strength Index (RSI) calculations based on a stock's historic price entity arrays.
*   [x] Expose calculated indicator arrays inside the `TradingViewModel` state.

### Sprint 4.2: Chart Overlay Renderer
*   [x] Update Canvas rendering code to draw auxiliary SMA/EMA lines on top of the stock line.
*   [x] Add an optional bottom sub-graph pane dedicated to drawing RSI oscillators.
*   [x] Build a drag gesture observer displaying stock prices dynamically as a user drags their finger across the chart.

---

## Epic 5: Serverless AI Portfolio Diagnostics (Status: 🟢 Complete)
Leverage Gemini to audit trading behaviors offline and protect retail speculators.

### Sprint 5.1: Secure Gemini SDK Integration
*   [x] Set up safe API credentials storage using AI Studio's Secrets panel.
*   [x] Configure Kotlin's Gemini API client (incorporating error handling and network status checks).

### Sprint 5.2: Portfolio Behavior Audit Engine
*   [x] Structure a structured prompt passing historic transaction logs, current holdings, and overall return metrics to the Gemini model.
*   [x] Query Gemini to diagnose behavioral biases (e.g., "Sunk-Cost Fallacy: holding losing trades too long", "Revenge Trading: excessive transaction volume").
*   [x] Display the generated advisor report in an aesthetic "AI Strategy Hub" dashboard tab.

---

## Epic 7: Multi-Watchlists, Auth Gate & Paywalls (Status: 🟢 Complete)
Support multiple persistent watchlists, user registration gating, and premium AI features.

### Sprint 7.1: Multi-Watchlist Schema & Management
*   [x] Implement `WatchlistNameEntity` and link to `WatchlistEntity` in `Entities.kt` to support up to 5 custom, renamable lists.
*   [x] Provide ViewModel states for current active watchlist selection and renaming.
*   [x] Create UI for Watchlist Screen with a dropdown/tab system to switch watchlists, a dialog to rename lists, and quick add/remove.

### Sprint 7.2: Registration Gate & Login Simulation
*   [x] Add action counter in local storage or database to track "trial actions" (e.g., executing trades, creating watchlists).
*   [x] Block further core actions after 5-10 trial attempts with an aesthetic registration screen.
*   [x] Store simulated user credentials in Room/Preferences to prepare for cross-platform cloud sync (KMP setup).

### Sprint 7.3: Paywall Gating & AI Analysis Hub
*   [x] Design an interactive premium paywall overlay highlighting locked features (e.g., "Talk to AI", Gemini portfolio diagnostics).
*   [x] Integrate Gemini API into a dedicated chat/audit view, protected behind the premium subscription simulator.

---

## Epic 6: Multi-platform KMP Core Migration (Status: 🟡 Active)
Scale the local architecture to support iOS and Web applications.

### Sprint 6.1: Shared Library Structuring
*   [x] Create a modular `:shared` project block inside `settings.gradle.kts`.
*   [x] Separate core repository, Room database schemas, and ViewModel flows from Android framework code (using TradingHelper and Platform).

### Sprint 6.2: Room KMP & Platform Adapters
*   [x] Configure Room KMP and multi-platform compilation compiler targets (Android and Wasm WebAssembly).
*   [ ] Configure Room KMP plugins in build dependencies.
*   [ ] Implement native SQLite factories for Android (`androidMain`) and iOS (`iosMain`).
*   [ ] Verify database read/write compilation on both platforms.

### Sprint 6.3: Web Client Setup
*   [ ] Configure Kotlin/Wasm platform compile targets.
*   [ ] Build a lightweight HTML5 canvas layout, or wrap shared states inside a React/JS framework.

---

## Epic 8: On-Demand Ads & Gamified Monetization (Status: 🟢 Complete)
Monetize the platform responsibly for the 16–35 demographic with a 100% non-intrusive, user-initiated rewarded-ad model ("Watch-to-Earn").

### Sprint 8.1: Rewarded State Architecture & Core Credit Wallet
*   [x] Add `brokerageCredits` (Int), `indicatorsUnlockedUntil` (Long), and `aiAuditCredits` (Int) properties to local user profile/state managers.
*   [x] Modify transaction execution routines in `TradingRepository` to check for `brokerageCredits`. If available, consume 20 credits to shield/waive simulated brokerage fees; if empty, deduct actual simulated cash (0.05% fee) from virtual capital.
*   [x] Link on-demand credit claims (e.g., "+100 Brokerage Credits", "+₹500 Emergency Recharge") to wallet and portfolio state handlers in the Repository.
*   [x] Integrate the Academy quiz state with ad-rewards: include fields for daily quiz limits, cooldown bypass state, and double reward claim triggers.

### Sprint 8.2: Visual Value-Exchange & Dedicated Power-Up Vault UI
*   [x] Design the Dedicated "Power-Up Vault" on the Profile screen allowing on-demand ad pre-watching to claim credits (+100 credits, +₹500 emergency cash, +1 AI credit).
*   [x] Add low-profile neon badges in the Slide-Up Order sheet displaying current credits: `🎫 Brokerage Wallet: X Credits` and the corresponding active shield status.
*   [x] Create locked indicator toggles (EMA/RSI) that open a premium dialog explaining the 12-hour unlock-on-ad-watch value offer.
*   [x] Design the "AI Strategy Hub" credit widget requesting ad-watches for extra Gemini diagnostics audit credits.
*   [x] Integrate ad booster options directly on the Academy quiz success screen (e.g., "Double My Capital Reward" or "Bypass Cooldown").

### Sprint 8.3: Video Player Simulation & SDK Integration (✅ Production Ready)
*   [x] Create a modular overlay showing a clean 5-second simulated video player with a progress bar and completion callback for offline testing.
*   [x] Add dependencies and initial bindings for Google Mobile Ads (AdMob) Rewarded Video API to prepare for live production deployment.
*   [x] Integrate official production AdMob App ID (`ca-app-pub-4153575596488132~8287049082`) and custom Rewarded Ad Unit IDs across all placements (Academy Double, Portfolio Shield, Profile Cash, AI Advisor, Indicators, Shield Max Limit).
*   [x] Wrap the integration inside standard, compiler-enforced `BuildConfig.DEBUG` checks using Gradle manifest placeholders and enums, ensuring safety (displaying standard Google test ads during debug builds and rendering live revenue-generating ads on release signed builds).

---

## Epic 9: Simulated F&O (Futures & Options) Engine & Premium Paywalls (Status: 🟢 Complete)
Develop a robust, offline-capable derivative simulation engine based on underlying assets, complete with compliance disclaimers, educational gated locks, premium subscriptions, and ad-supported premium "taste-testing."

### Sprint 9.1: Option Chain Generation Engine (Simulated Derivatives)
*   [x] Build an mathematical generator that dynamically structures Option Chains (Call/Put options) based on the underlying stock’s active simulated price.
*   [x] Define Option strike prices with uniform intervals (e.g., ±1%, ±2%, ±5% out-of-the-money/in-the-money).
*   [x] Calculate Option premiums dynamically using simplified Black-Scholes approximations or decaying pricing equations relative to remaining days-to-expiration (DTE).
*   [x] Implement trade execution blocks for Call/Put BUY/SELL, tracking premium expenditure, collateral requirements, and dynamic P/L changes as the underlying price fluctuates.

### Sprint 9.2: Strict Educational Level Locks & Monetization Paywalls
*   [x] Lock the entire F&O and Commodity trading section behind the Academy level gate. Users must complete and pass the "Equity Basics" curriculum to unlock F&O.
*   [x] Implement a Premium Paywall screen for F&O: once unlocked academically, accessing F&O trading requires a "Trade Lab Pro" premium subscription or a paywall pass.
*   [x] Create additional paywall lockouts for advanced premium tiers: Unlimited AI Behavioral Diagnostics, customized multi-watchlist profiles (more than 2), and advanced indicator suites (SMA/EMA/RSI combos).

### Sprint 9.3: Ad-Supported Premium "Taste-Tests" (Rewarded Trial Tokens)
*   [x] Implement a "Taste-Testing" rewarded ad value-loop: users who choose not to pay the cash subscription can watch a 30s video ad to receive **3 F&O Free Trade Tokens** or **1 AI Portfolio Audit Pass**.
*   [x] Track premium trial tokens in local preferences / state, permitting active use of paywalled screens for limited runs.
*   [x] Ensure elegant transitions/dialogs showing: `🎫 Premium Trial Token Active (Remaining: X)` to build high conversion interest without permanently restricting non-paying users.

### Sprint 9.4: Educational Compliance Disclaimers & Offline Error States
*   [x] Implement a **Progressive, Just-In-Time (JIT) Disclaimer Framework**: Disclaimers are never presented as a global app-entry block. Instead, they trigger dynamically *only* when a user actively attempts to use or trade in a specific asset class for the first time.
*   [x] **Capital Markets (NSE/BSE Equity) Disclaimer:** Upon entering the Search, Watchlist, or Order Ticket for the first time, present a bottom-sheet stating:
    > *"Educational Sandbox Disclaimer: Trade Lab utilizes delayed market feed quotes for Indian Equity markets (NSE/BSE). This software is strictly a simulated educational sandbox designed to build healthy risk management and position sizing habits. Prices and executions are delayed, simulated, and do not represent active real-world trading, live brokerage routing, or guaranteed market liquidity. Trade Lab is not a registered financial advisor or broker."*
*   [x] **F&O & Commodity Derivatives Disclaimer:** Upon unlocking and attempting to trade Options/Futures or Commodities for the first time, present a dedicated bottom-sheet stating:
    > *"Simulated Derivatives Compliance Notice: Trade Lab options and futures chains are mathematically synthesized internally based on historical underlying asset trends and localized pricing algorithms. These chains are designed purely for educational walkthroughs; they do NOT pull live contract options data from the National Stock Exchange (NSE) or Bombay Stock Exchange (BSE), nor do they reflect actual market open interest or clearinghouse operations. Derivative trading is highly speculative and carries extreme risk of capital loss."*
*   [x] **Consent Persistence:** Store the user's explicit consent for each disclaimer (`equity_disclaimer_accepted = true`, `fno_disclaimer_accepted = true`) inside Room/SharedPreferences so they only need to agree once, keeping future flows fast and fluid.
*   [x] Implement connectivity-sensing error flows: when the device has no internet connection, display a highly polished inline state banner or toast stating **"Network not available."** to manage user expectations gracefully during network transitions.

### Sprint 9.5: Infinite NSE/BSE Ticker Autocomplete & Dynamic DB Insertion
*   [x] Create a dynamic search auto-complete function that queries the Yahoo Finance Autocomplete API (`https://query2.finance.yahoo.com/v1/finance/search?q={query}&lang=en-IN&region=IN`) to instantly look up any Indian security listed on the NSE (`.NS`) or BSE (`.BO`).
*   [x] Implement a dynamic SQLite/Room injection flow: when a user clicks on an unseeded search result, fetch its latest core quote and write it as a new `StockPrice` row on the fly.
*   [x] Seed a rich, diverse local list of 100 core Indian stocks representing Nifty 50 and key indices, ensuring that if a user is completely offline, they still have an instant, diverse, and robust sandbox across all sectors (IT, Energy, Finance, FMCG).
*   [x] Maintain historical data simulation loops: any newly injected ticker will automatically receive simulated price tick variations in real-time, matching existing assets seamlessly.

### Sprint 9.6: Commodity Trading Desk & Ad-Wall Gate Integration
*   [x] Implement a dedicated **Commodities Desk** screen showing simulated MCX Indian commodities (Gold, Silver, Crude Oil, Natural Gas, Copper) alongside Global COMEX/NYMEX commodity indices.
*   [x] Establish highly realistic real-time price conversions: Indian MCX prices are dynamically converted on-the-fly from global COMEX/NYMEX USD values at a standard exchange rate (₹83/$), adjusted to correct trading units (e.g., per 10g for gold, per kg for silver, per barrel for crude oil).
*   [x] Implement an interactive **Derivatives Ad-Wall Security Gate**: since commodities trade on high leverage, gate access behind a premium-supported ad loop. Non-premium users can watch a simulated 5-second sponsor video with an active timer to unlock the desk for 12 hours.
*   [x] Fully integrate the Commodities Desk into the primary tab navigation (adding a beautiful new "Commodities" tab) and link it directly to the core ViewModel order ticket for buying and selling commodity contracts in real-time.

---

## Epic 10: Codebase Modularization & Refactoring (Status: 🟢 Complete)
Deconstruct the monolithic 9,000+ line `MainActivity.kt` into a highly decoupled, modular, feature-by-package structure. This establishes strict single-responsibility boundaries, simplifies future white-labeling configurations, improves compilation speed, and ensures high testability.

### Sprint 10.1: Package Architecture Setup & Core Utilities Isolation
*   [x] Define package structure under `com.ashwathai.tradelab.ui`: `common`, `portfolio`, `charts`, `watchlist`, `academy`, `derivatives`, `commodities`, `profile`, and `diagnostics`.
*   [x] Move common colors, styles, string formatters, global composables, and progressive JIT disclaimers to the `com.ashwathai.tradelab.ui.common` package.
*   [x] Verify empty module shells compile successfully with correct imports.

### Sprint 10.2: Feature Decomposition (Screens and Components)
*   [x] Extract **Portfolio & Dashboard View** (`PortfolioScreen.kt`, `PerformanceCharts.kt`) to `com.ashwathai.tradelab.ui.portfolio`.
*   [x] Extract **Interactive Technical Charting & Canvas System** (`StockChartCanvas.kt`) to `com.ashwathai.tradelab.ui.charts`.
*   [x] Extract **Watchlists, Stock Search & Main Order Tickets** (`WatchlistScreen.kt`, `OrderTicket.kt`) to `com.ashwathai.tradelab.ui.watchlist`.
*   [x] Extract **Academy Hub & Gamified Quiz Systems** (`AcademyScreen.kt`, `QuizViews.kt`) to `com.ashwathai.tradelab.ui.academy`.
*   [x] Extract **Derivatives Chain, Greeks & Option Order Ticket** (`FoDeskScreen.kt`, `OptionOrderTicket.kt`) to `com.ashwathai.tradelab.ui.derivatives`.
*   [x] Extract **Commodities Desk View** (`CommodityDeskScreen.kt`) to `com.ashwathai.tradelab.ui.commodities`.
*   [x] Extract **Profile Screen, Subscription Purchase & Habits Profiler** (`ProfileScreen.kt`, `ProfilerFlow.kt`) to `com.ashwathai.tradelab.ui.profile`.
*   [x] Extract **AI Behavioral Diagnostics View** (`DiagnosticsScreen.kt`) to `com.ashwathai.tradelab.ui.diagnostics`.

### Sprint 10.3: Root Shell Wiring & Whitelabel Toggle Layer
*   [x] Refactor `MainActivity.kt` to act as a slim router hosting only the `MainActivity` activity class and the core `TradeLabApp` bottom navigation shell.
*   [x] Create a whitelabel features configuration toggle class (`WhitelabelConfig.kt`) enabling fast runtime or compile-time whitelabel features masking (e.g., enable/disable options trading, toggle academy access).
*   [x] Perform comprehensive local JVM compilation and lint checks to ensure all imported packages compile flawlessly.



