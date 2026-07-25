# Tasks - Precision Ledger & Tradebook

- [x] **Data Layer**
    - [x] Add `LedgerEntry` entity to `Entities.kt`
    - [x] Add `LedgerDao` to `Daos.kt`
    - [x] Update `AppDatabase.kt` (Version 21)
    - [x] Update `DatabaseModule.kt`
- [x] **Business Logic**
    - [x] Implement `recordLedgerEntry` in `TradingRepository`
    - [x] Integrate ledger logging in `buyStock`, `sellStock`, `earnEmergencyCash`, etc.
- [x] **UI/UX Implementation**
    - [x] Add `formatLedgerAmount` to `CommonHelpers.kt`
    - [x] Update `TradingViewModel.kt` to expose ledger state
    - [x] Create `LedgerScreen.kt`
    - [x] Add "View Ledger" trigger to `PortfolioScreen.kt`
- [x] **Verification**
    - [x] Manual check of ledger entries after a trade
    - [x] Verify balance consistency
