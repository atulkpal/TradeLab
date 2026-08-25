package com.ashwathai.tradelab

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ashwathai.tradelab.market.MarketCalendar
import com.ashwathai.tradelab.notifications.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

/**
 * Epic 28 groundwork — notification framework:
 * market calendar correctness + channel setup + permission gating.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationFrameworkTest {

    private fun istCal(
        year: Int, month: Int, day: Int, hour: Int, minute: Int
    ): Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
        clear()
        set(year, month - 1, day, hour, minute, 0)
    }

    // ── Session boundaries ──

    @Test
    fun `session open at 9_15am on a trading day`() {
        // Wed 2026-08-19 09:15 IST (not a holiday)
        val t = istCal(2026, 8, 19, 9, 15).timeInMillis
        assertTrue(MarketCalendar.isIndianEquitySessionOpen(t))
    }

    @Test
    fun `session closed at 9_14am and at 3_30pm`() {
        val before = istCal(2026, 8, 19, 9, 14).timeInMillis
        val close = istCal(2026, 8, 19, 15, 30).timeInMillis
        assertFalse(MarketCalendar.isIndianEquitySessionOpen(before))
        assertFalse(MarketCalendar.isIndianEquitySessionOpen(close))
    }

    @Test
    fun `session closed on weekend and on a listed holiday`() {
        val saturday = istCal(2026, 8, 22, 10, 0).timeInMillis // Sat
        val holiday = istCal(2026, 10, 2, 10, 0).timeInMillis   // Gandhi Jayanti (Fri)
        assertFalse(MarketCalendar.isIndianEquitySessionOpen(saturday))
        assertFalse(MarketCalendar.isIndianEquitySessionOpen(holiday))
    }

    // ── Next-open computation ──

    @Test
    fun `next open from before 9_15 is same day`() {
        val now = istCal(2026, 8, 19, 8, 0).timeInMillis // Wed 08:00
        val expected = istCal(2026, 8, 19, 9, 15).timeInMillis
        assertEquals(expected, MarketCalendar.nextMarketOpenMillis(now))
    }

    @Test
    fun `next open from mid-session is next trading day 9_15`() {
        val now = istCal(2026, 8, 19, 12, 0).timeInMillis // Wed midday
        val expected = istCal(2026, 8, 20, 9, 15).timeInMillis // Thu
        assertEquals(expected, MarketCalendar.nextMarketOpenMillis(now))
    }

    @Test
    fun `next open from friday afternoon skips weekend to monday`() {
        val friday = istCal(2026, 8, 21, 14, 0).timeInMillis
        val monday = istCal(2026, 8, 24, 9, 15).timeInMillis
        assertEquals(monday, MarketCalendar.nextMarketOpenMillis(friday))
    }

    @Test
    fun `next open skips holidays`() {
        // 2026-10-01 (Thu) 14:00 -> 10-02 is a holiday -> next open Mon 10-05
        val thursday = istCal(2026, 10, 1, 14, 0).timeInMillis
        val monday = istCal(2026, 10, 5, 9, 15).timeInMillis
        assertEquals(monday, MarketCalendar.nextMarketOpenMillis(thursday))
    }

    // ── Channels & permission gate ──

    @Test
    fun `ensureChannels creates market and engagement channels`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NotificationHelper.ensureChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ids = Shadows.shadowOf(nm).notificationChannels.map { it.id }
        assertTrue(ids.contains(NotificationHelper.CHANNEL_MARKET))
        assertTrue(ids.contains(NotificationHelper.CHANNEL_ENGAGE))
    }

    @Test
    fun `show does not crash and respects permission gate`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Shadows.shadowOf(context as android.app.Application)
            .grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        NotificationHelper.show(
            context, "Test title", "Test body",
            NotificationHelper.CHANNEL_MARKET, notificationId = 7
        )
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertTrue(Shadows.shadowOf(nm).allNotifications.isNotEmpty())
    }

    @Test
    fun `show is a no-op without notification permission on api 33 plus`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Shadows.shadowOf(context as android.app.Application)
            .denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        NotificationHelper.show(
            context, "Blocked", "Should not post",
            NotificationHelper.CHANNEL_MARKET, notificationId = 8
        )
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertTrue(Shadows.shadowOf(nm).allNotifications.isEmpty())
    }
}
