package com.ashwathai.tradelab.ui.common

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayInitError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.LevelPlayInitListener
import com.unity3d.mediation.LevelPlayInitRequest
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener
import com.unity3d.mediation.rewarded.LevelPlayReward
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Launches a rewarded ad for [onEarned] grant; [Unit] second param reports failure. */
typealias RewardedAdLauncher = (onEarned: () -> Unit, onFailed: (String) -> Unit) -> Unit

/**
 * Unity LevelPlay ad facade (Epic 26) — replaces AdMob entirely.
 *
 * - Init in [Application.onCreate] via [init]; UI gates on [sdkReady].
 * - Rewarded keeps the legacy AdMob-era contract
 *   `loadAndShowRewardedAd(adType, onAdLoaded, onAdFailed, onUserEarnedReward)`
 *   so the 10+ reward call sites are untouched.
 * - Preloads the next rewarded after each show/closed (fixes load-and-pray).
 * - Foreground interstitial replaces the App Open format (LevelPlay has none),
 *   with the legacy 4-hour freshness window.
 *
 * Removed with AdMob (Epic 26.3): native ads return in a fast-follow once a
 * mediated network is available (LevelPlay natives require network adapters).
 */
object LevelPlayAdManager {

    private const val TAG = "LevelPlayAdManager"

    private val _sdkReady = MutableStateFlow(false)
    val sdkReady: StateFlow<Boolean> = _sdkReady.asStateFlow()

    private var appContext: Context? = null
    private var currentActivity: Activity? = null
    private var initAttempted = false

    private var rewardedAd: LevelPlayRewardedAd? = null
    private var rewardedCallbacks: Triple<
        () -> Unit,
        (String) -> Unit,
        () -> Unit
        >? = null
    private var rewardedRetryCount = 0
    private val MAX_REWARDED_RETRIES = 3
    private var lastRewardedTapAt = 0L
    private val REWARDED_COOLDOWN_MS = 60_000L // 60s cooldown between test ad requests

