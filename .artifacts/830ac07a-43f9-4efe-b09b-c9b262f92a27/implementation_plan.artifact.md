# Implementation Plan - Hyper-Personalization & Focus Suite

This plan details the implementation of specialized functional and visual modes for Trade Lab, designed to enhance privacy, focus, and aesthetic variety.

## User Review Required

> [!IMPORTANT]
> **Stealth Mode Implementation:** I will implement a custom `Modifier.stealthBlur()` that applies `Modifier.blur()` on supported devices (Android 12+) and falls back to a high-density "frosted" overlay for older devices.
>
> **Time-Travel Mode:** Per your request, this is now a backlog item. I will add a locked UI element to indicate it is "Coming Soon."

## Proposed Changes

### [Component Name] Core Infrastructure

#### [MODIFY] [TradingViewModel.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/TradingViewModel.kt)
- Add StateFlows: `isStealthMode: StateFlow<Boolean>` and `isZenMode: StateFlow<Boolean>`.
- Add toggle functions: `toggleStealthMode()` and `toggleZenMode()`.
- Ensure these states persist in the repository.

#### [NEW] [CompositionLocals.kt] (In `ui/theme` or `ui/common`)
- Define `LocalStealthMode` and `LocalZenMode` for deep-tree UI adaptation.

#### [MODIFY] [Theme.kt](file:///C:/Users/Atul\AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/theme/Theme.kt)
- Provide the new CompositionLocals in `MyApplicationTheme`.

### [Component Name] Mode Implementations

#### [MODIFY] [CommonUi.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/common/CommonUi.kt)
- Create `Modifier.stealthBlur(enabled: Boolean)`:
    - **API 31+:** Uses `Modifier.blur(16.dp)`.
    - **Fallback:** Uses a solid color or semi-transparent overlay.
- Define `ModeToggleRow` for the Profile screen.

#### [MODIFY] [PortfolioScreen.kt] & [WatchlistScreen.kt]
- Wrap sensitive currency text elements in `stealthBlur`.
- Implement Zen Mode logic: Conditionally hide `BreakingNewsTicker`, `MarketDashboardWidget`, and promotional banners.

#### [MODIFY] [ProfileScreen.kt]
- Add a new "Experimental Features" section with toggles for Stealth and Zen modes.
- Add a "Time-Travel Mode" button with a `Lock` icon and "Coming Soon" badge.

#### [MODIFY] [Type.kt] (Terminal Mode Prep)
- Add `MonospaceTypography` using `FontFamily.Monospace`.

### [Component Name] Visual Modes (Arcade & Terminal)
- **Terminal:** Apply `MonospaceTypography` and update `ColorScheme` to monochromatic (Greens/Ambers on Black).
- **Arcade:** Implement "Neon Retro" palette and CRT scanline overlay on charts.

## Verification Plan

### Automated Tests
- Verify that `isStealthMode` and `isZenMode` persist correctly in the database.

### Manual Verification
- **Stealth:** Verify that portfolio values and cash are blurred on both Portfolio and Watchlist screens.
- **Zen:** Verify that the UI simplifies significantly when enabled.
- **Visuals:** Cycle through Terminal and Arcade themes to ensure a consistent look.
- **Backlog:** Confirm the Time-Travel button is visible but locked.
