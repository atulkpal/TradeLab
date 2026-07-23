# Implement Global Leaderboard with Firestore

I will transition the Academy leaderboard from a static mock list to a dynamic, global system using **Cloud Firestore**. This will allow your testers to compete with each other in real-time.

## User Review Required

> [!IMPORTANT]
> - **Firestore Setup:** You must finish the "Create Database" flow in the Firebase Console (as shown in your screenshot) before this feature will work.
> - **Data Visibility:** This will upload the user's **Display Name**, **XP (Level progress)**, and **Total Portfolio Value** to the cloud.

## Proposed Changes

### Dependencies & Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/gradle/libs.versions.toml)
- Add `firebase-firestore-ktx`.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/build.gradle.kts)
- Add `implementation(libs.firebase.firestore)` to the dependencies.

### Core Logic

#### [NEW] [LeaderboardManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/data/LeaderboardManager.kt)
- Create a service to push the local user's stats to a `leaderboard` collection in Firestore.
- Implement a function to fetch the top 20 users ranked by XP.

#### [MODIFY] [TradingViewModel.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/TradingViewModel.kt)
- Trigger a sync to Firestore whenever a quiz is completed or the portfolio value changes significantly.
- Expose a `StateFlow<List<LeaderboardEntry>>` for the UI.

### UI Integration

#### [MODIFY] [AcademyScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/academy/AcademyScreen.kt)
- Replace the static `leaders` list with the dynamic data from the ViewModel.

## Verification Plan

### Manual Verification
- **Test User:** Complete a quiz and verify that your name and XP appear on the leaderboard.
- **Multi-Device:** Run the app on two emulators with different accounts and verify they can see each other's ranks.
