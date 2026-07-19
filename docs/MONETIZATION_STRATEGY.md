# TradeLab — On-Demand Ads & Gamified Monetization Strategy
**Target Audience:** 16 to 35 Years Old (Gen Z & Millennials)  
**Core Philosophy:** 100% Non-intrusive, On-demand, User-initiated Value Exchange ("Watch-to-Earn")

---

## 1. Demographic & Psychology Survey (16–35 Years Old)

The target audience spans from high school/college students (16–22) to young working professionals (23–35). Understanding their psychology is critical for successful monetization:

*   **Ad Tolerance:** Highly sensitive to intrusive ads (pop-ups, banners, auto-play interstitials). They will instantly uninstall an app that forces them to wait or blocks their core screen.
*   **Rewarded-Ad Preference:** Culturally accustomed to "Rewarded Ads" from mobile gaming and premium-freemium utilities. They appreciate clear, fair exchanges of their time (30 seconds) for tangible in-app utility.
*   **Gamified Mindset:** They view virtual capital, technical indicators, and analytics as "power-ups." Monetization that feels like unlocking a game feature is treated with curiosity, not frustration.

---

## 2. On-Demand Monetization Models: The "Value Exchange"

To make users actively choose to watch ads, we attach ad consumption to high-utility trading features and a strict, realistic virtual capital economy. Below are the key mechanics:

### A. The "Brokerage Shield" (Reconciling Realism & Gamification)
*   **The Dilemma:** If brokerage credits are a separate fictional currency that doesn't affect capital, we lose the crucial educational lesson that fees eat into real-world profits. If ads run *during* a trade, the 30-second delay creates critical execution slippage.
*   **The Solution (The "Shield" Mechanic):** We run a hybrid, highly realistic wallet:
    *   Every trade has a real-world simulated brokerage fee (e.g., 0.05% of transaction size, say ₹50).
    *   **With Brokerage Credits:** If the user has Brokerage Credits in their wallet, they are spent to **Shield** the trade. The ₹50 fee is 100% waived/absorbed, keeping their virtual capital fully intact.
    *   **Without Brokerage Credits:** If the wallet is empty, the ₹50 fee is **directly deducted** from their virtual cash.
*   **Rewarded Ad Action:** Pre-watch 30s ads at their convenience (e.g., in the "Power-Up Vault") to earn **100 Brokerage Credits** per ad.
*   **Educational Impact:** Teaches users that brokerages are expensive cost drags, while giving them a direct gamified way to "protect" their portfolio returns by choice.

