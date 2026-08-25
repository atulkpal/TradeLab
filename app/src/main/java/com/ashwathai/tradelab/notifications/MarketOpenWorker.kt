package com.ashwathai.tradelab.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ashwathai.tradelab.market.MarketCalendar
import java.util.concurrent.TimeUnit

/**
 * Fires the daily "Markets are LIVE" local notification at 09:15 IST on
 * trading days (weekend + NSE/BSE holiday aware), then schedules the next run.
 *
 * Self-rescheduling OneTime chain — no periodic drift, survives process death
 * via WorkManager. Started once from Application (Background Tasks Rule:
 * never in a ViewModel init).
 */
class MarketOpenWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        if (MarketCalendar.isIndianEquitySessionOpen(now)) {
            NotificationHelper.show(
                applicationContext,
                title = "Markets are LIVE 🔔",
                body = "NSE & BSE are open till 3:30 PM IST. Your virtual capital is waiting — make today's move!",
                channel = NotificationHelper.CHANNEL_MARKET,
                notificationId = ID_MARKET_OPEN
            )
        }
        scheduleNext(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK = "market_open_notification"
        const val ID_MARKET_OPEN = 42001

        /** Enqueue (or replace) the next market-open run. Idempotent. */
        fun scheduleNext(context: Context, nowMillis: Long = System.currentTimeMillis()) {
            val delayMs = (MarketCalendar.nextMarketOpenMillis(nowMillis) - nowMillis)
                .coerceAtLeast(0L)
            val request = OneTimeWorkRequestBuilder<MarketOpenWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag("market_open")
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
