# Trade Lab — Web Platform & Sandbox Documentation

This document outlines the architecture, file structure, and implementation details of the **Trade Lab Web Sandbox** (located in the `/website` folder). It also provides a comprehensive strategy for transitioning this responsive mockup into a production-ready **Next.js** application.

---

## 1. Overview & Core Philosophy
The **Trade Lab Web Platform** is designed to replicate the primary features of our offline-first Android application in a high-fidelity web sandbox. 

By maintaining a **zero-overhead, static-first architecture**, the web sandbox can be loaded on any browser instantly with zero build setup, while perfectly modeling the core application loops:
*   **Realistic Micro-Budgets:** Starts with ₹10,000 (INR) or $180 (USD) instead of arbitrary, massive fantasy cash.
*   **Simulated Price Fluctuation Walk:** A real-time market engine updating asset prices every 2.5 seconds.
*   **Live Vector Charts:** Rendered dynamically using inline SVGs, featuring on-demand technical indicators (**SMA-5**, **EMA-5**, **RSI-14**).
*   **Local Persistence:** Uses browser `localStorage` to emulate the Android app's local **Room SQLite** database, keeping user portfolios fully private and secure.
*   **Learn-to-Earn Academy:** Interactive financial literacy quizzes that award virtual trading capital.
*   **Behavioral Risk Auditor:** Real-time analysis rating trade sizes against the overall portfolio net worth to flag over-concentration or high-risk execution.

---

## 2. Directory & Tech Stack Breakdown

The web sandbox is housed inside the `/website` folder:

```
├── website/
│   ├── index.html        # High-contrast "Sophisticated Dark" dashboard, layout panels, and dialog modals.
│   ├── styles.css        # Glassmorphism utilities, neon glow classes, and simulated price tick flashing keyframes.
│   └── script.js         # Centralized reactive state machine, math engines, charts engine, and simulated AdMob flows.
```

### Technology Stack Used
1.  **HTML5 (Semantic Layout):** Organized into responsive flex-row and absolute overlays to support mobile, tablet, and wide desktop views seamlessly.
2.  **Tailwind CSS (Styling):** Pulled via CDN for zero-configuration startup. Employs the premium **Sophisticated Dark** aesthetic:
    *   Primary Canvas: Deep Space Blue (`#090D16` / `#0D1527`)
    *   Accents: Neon Cyan (`#06b6d4`), Emerald (`#10b881`), Rose (`#f43f5e`), and Indigo (`#6366f1`)
3.  **Vanilla ES6 JavaScript (Logic):** No external NPM package dependencies. Implements unified state models, mathematical vector projections, and browser-native event registries.

---

## 3. Structural State Architecture (`script.js`)

The web platform's business logic is built around a centralized **reactive state model** that closely matches the Android `TradingViewModel`:

```javascript
let state = {
    profile: {
        cash: 10000.0,
        startingCash: 10000.0,
        riskPreference: "Moderate",
        currency: "INR",
        brokerageCredits: 300,
        completedLevels: [],          // Earned quiz rewards
        isCalibrated: false,
        userName: "Retail Pioneer",
        userEmail: "atulkpal@gmail.com",
        investorPersonality: "Disciplined Compounder"
    },
    holdings: [],                     // Active stocks: { symbol, shares, averagePrice }
    transactions: [],                 // Ledger history: { symbol, type, shares, price, timestamp }
    pendingOrders: [],                // Limit/Stop Triggers: { symbol, orderType, shares, triggerPrice }
    stockPrices: [...],               // Active stock tickers & pricing queues
    selectedSymbol: "AAPL",
    selectedOrderType: "Market",
    activeTab: "portfolio",
    showSMA: false,
    showEMA: false,
    showRSI: false
};
```

### Key Subsystems:
*   **Simulated Market Ticker:** Updates prices using a random-walk mathematical process bounded within realistic intraday fluctuations ($\pm 1.2\%$).
*   **Chart Vector Generator:** Maps coordinate grids into custom `<polyline>` elements, drawing fluid technical trends directly inside the native SVG frame.
*   **Trigger Engine:** Compares simulated price changes against `pendingOrders` to execute **Limit**, **Stop-Loss**, and **GTT (Good-Till-Triggered)** parameters automatically.

