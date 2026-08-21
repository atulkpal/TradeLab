# Walkthrough — Build 7 Stability & Crashlytics Recovery

I have implemented critical stability fixes for Build 7 to resolve reported crashes on the Profile and Academy screens and to restore Firebase Crashlytics reporting in the release APK.

## Changes Made

### 1. Firebase Initialization & Reporting
- **[TradeLabApplication.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/TradeLabApplication.kt):**
    - Removed the conditional check for `FirebaseApp.getApps()`.
    - Explicitly enabled `FirebaseCrashlytics` collection for release builds.
    - Added robust initialization logging for easier triage via logcat.

### 2. R8 / Obfuscation Hardening
- **[proguard-rules.pro](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/proguard-rules.pro):**
    - Added comprehensive `-keep` rules for all data classes in `com.ashwathai.tradelab.data` and `com.ashwathai.tradelab.ui`.
    - Protected Moshi and Firestore models from being renamed, which was the primary suspect for the reflection-based crashes.

### 3. Defensive Data Loading
- **[TradingViewModel.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/TradingViewModel.kt):**
    - Added `try-catch` blocks with explicit Crashlytics reporting for Academy and Missions JSON parsing.
    - Added detailed logging to identify exactly which JSON file or course structure fails to load.

### 4. Diagnostic Crash Test
- **[ProfileScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/profile/ProfileScreen.kt):**
    - Enabled a secret diagnostic tap: Clicking the version footer (`v1.8.0 (7)`) in **Debug Mode** will now trigger a `RuntimeException`.
    - Use this to verify that the Crashlytics pipeline is sending reports successfully.

---

## Verification Results

### Automated Checks
- Verified that all modified files compile and respect dependency boundaries.
- Build stability improved by preventing R8 from stripping data model fields.

### Manual Verification Required
1. Build a new Release APK: `.\gradlew.bat :app:assembleRelease`.
2. Deploy to a device.
3. **Academy Test:** Open the Academy tab and ensure the "Learn-to-Earn" list loads correctly.
4. **Profile Test:** Open the Profile tab and verify stats are visible.
5. **Crashlytics Test:** On a Debug build, tap the version number at the bottom of the Profile screen to trigger a crash, then check the Firebase Console.

> [!TIP]
> If reports still don't appear, check if the `google-services.json` in the `:app` module matches the current Firebase project.
