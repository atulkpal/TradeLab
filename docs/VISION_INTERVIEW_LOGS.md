# Trade Lab — Vision & Interview Logs

This document aggregates the comprehensive product vision, strategic calibration, and user interview outcomes conducted between the Founder and the AI Product Architect. These details serve as the absolute source of truth for the upcoming product rewrite.

---

## 1. Absolute Target User Persona
- **Who they are:** Beginner to early intermediate individuals (specifically targeting the **16–18 age range**—students fresh out of high school or college, or retail newcomers).
- **Current Status:** They have acquired surface-level knowledge (exploring finance blogs, watching trading YouTube channels, reading tutorials), but have **never placed a single trade**.
- **Psychological Barrier:** They are highly anxious and finicky about taking the first step. They want to test their skills in a safe **swimming pool** before jumping into the real, deep **ocean**.
- **Parental Appeal:** The structured, educational nature of the app gains active parent endorsement, as children learn real risk allocation and financial literacy without risking actual family capital.

---

## 2. Core Strategic Pillars

### A. The "Learn-to-Earn" Virtual Capital Loop
- **Traditional Problem:** Standard paper traders give users an arbitrary $1,000,000. This gamifies their psychology, leading to reckless sizing habits that cause catastrophic losses when they transition to real money.
- **The Solution:** Users start with a highly realistic, disciplined virtual budget (e.g., ₹10,000 or ₹25,000).
- **The Incentive:** To unlock additional virtual capital, users must complete bite-sized, gamified tutorials and terminology quizzes. 
- **Effect:** Inculcates actual study habits, models realistic portfolios, and acts as a filter to reward disciplined learning with bigger virtual "paper" portfolios.

### B. The Recommended Core User Journey
1. **Explore & Curate (Watchlist & News):** Read financial terminology and review localized watchlists.
2. **Execute (Guided Trade Desk):** Enter a trade guided by active warning logs.
3. **Simulate (Real-time / 15-Min Delayed Data):** View the trade fluctuate based on actual market metrics.

### C. Localization & Currency Framework
- **Primary Market:** Indian Retail Market (denominated in **Indian Rupees - ₹**).
- **Secondary Configuration:** A localized settings toggle to switch between **USD ($)** and **INR (₹)**.
- **Future Milestone:** Automatic geolocation detection to set market feeds and base currency dynamically.

---

## 3. Product Features & User Experience Specification

### A. The Guided Trade Experience
When a user initiates their first virtual order:
- **Active Size Diagnostic:** If the user tries to allocate an unsafe portion of their portfolio (e.g., investing ₹50,000 out of a ₹100,000 wallet in a single volatile asset), the app intercepts them. It invites them to enter an interactive tutorial on **Position Sizing**.
- **Order-Type Tutorial:** Explains that market orders are not the only option. Introduces **Limit Orders** and **Good Till Triggered (GTT)** orders to encourage buying at support levels.
- **Post-Entry Performance Rating:** After placing a trade, the app rates the trade on a retail success scale (e.g., *"This trade has an 85% safety rating. If you stick to this sizing, your chances of outlasting the average retail speculator are exceptionally high"*).

### B. Pure Realism vs. Arcade Simulator
- **Default (Pure Realism):** Operates on 15-minute delayed actual market data. Promotes long-term patience, research, and authentic retail habits.
- **Optional Toggle (Fast-Forward Simulator):** A separate gamified mode allowing users to fast-forward price feeds to test speculative high-volatility trading. Comes with a prominent psychological warning: *"This mode is an arcade playground and may gamify your habits dangerously."*

---

## 4. Engineering Side-Quests

### A. Free/Affordable Indian Market APIs (Research Output)
To provide cost-effective 15-minute delayed stock price data for the Indian market (NSE/BSE):
1. **Yahoo Finance API (yfinance via RapidAPI or direct scraping):** Offers free endpoints for Indian stock tickers ending in `.NS` (NSE) or `.BO` (BSE). Extremely robust and widely supported.
2. **Alpha Vantage:** Offers a free tier with standard global market coverage including key Indian equities.
3. **Upstox Developer API / Angel One SmartAPI:** Provide free API access for historical/delayed data upon opening a developer account (requires periodic API token refreshes).
4. **NSE India Unofficial APIs:** Python/Node packages that fetch delayed market data directly from public NSE endpoints (requires managing user agents and cookies).

---

## 5. Ongoing Calibration Interview Logs
*This section logs active conversation points between the Founder and Architect to map future questions.*

- **Session 1 (Theme: Psychology & Realism):** Resolved that excessive capital gamifies the app. Integrated the multi-step "Realistic Investor Profiler" to recommend starting capital based on retail intent.
- **Session 2 (Theme: Sizing, Tutorials, & Learn-to-Earn):** Adopted Option B (Structured beginner courses) and fused it with Option A (Adaptive card alerts). Established the "Learn-to-Earn" extra virtual capital mechanic to engage younger cohorts.

---

*This document is maintained as a structural design blueprint. No changes to the codebase should be made until the subsequent interview phases are fully completed.*
