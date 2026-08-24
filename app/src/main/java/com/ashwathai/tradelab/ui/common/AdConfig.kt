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
    /**
     * TEST MODE: uses Unity's universal test app key (85460dcd) whose documented
     * test ad units serve real test ads immediately — no dashboard setup needed.
     * Flip to false + paste your production IDs (app 27b051bfd) to go live.
     */
    const val USE_TEST_ADS = true

    /** Production app key — TradeLab (grow.unity.com). */
    const val PROD_APP_KEY = "27b051bfd"
    const val PROD_REWARDED_AD_UNIT_ID = ""      // TODO: dashboard → Rewarded
    const val PROD_INTERSTITIAL_AD_UNIT_ID = ""  // TODO: dashboard → Interstitial

    // Unity test app key + documented test ad units (test ads, zero revenue)
    const val TEST_APP_KEY = "85460dcd"
    const val TEST_REWARDED_AD_UNIT_ID = "qri951hgt95e1cab"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "8iijoq7gtm5nci"

    val LEVELPLAY_APP_KEY: String get() = if (USE_TEST_ADS) TEST_APP_KEY else PROD_APP_KEY
    val REWARDED_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
    val INTERSTITIAL_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    // ── Placement names (analytics + pacing in LevelPlay dashboard) ──
    const val REWARDED_PLACEMENT = "reward"
    const val INTERSTITIAL_PLACEMENT = "foreground"

    /** True when rewarded ads can actually load. */
    val rewardedConfigured: Boolean get() = REWARDED_AD_UNIT_ID.isNotBlank()
    val interstitialConfigured: Boolean get() = INTERSTITIAL_AD_UNIT_ID.isNotBlank()
}
