# Trade Lab — Product Strategy & PRD

## 1. Product Vision & Mission
**Trade Lab** is an Android-first, realistic, offline-first paper trading platform designed to help users practice trading, test investment ideas, and build confidence before risking real money in the market. 

Unlike standard brokerages or crowded trading journals, Trade Lab focuses entirely on safe, tactile, and highly realistic simulation, wrapped in a high-contrast premium **Sophisticated Dark** aesthetic.

---

## 2. Core Value Proposition
- **Risk-Free Mastery:** Explore market operations, experiment with leverage/strategies, and visualize profit/loss metrics safely.
- **Offline-First Persistence:** Keep your trading activity private and fast. All transactions, watchlists, holdings, and profile stats are saved locally using a robust SQLite (Room) database.
- **Dynamic Simulation:** Simulate price action changes and market tick updates directly in the app to practice decision-making during volatile periods.
- **Premium Material 3 Styling:** High-contrast design using an premium dark-canvas theme, neon highlights, and custom live vector charts.

---

## 3. Minimum Viable Product (MVP) Scope
The MVP has been fully designed and integrated into the current Android application:

### A. Core Architecture & Local Persistence
- **Room SQLite Database:** Structured tables for `UserProfile`, `Holdings`, `Transactions`, `Watchlist`, and `StockPrices`.
- **Repository Pattern:** Centralized data access layer with transactional support to execute trades and manage cash balances reliably.
- **Jetpack Compose UI:** 100% Kotlin Compose architecture driven by unidirectional data flow with a central `ViewModel`.

### B. Core Functional Requirements
1. **Dynamic Portfolio Dashboard:**
   - Real-time aggregate values: **Total Portfolio Value**, **Available Cash (Buying Power)**, and **Total Return % / Open P/L**.
   - Live **Interactive Market Tick Simulator** button to instantly fluctuate asset prices and update charts dynamically.
2. **Assets Overview (Distribution):**
   - Live horizontal distribution bar visualizing current asset valuation vs. raw cash balance.
   - Expandable, detailed holdings list showing shares owned, cost-basis, and current profit/loss.
3. **Trade Desk & Stock Details:**
   - Full search functionality to filter stock tickers.
   - High-contrast, dynamic **Vector Canvas Line Charts** for each selected stock showing historic performance trends.
   - Interactive order ticket form validating transaction costs against raw buying power before placing **BUY** and **SELL** orders.
4. **Interactive Watchlist:**
   - Custom-curated stock watchlist persisted locally.
   - Toggle star icons next to stock symbols to add/remove assets directly from the watchlist.
5. **Interactive Psychological Alignment & Realistic Budget Profiler:**
   - 60-second interactive multi-step questionnaire built into the **Profile** screen.
   - Evaluates user intent: **Genuine Skill Learning** (focuses on matching virtual capital to real-world budgets) vs. **Gamified Speculation** (fantasy high-roller mode).
   - Dynamically calculates a personalized capital allocation and risk style recommendation (e.g., ₹10k, ₹50k, or ₹100k) based on real-world target budgets.
   - Restores user focus to retail realism to prevent downstream psychological over-trading habits. Applies direct database-level portfolio resets.

---

## 4. Product Roadmap

```
  ┌─────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐
  │         Phase 1         │      │         Phase 2         │      │         Phase 3         │
  │     Local MVP Engine    │ ───> │     Advanced Tactics    │ ───> │   Social & AI Insights  │
  │       (CURRENT V1)      │      │        (Mid-Term)       │      │       (Long-Term)       │
  └─────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘
```

### Phase 1: Local MVP (Completed V1)
- [x] Room-persisted stock database with mock data initializer.
- [x] Dynamic vector chart generator using Android Compose Canvas.
- [x] Local buy/sell transactional validation engine.
- [x] Interactive watchlist toggling.
- [x] Profile reset mechanisms and custom Material 3 "Sophisticated Dark" styling.

### Phase 2: Advanced Tactics (Completed V2)
- [x] **Limit and Stop Orders:** Add advanced order types (Limit, Stop-Loss, Take-Profit) matching standard trading platforms.
- [x] **Technical Indicators:** Overlay simple technical indicators (Moving Averages, RSI) directly onto the vector charts.
- [x] **Historical Performance Metrics:** Display transaction history logs with detailed metrics showing daily/weekly wins and losses.
- [x] **Offline On-Demand Ads Monetization:** Configured AdMob rewarded-ad "Watch-to-Earn" flows (+100 brokerage credits, emergency recharges, unlocking technical overlays).

### Phase 3: AI Integration & Long-Term Roadmap (Completed V2)
- [x] **Serverless Gemini API Strategy Advisor:** Generate personalized AI portfolio diagnostics, scanning the user's trading history to point out behavioral pitfalls (e.g., holding onto losing trades too long).
- [ ] **Multi-Platform Native Core Sync:** Enable secondary secure cloud sync to view portfolios from a web browser while keeping data private (Currently in active development with KMP Core Migration).

---

## 5. Technical Stack

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin | Primary application logic |
| **UI Framework** | Jetpack Compose | Declarative UI development |
| **Design System** | Material Design 3 | Grid alignment, layouts, and accessibility |
| **Database** | Android Room | Client-side persistent storage |
| **Concurrency** | Kotlin Coroutines & Flow | Asynchronous database updates and responsive state rendering |
| **Theme** | Sophisticated Dark | Custom high-contrast visual palette |
