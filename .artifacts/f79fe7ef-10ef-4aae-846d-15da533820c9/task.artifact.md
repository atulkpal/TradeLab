# Task: Institutional Power Release (v1.7.0)

- [x] Update documentation and metadata
    - [x] Update `AGENTS.md` with release policy and v1.7.0 status
    - [x] Update `CHANGELOG.md` with v1.7.0 changes
    - [x] Update `RELEASES.md` with v1.7.0 ledger entry
    - [x] Update `app/build.gradle.kts` (versionName = "1.7.0")
- [x] Implement technical fixes and UI polish
    - [x] Fix Firebase initialization in `TradeLabApplication.kt`
    - [x] Promote Equity Curve in `PortfolioScreen.kt`
- [x] Execute build and archiving process
    - [x] Archive existing v1.6.0 artifacts (renaming/moving)
    - [x] Generate v1.7.0 artifacts (`:app:assembleDebug`, `:app:assembleRelease`, `:app:bundleRelease`)
- [x] Verification
    - [x] Run unit tests (`:app:testDebugUnitTest`)
    - [x] Verify build artifacts presence and naming
    - [x] Manual verification of UI changes
