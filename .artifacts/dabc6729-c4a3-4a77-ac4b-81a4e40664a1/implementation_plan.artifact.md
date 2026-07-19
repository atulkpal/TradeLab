# Fix Missing debug.keystore

The build is failing because the `:app` module's `build.gradle.kts` expects a `debug.keystore` file in the project root, but it's missing. This file is also listed in `.gitignore`, so it wouldn't be present in a fresh clone.

## User Review Required

> [!IMPORTANT]
> I am proposing to modify the Gradle configuration to automatically fallback to your system's default debug keystore (`~/.android/debug.keystore`) if the local project one is missing. This will allow the build to succeed without requiring you to manually copy or generate a keystore file.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/build.gradle.kts)
- Update `signingConfigs` -> `debugConfig` to check for the existence of `debug.keystore` in the root directory.
- Fallback to the default Android debug keystore if it doesn't exist.

## Verification Plan

### Automated Tests
- Run `:app:validateSigningDebug` task to verify the fix.
- Run `gradlew assembleDebug` to ensure the app builds successfully.
