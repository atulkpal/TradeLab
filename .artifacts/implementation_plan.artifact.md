# Implementation Plan — Release Stability & Crashlytics Recovery

Systematic triage and fix for navigation crashes in release builds and restoration of Firebase Crashlytics reporting.

## User Review Required

> [!IMPORTANT]
> The missing Crashlytics reports suggest that Firebase initialization is either failing or being bypassed in the release environment. I will be removing the conditional check in `TradeLabApplication` to ensure Firebase starts reliably.

## Open Questions

- **Device Logcat:** Are you able to provide a logcat output from a device where the crash occurs? Even without Crashlytics, a local `adb logcat` would pinpoint the exact line.
- **R8 Mapping:** Have you recently updated R8 or Gradle? (The build file shows AGP 9.1.1, which is very recent).

## Proposed Changes

Following the `debugging-and-error-recovery` skill, I am targeting the most likely root causes: **R8 Obfuscation of Data Models** and **Firebase Initialization Gaps**.

### 1. Firebase & Stability Hub

#### [MODIFY] [TradeLabApplication.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/TradeLabApplication.kt)
- Remove the `FirebaseApp.getApps().isNotEmpty()` guard. This guard is likely preventing initialization if the auto-init provider is delayed.
- Add explicit `FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)` to ensure it's active in release.
- Wrap the setup in a try-catch that logs to standard `android.util.Log` as a final fallback.

### 2. Obfuscation & R8 Hardening

#### [MODIFY] [proguard-rules.pro](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/proguard-rules.pro)
- **Data Model Protection:** Add `-keep class com.ashwathai.tradelab.data.** { *; }`. This prevents R8 from renaming fields in Room entities and JSON models, which breaks Moshi and Firestore reflection.
- **UI Model Protection:** Add `-keep class com.ashwathai.tradelab.ui.** { *; }` to protect models used in `AcademyScreen` and `ProfileScreen`.
- **Moshi Reflection Fix:** Add rules to keep `KotlinJsonAdapterFactory` related internals if necessary (though keeping data classes usually suffices).

### 3. Graceful Data Loading

#### [MODIFY] [TradingViewModel.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/TradingViewModel.kt)
- Improve error logging in `loadAcademyAndMissionsData()`. Currently, it just calls `e.printStackTrace()`. I will add a `Log.e` with a specific tag so it's visible in logcat even if Crashlytics is down.
- Ensure that if JSON parsing fails, the UI receives a valid (empty) state instead of potentially null or corrupted objects.

---

## Verification Plan

### Automated Tests
- `.\gradlew :app:assembleRelease`: Verify the build still completes with new ProGuard rules.
- Run `TradingViewModelTest` to ensure logic remains sound.

### Manual Verification
- **Production Build:** Build the release APK/AAB and deploy to a physical device.
- **Navigation Stress Test:** Rapidly switch between Portfolio, Academy, and Profile tabs.
- **Crashlytics Verification:** I will add a temporary "Simulate Crash" button in the Profile screen (only visible in DEV or triggered by a secret tap) to confirm that the crash reporting pipeline is 100% functional.
- **Logcat Monitoring:** Monitor `adb logcat` during navigation to catch any swallowed `JsonDataException` or Room warnings.
