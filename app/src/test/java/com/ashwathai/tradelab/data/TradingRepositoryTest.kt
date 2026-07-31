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
    private val userProfileDao = mockk<UserProfileDao>(relaxed = true)
    private val holdingDao = mockk<HoldingDao>(relaxed = true)
    private val transactionDao = mockk<TransactionDao>(relaxed = true)
    private val watchlistDao = mockk<WatchlistDao>(relaxed = true)
    private val stockPriceDao = mockk<StockPriceDao>(relaxed = true)
    private val candleEntryDao = mockk<CandleEntryDao>(relaxed = true)
    private val optionContractDao = mockk<OptionContractDao>(relaxed = true)
    private val watchlistV2Dao = mockk<WatchlistV2Dao>(relaxed = true)
    private val pendingOrderDao = mockk<PendingOrderDao>(relaxed = true)
    private val appNotificationDao = mockk<AppNotificationDao>(relaxed = true)
    private val marketNewsDao = mockk<MarketNewsDao>(relaxed = true)
    private val accountSnapshotDao = mockk<AccountSnapshotDao>(relaxed = true)
    private val ledgerDao = mockk<LedgerDao>(relaxed = true)
    private val disciplineCalculator = mockk<DisciplineCalculator>(relaxed = true)
    private lateinit var repository: TradingRepository

    @Before
    fun setup() {
        every { db.userProfileDao() } returns userProfileDao
        every { db.holdingDao() } returns holdingDao
        every { db.transactionDao() } returns transactionDao
        every { db.watchlistDao() } returns watchlistDao
        every { db.stockPriceDao() } returns stockPriceDao
        every { db.candleEntryDao() } returns candleEntryDao
        every { db.optionContractDao() } returns optionContractDao
        every { db.watchlistV2Dao() } returns watchlistV2Dao
        every { db.pendingOrderDao() } returns pendingOrderDao
        every { db.appNotificationDao() } returns appNotificationDao
        every { db.marketNewsDao() } returns marketNewsDao
        every { db.accountSnapshotDao() } returns accountSnapshotDao
        every { db.ledgerDao() } returns ledgerDao

        repository = TradingRepository(db, disciplineCalculator)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `simulateMarketTick steers price towards target`() = runTest {
        val stock = StockPrice(
            symbol = "BAD",
            companyName = "Bad Stock",
            currentPrice = 100.0,
            dailyChangePct = 0.0,
            previousClose = 100.0,
            highPrice = 100.0,
            lowPrice = 100.0,
            historyData = "100.0",
            targetPrice = 200.0
        )

        every { stockPriceDao.getAllStockPricesFlow() } returns flowOf(listOf(stock))

        val updatedSlot = slot<List<StockPrice>>()
        coEvery { stockPriceDao.insertStockPrices(capture(updatedSlot)) } returns Unit

        repository.simulateMarketTick()

        assertTrue("insertStockPrices should have been called", updatedSlot.isCaptured)
        val updated = updatedSlot.captured.first { it.symbol == "BAD" }
        // Noise is +/-0.3% (+/-0.30) while the gravity drift pulls 5% of the distance (5.0)
        // toward the anchor. The new price must land strictly between the two.
        assertTrue("Price should steer towards target 200, got ${updated.currentPrice}", updated.currentPrice > 100.0)
        assertTrue("Price should not overshoot target, got ${updated.currentPrice}", updated.currentPrice < 200.0)
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

        every { stockPriceDao.getAllStockPricesFlow() } returns flowOf(listOf(initialStock.copy(currentPrice = 3050.0)))
        every { pendingOrderDao.getPendingOrdersFlow() } returns flowOf(listOf(trailingOrder))
        coEvery { userProfileDao.getUserProfile() } returns UserProfile(id = 1, cash = 25000.0, startingCash = 25000.0)

        val capturedTrigger = slot<Double>()
        coEvery { pendingOrderDao.updateTrailingOrder(1, capture(capturedTrigger), any()) } returns Unit

        repository.simulateMarketTick()

        // Price rose to 3050. Baseline was 3000. New baseline = 3050. New trigger = 3050 - 10 = 3040.
        assertTrue("Trigger price should have moved up to 3040. Got: ${capturedTrigger.captured}", capturedTrigger.captured == 3040.0)
    }

    @Test
    fun `auto liquidation triggers when equity falls below maintenance margin`() = runTest {
        val profile = UserProfile(id = 1, cash = 1000.0, startingCash = 1000.0, currency = "USD")
        val prices = listOf(StockPrice("BAD", "Bad Stock", 60.0, 0.0, 100.0, 100.0, 100.0, "60.0"))

        // 50 shares bought at 100 in MIS (5x). Price drops to 60.
        // Used Margin = (60 * 50) / 5 = 600.
        // Unrealized PnL = (60 - 100) * 50 = -2000.
        // Account Equity = 1000 - 2000 = -1000.
        // Maintenance Threshold = 600 * 0.5 = 300.
        // -1000 < 300 -> Should liquidate.
        val holding = Holding("BAD", 50.0, 100.0, isDelivery = false)

        coEvery { userProfileDao.getUserProfile() } returns profile
        coEvery { holdingDao.getAllHoldings() } returns listOf(holding)
        every { stockPriceDao.getAllStockPricesFlow() } returns flowOf(prices.map { it.copy(currentPrice = 60.0) })
        coEvery { stockPriceDao.getStockPrice("BAD") } returns prices[0]
        coEvery { holdingDao.getHolding("BAD", false) } returns holding

        coEvery { holdingDao.deleteHolding("BAD", false) } returns Unit
        
        repository.simulateMarketTick()

        // Verify sellStock was called (liquidated)
        coVerify { transactionDao.insertTransaction(match { it.type == "SELL" && it.symbol == "BAD" }) }
    }
}
