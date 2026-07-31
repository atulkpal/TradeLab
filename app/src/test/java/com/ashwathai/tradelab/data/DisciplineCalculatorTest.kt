package com.ashwathai.tradelab.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisciplineCalculatorTest {

    private val calculator = DisciplineCalculator()
    private val defaultProfile = UserProfile(disciplineScore = 75)

    @Test
    fun `calculateNewScore penalizes oversized trades`() {
        val holdings = emptyList<Holding>()
        val totalValue = 100000.0
        val lastTradeValue = 20000.0 // 20% of capital (Threshold is 12%)
        
        val newScore = calculator.calculateNewScore(defaultProfile, holdings, lastTradeValue, totalValue)
        
        // Expected penalty: (20 - 12) / 5 = 1.6 -> 1 point (min 2 as per code logic .coerceIn(2, 10))
        // Actually (20-12)/5 = 1.6. toInt() is 1. coerceIn(2, 10) makes it 2.
        // 75 - 2 = 73
        assertEquals(73, newScore)
    }

    @Test
    fun `calculateNewScore rewards disciplined sizing`() {
        val holdings = emptyList<Holding>()
        val totalValue = 100000.0
        val lastTradeValue = 5000.0 // 5% of capital
        
        val newScore = calculator.calculateNewScore(defaultProfile, holdings, lastTradeValue, totalValue)
        
        // 75 - (-2) = 77
        assertEquals(77, newScore)
    }

    @Test
    fun `calculateNewScore rewards diversification`() {
        val holdings = listOf(
            Holding("RELIANCE", 10.0, 2500.0), // Energy
            Holding("TCS", 5.0, 3500.0),      // IT
            Holding("HDFCBANK", 20.0, 1500.0) // Banking
        )
        val totalValue = 100000.0
        
        val newScore = calculator.calculateNewScore(defaultProfile, holdings, 0.0, totalValue)
        
        // 75 + 5 (diversification) + 5 (patience, all shares settled) = 85
        assertEquals(85, newScore)
    }

    @Test
    fun `evaluateBadges awards Sizing Master for high scores`() {
        val highProfile = UserProfile(disciplineScore = 95)
        val badges = calculator.evaluateBadges(highProfile.disciplineScore, emptyList())
        
        assertTrue(badges.contains("Sizing Master"))
    }

    @Test
    fun `evaluateBadges awards Patience King for settled holdings`() {
        val holdings = listOf(
            Holding("RELIANCE", 10.0, 2500.0, sharesT1 = 0.0) // 100% settled
        )
        val badges = calculator.evaluateBadges(75, holdings)
        
        assertTrue(badges.contains("Patience King"))
    }
}
