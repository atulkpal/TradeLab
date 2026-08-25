package com.ashwathai.tradelab.service

import android.content.SharedPreferences
import com.ashwathai.tradelab.notifications.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM entry point (Epic 28 groundwork).
 *
 * Remote contracts (data payload — sent by the future Firestore/Cloud orchestration):
 *   title   (String, required)
 *   body    (String, required)
 *   channel (String, optional) "market_alerts" (default) | "engagement"
 *
 * Token registration: persisted locally + best-effort mirrored to
 * Firestore `fcm_tokens/{token}` so the orchestration layer can target
 * devices once its rules are opened. Failure to sync is non-fatal.
 */
class TradeLabMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        prefs().edit()
            .putString(KEY_TOKEN, token)
            .remove(KEY_SYNCED_TOKEN) // force the change-guard to push this new token
            .remove(KEY_SYNCED_UID)
            .apply()
        syncCachedToken(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val title = data["title"]
            ?: remoteMessage.notification?.title
            ?: "TradeLab"
        val body = data["body"]
            ?: remoteMessage.notification?.body
            ?: ""
        val channel = if (data["channel"] == NotificationHelper.CHANNEL_ENGAGE) {
            NotificationHelper.CHANNEL_ENGAGE
        } else {
            NotificationHelper.CHANNEL_MARKET
        }
        if (body.isNotBlank()) {
            NotificationHelper.show(this, title, body, channel)
        }
    }

    private fun prefs(): SharedPreferences =
        getSharedPreferences(PREFS, MODE_PRIVATE)

    companion object {
        private const val PREFS = "notifications_prefs"
        private const val KEY_TOKEN = "fcm_token"
        private const val KEY_SYNCED_TOKEN = "fcm_synced_token"
        private const val KEY_SYNCED_UID = "fcm_synced_uid"

        /** Locally cached FCM token (may be null until FCM issues/refreshes one). */
        fun cachedToken(context: android.content.Context): String? =
            context.getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_TOKEN, null)

        /**
         * Firestore cost guard: push ONLY when the token or signed-in uid changed
         * since the last successful sync. One write per token/uid lifetime —
         * zero Firestore traffic on normal app starts. Rules-denied or offline
         * attempts are retried on a future start (denied ops are not billed).
         */
        fun syncCachedToken(context: android.content.Context) {
            val prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE)
            val token = prefs.getString(KEY_TOKEN, null) ?: return
            val uid = try {
                FirebaseAuth.getInstance().currentUser?.uid
            } catch (_: Exception) {
                null
            }
            val syncedToken = prefs.getString(KEY_SYNCED_TOKEN, null)
            val syncedUid = prefs.getString(KEY_SYNCED_UID, null)
            if (token == syncedToken && uid == syncedUid) return // nothing changed — skip

            pushTokenToFirestore(token, uid) { accepted ->
                if (accepted) {
                    prefs.edit()
                        .putString(KEY_SYNCED_TOKEN, token)
                        .putString(KEY_SYNCED_UID, uid)
                        .apply()
                }
            }
        }

        /** Confirms via Task result (rules denial fails the Task asynchronously). */
        private fun pushTokenToFirestore(token: String, uid: String?, onResult: (Boolean) -> Unit) {
            try {
                FirebaseFirestore.getInstance().collection("fcm_tokens").document(token).set(
                    mapOf(
                        "token" to token,
                        "uid" to uid,
                        "platform" to "android",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }
}
