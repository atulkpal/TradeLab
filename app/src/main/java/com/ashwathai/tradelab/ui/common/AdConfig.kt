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
    /** LevelPlay app key — TradeLab (grow.unity.com). */
    const val LEVELPLAY_APP_KEY = "27b051bfd"

    // ── Ad unit IDs (from LevelPlay dashboard) ──
    // TODO(Epic 26): create in dashboard → paste IDs. Empty = not configured.
    const val REWARDED_AD_UNIT_ID = ""
    const val INTERSTITIAL_AD_UNIT_ID = ""

    // ── Placement names (analytics + pacing in LevelPlay dashboard) ──
    const val REWARDED_PLACEMENT = "reward"
    const val INTERSTITIAL_PLACEMENT = "foreground"

    /** True when rewarded ads can actually load. */
    val rewardedConfigured: Boolean get() = REWARDED_AD_UNIT_ID.isNotBlank()
    val interstitialConfigured: Boolean get() = INTERSTITIAL_AD_UNIT_ID.isNotBlank()
}
