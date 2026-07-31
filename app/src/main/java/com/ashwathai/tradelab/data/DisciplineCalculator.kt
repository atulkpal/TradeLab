package com.ashwathai.tradelab.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class DisciplineCalculator @Inject constructor() {

    fun calculateNewScore(
        currentProfile: UserProfile,
        holdings: List<Holding>,
        lastTradeValue: Double,
        totalPortfolioValue: Double
    ): Int {
        var sizingPenalty = 0
        var diversificationBonus = 0
        var patienceBonus = 0

        // 1. Position Sizing (50% weight impact)
        if (totalPortfolioValue > 0) {
            val tradePct = (lastTradeValue / totalPortfolioValue) * 100.0
            if (tradePct > 12.0) {
                sizingPenalty = ((tradePct - 12.0) / 5.0).toInt().coerceIn(2, 10)
            } else if (tradePct in 1.0..10.0) {
                sizingPenalty = -2
            }
        }

        // 2. Diversification (20% weight impact)
        val distinctSectors = holdings.map { getIndustryForSymbol(it.symbol) }.distinct().size
        if (distinctSectors >= 5) {
            diversificationBonus = 10
        } else if (distinctSectors >= 3) {
            diversificationBonus = 5
        }

        // 3. Holding Time / Patience (30% weight impact)
        val totalShares = holdings.sumOf { it.shares + it.sharesT1 }
        val settledShares = holdings.sumOf { it.shares }
        if (totalShares > 0) {
            val settledPct = (settledShares / totalShares) * 100.0
            if (settledPct > 80.0) {
                patienceBonus = 5
            }
        }

        // 4. Time Decay: -1pt per week of inactivity after 30 days
        val decayPenalty = calculateTimeDecay(currentProfile)

        val newScore = currentProfile.disciplineScore - sizingPenalty + diversificationBonus + patienceBonus - decayPenalty
        return newScore.coerceIn(0, 100)
    }

    fun calculateTimeDecay(profile: UserProfile): Int {
        val now = System.currentTimeMillis()
        val inactiveThreshold = 30L * 24 * 60 * 60 * 1000
        val weekMs = 7L * 24 * 60 * 60 * 1000

        val lastActivity = profile.lastActiveTimestamp
        if (lastActivity <= 0L) return 0

        val elapsed = now - lastActivity
        if (elapsed <= inactiveThreshold) return 0

        val extraWeeks = ((elapsed - inactiveThreshold) / weekMs).toInt()
        return extraWeeks.coerceIn(1, 20)
    }

    fun getNextBadge(currentProfile: UserProfile, holdings: List<Holding>): String? {
        val score = currentProfile.disciplineScore
        val settledShares = holdings.sumOf { it.shares }
        val totalShares = holdings.sumOf { it.shares + it.sharesT1 }
        val settledPct = if (totalShares > 0) settledShares.toDouble() / totalShares else 0.0
        val sectors = holdings.map { getIndustryForSymbol(it.symbol) }.distinct().size

        return when {
            score < 90 && score >= 80 -> "\"Sizing Master\" at 90+ (${90 - score} pts away)"
            score < 80 && score >= 70 -> "\"Discipline Ninja\" at 80+ (${80 - score} pts away)"
            score < 70 -> "Discipline Ninja at 80 (${80 - score} pts away)"
            settledPct <= 0.9 && score >= 80 -> "\"Patience King\" when 90%+ shares settle"
            sectors < 5 && score >= 80 -> "\"Sector Explorer\" with 5+ sectors (${5 - sectors} more)"
            else -> null
        }
    }

    fun evaluateBadges(score: Int, holdings: List<Holding>): List<String> {
        val badges = mutableListOf<String>()
        
        if (score >= 90) badges.add("Sizing Master")
        if (score >= 80) badges.add("Discipline Ninja")
        
        val settledShares = holdings.sumOf { it.shares }
        val totalShares = holdings.sumOf { it.shares + it.sharesT1 }
        if (totalShares > 0 && (settledShares / totalShares) > 0.9) {
            badges.add("Patience King")
        }

        val sectors = holdings.map { getIndustryForSymbol(it.symbol) }.distinct().size
        if (sectors >= 5) badges.add("Sector Explorer")

        return badges
    }

    private val TICKER_INDUSTRY_MAP = mapOf(
        "RELIANCE" to "Energy & Petrochemicals",
        "TCS" to "IT Services",
        "INFY" to "IT Services",
        "HDFCBANK" to "Banking & Finance",
        "ICICIBANK" to "Banking & Finance",
        "SBIN" to "Banking & Finance",
        "BHARTIARTL" to "Telecommunications",
        "ITC" to "FMCG & Consumer Goods",
        "WIPRO" to "IT Services",
        "HINDUNILVR" to "FMCG & Consumer Goods",
        "TATAMOTORS" to "Automotive",
        "TATASTEEL" to "Metals & Mining",
        "AAPL" to "Technology",
        "TSLA" to "Automotive & Energy",
        "MSFT" to "Technology",
        "BTC-USD" to "Cryptocurrency",
        "ETH-USD" to "Cryptocurrency",
        "MCX_GOLD" to "Commodities",
        "MCX_CRUDE" to "Commodities"
    )

    private fun getIndustryForSymbol(symbol: String): String {
        val clean = symbol.substringBefore(".NS").substringBefore(".BO").uppercase()
        return TICKER_INDUSTRY_MAP[clean] ?: "Diversified"
    }
}
