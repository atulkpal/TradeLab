package com.ashwathai.tradelab.billing

import java.util.Calendar
import java.util.TimeZone

/**
 * Central configuration for TradeLab subscriptions.
 *
 * Early-launch offer: ₹99 is the list price, but everyone who subscribes through
 * 2026-08-31 gets a 7-day free trial + ₹49/mo (50% OFF). At 2026-09-01 00:00 IST
 * the app automatically switches to offering the ₹99 product for new purchases.
 *
 * Lock-in is handled by Play Billing's per-product pricing: a subscriber on
 * [PRODUCT_LAUNCH_49] keeps renewing at ₹49, and a buyer of [PRODUCT_REGULAR_99]
 * pays ₹99 — the app only picks which product to offer for NEW purchases.
 */
object SubscriptionConfig {

    // Google Play Console product IDs
    const val PRODUCT_LAUNCH_49 = "tradelab_subs"
    const val PRODUCT_REGULAR_99 = "trade_lab_subs_99"

    // Pricing (₹)
    const val LAUNCH_PRICE_INR = 49
    const val REGULAR_PRICE_INR = 99
    const val FREE_TRIAL_DAYS = 7

    // Early-bird offer valid through 2026-08-31; ₹99 pricing begins 2026-09-01 00:00 IST.
    val PROMO_END_UTC_MS: Long = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
        clear()
        set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
    }.timeInMillis

    fun isLaunchPromoActive(now: Long = System.currentTimeMillis()): Boolean =
        now < PROMO_END_UTC_MS

    fun activeProductId(now: Long = System.currentTimeMillis()): String =
        if (isLaunchPromoActive(now)) PRODUCT_LAUNCH_49 else PRODUCT_REGULAR_99

    fun displayPrice(now: Long = System.currentTimeMillis()): String =
        if (isLaunchPromoActive(now)) "₹$LAUNCH_PRICE_INR" else "₹$REGULAR_PRICE_INR"

    fun monthlyPrice(now: Long = System.currentTimeMillis()): String =
        if (isLaunchPromoActive(now)) "₹$LAUNCH_PRICE_INR.00" else "₹$REGULAR_PRICE_INR.00"

    fun promoEndsInMillis(now: Long = System.currentTimeMillis()): Long =
        (PROMO_END_UTC_MS - now).coerceAtLeast(0L)

    fun countdownLabel(now: Long = System.currentTimeMillis()): String {
        val remainingSeconds = promoEndsInMillis(now) / 1000L
        if (remainingSeconds <= 0L) return ""
        val days = remainingSeconds / 86400L
        val hours = (remainingSeconds % 86400L) / 3600L
        val minutes = (remainingSeconds % 3600L) / 60L
        val seconds = remainingSeconds % 60L
        return buildString {
            if (days > 0) append(days).append("d ")
            append(hours.toString().padStart(2, '0')).append("h ")
            append(minutes.toString().padStart(2, '0')).append("m ")
            append(seconds.toString().padStart(2, '0')).append("s")
        }
    }
}
