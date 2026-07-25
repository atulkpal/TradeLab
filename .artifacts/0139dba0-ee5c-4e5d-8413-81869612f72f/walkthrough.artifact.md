# Walkthrough: Trade Lab Next.js Web App Roadmap

I have established a comprehensive planning and documentation framework for the upcoming Trade Lab Web App, moving from a static mockup to a production-ready **Next.js** ecosystem.

## Key Strategic Decisions

### 1. Technology Choice: Next.js + Zustand
Selected **Next.js 14+** for its superior SEO capabilities and server-side security (for Gemini API calls). **Zustand** was chosen for state management due to its high performance and built-in persistence, perfectly matching our "Local-First" reactive simulator state.

### 2. Charting Upgrade
Planned a migration from simple SVG-based line charts to **Lightweight Charts** (by TradingView). This will enable professional-grade **Candlestick** rendering and user-drawn support/resistance lines on the web.

### 3. Unified Cross-Platform Identity
Defined a cross-platform synchronization strategy using **Firebase Auth** and **Firestore**. This ensures users have a consistent portfolio and "Discipline Score" whether they are on their Android app or the web dashboard.

---

## Documentation Updates

| Document | Changes |
| :--- | :--- |
| **[architecture.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/docs/architecture.md)** | Updated the Web Platform section to specify the Next.js/React architecture and its role post-KMP migration. |
| **[WEB_PLATFORM_DOCUMENTATION.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/docs/WEB_PLATFORM_DOCUMENTATION.md)** | Added a detailed, 4-phase implementation roadmap covering foundation, execution engine, sync, and social features. |

---

## Next.js Implementation Phases

1.  **Phase 1 (Foundation):** Setup project, absolute pathing, and "Sophisticated Dark" component library.
2.  **Phase 2 (Execution):** Port Random Walk engine and implement multi-leg Bracket Orders.
3.  **Phase 3 (Sync):** Firebase integration for unified portfolios and secure server-side AI auditing.
4.  **Phase 4 (Social):** Implementation of Discipline Scores and retention-driving social leaderboards.

> [!TIP]
> By using Next.js API Routes, we ensure that the Gemini API keys used for behavioral diagnostics are never exposed to the client-side browser, significantly increasing security.
