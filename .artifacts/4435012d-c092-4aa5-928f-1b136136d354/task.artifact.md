# Tasks: Version 1.8.0 Hyper-Personalization Release

- [ ] **Phase 0: Pre-Build Archiving & Cleanup**
    - [ ] Move existing artifacts from `app/build/outputs/` to `releases/`
    - [ ] Ensure `app/build/outputs/` is clean
- [ ] **Phase 1: Build Configuration**
    - [ ] Update `versionCode` to `7` and `versionName` to `"1.8.0"` in `app/build.gradle.kts`
- [ ] **Phase 2: Documentation**
    - [ ] Update `CHANGELOG.md` with v1.8.0 entry
    - [ ] Update `RELEASES.md` with v1.8.0 entry
- [ ] **Phase 3: Generation & Verification**
    - [ ] Run `:app:assembleRelease`
    - [ ] Run `:app:bundleRelease`
    - [ ] Run `:app:assembleDebug`
- [ ] **Phase 4: Post-Build Archiving**
    - [ ] Move `debug-1.8.0-7.apk` to `releases/`
    - [ ] Move `release-apk-1.8.0-7.apk` to `releases/`
    - [ ] Move `release-aab-1.8.0-7.aab` to `releases/`
