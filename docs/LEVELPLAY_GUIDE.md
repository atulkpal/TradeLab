# Unity LevelPlay Integration Guide (Internal — Ashwath AI)

> **Purpose:** Reusable guide for integrating Unity LevelPlay (ironSource) ads across
> all Ashwath AI products. Written from hands-on integration experience with TradeLab
> (Epic 26). Follow this checklist for any new product.
>
> **SDK version tested:** 9.5.0 · **Platform:** Android (Jetpack Compose) · **Date:** 2026-08-25

---

## 1. Architecture Overview

```
App Code → AdConfig (IDs/knobs) → LevelPlayAdManager (facade)
    → LevelPlay SDK 9.5.0 (mediation) → ironSource Exchange (default demand)
    → Mediated Networks (Unity Ads, Meta, etc. — optional, higher eCPM)
```

**Key concept:** LevelPlay is a **mediation** platform, not an ad network. It routes
your ad requests through multiple networks. The built-in **ironSource Exchange** fills
ads by default — no additional networks needed for basic serving.

**What we learned:** Without at least one network adapter loaded, LevelPlay can still
serve via ironSource Exchange, but fill rates are lower. Adding network adapters
(Unity Ads, Meta, AppLovin) increases competition and eCPM.

---

## 2. Gradle Setup

### `gradle/libs.versions.toml`
```toml
[versions]
levelPlay = "9.5.0"
playServicesAppset = "16.0.0"
playServicesAdsIdentifier = "18.1.0"
playServicesBasement = "18.1.0"
unityAdsAdapter = "4.3.55"    # For Unity Ads demand (demo/test units)

[libraries]
levelplay-sdk = { group = "com.unity3d.ads-mediation", name = "mediation-sdk", version.ref = "levelPlay" }
unityads-adapter = { group = "com.unity3d.ads-mediation", name = "unityads-adapter", version.ref = "unityAdsAdapter" }
play-services-appset = { group = "com.google.android.gms", name = "play-services-appset", version.ref = "playServicesAppset" }
play-services-ads-identifier = { group = "com.google.android.gms", name = "play-services-ads-identifier", version.ref = "playServicesAdsIdentifier" }
play-services-basement = { group = "com.google.android.gms", name = "play-services-basement", version.ref = "playServicesBasement" }
```

### `app/build.gradle.kts`
```kotlin
implementation(libs.levelplay.sdk)
implementation(libs.unityads.adapter)          // Required for Unity Ads demand
implementation(libs.play.services.appset)
implementation(libs.play.services.ads.identifier)
implementation(libs.play.services.basement)
```

### `app/proguard-rules.pro`
```
-keep class com.ironsource.** { *; }
-keep class com.unity3d.** { *; }
-dontwarn com.ironsource.**
-dontwarn com.unity3d.**
```

