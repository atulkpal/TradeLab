package com.ashwathai.tradelab.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ashwathai.tradelab.MainActivity
import com.ashwathai.tradelab.R

/**
 * Notification framework (Epic 28 groundwork).
 *
 * Channels:
 *  - [CHANNEL_MARKET]  Market alerts (open/close, triggers)  — HIGH importance
 *  - [CHANNEL_ENGAGE]  Re-engagement tips & invites           — DEFAULT importance
 *
 * Remote path: FCM data payloads routed by TradeLabMessagingService.
 * Local path:  MarketOpenWorker fires the daily market-open alert.
 */
object NotificationHelper {

    const val CHANNEL_MARKET = "market_alerts"
    const val CHANNEL_ENGAGE = "engagement"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MARKET,
                "Market Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Market open/close and price trigger alerts" }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ENGAGE,
                "Tips & Invites",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Learning tips, challenges and re-engagement invites" }
        )
    }

    /** Whether we may post notifications (runtime grant on API 33+). */
    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun show(
        context: Context,
        title: String,
        body: String,
        channel: String = CHANNEL_MARKET,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        ensureChannels(context)
        if (!canPost(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(
                if (channel == CHANNEL_MARKET) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
