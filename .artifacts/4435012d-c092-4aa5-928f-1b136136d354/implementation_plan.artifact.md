# Implementation Plan: Version 1.7.1 Maintenance Release

This plan outlines the steps to prepare and document the **v1.7.1 (Build 7)** release, which addresses critical stability issues identified after the v1.7.0 launch.

## Rationale for Version Bump

The transition from **v1.7.0 (Build 6)** to **v1.7.1 (Build 7)** is required to deliver essential hotfixes to production users.

### Key Drivers:
1.  **Critical Crash Resolution**: Fixes a high-severity `SQLiteDatabaseCorruptException` by enabling Write-Ahead Logging (WAL) and adding defensive `.catch` handlers in the `TradingRepository`.
2.  **UI Stability**: Resolves an `IllegalArgumentException` (NaN progress) occurring during asynchronous data loading in the `ProfileScreen` and `MainActivity`.
3.  **Market Readiness**: Ensures the latest stability improvements are packaged in a new build recognized by the Google Play Console (incremented `versionCode`).

---

## Proposed Changes

### [Component: Build Configuration]

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/build.gradle.kts)
- Increment `versionCode` from `6` to `7`.
- Update `versionName` from `"1.7.0"` to `"1.7.1"`.

### [Component: Documentation]

#### [MODIFY] [CHANGELOG.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/CHANGELOG.md)
- Add a new section for `[1.7.1] - Maintenance & Stability`.
- Document the Database Corruption Fix and NaN Progress Indicator Fix.

#### [MODIFY] [RELEASES.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/RELEASES.md)
- Add a new release entry for `[1.7.1]`.
- Update the status of v1.7.0 to clearly indicate v1.7.1 is the latest stable.

---

## Verification Plan

### Automated Tests
- Run `:app:testDebugUnitTest` to ensure no regressions in business logic.
- Run `:app:assembleRelease` to verify the build configuration and signing.

### Manual Verification
- Verify that the version name shown in the app footer (Profile Screen) correctly displays `v1.7.1 (7)`.
- Perform a smoke test on the Database initialization to ensure WAL is active and stable.
- Verify progress indicators load correctly without crashing on a fresh install.
