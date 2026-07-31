package com.ashwathai.tradelab.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SubscriptionConfigTest {

    private fun istEpoch(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0
    ): Long = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
        clear()
        set(year, month, day, hour, minute, second)
    }.timeInMillis

    @Test
    fun `launch promo is active through august 31`() {
        val beforeCutoff = istEpoch(2026, Calendar.AUGUST, 31, 23, 59, 59)

        assertTrue(SubscriptionConfig.isLaunchPromoActive(beforeCutoff))
        assertEquals(SubscriptionConfig.PRODUCT_LAUNCH_49, SubscriptionConfig.activeProductId(beforeCutoff))
        assertEquals("₹49", SubscriptionConfig.displayPrice(beforeCutoff))
        assertEquals("₹49.00", SubscriptionConfig.monthlyPrice(beforeCutoff))
    }

    @Test
    fun `promo flips to regular 99 product exactly at september 1 ist`() {
        val atCutoff = istEpoch(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)

        assertFalse(SubscriptionConfig.isLaunchPromoActive(atCutoff))
        assertEquals(SubscriptionConfig.PRODUCT_REGULAR_99, SubscriptionConfig.activeProductId(atCutoff))
        assertEquals("₹99", SubscriptionConfig.displayPrice(atCutoff))
        assertEquals("₹99.00", SubscriptionConfig.monthlyPrice(atCutoff))
    }

    @Test
    fun `promo stays off after cutoff`() {
        val afterCutoff = istEpoch(2026, Calendar.SEPTEMBER, 1, 0, 0, 1)

        assertFalse(SubscriptionConfig.isLaunchPromoActive(afterCutoff))
        assertEquals(SubscriptionConfig.PRODUCT_REGULAR_99, SubscriptionConfig.activeProductId(afterCutoff))
    }

    @Test
    fun `free trial is 7 days for both products`() {
        assertEquals(7, SubscriptionConfig.FREE_TRIAL_DAYS)
    }

    @Test
    fun `countdown is positive during promo and zero after cutoff`() {
        val beforeCutoff = istEpoch(2026, Calendar.AUGUST, 30, 12, 0, 0)
        val afterCutoff = istEpoch(2026, Calendar.SEPTEMBER, 2, 0, 0, 0)

        assertTrue(SubscriptionConfig.promoEndsInMillis(beforeCutoff) > 0L)
        assertTrue(SubscriptionConfig.countdownLabel(beforeCutoff).isNotBlank())

        assertEquals(0L, SubscriptionConfig.promoEndsInMillis(afterCutoff))
        assertEquals("", SubscriptionConfig.countdownLabel(afterCutoff))
    }
}
