package com.ashwathai.tradelab.ui.common

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ashwathai.tradelab.R
import com.ashwathai.tradelab.ui.theme.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdRow(nativeAd: NativeAd) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorderElevated, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        AndroidView(
            factory = { context ->
                val adView = LayoutInflater.from(context).inflate(R.layout.ad_unified_row, null) as NativeAdView
                
                val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                headlineView.text = nativeAd.headline
                adView.headlineView = headlineView

                val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                bodyView.text = nativeAd.body
                adView.bodyView = bodyView

                val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
                callToActionView.text = nativeAd.callToAction
                adView.callToActionView = callToActionView

                val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
                if (nativeAd.icon != null) {
                    iconView.setImageDrawable(nativeAd.icon?.drawable)
                }
                adView.iconView = iconView

                adView.setNativeAd(nativeAd)
                adView
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
