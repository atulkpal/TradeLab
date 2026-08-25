# TradeLab Release Ledger 🚀

This document serves as the authoritative history of all production and candidate releases for TradeLab. It tracks version codes, feature sets, and signing information for distribution.

---

## [2.0.0] - Academy Video Platform & Monetization Migration
**Release Date:** August 25, 2026  
**Version Code:** `10`  
**Status:** 🟡 CLOSED TESTING (beta track)

### Summary of Changes
The Academy becomes a video-first learning platform with remote content delivery (zero-release content updates), Unity LevelPlay replaces AdMob end-to-end, and the notification framework lands.

### Core Features
- **Epic 25 — Academy Experience Redesign:** full-screen LectureScreen, video-first lessons, Shorts-style fullscreen playback, course card v3 (identity gradients, icon chips, glow), full theme compliance.
- **Epic 26 — Unity LevelPlay Monetization:** AdMob extirpated (zero gms.ads); rewarded + interstitial wired; all fallback auto-grant leaks closed; graceful no-ad states. Live ads pending LevelPlay account approval.
- **Epic 27 — Remote Video Manifest (LIVE):** 92 branded lectures in production on Firebase Storage; manifest-driven delivery (24h cache, offline-surviving); dynamic per-lecture EN/हिंदी toggle; new batches ship without app releases.
- **Epic 28 groundwork — Notifications:** FCM data-payload routing (market_alerts / engagement channels), token registration with change-only Firestore writes, local market-open alert at 09:15 IST (weekend + holiday aware, self-rescheduling WorkManager).
- **Play Store listing 2.0:** ASO-optimized text live (200+ videos messaging, course catalog).

### Build Artifacts
- **Release AAB:** `release-aab-2.0.0-10.aab`

---

## [1.8.1] - Personalization & Stability Release
**Release Date:** August 4, 2026  
**Version Code:** `8`  
**Status:** 🟢 STABLE

### Summary of Changes
Introduction of the 5-mode Theme Engine and critical stabilization logic to resolve release-only crashes.

### Core Features
- **Hyper-Personalization Suite:** 5-mode Theme Engine (Serious, Vibrant, Terminal, Arcade, Light).
- **Privacy & Focus:** Stealth Mode blurring and minimalist Zen Mode.
- **Deep Stability Hardening:**
    - **Moshi Codegen Migration:** Eliminated reflection-based parsing to prevent R8/obfuscation failures.
    - **Shared Module Protection:** Explicit ProGuard rules to preserve the KMP core.
    - **In-App Crash Viewer:** Secret diagnostic tool (Long-press version on Profile) to capture phone-side logs.

### Build Artifacts
- **Debug APK:** `debug-1.8.1-8.apk`
- **Release APK:** `release-apk-1.8.1-8.apk`
- **Release AAB:** `release-aab-1.8.1-8.aab`

---

## [1.7.0] - Institutional Power Release
**Release Date:** August 4, 2026  
**Version Code:** `6`  
**Status:** 🟢 STABLE

### Summary of Changes
A major milestone formally launching professional-grade technical analysis and risk management tools.

### Core Features
- **Technical Analysis Suite:** Native candlestick rendering, indicators (MACD, Bollinger), and volume histograms.
- **Advanced Order Desk:** 5x intraday leverage, Bracket Orders, and Trailing Stop-Loss.
- **Home Dashboard Evolution:** Integrated live Equity Curve for immediate feedback on portfolio performance.

### Build Artifacts
- **Debug APK:** `debug-1.7.0-6.apk`
- **Release APK:** `release-apk-1.7.0-6.apk`
- **Release AAB:** `release-aab-1.7.0-6.aab`

---

## [1.6.0] - Varsity Academy & Multi-Format Monetization
**Release Date:** August 4, 2026  
**Version Code:** `6`  
**Status:** 🟢 STABLE

### Summary of Changes
A major release expanding the educational foundation to 68 chapters and diversifying monetization with non-intrusive App Open and Native Ad formats.

### Core Features
- **Varsity Academy:** 6-Course, 68-Chapter curriculum with multi-question knowledge checks and mission rewards.
- **Diversified Monetization:** App Open Ads on resume and Native Advanced Ads blended into Dashboard/Watchlist.
- **Institutional Polish:** Full GTT persistence, high-precision ledger redesign, and one-tap Square Off.
- **Vector Migration:** 100% vector-based iconography (no more decorative emoji).

### Build Artifacts
- **Debug APK:** `app-debug.apk`
- **Release AAB:** `app-release.aab` (v1.6.0 Build 6)

---

## [1.5.0] - Launch Pricing & Store-Listing Compliance
**Release Date:** July 31, 2026  
**Version Code:** `5`  
**Status:** 🟢 STABLE

### Summary of Changes
Ship the real Google Play subscription products with a time-boxed ₹49 Early-Bird promo and resolve Play Store listing compliance gaps.

### Core Features
- **Real Subscription Products:** `tradelab_subs` (₹49 launch) & `trade_lab_subs_99` (₹99 regular), each with a **7-day free trial**.
- **Early-Bird Promo:** ₹49/mo (50% OFF) for all subscribers through **2026-09-01 00:00 IST**; automatic flip to ₹99 after cutoff; existing ₹49 subscribers keep renewing at ₹49 via Play Billing.
- **Promo Countdown UI:** Ticking "₹49 launch offer ends in DD:HH:MM:SS" banner on Paywall + Premium Hub.
- **Privacy Hardening:** Public leaderboard IDs are SHA-256 hashes of emails (no PII exposure).
- **Account Deletion:** In-app Delete Account entry linking to the Play-compliant deletion request page.

