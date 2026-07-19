# Change Log
All notable changes to the TradeLab project will be documented in this file. This project adheres to a spec-first iteration cycle.

---

## [1.3.0] - 2026-07-18

### Added
- **Production-Ready Google Mobile Ads (AdMob) Integration**:
  - Added actual `com.google.android.gms:play-services-ads` SDK dependency via Version Catalog (`gradle/libs.versions.toml`) and referenced it in the App module's `build.gradle.kts`.
  - Configured `app/src/main/AndroidManifest.xml` with Google's official, standard test Application ID `ca-app-pub-3940256099942544~3347511713`.
  - Initialized the Mobile Ads SDK asynchronously on app startup inside the `MainActivity.onCreate` life-cycle hook.
  - Implemented the unified `loadAndShowRewardedAd` orchestrator within `MainActivity.kt` targeting the official rewarded video test unit ID `ca-app-pub-3940256099942544/5224354917`.
  - Refactored both user-initiated ad portals (Brokerage Shield and Ashwath Rewards Station) to fetch and render live AdMob streams, complete with a network loading overlay and a self-healing offline fallback player.
  - **Double Capital Reward on Quiz Completion**: Refactored the Learn-to-Earn quiz dialog completion screen to present a high-contrast "Double Reward" ad-watching CTA. When clicked, it loads and plays Google AdMob rewarded videos or plays simulated fallback streams, granting twice the virtual capital reward upon success.

---

## [1.2.0] - 2026-07-18

### Added
- **Rewarded State Architecture & Core Credit Wallet (Epic 8.1/8.2/8.3)**:
  - Integrated `brokerageCredits`, `indicatorsUnlockedUntil`, and `aiAuditCredits` user attributes into the Room database `UserProfile` entity.
  - Set default user starting capital to ₹10,000 (INR) or $120.00 (USD) based on target audience profile sizing disciplines.
  - Developed the **Brokerage Shield** execution logic inside `TradingRepository` which enforces a 0.05% brokerage fee on trades unless waived by consuming 20 brokerage credits.
  - Created corresponding `TradingViewModel` state flows and async handlers for simulated commercial watch triggers: `earnBrokerageCredits`, `earnEmergencyCash`, `earnAiAuditCredit`, and `unlockPremiumIndicators`.
  - Built the interactive **Ashwath Rewards Station** card inside the Profile tab allowing users to stream 3-second sponsor messages to claim emergency capital, recharge shields, unlock premium charts, and earn AI advisor credits.
  - Embedded a dynamic green/red **Brokerage Shield Status Widget** in the primary Portfolio screen displaying real-time protection metrics and ad-watching triggers.
  - Implemented technical indicator lockouts inside the Buy/Sell chart view, gating the SMA (Simple Moving Average) overlay behind the active sponsor unlock timer.

---

## [1.1.0] - 2026-07-18

### Added
- **Firebase Auth Gating & Auth Screen (`/app/src/main/java/com/ashwathai/tradelab/ui/AuthScreen.kt`)**:
  - Implemented multi-channel authentication flows supporting **Email & Password**, **Google Sign-In**, and **Phone (OTP)**.
  - Developed a seamless **Local Sandbox Simulation Dialog** that intercepts missing credentials gracefully and lets users test all premium features offline instantly.
  - Linked auth state checking to prevent access to core portfolio features for unregistered guests (unless specifically bypassing).
- **Comprehensive Guides & Documentation**:
  - Created `/docs/FIREBASE_SETUP.md` with step-by-step instructions for Firebase registration, Google console Web Client ID generation, and SHA-1 setup.
  - Created `/docs/MONETIZATION_STRATEGY.md` mapping out target audience demographics (16–35 years old), ad placement logic, and the "Brokerage Shield" credit mechanism.

### Changed
- Marked **Sprint 7.2 (Registration Gate & Login Simulation)** as completed in `/docs/epics_and_sprints.md`.
- Updated `/docs/epics_and_sprints.md` with **Epic 8: On-Demand Ads & Gamified Monetization** to track monetization milestones.

---

## [1.0.0] - MVP Release - 2026-07-15

### Added
- **Local Room Database Schema (`/app/src/main/java/com/ashwathai/tradelab/data`)**:
  - Established SQLite entities for `UserProfile`, `Holdings`, `Transactions`, `Watchlist`, and `StockPrices`.
  - Configured automatic seed values for Indian equity markets (TCS, RELIANCE, INFOSYS, HDFCBANK).
- **Native Live Charts Engine (`/app/src/main/java/com/ashwathai/tradelab/MainActivity.kt`)**:
  - Built a fluid, high-contrast custom drawing canvas rendering live price tick fluctuations as continuous line charts.
- **Unidirectional State Flow Engine (`/app/src/main/java/com/ashwathai/tradelab/ui/TradingViewModel.kt`)**:
  - Centralized portfolio balance evaluation, asset weight calculations, and transactional validations.
- **Position Sizing Profiler**:
  - Designed a 60-second questionnaire advising young/novice traders on appropriate realistic starter capitals (e.g., ₹10,000) to build sustainable real-world habits.
