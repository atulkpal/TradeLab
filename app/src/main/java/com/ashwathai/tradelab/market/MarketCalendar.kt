package com.ashwathai.tradelab.market

import java.util.Calendar
import java.util.TimeZone

/**
 * Single source of truth for Indian market calendar + session hours.
 *
 * Used by TradingRepository (trade gating) and the notification scheduler
 * (market-open alerts). Keep the holiday list in sync when NSE/BSE publish
 * new-year lists.
 */
object MarketCalendar {

    private val IST: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    // Indian Market Holidays (NSE/BSE). Format: "YYYY-MM-DD"
    val HOLIDAYS: Set<String> = setOf(
        "2026-01-26", "2026-03-06", "2026-03-27", "2026-04-14", "2026-05-01",
        "2026-05-22", "2026-08-15", "2026-10-02", "2026-10-21", "2026-11-12",
        "2026-12-25",
        "2027-01-26", "2027-03-22", "2027-03-26", "2027-04-01", "2027-04-14",
        "2027-05-01", "2027-08-15", "2027-10-02", "2027-10-09", "2027-11-01",
        "2027-12-25"
    )

    const val OPEN_HOUR = 9
    const val OPEN_MINUTE = 15
    const val CLOSE_HOUR = 15
    const val CLOSE_MINUTE = 30

    fun istCalendar(nowMillis: Long = System.currentTimeMillis()): Calendar =
        Calendar.getInstance(IST).apply { timeInMillis = nowMillis }

    fun dateKey(calendar: Calendar): String = String.format(
        "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun isHoliday(calendar: Calendar): Boolean = HOLIDAYS.contains(dateKey(calendar))

    fun isWeekend(calendar: Calendar): Boolean {
        val dow = calendar.get(Calendar.DAY_OF_WEEK)
        return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
    }

    /** Indian equity session: Mon-Fri, non-holiday, 09:15-15:30 IST. */
    fun isIndianEquitySessionOpen(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val cal = istCalendar(nowMillis)
        if (isWeekend(cal) || isHoliday(cal)) return false
        val minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val open = OPEN_HOUR * 60 + OPEN_MINUTE
        val close = CLOSE_HOUR * 60 + CLOSE_MINUTE
        return minutes in open until close
    }

    /**
     * Next market-open moment (09:15 IST on a weekday that is not a holiday),
     * strictly after [nowMillis]. Returns epoch millis.
     */
    fun nextMarketOpenMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = istCalendar(nowMillis)
        cal.set(Calendar.HOUR_OF_DAY, OPEN_HOUR)
        cal.set(Calendar.MINUTE, OPEN_MINUTE)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Today's 09:15 counts only if still in the future and a trading day
        if (cal.timeInMillis > nowMillis && !isWeekend(cal) && !isHoliday(cal)) {
            return cal.timeInMillis
        }

        var next = cal.clone() as Calendar
        var guard = 0
        while (guard++ < 400) {
            next.add(Calendar.DAY_OF_MONTH, 1)
            if (!isWeekend(next) && !isHoliday(next)) {
                return next.timeInMillis
            }
        }
        return next.timeInMillis // unreachable in practice
    }
}
