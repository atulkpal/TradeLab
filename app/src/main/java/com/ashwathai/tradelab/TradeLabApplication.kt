package com.ashwathai.tradelab

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TradeLabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase Analytics
        FirebaseAnalytics.getInstance(this)
    }
}
