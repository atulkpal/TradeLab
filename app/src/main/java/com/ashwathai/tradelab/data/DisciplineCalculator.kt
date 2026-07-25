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
        // If a single trade exceeds 12% of total capital, penalize
        if (totalPortfolioValue > 0) {
            val tradePct = (lastTradeValue / totalPortfolioValue) * 100.0
            if (tradePct > 12.0) {
                // Deduct 2 to 10 points depending on how much they over-sized
                sizingPenalty = ((tradePct - 12.0) / 5.0).toInt().coerceIn(2, 10)
            } else if (tradePct in 1.0..10.0) {
                // Reward for disciplined sizing
                sizingPenalty = -2 // Negative penalty is a reward
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

        val newScore = currentProfile.disciplineScore - sizingPenalty + diversificationBonus + patienceBonus
        return newScore.coerceIn(0, 100)
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

    // Duplicate mapping from Repository to keep logic contained, or we should move it to a shared Utility
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
