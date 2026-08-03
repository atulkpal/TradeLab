# Task List - Hyper-Personalization & Focus Suite

## Phase 1: Privacy & Focus (Functional)
- `[x]` Add `isStealthMode` and `isZenMode` to `UserProfile` entity and DAOs
- `[x]` Update `TradingRepository` and `TradingViewModel` for state management & persistence
- `[x]` Create `CompositionLocals.kt` for theme-wide mode access
- `[x]` Implement `Modifier.stealthBlur` in `CommonUi.kt`
- `[x]` Apply `stealthBlur` and Zen mode hiding in `PortfolioScreen.kt` and `WatchlistScreen.kt`
- `[x]` Update `ProfileScreen.kt` with toggles and locked Time-Travel button

## Phase 2: Professional Overhaul (Terminal Mode)
- `[x]` Add `TERMINAL` to `ThemeMode` and update `Color.kt`
- `[x]` Implement `MonospaceTypography` in `Type.kt`
- `[x]` Update `MyApplicationTheme` to switch typography based on mode

## Phase 3: Retro Nostalgia (Arcade Mode)
- `[x]` Add `ARCADE` to `ThemeMode` and update `Color.kt` with Synthwave palette
- `[x]` Add CRT scanline overlay effect to `StockChart.kt`

## Final Tasks
- `[x]` Final Verification of all modes
- `[x]` Create walkthrough
