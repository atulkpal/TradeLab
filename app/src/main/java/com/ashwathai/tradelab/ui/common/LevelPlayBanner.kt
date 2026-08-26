package com.ashwathai.tradelab.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdSize
import com.unity3d.mediation.banner.LevelPlayBannerAdView
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener

@Composable
fun LevelPlayBanner(
    modifier: Modifier = Modifier,
    placement: String = AdConfig.BANNER_PLACEMENT
) {
    if (!AdConfig.bannerConfigured) return
    val sdkReady by LevelPlayAdManager.sdkReady.collectAsStateWithLifecycle()
    if (!sdkReady) return

    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }
    var viewRef by remember { mutableStateOf<LevelPlayBannerAdView?>(null) }

    AndroidView(
        factory = {
            try {
                LevelPlayBannerAdView(
                    context,
                    AdConfig.BANNER_AD_UNIT_ID,
                    LevelPlayBannerAdView.Config.Builder()
                        .setAdSize(LevelPlayAdSize.createAdaptiveAdSize(context) ?: LevelPlayAdSize.BANNER)
                        .setPlacementName(placement)
                        .build()
                ).apply {
                    setBannerListener(object : LevelPlayBannerAdViewListener {
                        override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
                            android.util.Log.d("LevelPlayBanner", "loaded | unit=${adInfo.adUnitName} net=${adInfo.adNetwork}")
                            isLoaded = true
                        }
                        override fun onAdLoadFailed(error: LevelPlayAdError) {
                            android.util.Log.e("LevelPlayBanner", "load failed: ${error.errorMessage}")
                            isLoaded = false
                        }
                    })
                    loadAd()
                    viewRef = this
                }
            } catch (_: Throwable) {
                android.util.Log.e("LevelPlayBanner", "init failed (SDK not ready)")
                null
            } ?: android.view.View(context)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(if (isLoaded) 60.dp else 0.dp)
    )

    DisposableEffect(Unit) {
        onDispose { viewRef?.destroy() }
    }
}
