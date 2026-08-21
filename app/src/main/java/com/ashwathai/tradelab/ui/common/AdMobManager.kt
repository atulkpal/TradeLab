package com.ashwathai.tradelab.ui.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ashwathai.tradelab.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.nativead.NativeAdOptions

/**
 * Manages App Open Ads to show an ad when the user returns to the app.
 */
class AppOpenAdManager(private val application: Application) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null

    private var nativeAd: NativeAd? = null
    private var isLoadingNativeAd = false

    private val appOpenUnitId: String
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395912" 
                else "ca-app-pub-3038055603735419/7727238314"

    private val nativeUnitId: String
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110"
                else "ca-app-pub-3038055603735419/1301430769"

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /** Request an App Open ad. */
    fun fetchAd() {
        if (isAdAvailable()) return

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            appOpenUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    /** Request a Native ad. */
    fun fetchNativeAd(onAdLoaded: (NativeAd) -> Unit = {}) {
        if (nativeAd != null || isLoadingNativeAd) return

        isLoadingNativeAd = true
        val adLoader = AdLoader.Builder(application, nativeUnitId)
            .forNativeAd { ad ->
                nativeAd = ad
                isLoadingNativeAd = false
                onAdLoaded(ad)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoadingNativeAd = false
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun getNativeAd(): NativeAd? = nativeAd

    fun clearNativeAd() {
        nativeAd?.destroy()
        nativeAd = null
    }

    /** Shows the ad if one isn't already showing. */
    fun showAdIfAvailable(activity: Activity) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) return

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            fetchAd()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Set the ad reference to null so that this ad won't be shown again.
                appOpenAd = null
                isShowingAd = false
                fetchAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                fetchAd()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
        isShowingAd = true
        appOpenAd?.show(activity)
    }

    /** Check if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            // Only show ad if it's not the MainActivity during fresh launch (handled by OS/splash)
            // or if we want it every time they return.
            showAdIfAvailable(it)
        }
    }

    // ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) {
            currentActivity = activity
        }
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }
}