---

## 4. How to Run the Web Sandbox

### Option A: Direct Execution (Zero Setup)
Simply open the `/website/index.html` file directly in any modern browser (Chrome, Safari, Firefox, Edge). Double-clicking the file is sufficient!

### Option B: Local Web Server (For Hot Reloads)
If you are using a code editor like VSCode:
1.  Install the **Live Server** extension.
2.  Right-click `index.html` and select **Open with Live Server**.
3.  Alternatively, run this simple command in your terminal inside the project directory:
    ```bash
    npx serve website
    ```
4.  Open the returned port in your browser (usually `http://localhost:3000` or `http://localhost:5000`).

---

## 5. Next.js Scaling & Migration Strategy

Since you plan to build your product using **Next.js**, you can easily migrate this high-fidelity sandbox into a production-ready React web app. Use the following structured roadmap:

### Step 1: Initialize the Next.js Workspace
Create a fresh TypeScript/Tailwind Next.js app using the App Router:
```bash
npx create-next-app@latest trade-lab-web --typescript --tailwind --app
```

### Step 2: Componentize the Architecture
Break down our monolithic `/website/index.html` file into modular React components under `/components`:

1.  **`components/Navbar.tsx`**: Header bar containing the currency toggle, market status indicator, and Brokerage Shield credits.
2.  **`components/Sidebar.tsx`**: Left-side navigation drawer handling tab states.
3.  **`components/PortfolioDesk.tsx`**: Renders portfolio net worth cards, active holdings table, and historic ledger.
4.  **`components/TradingDesk.tsx`**: Combines the Stock Selection watchlist list, interactive Chart view, and Order Ticket.
5.  **`components/AcademyHub.tsx`**: Renders the "Learn-to-Earn" lecture lists and loads the modal quiz view.
6.  **`components/InvestorProfiler.tsx`**: Replaces the static form with a multi-step slide-deck component.

### Step 3: Centralize State Management (React Context or Zustand)
Rather than writing to `localStorage` directly inside files, encapsulate the state inside a unified state hook (e.g., **Zustand** or **React Context**).

*Example Zustand store mapping our state logic (`store/useTradingStore.ts`):*
```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface TradingState {
  cash: number;
  holdings: Array<{ symbol: string; shares: number; averagePrice: number }>;
  transactions: Array<any>;
  buyStock: (symbol: string, shares: number, price: number) => void;
  // ... other actions matching script.js logic
}

export const useTradingStore = create<TradingState>()(
  persist(
    (set) => ({
      cash: 10000.0,
      holdings: [],
      transactions: [],
      buyStock: (symbol, shares, price) => set((state) => {
         // Implement unified trade execution matching script.js
      }),
    }),
    { name: 'trade-lab-storage' } // Automatically syncs with localStorage!
  )
);
```

### Step 4: Upgrade the Dynamic Chart Subsystem
While SVGs are lightweight and great for prototypes, you can easily swap the vector chart engine for professional chart packages during production scale:
*   **Recharts:** For highly readable, stylized, smooth Material 3 line charts.
*   **Lightweight Charts (by TradingView):** If you want to offer professional candle/line charts with live overlay tools for power users.

### Step 5: Secure Your API Keys
In Next.js, never expose your private API keys in client-side code. Instead, route your Gemini AI requests through a secure **Next.js API Route**:

*File: `app/api/ai-audit/route.ts`*
```typescript
import { GoogleGenAI } from '@google/genai';
import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  const { transactions, holdings } = await request.json();
  
  // Read key safely from server environment variables (.env.local)
  const apiKey = process.env.GEMINI_API_KEY;
  const ai = new GoogleGenAI({ apiKey });

  try {
    const response = await ai.models.generateContent({
      model: 'gemini-1.5-flash',
      contents: `Audit these trades for behavioral biases: ${JSON.stringify(transactions)}`,
    });
    return NextResponse.json({ audit: response.text });
  } catch (error) {
    return NextResponse.json({ error: 'Failed to generate diagnostic' }, { status: 500 });
  }
}
```
In your frontend components, simply query `/api/ai-audit` to execute the behavior analysis securely!
