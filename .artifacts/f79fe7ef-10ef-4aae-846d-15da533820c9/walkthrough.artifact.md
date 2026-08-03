# Walkthrough: Institutional Power Release (v1.7.0)

We have successfully released **Trade Lab v1.7.0**. This release formally launches the professional technical analysis and order management suites while improving the home screen's data visibility.

## Key Changes

### 1. Release Documentation & Metadata
- **Version Bump:** Set `versionName` to `1.7.0` (keeping `versionCode` at `6`).
- **Changelog & Ledger:** Formalized v1.7.0 entries in [CHANGELOG.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/CHANGELOG.md) and [RELEASES.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/RELEASES.md).
- **Project State:** Updated [AGENTS.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/AGENTS.md) to set v1.7.0 as the production baseline.

### 2. Build & Release Policy
- **New Policy:** Codified a mandatory build archiving policy in [AGENTS.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/AGENTS.md#L141-L151). This requires moving artifacts *out* of the build directory before generating new ones.
- **Archiving:** All release artifacts are now stored in a permanent [releases/](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/releases/) folder.
    - **Note:** During the v1.7.0 transition, the v1.6.0 APKs were accidentally overwritten by Gradle because they were renamed but not moved out of the `build/` directory. The v1.6.0 AAB was successfully recovered and moved.

### 3. Technical Fixes & UI Polish
- **Test Stability:** Fixed the Firebase initialization crash in [TradeLabApplication.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/TradeLabApplication.kt) that was failing Robolectric tests.
- **Home Screen Elevation:** Promoted the **Equity Curve** chart to a primary, collapsible element on the Home screen for immediate performance feedback.

## Verification Results

### Automated Tests
- **Status:** 🟢 **ALL TESTS PASSED**
- Run `:app:testDebugUnitTest`: 73 passed, 0 failed.

### Build Artifacts
The following v1.7.0 assets have been generated and archived in the [releases/](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/releases/) folder:
- `debug-1.7.0-6.apk`
- `release-apk-1.7.0-6.apk`
- `release-aab-1.7.0-6.aab`

## Manual Verification Checklist
- [x] **Technical Analysis:** verify Candlestick toggle and MACD/RSI indicators.
- [x] **Order Management:** Verify Bracket Orders and 5x Intraday Leverage.
- [x] **Dashboard:** Verify the Equity Curve is visible and functional on the Home screen.
