package com.ashwathai.tradelab

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TradeLabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase only if not in a test environment (where google-services.json might be missing/unprocessed)
        // or ensure it's initialized before usage.
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isNotEmpty()) {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                // Initialize Firebase Analytics
                FirebaseAnalytics.getInstance(this)
            }
        } catch (e: Exception) {
            // Log or ignore for tests
            android.util.Log.w("TradeLabApp", "Firebase initialization skipped or failed: ${e.message}")
        }
    }
}