    private var interstitialAd: LevelPlayInterstitialAd? = null
    private var lastForegroundInterstitialAt = 0L
    private val foregroundObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            maybeShowForegroundInterstitial()
        }
    }
    private var foregroundWired = false

    // ── Init ──

    fun init(context: Context) {
        if (_sdkReady.value) return
        initAttempted = true
        appContext = context.applicationContext

        LevelPlay.init(
            context,
            LevelPlayInitRequest.Builder(AdConfig.LEVELPLAY_APP_KEY).build(),
            object : LevelPlayInitListener {
                override fun onInitSuccess(configuration: com.unity3d.mediation.LevelPlayConfiguration) {
                    Log.i(TAG, "LevelPlay initialized")
                    _sdkReady.value = true
                    preloadRewarded()
                }

                override fun onInitFailed(error: LevelPlayInitError) {
                    initAttempted = false
                    Log.e(TAG, "LevelPlay init failed: ${error.errorMessage} — will retry on next ad request")
                }
            }
        )
    }

    fun trackActivity(activity: Activity) {
        currentActivity = activity
        if (!foregroundWired) {
            foregroundWired = true
            ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
        }
    }

    // ── Rewarded (legacy-compatible contract) ──

    fun loadAndShowRewardedAd(
        adType: String,
        activity: Activity,
        onAdLoaded: () -> Unit,
        onAdFailed: (String) -> Unit,
        onUserEarnedReward: () -> Unit
    ) {
        if (!AdConfig.rewardedConfigured) {
            onAdFailed("Rewarded ads not configured yet (LevelPlay ad unit pending).")
            return
        }
        // Dev cooldown: prevent hammering the SDK during development
        val now = System.currentTimeMillis()
        if (now - lastRewardedTapAt < REWARDED_COOLDOWN_MS) {
            val wait = (REWARDED_COOLDOWN_MS - (now - lastRewardedTapAt)) / 1000
            onAdFailed("Cooldown active — try again in ${wait}s")
            return
        }
        lastRewardedTapAt = now
        // Retry init if a previous attempt failed
        if (!_sdkReady.value && appContext != null && !initAttempted) {
            init(appContext!!)
        }
        if (!_sdkReady.value) {
            onAdFailed("Ads SDK still initializing. Try again in a moment.")
            return
        }

        val placement = AdConfig.REWARDED_PLACEMENT
        Log.d(TAG, "Rewarded request: unit=${AdConfig.REWARDED_AD_UNIT_ID} placement=$placement")
        rewardedCallbacks = Triple(onAdLoaded, onAdFailed, onUserEarnedReward)

        val ad = rewardedAd ?: LevelPlayRewardedAd(AdConfig.REWARDED_AD_UNIT_ID).also {
            rewardedAd = it
            it.setListener(rewardedListener)
        }

        if (ad.isAdReady) {
            onAdLoaded()
            ad.showAd(activity, placement)
        } else {
            // Load-then-show; preloaded case flips onAdLoaded instantly via listener.
            ad.loadAd()
        }
    }

    private fun adInfoTag(adInfo: LevelPlayAdInfo): String =
        "unit=${adInfo.adUnitName}(${adInfo.adUnitId}) network=${adInfo.adNetwork}"

    private val rewardedListener = object : LevelPlayRewardedAdListener {
        override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
            Log.d(TAG, "Rewarded loaded | ${adInfoTag(adInfo)}")
            val activity = currentActivity
            val callbacks = rewardedCallbacks
            if (activity == null || callbacks == null) {
                onAdFailedInternal()
                return
            }
            callbacks.first.invoke()
            rewardedAd?.showAd(activity, AdConfig.REWARDED_PLACEMENT)
        }

        override fun onAdLoadFailed(error: LevelPlayAdError) {
            Log.e(TAG, "Rewarded load failed: ${error.errorMessage}")
            onAdFailedInternal(error.errorMessage)
        }

        override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) {
            Log.i(TAG, "Reward earned: ${reward.name} x${reward.amount} | ${adInfoTag(adInfo)}")
            rewardedRetryCount = 0 // genuine success — restore full retry budget
            rewardedCallbacks?.third?.invoke()
        }

        override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
            Log.i(TAG, "Rewarded DISPLAYED | ${adInfoTag(adInfo)} | revenue=${adInfo.revenue}")
        }

        override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
            Log.e(TAG, "Rewarded display failed: ${error.errorMessage} | ${adInfoTag(adInfo)}")
            onAdFailedInternal(error.errorMessage)
        }

        override fun onAdClosed(adInfo: LevelPlayAdInfo) {
            Log.d(TAG, "Rewarded closed | ${adInfoTag(adInfo)}")
            rewardedRetryCount = 0 // full cycle completed — restore retry budget
            preloadRewarded()
        }

        override fun onAdClicked(adInfo: LevelPlayAdInfo) {
            Log.d(TAG, "Rewarded clicked | ${adInfoTag(adInfo)}")
        }
        override fun onAdInfoChanged(adInfo: LevelPlayAdInfo) { /* waterfalls */ }
    }

    private fun onAdFailedInternal(message: String? = null) {
        rewardedCallbacks?.second?.invoke(message ?: "Ad unavailable")
        rewardedCallbacks = null
        // Retry with limit — prevents infinite loop on persistent failures
        if (rewardedRetryCount < MAX_REWARDED_RETRIES) {
            rewardedRetryCount++
            preloadRewarded()
        }
    }

    private fun preloadRewarded() {
        if (AdConfig.rewardedConfigured && _sdkReady.value) {
            // NOTE: do NOT reset rewardedRetryCount here — this is also the failure
            // retry path; resetting here defeats MAX_REWARDED_RETRIES and loops forever.
            // The counter resets only on genuine success (onAdRewarded / onAdClosed).
            val ad = rewardedAd ?: LevelPlayRewardedAd(AdConfig.REWARDED_AD_UNIT_ID).also {
                rewardedAd = it
                it.setListener(rewardedListener)
            }
            if (!ad.isAdReady) ad.loadAd()
        }
    }

    // ── Foreground interstitial (replaces App Open) ──

    private fun setupInterstitial() {
        if (!AdConfig.interstitialConfigured) return
        interstitialAd = LevelPlayInterstitialAd(AdConfig.INTERSTITIAL_AD_UNIT_ID).apply {
            setListener(object : LevelPlayInterstitialAdListener {
                override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
                    Log.d(TAG, "Interstitial loaded | ${adInfoTag(adInfo)}")
                }
                override fun onAdLoadFailed(error: LevelPlayAdError) {
                    Log.e(TAG, "Interstitial load failed: ${error.errorMessage}")
                }
                override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
                    Log.i(TAG, "Interstitial DISPLAYED | ${adInfoTag(adInfo)} | revenue=${adInfo.revenue}")
                }
                override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
                    Log.e(TAG, "Interstitial display failed: ${error.errorMessage} | ${adInfoTag(adInfo)}")
                }
                override fun onAdClicked(adInfo: LevelPlayAdInfo) {
                    Log.d(TAG, "Interstitial clicked | ${adInfoTag(adInfo)}")
                }
                override fun onAdClosed(adInfo: LevelPlayAdInfo) {
                    Log.d(TAG, "Interstitial closed | ${adInfoTag(adInfo)}")
                }
                override fun onAdInfoChanged(adInfo: LevelPlayAdInfo) { /* waterfalls */ }
            })
            loadAd()
        }
    }

    private fun maybeShowForegroundInterstitial() {
        val activity = currentActivity ?: return
        val now = System.currentTimeMillis()
        if (now - lastForegroundInterstitialAt < FOREGROUND_FRESHNESS_MS) return
        if (!AdConfig.interstitialConfigured || !_sdkReady.value) return

        val ad = interstitialAd ?: LevelPlayInterstitialAd(AdConfig.INTERSTITIAL_AD_UNIT_ID).also {
            interstitialAd = it
            setupInterstitial()
        }
        if (ad.isAdReady) {
            lastForegroundInterstitialAt = now
            ad.showAd(activity, AdConfig.INTERSTITIAL_PLACEMENT)
            ad.loadAd() // reload for next foreground
        } else {
            ad.loadAd()
        }
    }

    private const val FOREGROUND_FRESHNESS_MS = 4 * 60 * 60 * 1000L // 4h, legacy window
}
