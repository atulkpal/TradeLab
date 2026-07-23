# Version Bump & Release Preparation Plan

Bump the application version to `1.2.0` (Build 4) and document the final pre-launch polish items in the project history.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/build.gradle.kts)
- Increment `versionCode` from `3` to `4`.
- Update `versionName` from `1.1.0` to `1.2.0`.

---

### Project Documentation

#### [MODIFY] [CHANGELOG.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/CHANGELOG.md)
- Add entry for version `1.2.0`.
- Document viral sharing hooks, professional order toggles (MIS/CNC), and T+1 UI refinements.

#### [MODIFY] [RELEASES.md](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/RELEASES.md)
- Add release ledger entry for `1.2.0` with Build 4.
- Update status of `1.1.0` to legacy/stable.

## Verification Plan

### Manual Verification
- Verify the `versionName` and `versionCode` in the IDE's project structure or by running a build.
- Ensure documentation files are correctly formatted and updated.
