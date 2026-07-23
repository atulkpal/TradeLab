# TradeLab Release Ledger 🚀

This document serves as the authoritative history of all production and candidate releases for TradeLab. It tracks version codes, feature sets, and signing information for distribution.

---

## [1.2.0] - Pre-Launch Polish & Advanced Realism
**Release Date:** July 23, 2026  
**Version Code:** `4`  
**Status:** 🟡 RELEASE CANDIDATE

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