### Build Artifacts
- **Debug APK:** `app-debug.apk`
- **Release AAB:** `app-release.aab` (signed with `app/tradelab-release.keystore`)

---

## [1.3.0] - Social Maturity & Global Arena
**Release Date:** July 25, 2026  
**Version Code:** `4`  
**Status:** 🟢 STABLE

### Summary of Changes
Introduction of the social competition layer and quantified investor maturity metrics to drive user retention and viral growth.

### Core Features
- **Investor Maturity Engine:** 0-100 Discipline Score quantifying risk management behavior.
- **Dynamic Leaderboards:** Multi-sort support (Wealth vs. Maturity) and top 100 visibility.
- **Social Badges:** Quantified milestones for Sizing, Patience, and Sector Exploration.
- **Viral Growth hooks:** "Discipline Challenge" sharing and friend invite system.

### Build Artifacts
- **Debug APK:** `app-debug.apk`
- **Release APK:** `app-release.apk`
- **App Bundle:** `app-release.aab`

---

## [1.2.0] - Pre-Launch Polish & Advanced Realism
**Release Date:** July 23, 2026  
**Status:** ⚪ INTERNAL CANDIDATE (Skipped for v1.3.0)

### Summary of Changes
Final polish before launch, focusing on social virality and professional trading desk mechanics.

### Core Features
- **Viral Sharing Hooks:** Catchy, dynamic phrases integrated into portfolio snapshots.
- **Professional Order Toggles:** CNC/MIS and NRML/MIS selectors for advanced realism.
- **T+1 Logic Refinement:** Visual clarity for unsettled shares in derivatives.

### Build Artifacts
- **Debug APK:** `app-debug.apk`
- **Release APK:** `app-release.apk`
- **App Bundle:** `app-release.aab`

---

## [1.1.0] - Foundation Modernization & Market Realism
**Release Date:** July 21, 2026  
**Version Code:** `3`  
**Status:** 🟢 STABLE

### Summary of Changes
This release stabilizes the application architecture and introduces authentic market session logic for the Indian stock market.

### Core Features
- **Hilt Architecture:** Fully dependency-injected core logic for high testability.
- **Indian Market Hours:** Enforced NSE/BSE session cutoffs and holiday support for simulations.
- **Swipe Navigation:** Fluid horizontal pager navigation between all primary app screens.
- **Ultra-Dense Watchlist:** Refactored UI for higher data density with persistent view preferences.

### Signing & Security
This version is signed with the production `tradelab-release.keystore`.
- **SHA-1 (Release):** `4E:0D:BE:63:7E:86:31:0A:35:B7:9C:D3:D0:F0:10:F6:47:DD:3A:E9`
- **SHA-256 (Release):** `BE:C3:0C:BC:B2:F7:FA:6A:4A:51:B1:B3:9C:52:20:3E:66:8E:BD:AA:AA:7D:4D:1B:19:59:94:17:53:FD:54:FC`

### Build Artifacts
- **Debug APK:** `app-debug.apk` (Hilt enabled, Sandbox mode)
- **Release APK:** `app-release.apk` (Signed, Production SDKs)
- **App Bundle:** `app-release.aab` (v1.1.0 Build 3)

---

## [1.0.0] - Stable Release Candidate 
**Release Date:** July 19, 2026  
**Version Code:** `2`  
**Status:** 🟢 STABLE / RC

### Summary of Changes
This release marks the transition from a 100% simulated sandbox to a production-ready application integrated with official Google Play and Firebase services.

### Core Integrations
- **Google Play Billing:** Integrated `billing-ktx:7.1.1`. Release builds now trigger real Google Play purchase sheets for the `tradelab_pro_monthly` subscription.
- **Firebase Auth (Real SDKs):** Implemented production login flows for Release builds:
    - **Google Sign-In:** Uses the modern `Credential Manager API` with Web Client ID.
    - **Phone Auth:** Uses real SMS OTP verification via Firebase.
- **Hybrid Sandbox Logic:** All development (Debug) builds retain the high-fidelity simulations for Auth and Billing to enable rapid UI and business logic testing.
- **Branded App Icon:** Replaced default assets with a custom-designed, premium TL logo featuring 3D effects and neon highlights.
- **Compliance Setup:** Deployed official `app-ads.txt` and `privacy.html` to [tradelab-4f858.web.app](https://tradelab-4f858.web.app).

### Signing & Security
This version is signed with the production `tradelab-release.keystore`.
- **SHA-1 (Release):** `4E:0D:BE:63:7E:86:31:0A:35:B7:9C:D3:D0:F0:10:F6:47:DD:3A:E9`
- **SHA-256 (Release):** `BE:C3:0C:BC:B2:F7:FA:6A:4A:51:B1:B3:9C:52:20:3E:66:8E:BD:AA:AA:7D:4D:1B:19:59:94:17:53:FD:54:FC`

### Build Artifacts
- **Debug APK:** `app-debug.apk` (Simulated Sandbox)
- **Release APK:** `app-release.apk` (Signed, Production SDKs)
- **App Bundle:** `app-release.aab` (Ready for Play Store Upload)

---

## [0.9.0] - Initial MVP Prototype
**Version Code:** `1`  
**Status:** ⚪ Legacy

- First functional prototype with local Room DB.
- 100% simulated billing and auth dialogs.
- Custom Canvas charting engine.
- Initial "Learn-to-Earn" quiz implementation.
