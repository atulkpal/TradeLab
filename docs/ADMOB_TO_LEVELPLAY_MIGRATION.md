# AdMob → Unity LevelPlay Migration Playbook (Internal)

> **Purpose:** Step-by-step playbook for migrating ANY Ashwath AI app from AdMob
> (banned) to Unity LevelPlay, and for wiring fresh apps. Distilled from TradeLab's
> live migration (Epic 26, Aug 2026) — every landmine here was hit for real.
>
> **Companion doc:** [`LEVELPLAY_GUIDE.md`](LEVELPLAY_GUIDE.md) — deep integration
> reference (gradle, init, ad formats, troubleshooting, logcat signatures).
> **Read both. This doc = process. That doc = technical reference.**

---

## 0. Account & Dashboard Prerequisites (once per org, then per app)

| Step | Where | Notes |
|------|-------|-------|
| Org setup | grow.unity.com | One org for ALL apps. Org **API Key + Core ID** (cloud.unity.com → Monetization → Settings → API Management) are shared by every app |
| Add app | LevelPlay → Apps | Package name + platform. Note the **App Key** |
| Ad units | LevelPlay → Setup → **Ad units** (⚠️ the **LevelPlay** product, NOT the "Ads"/ironSource product) | Rewarded + Interstitial (+ Native armed). Copy IDs exactly |
| **Activate Unity Ads network** | Setup → Networks → Unity Ads → Setup | Needs **Monetization API Key + Org Core ID** + per-app **Game ID** (create the app in cloud.unity.com Monetization first) + per-format **Placement IDs** (`Rewarded_Android` etc.). Bidder is a shell without these |
| Info-request emails | Email | Two forms (payment + company). **Approval gates ALL serving — including test ads.** Reply immediately |
| app-ads.txt | Your developer domain root | Copy the LevelPlay-generated list + `ownerdomain=<your domain>`. Unity verifies fast; full crawl 24-48h |
| Test device | LevelPlay + Unity dashboards | Register your device's **Advertising ID** on BOTH sides |

---

## 1. AdMob Removal (do this FIRST — clean sweep)

Grep-verified removal checklist (TradeLab reference: Epic 26, commit history):

```
□ gradle:  remove implementation(libs.play.services.ads) / com.google.android.gms:play-services-ads
□ manifest: remove <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" …>
□ code:    delete AdMobManager (or equivalent init/show class)
□ code:    delete BannerAdView / NativeAdView / custom ad views + their XML layouts
□ code:    remove AppOpenAdManager (LevelPlay replaces App-Open with a foreground interstitial)
□ code:    remove ALL com.google.android.gms.ads.* imports (grep to zero)
□ code:    audit every ad call site → route through the new manager (see §2)
□ proguard: remove AdMob keep rules if any
```

**⚠️ LEAK AUDIT (critical — do not skip):** AdMob-era code often has "graceful
fallbacks" that **grant rewards when ads fail**. With no fill, that = free rewards.
Grep every `onAdFailed` / failure callback and remove ANY reward grant or gated-flow
bypass. **Fail-closed contract: no ad → no reward → user-facing feedback only.**
Pin it with a source-scan test (see TradeLab's `AdFailClosedGuardTest`).

---

## 2. LevelPlay Integration (reference: LEVELPLAY_GUIDE.md §2/§4/§5)

1. Gradle: mediation-sdk + **unityads-adapter 5.x** + **unity-ads (explicit!)** + play-services trio + ProGuard rules + `AD_ID` permission
2. `AdConfig`-style single source: app key, ad unit IDs, placements, `USE_TEST_ADS` toggle
3. Manager facade with the **same public contract your call sites already use** (TradeLab kept `loadAndShowRewardedAd(adType, onAdLoaded, onAdFailed, onUserEarnedReward)` — 10+ call sites untouched)
4. Init in `Application.onCreate`; UI gates on `sdkReady`
5. Rewarded: create-once/reuse, listener before load, preload after close, **clear callbacks at cycle end**, `isPlacementCapped` check
6. Foreground interstitial replaces App-Open (lifecycle observer + freshness window)
7. **Fail-closed everywhere.** No silent rewards, no fallback grants, no fake ads

---

## 3. The Landmines (each cost TradeLab real time — read twice)

| # | Landmine | Symptom | Prevention |
|---|----------|---------|------------|
| 1 | **Adapter does NOT bundle the Unity Ads SDK** (zero-dep POM, any version) | Compiles + runs fine; `NoClassDefFoundError: com.unity3d.ads.*` in logcat; zero Unity Ads demand forever | Explicit `unity-ads` dependency; verify APK has `libunitycoherencenative.so` |
| 2 | **Adapter generation mismatch** | 4.x adapters are 8.x-era; LevelPlay 9.x needs **5.x** adapters | Use the docs pairing table (adapter 5.12.0 ↔ SDK 4.20.0) |
| 3 | **Approval gates ALL serving — including test ads** | `626 Invalid ad unit id` with perfect config, for hours | Reply to the 2 info emails immediately; test creatives flow to **registered test devices** even pre-approval |
| 4 | **New units provision over HOURS** (not 15-30 min) | 626 on fresh units while older ones serve | Patience; per-unit timelines are independent |
| 5 | **Ad units under the wrong product** | Dashboard shows units; serving rejects | Units must be under **LevelPlay → Setup → Ad units**, not the "Ads" product |
| 6 | **Bidder shell without Game ID/Placement IDs** | Unity Ads "active" but never bids | Fill Game ID + per-format Placement IDs in network setup |
| 7 | **Retry loop** (reset counter in the failure path) | Dozens of load/fail cycles per second (looks like invalid traffic) | Reset retry budget only on genuine success |
| 8 | **Stale callbacks** (not cleared at cycle end) | Spontaneous re-shows; double reward grants | Clear callbacks in `onAdClosed` |
| 9 | **Emulator exclusion** | No fill on emulators even when everything works | Verify on real devices only |
| 10 | **Test mode is a dead end** | Demo appkey = package mismatch = permanent no-fill | Production key + registered test devices is the real path |

---

## 4. Verification (in order)

1. **Build check:** APK contains `lib/*/libunitycoherencenative.so` (Unity Ads SDK present)
2. **Init:** logcat `UnityAds: Initialized successfully` + `Generated a valid token` + `LevelPlay initialized`
3. **Load:** `Rewarded loaded | unit=<name>(<id>) network=<network>`
4. **Serve:** `Rewarded DISPLAYED | … revenue=…` → `Reward earned` → `closed` (full cycle)
5. **Dashboard:** Integration Test Suite (Mediation → "Start test") pushes a test ad to registered devices
6. **Attribution:** every ad self-logs `adUnitName/adNetwork` — ironSource Exchange test creatives are expected first on registered devices; Unity Ads wins come later

---

## 5. Rollout Sequence

1. Merge to develop → tag (`vX.Y.Z`) + cut `stable` branch (fallback discipline)
2. Build release AAB → archive (`releases/release-aab-<v>-<code>.aab`)
3. Upload to **Open testing** first — registered devices verify serving; unregistered devices measure real-world pre-approval fill
4. Production promotion only after: approval email + real (non-test) fill observed + fail-closed audit passed

---

*Source: TradeLab Epic 26-28 (Aug 2026). Maintained by the TradeLab repo — update
this file when Unity changes versions/flows. Last verified: 2026-08-26 (SDK 9.5.0,
adapter 5.12.0, Unity Ads 4.20.0, serving confirmed on device).*
