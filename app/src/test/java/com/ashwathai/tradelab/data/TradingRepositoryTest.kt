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
        // ... existing test code ...
    }

    @Test
    fun `trailing stop loss moves up with price for sell order`() = runTest {
        val initialStock = StockPrice("RELIANCE", "Reliance", 3000.0, 0.0, 3000.0, 3000.0, 3000.0, "3000.0")
        val trailingOrder = PendingOrder(
            id = 1,
            symbol = "RELIANCE",
            type = "SELL",
            orderType = "Stop-Loss",
            shares = 10.0,
            triggerPrice = 2990.0,
            isTrailing = true,
            trailingGap = 10.0,
            trailingBaselinePrice = 3000.0
        )

        every { db.stockPriceDao().getAllStockPricesFlow() } returns flowOf(listOf(initialStock.copy(currentPrice = 3050.0)))
        every { db.pendingOrderDao().getPendingOrdersFlow() } returns flowOf(listOf(trailingOrder))

        val capturedTrigger = slot<Double>()
        coEvery { db.pendingOrderDao().updateTrailingOrder(1, capture(capturedTrigger), any()) } returns Unit

        repository.simulateMarketTick()

        // Price rose to 3050. Baseline was 3000. New baseline = 3050. New trigger = 3050 - 10 = 3040.
        assertTrue("Trigger price should have moved up to 3040. Got: ${capturedTrigger.captured}", capturedTrigger.captured == 3040.0)
    }

    @Test
    fun `auto liquidation triggers when equity falls below maintenance margin`() = runTest {
        val profile = UserProfile(id = 1, cash = 1000.0, startingCash = 1000.0)
        val prices = listOf(StockPrice("BAD", "Bad Stock", 100.0, 0.0, 100.0, 100.0, 100.0, "100.0"))
        
        // 50 shares at 100. Value = 5000. MIS Margin used (5x) = 1000.
        // Account Equity = 1000 (cash) + 0 (PnL) = 1000.
        // Maintenance Margin = 1000 * 0.5 = 500.
        val holding = Holding("BAD", 50.0, 100.0, isDelivery = false)

        coEvery { db.userProfileDao().getUserProfile() } returns profile
        coEvery { db.holdingDao().getAllHoldings() } returns listOf(holding)
        every { db.stockPriceDao().getAllStockPricesFlow() } returns flowOf(prices.map { it.copy(currentPrice = 85.0) })

        // Price dropped to 85. PnL = (85 - 100) * 50 = -750.
        // Account Equity = 1000 - 750 = 250.
        // Used Margin at 85 = (85 * 50) / 5 = 850.
        // Maintenance Threshold = 850 * 0.5 = 425.
        // 250 < 425 -> Should liquidate.

        coEvery { db.holdingDao().deleteHolding("BAD", false) } returns Unit
        
        repository.simulateMarketTick()

        // Verify sellStock was called (liquidated)
        coVerify { db.transactionDao().insertTransaction(match { it.type == "SELL" && it.symbol == "BAD" }) }
    }
}
