# Trade Lab 📈

**Trade Lab** is a realistic, offline-first paper trading simulator designed specifically for beginners and young investors (16–18 years old). It is designed to teach disciplined position sizing, risk management, and market mechanics using virtual budgets denominated in Indian Rupees (₹) and US Dollars ($).

Unlike traditional trading simulators that encourage reckless "fantasy high-roller" behaviors with millions of mock dollars, Trade Lab instills real-world investing habits by starting users with realistic budgets (e.g., ₹10,000) and enforcing strict transactional boundaries.

---

## 🌟 Key Features

*   **Psychological Profiler:** A tailored 60-second onboarding questionnaire that aligns your virtual capital sizes directly with real-world target budgets, building practical investing habits from day one.
*   **Dynamic Simulation Canvas:** Real-time interactive price fluctuation engine that renders custom, animated vector stock charts on-the-fly using native Android `Canvas`.
*   **Unidirectional State Engine:** A centralized `TradingViewModel` managing real-time states for watchlists, portfolio values, active positions, and mock market prices.
*   **Buy/Sell Order Tickets:** Real-time transactional validation verifying cash-on-hand before execution to prevent over-leveraging.
*   **Learn-to-Earn (Upcoming):** Acquire more virtual capital to trade by completing educational modules and finance quizzes.

---

## 🎨 Visual Identity & Theme

Trade Lab features a premium, high-contrast **Sophisticated Dark** theme. Styled with bright neon accent highlights, modern typography, and generous layout spacing, it is optimized for high-readability and visual scanning.

---

## 🛠️ Technology Stack & Architecture

Built using modern Android development best practices and guidelines:

*   **User Interface:** 100% Kotlin & **Jetpack Compose** with Material Design 3 (M3).
*   **Data Persistence:** Local SQLite database managed securely through **Room Database** with Kotlin Symbol Processing (KSP).
*   **State Management:** Architecture following **MVVM** (Model-View-ViewModel) with structured, unidirectional data streams using Kotlin `StateFlow`.
*   **Local JVM Testing:** Built-in unit and screenshot tests powered by **Robolectric** and **Roborazzi**.

---

## 🚀 Getting Started

To run or build the application locally:

```bash
# Verify compilation and build status
gradle assembleDebug

# Run unit and local integration tests
gradle :app:testDebugUnitTest
```

---

## ❤️ About Ashwath AI

Trade Lab is designed and developed with precision as part of the Ashwath AI suite of tools.

Built with ❤️ by **Ashwath AI**
*Building free, open-source software, AI, and games for everyone.*
