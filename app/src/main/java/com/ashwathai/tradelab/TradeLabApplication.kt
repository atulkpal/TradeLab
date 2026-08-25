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
        
        // Initialize Unity LevelPlay (Epic 26) - replaces AdMob
        com.ashwathai.tradelab.ui.common.LevelPlayAdManager.init(this)

        // Initialize Firebase Components
        try {
            // Ensure App Check is initialized for Play Integrity in production
            com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            
            // Explicitly enable Crashlytics collection to ensure reports are sent from release builds
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            
            // Initialize Firebase Analytics
            com.google.firebase.analytics.FirebaseAnalytics.getInstance(this)
            
            android.util.Log.d("TradeLabApp", "Firebase components initialized successfully.")
        } catch (e: Exception) {
            // Log to logcat as a last resort
            android.util.Log.e("TradeLabApp", "Firebase initialization error: ${e.message}", e)
        }

        // Notification framework (Epic 28 groundwork): channels + market-open scheduler.
        // WorkManager chain survives process death; guarded for test environments.
        try {
            com.ashwathai.tradelab.notifications.NotificationHelper.ensureChannels(this)
            com.ashwathai.tradelab.notifications.MarketOpenWorker.scheduleNext(this)
            com.ashwathai.tradelab.service.TradeLabMessagingService.syncCachedToken(this)
        } catch (e: Exception) {
            android.util.Log.w("TradeLabApp", "Notification framework init skipped: ${e.message}")
        }

        // Setup Global Crash Logger for "blind" debugging on user devices
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val prefs = getSharedPreferences("tradelab_diagnostics", android.content.Context.MODE_PRIVATE)
                val sw = java.io.StringWriter()
                val pw = java.io.PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()
                
                prefs.edit().putString("last_crash_trace", stackTrace).apply()
                android.util.Log.e("TradeLabApp", "FATAL CRASH CAPTURED: $stackTrace")
            } catch (e: Exception) {
                // Ignore errors during crash logging
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
