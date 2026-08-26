package com.ashwathai.tradelab.ui.common

/**
 * Single source of truth for Unity LevelPlay monetization (Epic 26).
 * Replaces the scattered hardcoded AdMob IDs (MainActivity / AdMobManager / BannerAdView).
 *
 * Setup: app 27b051bfd on grow.unity.com → create ad units → paste their IDs below.
 * Until an ID is set, the manager fails gracefully ("not configured") — no crashes,
 * no silent rewards.
 */
object AdConfig {
    /** Debug builds always use test ads; release builds use production. */
    val USE_TEST_ADS: Boolean get() = com.ashwathai.tradelab.BuildConfig.DEBUG

    /** Production app key — TradeLab (grow.unity.com). */
    const val PROD_APP_KEY = "27b051bfd"
    // rewarded_v2 — recreated 2026-08-26 (old rewarded_main b1o5nmpzepmzn76f stuck
    // un-provisioned server-side after the retry-loop hammering incident)
    const val PROD_REWARDED_AD_UNIT_ID = "349kle4725uh1kfa"      // rewarded_v2
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "0pv0ggz19gmfkp18"  // interstitial_main
    const val PROD_NATIVE_AD_UNIT_ID = "2r6tdjjzxi0jq4hpd"       // native_main
    // banner_main — CREATE IN DASHBOARD (LevelPlay → Setup → Ad units → Banner) and
    // paste the ID here. Until set, bannerConfigured=false and banners collapse
    // gracefully (zero footprint).
    const val PROD_BANNER_AD_UNIT_ID = "ptz6e25xm7sud8lo"        // banner_main

    // Unity LevelPlay official test key + documented test ad units (test ads, zero revenue)
    const val TEST_APP_KEY = "25b63cf85" // Unity LevelPlay official test key
    const val TEST_REWARDED_AD_UNIT_ID = "syz3d8ekts22q0or" // Unity demo rewarded
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "h3xw38h9214adgxo" // Unity demo interstitial
    const val TEST_BANNER_AD_UNIT_ID = "4fpetq4lhe5lsw3e" // Unity test banner (paired with 25b63cf85)

    val LEVELPLAY_APP_KEY: String get() = if (USE_TEST_ADS) TEST_APP_KEY else PROD_APP_KEY
    val REWARDED_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
    val INTERSTITIAL_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    // ── Placement names (analytics + pacing in LevelPlay dashboard) ──
    const val REWARDED_PLACEMENT = "reward"
    const val INTERSTITIAL_PLACEMENT = "foreground"
    const val BANNER_PLACEMENT = "banner"

    /** True when rewarded ads can actually load. */
    val rewardedConfigured: Boolean get() = REWARDED_AD_UNIT_ID.isNotBlank()
    val interstitialConfigured: Boolean get() = INTERSTITIAL_AD_UNIT_ID.isNotBlank()
    val bannerConfigured: Boolean get() = BANNER_AD_UNIT_ID.isNotBlank()

    /** Banner ad unit — switches between test/prod. */
    val BANNER_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
}
