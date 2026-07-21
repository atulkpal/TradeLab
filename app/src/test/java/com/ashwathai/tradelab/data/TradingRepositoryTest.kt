package com.ashwathai.tradelab.data

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TradingRepositoryTest {

    private val db = mockk<AppDatabase>(relaxed = true)
    private val stockPriceDao = mockk<StockPriceDao>(relaxed = true)
    private lateinit var repository: TradingRepository

    @Before
    fun setup() {
        every { db.stockPriceDao() } returns stockPriceDao
        repository = TradingRepository(db)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `simulateMarketTick steers price towards target`() = runTest {
        // Start: 100.0, Target: 110.0
        val initialStock = StockPrice(
            symbol = "TEST",
            companyName = "Test Co",
            currentPrice = 100.0,
            dailyChangePct = 0.0,
            previousClose = 100.0,
            highPrice = 100.0,
            lowPrice = 100.0,
            historyData = "100.0",
            targetPrice = 110.0
        )

        every { stockPriceDao.getAllStockPricesFlow() } returns flowOf(listOf(initialStock))
        
        val capturedPrices = mutableListOf<List<StockPrice>>()
        coEvery { stockPriceDao.insertStockPrices(capture(capturedPrices)) } returns Unit

        repository.simulateMarketTick()

        val updatedPrice = capturedPrices.first()[0].currentPrice
        
        // Math: drift = (110 - 100) * 0.05 = 0.5
        // Noise: max +/- 0.4
        // Predicted newPrice range: [100 + 0.5 - 0.4, 100 + 0.5 + 0.4] -> [100.1, 100.9]
        
        assertTrue("Price should drift up. Current: $updatedPrice", updatedPrice > 100.0)
        assertTrue("Price should be within drift+noise range. Current: $updatedPrice", updatedPrice <= 100.9)
    }
}
