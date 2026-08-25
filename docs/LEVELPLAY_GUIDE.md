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
4. **Setup → Ad units** (under the **LevelPlay** product — NOT the separate "Ads"
   / ironSource Ads product in the sidebar!) → Create:
   - Rewarded Video (for all rewarded surfaces)
   - Interstitial (for foreground/app-transition)
   - Native (armed for future — needs mediated network to serve)
5. **⚠️ Setup → Networks → activate Unity Ads (REQUIRED for demand):**
   - Click **Setup** on the Unity Ads row
   - It asks for **Monetization API Key + Organization Core ID** — both live in
     [cloud.unity.com](https://cloud.unity.com) → Monetization → **Settings → API
     Management** (org-level; one org credential pair covers ALL your apps)
   - Prerequisite: a Unity **Monetization app** for your package (Add app → Android →
     package name; auto-links to Google Play) with placements created
   - Bidder auto-setup then attaches Unity Ads demand to your ad units
   - **Without an activated network you have ZERO configured demand** — ironSource
     Ads direct-demand is being retired (Apr 30, 2026) and its approval is separate
6. **Setup → Test devices** → add your device's **Advertising ID** (GAID) — also add
   it on the Unity side (Monetization → Test devices) for full coverage.
   Android 13+: the ID can be *deleted* by the user — reset it in Settings → "Ads"
7. **app-ads.txt**: copy the authorized-sellers list from LevelPlay (Apps → your app
   → app-ads.txt) to your developer domain root (we host at
   `https://tradelab-4f858.web.app/app-ads.txt` via Firebase Hosting). Missing
   entries = demand partners won't bid.

### Account Approval (gates ALL serving — including test ads)

Activating ad units triggers a **mandatory account review**. The dashboard shows
*"Your ironSource Ads account is pending approval"* until it completes.

**Process:**
1. After ad unit activation, Unity emails you **information requests** (payment info +
   company info — completed via dashboard forms linked from the emails)
2. Submit both → account enters review
3. Approval lands by email, typically **1-3 business days**

**Key semantics (verified from Unity docs + community, 2023→2025):**
- Live ads **start serving immediately** on approval — no app update, no redeploy
- ⚠️ **Approval gates ALL serving — INCLUDING test ads.** Marketing copy suggesting
  "use LevelPlay while pending" is misleading: until approved, ad requests fail
  (observed as `626 Invalid ad unit id` even with correct, active units — the
  serving backend doesn't provision them for pending accounts)
- Mediated third-party networks (Unity Ads bidder) *may* serve independently of the
  ironSource Ads approval — worth testing, not guaranteed
- If info requests never arrive: check Spam/Promotions, or 24h+ → contact support via dashboard

---

## 4. SDK Initialization

### ✅ Correct init (SDK 9.x — plain Builder)

```kotlin
LevelPlay.init(
    context,
    LevelPlayInitRequest.Builder(AdConfig.LEVELPLAY_APP_KEY).build(),
    listener
)
```

**SDK 9.0.0 (Sept 2025) REMOVED ad-format parameters from Init entirely** ("Init
simplified, removing ad format parameters" — official changelog). Plain Builder is
the correct, complete modern init. Older 8.x-era guides mentioning ad-format/init
config are obsolete — do NOT add format parameters back.

### Init retry on failure
If init fails (network hiccup), reset the `initAttempted` flag so the next
ad request retries init automatically.

### ⚠️ Retry-loop trap (found the hard way)
If your preload function is also your failure-retry path, NEVER reset the retry
counter inside it — failure→retry→reset→failure loops forever, hammering the SDK
dozens of times per second (looks like invalid traffic to the network's risk
system). Reset the counter only on genuine success (onAdRewarded / onAdClosed).

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
- **Check `isPlacementCapped(placement)`** before `showAd(activity, placement)` — official recommendation, prevents show failures on capped placements
- **Pass `Activity`** to `showAd()` — required, not optional
- **Reward data via `getReward(placement)`** (SDK 9.3+) — read reward name/amount from the dashboard cache to build dynamic "Watch to earn X" UIs instead of hardcoding

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
| App Key | Unity demo key `25b63cf85` (their demo app) | Your production key |
| Ad Units | Unity demo units | Your dashboard units |
| Ads Serve? | ⚠️ Often NO — demo appkey has package mismatch with your app | ✅ Once account approved |
| Revenue | Zero (test) | Real |

### ⚠️ Hard-won lessons (TradeLab, Aug 2026)
- **Test mode with the demo appkey usually serves NOTHING for your package** — the
  demo units belong to Unity's demo app; package mismatch → permanent no-fill.
  Don't burn time debugging test mode; go straight to production key + registered
  test device.
- **Approval gates ALL serving — including test ads.** Until the account is
  approved, requests fail (observed: `626 Invalid ad unit id` with correct, active
  units). Community reports confirm this consistently.
- **No Universal Test IDs (unlike AdMob).** Each app needs its own dashboard units.
  The replacement for "universal test ads" = register your device's Advertising ID
  as a **Test Device** (both dashboards), then production units serve test
  creatives to that device post-approval.
- **Emulator limitation:** rewarded/interstitial demand frequently excludes
  emulators entirely — always verify on a real device.

### Emulator limitation
Unity Ads frequently **excludes emulators** from ad serving. Rewarded/interstitial
may return 1024 "no available ad" on emulators even with correct credentials.
**Always test on a real device for ad verification.**

---

## 9. Known Issues & Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| **626 Invalid ad unit id** | ① Units under wrong product section ("Ads" vs "LevelPlay") ② **Account pending approval — serving backend doesn't provision units** (verified: correct+active units still 626 until approved) ③ ID mismatch ④ propagation (15-30 min after creation) | ① Verify units under LevelPlay → Setup → Ad units ② Wait for approval email ③ Character-check IDs ④ Wait/retry |
| **1024 No available ad** | Ad unit valid but no fill (emulator, region, zero demand configured) | Real device; **verify a network is ACTIVATED** (Unity Ads bidder); check Unity Ads adapter loaded |
| **Adapter load error** | Network adapter dependency missing | Add adapter to gradle (e.g. `unityads-adapter`) |
| **Init failed** | Network issue or invalid appkey | Retry; check appkey in dashboard |
| **sdkReady stuck false** | Init failed silently, no retry | Reset `initAttempted` on failure; retry on next request |
| **Infinite load/fail loop** | Retry counter reset inside the preload/failure path | Reset retry budget only on genuine success (onAdRewarded/onAdClosed) |
| **Sessions revoked (2h-20h)** | Google risk system flags automation | L3 self-heal; re-auth via fresh login; space logins |

### Platform Facts (verified Aug 2026)

- **ironSource brand/APIs → deprecated**: SDK 9.0.0 (Sept 2025) refactored all
  public APIs to LevelPlay; Init lost its ad-format parameters (plain Builder is
  correct). Anything describing format-based init is 8.x-era — obsolete.
- **"IronSource Ads" direct-demand network: retired April 30, 2026.** LevelPlay
  mediation itself is unaffected and is Unity's core platform (Android SDK 9.6.0,
  Aug 2026). **You MUST activate at least one network** (Unity Ads bidder is the
  natural choice) — otherwise zero configured demand.
- **app-ads.txt**: copy the LevelPlay-generated authorized-sellers to your developer
  domain root (Play Console "website" field). Crawlers take **24-48h** to pick it
  up — dashboard errors during that window are expected, not actionable.
- **Consent**: `LevelPlay.setMetaData` consent APIs deprecated (hard removal ~Aug
  2026) — use `LevelPlayPrivacySettings` (SDK 9.4+) when implementing consent.

### Session Revocation Waves
Google's risk system may kill ALL sessions for accounts it flags. Symptoms:
all accounts die simultaneously, SDK init succeeds but ad serving rejects.
Fix: fresh logins via option 7 (space them out), then immediately use each
healed account for real work.

---

## 10. Onboarding Checklist (Any New Product)

- [ ] Create app in LevelPlay dashboard (package name + platform) — note App Key
- [ ] Create ad units under **LevelPlay → Setup → Ad units**: Rewarded + Interstitial + Native
- [ ] **Activate Unity Ads network** (Setup → Networks → Setup → org API key + core ID from cloud.unity.com → Monetization → Settings → API Management)
- [ ] Create the Unity Monetization app + placements (`reward` / `foreground`) in cloud.unity.com
- [ ] Copy app key + ad unit IDs into product's `AdConfig` (`USE_TEST_ADS = false` — test mode is a dead end)
- [ ] Register your device's Advertising ID as Test Device (LevelPlay + Unity dashboards)
- [ ] Deploy **app-ads.txt** (LevelPlay-generated list) to your developer domain root; allow 24-48h for crawling
- [ ] Add gradle dependencies (SDK + unityads-adapter + play-services) + ProGuard rules + `AD_ID` permission
- [ ] Call `LevelPlayAdManager.init(context)` in `Application.onCreate`
- [ ] Wire rewarded call sites through the manager (fail-closed: no ad → no reward)
- [ ] Test on a real device (emulator may not serve)
- [ ] Verify with LevelPlay Integration Test Suite
- [ ] Reply to the two ironSource info-request emails (approval gates ALL serving)

---

*Written from TradeLab Epic 26-28 hands-on integration, corrected after live
dashboard + device verification (2026-08-25). SDK: 9.5.0 (9.6.0 available).*
