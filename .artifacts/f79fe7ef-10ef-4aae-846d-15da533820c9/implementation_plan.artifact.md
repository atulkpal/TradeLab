# Implementation Plan - Precision Ledger & Tradebook

This plan introduces a high-precision **Ledger** (Account Statement) system to track every virtual penny, ensuring total transparency in where capital goes (Trades, Taxes, Charges, and Rewards).

## User Review Required

> [!IMPORTANT]
> - We will add a new `LedgerEntry` entity to track running cash balances.
> - We will introduce a new "Ledger" view accessible from the Portfolio tab to show detailed cash flow with 4-decimal precision.

## Proposed Changes

### 1. Data Layer Enhancements

#### [NEW] [Entities.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/data/Entities.kt)
- Create `LedgerEntry` entity:
    - `id: Int` (Primary Key)
    - `timestamp: Long`
    - `description: String`
    - `type: String` (e.g., "DEBIT", "CREDIT")
    - `amount: Double`
    - `runningBalance: Double`
    - `refId: Int?` (Optional link to Transaction ID)

#### [MODIFY] [Daos.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/data/Daos.kt)
- Add `LedgerDao` with methods to insert and query ledger entries.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/data/AppDatabase.kt)
- Include `LedgerEntry` in the database and bump version to **21**.

### 2. Business Logic Integration

#### [MODIFY] [TradingRepository.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/data/TradingRepository.kt)
- Update `buyStock`, `sellStock`, `earnEmergencyCash`, `completeTutorialLevel`, and `resetPortfolio` to record a `LedgerEntry` whenever cash changes.
- Ensure all intermediate math uses `Double` precision and is logged accurately.

### 3. UI/UX Implementation

#### [MODIFY] [CommonHelpers.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/common/CommonHelpers.kt)
- Add `formatLedgerAmount(Double)` to show up to 4 decimal places for the Ledger view.

#### [MODIFY] [TradingViewModel.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/TradingViewModel.kt)
- Expose `ledgerEntries: StateFlow<List<LedgerEntry>>`.

#### [NEW] [LedgerScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/portfolio/LedgerScreen.kt)
- A detailed list view showing:
    - Date/Time
    - Activity Description
    - Amount (Red for Debit, Green for Credit)
    - Running Balance (High precision)

#### [MODIFY] [PortfolioScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/portfolio/PortfolioScreen.kt)
- Add a "View Ledger / Tradebook" button in the Account Card or Lifetime Metrics section.

## Verification Plan

### Automated Tests
- `LedgerLogicTest`: Verify that buying 10 shares at a specific price results in correct debits for:
    1. Stock Cost
    2. STT (Tax)
    3. Transaction Charges
    4. Correct final running balance.

### Manual Verification
1. Execute a trade and open the Ledger.
2. Verify 3 separate entries appear for a single BUY: the principal, the tax, and the charges.
3. Verify ad rewards (Emergency Cash) appear as a single CREDIT entry.
4. Verify the "Total Portfolio Value" on the home screen matches the final running balance in the Ledger (plus current holding values).
