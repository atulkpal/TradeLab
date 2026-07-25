# Implementation Plan: Trade Lab Next.js Web App

This plan outlines the architectural and engineering roadmap to transition Trade Lab from a static sandbox to a professional-grade, responsive **Next.js** web application with full feature parity with the Android app.

## User Review Required

> [!IMPORTANT]
> - **Technology Choice:** Next.js (App Router), TypeScript, Tailwind CSS, and Zustand for state management.
> - **Data Strategy:** Initially, we will use `localStorage` (via Zustand persistence) to maintain the "Local-First" philosophy. Later, we will integrate Firebase Firestore for cross-platform synchronization as per the KMP roadmap.
> - **Visual Identity:** The "Sophisticated Dark" theme will be strictly maintained using Tailwind CSS variables and custom components.

## Proposed Architecture

### 1. Technology Stack
- **Framework:** [Next.js 14+](https://nextjs.org/) (App Router) for SEO, performance, and API routes.
- **Language:** TypeScript for type-safe trading logic.
- **Styling:** Tailwind CSS + Framer Motion (for premium UI animations).
- **State Management:** [Zustand](https://github.com/pmndrs/zustand) (Simpler and lighter than Redux, perfect for our reactive state).
- **Charts:** [Lightweight Charts](https://www.tradingview.com/lightweight-charts/) (Institutional grade) or [Recharts](https://recharts.org/).
- **Backend/Auth:** Firebase (Auth, Firestore, Cloud Functions).

### 2. Feature Roadmap

#### Phase 1: Core Foundation & UI (Production Setup)
- [ ] Initialize Next.js project with TypeScript and Tailwind.
- [ ] Implement the **Unified Layout Shell**:
    - Sticky Header (Currency toggle, Market Status).
    - Responsive Sidebar/Rail (Portfolio, Watchlist, Academy, AI Hub).
- [ ] Migrate the "Sophisticated Dark" theme system from `styles.css` to Tailwind config.

#### Phase 2: Reactive Trading Engine (Logic Port)
- [ ] Port the mathematical Random Walk engine from `script.js` to a custom React Hook or Zustand Store.
- [ ] Implement the **Order Execution Engine** in TypeScript:
    - Support for Market, Limit, Stop-Loss, and GTT.
    - Commission & Tax calculation logic (matching Android's `TradingRepository`).
- [ ] Integrate **Lightweight Charts** for professional Candlestick/Line rendering.

#### Phase 3: Learn-to-Earn & AI Integration
- [ ] **Academy Hub:** Port the interactive quiz engine with Framer Motion animations for reward celebrations.
- [ ] **AI Strategy Hub:** Implement secure server-side API routes to query Gemini for behavioral diagnostics (preventing API key exposure).

#### Phase 4: Cross-Platform Synchronization
- [ ] Integrate **Firebase Auth** (Google/Email).
- [ ] Implement a **Sync Engine**:
    - When a user logs in, pull their portfolio from Firestore and merge with local state.
    - Push local trades to Firestore for backup/sync.

### 3. Proposed Directory Structure

```
/src
  /app                # Next.js App Router (Pages & API Routes)
  /components
    /common           # Buttons, Inputs, Modals (Brand Neutral)
    /dashboard        # Main navigation shell
    /charts           # High-performance chart wrappers
    /trading          # Order tickets, Watchlist rows
    /academy          # Quiz components
  /store              # Zustand stores (useTradingStore, useAuthStore)
  /hooks              # useMarketSimulation, usePriceSteering
  /lib                # Utility functions, Currency formatters, Math helpers
  /services           # Firebase, Gemini API clients
```

## Proposed Documentation Updates

### [MODIFY] [architecture.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/docs/architecture.md)
- Update Step 4 (Web Presentation) to explicitly specify Next.js and the React-based component architecture.
- Detail the state synchronization mechanism between the KMP core (future) and the Next.js frontend.

### [MODIFY] [WEB_PLATFORM_DOCUMENTATION.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/docs/WEB_PLATFORM_DOCUMENTATION.md)
- Add a new section **"6. Detailed Next.js Implementation Roadmap"** summarizing the phases above.

## Verification Plan

### Manual Verification
- Ensure the web app is "Mobile-First" and feels like a native app on iOS/Android browsers.
- Verify trade execution logic matches the Android app's math exactly (to the second decimal).
- Test Firestore sync between a web browser and a simulated environment.