### `app/src/main/AndroidManifest.xml`
```xml
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```
No AdMob `APPLICATION_ID` meta-data needed (that's AdMob-specific).

### ⚠️ REMOVE if migrating from AdMob
```kotlin
// DELETE these:
implementation(libs.play.services.ads)          // AdMob SDK
// DELETE files:
// AdMobManager.kt, BannerAdView.kt, NativeAdView.kt, ad_unified_row.xml
// DELETE manifest meta-data:
// com.google.android.gms.ads.APPLICATION_ID
```

---

## 3. Dashboard Setup

1. Go to [grow.unity.com](https://app.unity.com)
2. **Monetize → Setup → Apps** → Add your app (package name required)
3. Note the **App Key** (e.g. `27b051bfd`)
4. **Ad Units** tab → Create:
   - Rewarded Video (for all rewarded surfaces)
   - Interstitial (for foreground/app-transition)
   - Native (armed for future — needs mediated network to serve)
5. **Networks** tab → ironSource Exchange is enabled by default
6. **Test Devices** → add your device's GAID for test ads on real devices

### Account Approval (REQUIRED for live ads)

Activating ad units triggers a **mandatory account review**. The dashboard shows
*"Your ironSource Ads account is pending approval"* until it completes.

**Process:**
1. After ad unit activation, Unity emails you **information requests** (payment info +
   company info — completed via dashboard forms linked from the emails)
2. Submit both → account enters review
3. Approval lands by email, typically **1-3 business days**

**Key semantics (verified from Unity docs):**
- Live ads **start serving immediately** on approval — no app update, no redeploy
- Test mode is **unaffected** by approval; keep testing before and after
- When approved mid-test, tests aren't interrupted — just turn off test mode to go live
- If info requests never arrive: check Spam/Promotions, or 24h+ → contact support via dashboard

---

## 4. SDK Initialization (CRITICAL)

### ⚠️ THE #1 GOTCHA: `legacyAdFormats`

**The init request MUST include legacy ad formats.** Without them, the SDK does NOT
configure the rewarded/interstitial pipelines → every `loadAd()` returns
**1024 "no available ad to load"**.

```kotlin
// ✅ CORRECT — specifies formats
LevelPlay.init(
    context,
    LevelPlayInitRequest(
        appKey = AdConfig.LEVELPLAY_APP_KEY,
        legacyAdFormats = listOf(
            LevelPlay.AdFormat.REWARDED,
            LevelPlay.AdFormat.INTERSTITIAL,
            LevelPlay.AdFormat.NATIVE_AD
        )
    ),
    listener
)

// ❌ WRONG — formats empty → rewarded/interstitial pipelines NOT configured
LevelPlay.init(
    context,
    LevelPlayInitRequest.Builder(AdConfig.LEVELPLAY_APP_KEY).build(),
    listener
)
```

**NOTE:** The `Builder` class does NOT expose `legacyAdFormats`. Use the Kotlin
constructor directly (it accepts named parameters).

### Init retry on failure
If init fails (network hiccup), reset the `initAttempted` flag so the next
ad request retries init automatically.

---

## 5. Rewarded Ads

### API
```kotlin
val ad = LevelPlayRewardedAd(adUnitId)
ad.setListener(object : LevelPlayRewardedAdListener {
    override fun onAdLoaded(adInfo: LevelPlayAdInfo) { /* ready to show */ }
    override fun onAdLoadFailed(error: LevelPlayAdError) { /* handle */ }
    override fun onAdDisplayed(adInfo: LevelPlayAdInfo) { /* showing */ }
    override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) {
        // Grant reward here
    }
    override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) { /* handle */ }
    override fun onAdClosed(adInfo: LevelPlayAdInfo) { /* preload next */ }
    override fun onAdClicked(adInfo: LevelPlayAdInfo) {}
    override fun onAdInfoChanged(adInfo: LevelPlayAdInfo) {}
})
ad.loadAd()
// Later:
if (ad.isAdReady) ad.showAd(activity, placementName)
```

### Best practices
- **Create once, reuse** — the ad object handles multiple load/show cycles
- **Preload** after each show/close for the next impression
- **Check `isAdReady`** before showing
- **Pass `Activity`** to `showAd()` — required, not optional

---

## 6. Interstitial Ads (Foreground Replacement)

LevelPlay has no App Open format. Use **interstitial on lifecycle start** instead:

```kotlin
// Wire ProcessLifecycleOwner observer
// Show on onStart if: isReady && lastShown > 4 hours ago
interstitialAd.showAd(activity, placementName)
```

---

## 7. Native Ads (LIMITATION)

⚠️ **LevelPlay natives require a mediated network adapter** (Meta, AdMob, etc.)
to render. The base SDK has no standalone `LevelPlayNativeAd` class.

**Workaround:** Remove native ad spots from the UI. When a network is onboarded
in the LevelPlay dashboard → add the adapter dependency → natives activate.

---

## 8. Test vs Production

| Setting | Test Mode | Production |
|---------|-----------|------------|
| App Key | Unity test key `25b63cf85` OR your own test app | Your production key |
| Ad Units | Unity demo units OR your dashboard units | Your production units |
| Ads Serve? | ✅ Usually (test creatives) | ✅ Once account approved |
| Revenue | Zero (test) | Real |

### ⚠️ No Universal Test IDs (unlike AdMob)
AdMob has universal test IDs (`ca-app-pub-3940256099942544/...`) that serve test
ads for ANY app. **LevelPlay does NOT.** Each app needs its own ad units created
in the dashboard. The dashboard serves test ads automatically for new/unreleased apps.

### Emulator limitation
Unity Ads frequently **excludes emulators** from ad serving. Rewarded/interstitial
may return 1024 "no available ad" on emulators even with correct credentials.
**Always test on a real device for ad verification.**

---

## 9. Known Issues & Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| **626 Invalid ad unit id** | Ad unit doesn't exist for this appkey | Check dashboard; create unit; wait 15-30 min for propagation |
| **1024 No available ad** | Ad unit valid but no fill (emulator, region, demand) | Retry later; test on real device; check Unity Ads adapter loaded |
| **Adapter load error** | Network adapter dependency missing | Add adapter to gradle (e.g. `unityads-adapter`) |
| **Init failed** | Network issue or invalid appkey | Retry; check appkey in dashboard |
| **sdkReady stuck false** | Init failed silently, no retry | Reset `initAttempted` on failure; retry on next request |
| **Sessions revoked (2h-20h)** | Google risk system flags automation | L3 self-heal; re-auth via fresh login; space logins |

### Session Revocation Waves
Google's risk system may kill ALL sessions for accounts it flags. Symptoms:
all accounts die simultaneously, SDK init succeeds but ad serving rejects.
Fix: fresh logins via option 7 (space them out), then immediately use each
healed account for real work.

---

## 10. Onboarding Checklist (Any New Product)

- [ ] Create app in LevelPlay dashboard (package name + platform)
- [ ] Create ad units: Rewarded + Interstitial + Native (free)
- [ ] Copy app key + ad unit IDs into product's `AdConfig`
- [ ] Add gradle dependencies (SDK + adapter + play-services)
- [ ] Add ProGuard rules
- [ ] Add `AD_ID` permission to manifest
- [ ] Call `LevelPlayAdManager.init(context)` in `Application.onCreate`
- [ ] Wire rewarded call sites through the manager
- [ ] Test on real device (emulator may not serve)
- [ ] Verify with LevelPlay Integration Test Suite

---

*Written from TradeLab Epic 26 hands-on integration (2026-08-24). SDK version: 9.5.0.*
