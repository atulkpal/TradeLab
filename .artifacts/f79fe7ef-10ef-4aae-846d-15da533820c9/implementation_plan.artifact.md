# Implementation Plan: Institutional Power Release (v1.7.0)

This plan outlines the final steps to ship **Trade Lab v1.7.0**. This release formally launches the "Institutional Power" features and establishes a new build archiving policy for all primary artifacts.

## User Review Required

> [!IMPORTANT]
> **Version Configuration:**
> - `versionName` → `1.7.0`
> - `versionCode` → `6` (No increment per instruction).

> [!IMPORTANT]
> **Build Archiving Policy:**
> Before generating new artifacts, ALL existing artifacts in the output folders must be archived.
> - **Debug APK:** `debug-<version>-<versionCode>.apk`
> - **Release APK:** `release-apk-<version>-<versionCode>.apk`
> - **Release AAB:** `release-aab-<version>-<versionCode>.aab`

## Proposed Changes

### 1. Policies & Documentation

#### [MODIFY] [AGENTS.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/AGENTS.md)
- **Add Section 9: Build & Release Policy**:
    - Policy: Before generating new build artifacts, existing files must be archived to prevent overwriting.
    - Format:
        - `debug-<version>-<versionCode>.apk`
        - `release-apk-<version>-<versionCode>.apk`
        - `release-aab-<version>-<versionCode>.aab`
    - Purpose: Maintain a local history of distributable assets.
- **Update Section 4**: Set v1.7.0 as the current production state.

#### [MODIFY] [CHANGELOG.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/CHANGELOG.md) & [RELEASES.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/RELEASES.md)
- Formalize v1.7.0 entry with Track A/B features and UI updates.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/build.gradle.kts)
- Update `versionName` to `"1.7.0"`.

---

### 2. Technical Stability & UI Polish

#### [MODIFY] [TradeLabApplication.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/TradeLabApplication.kt)
- Add null-safety/initialization checks for Firebase to fix Robolectric test crashes.

#### [MODIFY] [PortfolioScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/portfolio/PortfolioScreen.kt)
- Promote **Equity Curve** to a primary, collapsible element on the Home screen.

---

### 3. Build & Archiving (v1.7.0 Execution)

#### [ARCHIVE] Existing Artifacts
- Rename `app/build/outputs/apk/debug/app-debug.apk` → `debug-1.6.0-6.apk`.
- Rename `app/build/outputs/apk/release/app-release.apk` → `release-apk-1.6.0-6.apk`.
- Rename `app/build/outputs/bundle/release/app-release.aab` → `release-aab-1.6.0-6.aab`.

#### [BUILD] New Artifacts
- Run Gradle tasks to generate all three v1.7.0 assets:
    - `:app:assembleDebug` (Debug APK)
    - `:app:assembleRelease` (Release APK)
    - `:app:bundleRelease` (Release AAB)

## Verification Plan

### Automated Tests
- Run `:app:testDebugUnitTest`. Expected: 73 passed, 0 failed.

### Manual Verification
- **Build Output:** Confirm the presence of `app-debug.apk`, `app-release.apk`, and `app-release.aab`.
- **Archiving:** Confirm the renamed v1.6.0 files exist in their respective directories.
- **App Behavior:**
    - Verify Candlesticks and indicators on charts.
    - Verify Bracket Order entry in the order ticket.
    - Verify Equity Curve on the Portfolio Dashboard.
