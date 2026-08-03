# Implementation Plan - Bug Fixes for Crashlytics Reports

This plan addresses two critical bugs identified in Crashlytics:
1.  **Bug #1 (v1.5.0):** `SQLiteDatabaseCorruptException` in `MarketNewsDao`.
2.  **Bug #2 (v1.7.0):** `IllegalArgumentException: current must not be NaN` in `LinearProgressIndicator`.

## Proposed Changes

### 1. Database Corruption Handling (Bug #1)

The app experiences `SQLiteDatabaseCorruptException` during database operations. While Room handles some migrations, runtime corruption can still occur.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/data/AppDatabase.kt)
- Add a custom `DatabaseErrorHandler` to the `RoomDatabase.Builder` if possible, or ensure `JournalMode.WRITE_AHEAD_LOGGING` is explicitly set (it's generally more robust).
- Implement a safety check in `getDatabase` to handle scenarios where the database file exists but is corrupted.
- Since standard `Room.databaseBuilder` doesn't easily expose the error handler, I will add `.setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)` to improve reliability and potentially add a check to recreate the database if opening fails.

### 2. NaN Progress Value Fix (Bug #2)

The `LinearProgressIndicator` crashes when passed a `NaN` value for progress, which happens when dividing by zero (e.g., when a list is empty).

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/profile/ProfileScreen.kt)
- Fix division by zero in the quiz progress indicator.
- Ensure all lambda-based progress calculations handle empty state.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/MainActivity.kt)
- Audit `LinearProgressIndicator` usages and add safety checks for progress values.

## Verification Plan

### Automated Tests
- I will attempt to write a unit test or use a scratch script to verify the division by zero logic in a mocked UI state.
- I'll verify the `AppDatabase` configuration.

### Manual Verification
- Deploy to an emulator/device.
- Navigate to the Profile screen when no quiz modules are loaded (e.g., initial state) to ensure it doesn't crash.
- Verify that `LinearProgressIndicator` works correctly with valid data.
