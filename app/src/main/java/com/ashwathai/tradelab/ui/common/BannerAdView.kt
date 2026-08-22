package com.ashwathai.tradelab.ui.common

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ashwathai.tradelab.BuildConfig

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String? = null
) {
    val context = LocalContext.current
    val effectiveAdUnitId = adUnitId ?: getDebugBannerAdUnitId()

    AndroidView(
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = effectiveAdUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}

private fun getDebugBannerAdUnitId(): String {
    return if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/6300978111"
    } else {
        "ca-app-pub-3038055603735419/TODO_ADD_BANNER_ID"
    }
}