### B. The Realist Budget & "Learn-to-Earn" Academy Fusion
*   **The Dilemma:** Beginners often trade with huge fantasy balances (e.g., ₹10 Lakhs) and learn reckless habits.
*   **The Solution (Small-Budget Friction):**
    *   Users start with a highly realistic, disciplined capital of exactly **₹10,000**.
    *   They cannot easily place large, speculative trades, forcing them to learn position sizing and risk management.
    *   **To Earn More Capital (The Academy Integration):** Users must complete bitesize lessons in the **Academy screen**. Passing a 3-question financial quiz grants them a premium **₹2,000 Smart Capital Grant**.
    *   **The Monetization Boosters:**
        *   **Emergency Capital Injection (Rewarded Ad):** If a user goes bankrupt or depletes their capital, they can watch an on-demand ad to claim an immediate, small **₹500 emergency top-up**. It is small enough to discourage reckless play (they'd have to watch 10 ads to make ₹5,000), keeping the incentive focused on actual learning.
        *   **Quiz Cooldown Bypass (Rewarded Ad):** Academy quizzes have a daily cooldown (e.g., 4 hours). To bypass the cooldown and take another quiz immediately to earn capital, they can watch a 30s ad.
        *   **Double Rewards (Rewarded Ad):** Watch an ad at the end of a completed quiz to double their quiz grant from ₹2,000 to ₹4,000.

### C. Technical Indicator Passes (Advanced Analytics)
*   **The Scenario:** Beginners want Simple Moving Averages (SMA), while advanced traders (22-35) want Exponential Moving Averages (EMA) and Relative Strength Index (RSI).
*   **On-Demand Ad Action:** Advanced technical overlays are labeled "Premium Indicators." A user can watch an ad to "Unlock RSI and EMA charts for 12 hours."
*   **User Sentiment:** *"I only need technical charts when I am actively analyzing. I’ll gladly watch an ad to turn on these high-end tools for my trading session."*

### D. Gemini AI Audit Credits (The Intelligent Coach)
*   **The Scenario:** Users want a personalized, AI-powered audit of their trading behavior (detecting biases like Revenge Trading or Sunk-Cost Fallacy) using Gemini.
*   **On-Demand Ad Action:** An AI audit consumes 1 "Diagnostic Credit." Users can watch 2 short video ads to earn 1 free Diagnostic Credit, or upgrade to Premium.
*   **User Sentiment:** *"AI reports are highly valuable. Watching an ad to get personalized financial feedback is a massive win-win."*

### E. The Academy "Cheat Sheet" or "Quiz Retake"
*   **The Scenario:** A user fails a financial literacy quiz in the Academy screen and is locked out of retaking it for 4 hours, or they want the answer explanations instantly.
*   **On-Demand Ad Action:** Tapping "Unlock Quiz Retake" or "Show Smart Hints" triggers a rewarded ad.
*   **User Sentiment:** *"I want to pass this quiz to earn more starting capital. This ad saves me a 4-hour wait."*

---

## 3. Placement & UI Design (Where and How)

Ads must be completely invisible until requested. Here is exactly where the triggers should sit, categorized by their corresponding screen:

### 1. Order Ticket Sheet (Portfolio/Watchlist Screen)
*   **Placement:** Placed directly inside the Buy/Sell transaction card, right below the estimated charges block.
*   **Visual Affordance:** A neon-accented, low-profile indicator displaying current credits:  
    `🎫 Brokerage Wallet: 340 Credits (This trade: -20 credits). [Refill Credits ↗]`
*   **Interaction:** If they have credits, simulated brokerage fees are instantly waived. If they run out, it gently alerts them: `⚠️ Out of Brokerage Credits. Standard simulated brokerage of 0.05% applies. [Earn Credits ⚡]`
*   **Interactive Tag:** `brokerage_credit_status_badge`

### 2. Dedicated "Power-Up Vault" (Pre-Watching Section)
*   **Placement:** Accessible via a prominent but non-intrusive action button on the **Profile Screen** or a shortcut on the **Portfolio Screen**.
*   **Visual Affordance:** A gaming-inspired dashboard with visual power-ups:
    *   `📺 Watch Ad: Earn +100 Brokerage Credits (To shield trades)`
    *   `📺 Watch Ad: Emergency Capital Injector (+₹500 virtual cash)`
    *   `📺 Watch Ad: Get 1 Gemini AI Strategy Diagnostic Credit`
*   **Interactive Tag:** `power_up_vault_button`

### 3. Live Chart Controls (Detailed View Screen)
*   **Placement:** At the indicator toggle bar (SMA, EMA, RSI).
*   **Visual Affordance:** Tapping locked indicators (EMA/RSI) opens a subtle bottom sheet:  
    `📊 EMA and RSI are Premium Features. Watch an ad to unlock them for 12 Hours or upgrade to TradeLab Pro.`
*   **Interactive Tag:** `unlock_indicators_ad_trigger`

### 4. Profile & Portfolio Settings Screen
*   **Placement:** Next to the starting capital balance and Portfolio Reset buttons.
*   **Visual Affordance:** A distinct "Power-Up Hub" section:
    *   `💰 Watch Ad: Emergency Wallet Recharge (+₹500)`
    *   `🔄 Watch Ad: Reset Portfolio History to Day 1`
*   **Interactive Tag:** `recharge_capital_ad_trigger`

### 5. AI Strategy Hub (Diagnostics Screen)
*   **Placement:** Directly in the AI audit request block.
*   **Visual Affordance:** A card showing credit counts:  
    `🧠 0 AI Audit Credits remaining. [Watch Ad for +1 Credit] or [Get Unlimited AI with Pro]`.
*   **Interactive Tag:** `earn_ai_credit_ad_trigger`

---

## 4. Launch Timeline to Beta (Next Saturday Target)

To align with our target **Beta Launch by Saturday next week**, we will structure this into three parallel development sprints:

| Phase | Core Objective | Deliverables |
|---|---|---|
| **Step 1 (Early Week)** | **UI Triggers & State** | Design the high-contrast on-demand ad buttons and state managers in `TradingViewModel` to track ad-unlocked permissions, brokerage credits, and starting capital recharges. |
| **Step 2 (Mid-Week)** | **Simulated Ad Playback SDK** | Build a beautiful, interactive overlay in `AuthScreen.kt` or `MainActivity.kt` showing a simulated ad timer (e.g., 5-second count-down video player simulation) to guarantee the UX is robust before loading live SDKs. |
| **Step 3 (End-Week)** | **AdMob / AppLovin SDK Bindings** | Place final production dependency hooks for AdMob Rewarded Video Ads, loading actual ads when on production builds. |

---

## 5. Google AdMob Production Integration & Tracking Setup

When you create your application in the **Google AdMob Console**, you should set up dedicated **Rewarded Ad Units** for each of the on-demand reward mechanics. This provides clean tracking analytics to observe which feature is driving the most engagement and monetization, while allowing you to adjust individual reward values directly inside the AdMob console or our app configuration.

### A. Recommended List of Ad Units to Create

For maximum tracking granularity and revenue optimization, **do not reuse a single ad unit ID**. Instead, create three (3) distinct **Rewarded Ad Units**:

1.  **Quiz Capital Doubler Ad Unit**
    *   **Ad Format:** Rewarded Ad (`RewardedAd`)
    *   **Purpose:** Triggers on the Learn-to-Earn Quiz Success dialog when a student completes a quiz and chooses the "Double Reward" option.
    *   **AdUnit Name in Console:** `Quiz_Capital_Doubler`
    *   **Implementation ID Location:** `MainActivity.kt` inside the Quiz dialog `Double Reward` button trigger.
    *   **Value Exchange:** Grants $2.0 \times$ standard tutorial capital reward (e.g., doubles ₹2,000 to ₹4,000, or $30 to $60 depending on starting currency).

2.  **Brokerage Shield Refill Ad Unit**
    *   **Ad Format:** Rewarded Ad (`RewardedAd`)
    *   **Purpose:** Triggers on the Order/Buy/Sell Ticket and inside the dedicated "Power-Up Vault" on the Profile Screen.
    *   **AdUnit Name in Console:** `Brokerage_Shield_Refill`
    *   **Implementation ID Location:** `MainActivity.kt` under the Order Ticket and the profile vault's Brokerage Credits button.
    *   **Value Exchange:** Grants **50 to 100 Brokerage Credits** used to absorb simulated transaction commissions.

3.  **Emergency Capital & Power-Up Station Ad Unit**
    *   **Ad Format:** Rewarded Ad (`RewardedAd`)
    *   **Purpose:** Triggers inside the Power-Up Station on the Profile Screen for on-demand capital injections, AI diagnostics audits, and indicator unlocking.
    *   **AdUnit Name in Console:** `Power_Up_Station`
    *   **Implementation ID Location:** `MainActivity.kt` under the respective "Power-Up Vault" item list buttons.
    *   **Value Exchange:** Refills emergency portfolio cash (₹500 / $6.0), unlocks advanced Moving Averages (EMA) / Relative Strength Index (RSI) chart overlays for 12 hours, or adds Gemini AI behavioral audit credits.

### B. Tracking and Optimization Best Practices

*   **AdMob Reporting Dashboard:** In your AdMob Console, filter reports by **Ad Unit** to track eCPM (Effective Cost Per Mille), CTR (Click-Through Rate), and Reward Completion rates for each of the three streams. This reveals whether users are more incentivized by direct virtual cash (doubling quiz rewards) or by advanced utilities (AI auditing / brokerage shield).
*   **AdMob Reward Settings:** Under the settings for each Rewarded Ad Unit, you can toggle **"Override reward settings"**. If checked, the SDK will automatically deliver reward amounts via the metadata, though our frontend is also configured with local self-healing fallbacks if networks are slow.
*   **Production Handover Plan:** Once you generate these production IDs from AdMob, simply provide them as follows:
    *   **AdMob Application ID:** In `app/src/main/AndroidManifest.xml` under `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="YOUR_PRODUCTION_APP_ID"/>`
    *   **Rewarded Ad Unit IDs:** In `MainActivity.kt` replace the Google test ID (`ca-app-pub-3940256099942544/5224354917`) with your custom live ad unit IDs.

---

## 6. Advanced Monetization: F&O Paywall & Ad-Supported "Taste Tests"

To drive maximum revenue from non-paying users while maintaining a high-converting Premium subscription funnel, we implement a **Freemium Hybrid Model**. This ensures every advanced feature has a dual path: standard direct purchase or ad-supported micro-consumption ("Taste-Testing").

### A. The Premium Paywall Features
We lock high-value, high-maintenance features behind the **"Trade Lab Pro"** premium tier:
1.  **F&O & Commodity Desk (The Premium Trading Core):** Trading complex financial derivatives (Call/Put Options, Futures, Commodities) requires an active subscription.
2.  **Unlimited AI Behavior Diagnostics:** Complete access to the server-side Gemini auditor that evaluates portfolio biases, risk exposures, and emotional trading loops without credit caps.
3.  **Pro Watchlist Suites:** Unlocking more than two custom, persistent watchlists (up to 5 lists, each supportable with sorting, tagging, and unique asset groupings).
4.  **Advanced Overlay Configurations:** Simultaneously overlaying multiple Moving Average lines (SMA & EMA) along with the RSI indicator sub-pane.

### B. Ad-Supported "Taste-Testing" (Trial Token Economy)
For users who do not purchase the direct cash subscription immediately, we offer short-term utility in exchange for ad-watching. This gives them a taste of the premium experience and generates high eCPM ad revenues:
*   **F&O Option Trade Pass:** Pre-watching a 30s ad grants the user **3 Option Trade Tokens**. Each token allows them to open one Option contract or enter a Futures trade. Once depleted, the derivative desk locks again.
*   **Single AI Diagnostic Pass:** Pre-watching a 30s ad grants **1 AI Audit Token** to trigger a personalized portfolio behavior critique from Gemini.
*   **Temporary Indicator Key:** Pre-watching a 30s ad unlocks the advanced EMA/RSI indicator widgets for **12 continuous hours**.
*   **Conversion Psychology:** Tapping on a locked premium asset immediately brings up the Paywall dialog. Instead of a dead-end "Subscribe Now" screen, it offers a secondary low-friction button: *"Try 3 trades for free (Watch Ad)"*. This drives continuous "taste-testing," proving the value of the feature and increasing subscription intent.

---

## 7. Educational Compliance, JIT Disclaimer Flows & Offline Resiliency

Given the complex nature of financial assets and derivatives, we must protect our users, maintain complete transparency, and establish robust shields against any liability when going to market.

### A. Progressive, Just-In-Time (JIT) Non-Blocking Experience
Disclaimers should never act as a tedious upfront blocker when first launching the app. Instead, they are loaded **dynamically and contextualized** exactly when a user attempts to interact with that specific asset class:
1.  **Capital Markets (NSE/BSE Equity):** Triggers only when the user opens the stock search, custom watchlist, or attempts their first stock purchase.
2.  **F&O & Commodities:** Triggers only when the user completes the prerequisite academic gate and taps on options, futures, or commodity tickers.
3.  **State Persistence:** Once accepted, the flag is saved locally (`equity_disclaimer_accepted = true` or `fno_disclaimer_accepted = true`). Future loads bypass the bottom sheets entirely to preserve high user velocity.

### B. The Mandatory Capital Markets Disclaimer
Upon attempting to search or trade an Indian security for the first time, a clean, high-contrast bottom sheet displays:
*   **Explicit Text Copy:**
    > *"Educational Sandbox Disclaimer: Trade Lab utilizes delayed market feed quotes for Indian Equity markets (NSE/BSE). This software is strictly a simulated educational sandbox designed to build healthy risk management, position sizing, and portfolio habits. Prices, tickers, and order executions are delayed, simulated, and do not represent active real-time trading, live brokerage routing, or guaranteed market liquidity. Trade Lab is not a registered financial advisor or broker. All investments carry market risk."*

### C. The Mandatory F&O Simulation Disclaimer
Before executing their first simulated Option or Future trade (whether subscribed or using a trial token), the user is presented with a scrollable, high-contrast **F&O Compliance Bottom Sheet**. They must check an acknowledgment box and tap "I Understand & Accept" to proceed.
*   **Explicit Text Copy:**
    > *"Simulated Derivatives Compliance Notice: Trade Lab options and futures chains are mathematically synthesized internally based on historical underlying asset trends and localized pricing algorithms. These chains are designed purely for educational walkthroughs; they do NOT pull live contract options data from the National Stock Exchange (NSE) or Bombay Stock Exchange (BSE), nor do they reflect actual market open interest or clearinghouse operations. Derivative trading is highly speculative and carries extreme risk of capital loss."*

### D. Strict Educational Gate (The Progression Curve)
Users cannot bypass learning to jump straight into risky options trading. We enforce an educational unlock:
*   **The Lock:** F&O and Commodity screens are completely locked from inception.
*   **The Key:** The user must read and pass the **"Equity Basics"** course curriculum and clear all fundamental quizzes in the Academy.
*   **The Narrative:** This progression prevents high-risk speculation until basic market mechanisms (long positions, stop-loss orders, capital sizing) are demonstrated.

### E. Offline Connectivity Sensing ("Network not available.")
While Trade Lab operates on an offline-first architecture, specific actions (such as fetching live ticker price updates, loading the Google Rewarded Ad SDK, or compiling Gemini AI audits) require internet access.
*   **Graceful Degradation:** When internet access is lost, the app must not freeze or crash.
*   **The Banner UI:** The app will detect lack of connectivity and display a highly polished inline error banner or a standard Toast: **"Network not available."**
*   **The Simulated Fallback:** When offline, any action requiring AdMob or external APIs will trigger a seamless, offline mock playback/callback wrapper, ensuring the trading sandbox continues running smoothly on simulated historical cycles.


