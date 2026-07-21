package com.ashwathai.tradelab.ui

import android.content.Context
import android.util.Log
import app.cash.turbine.test
import com.ashwathai.tradelab.data.*
import com.ashwathai.tradelab.util.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TradingViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val context = mockk<Context>(relaxed = true)
    private val repository = mockk<TradingRepository>(relaxed = true)
    private val leaderboardManager = mockk<LeaderboardManager>(relaxed = true)

    private lateinit var viewModel: TradingViewModel

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        
        // Mock repository flows
        every { repository.userProfile } returns flowOf(null)
        every { repository.holdings } returns flowOf(emptyList())
        every { repository.transactions } returns flowOf(emptyList())
        every { repository.watchlist } returns flowOf(emptyList())
        every { repository.stockPrices } returns flowOf(emptyList())

        every { leaderboardManager.getTopUsersFlow() } returns flowOf(emptyList())

        viewModel = TradingViewModel(
            context,
            repository,
            leaderboardManager,
            testCoroutineRule.testDispatcher,
            testCoroutineRule.testDispatcher
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `globalLeaderboard emits empty list by default`() = runTest {
        viewModel.globalLeaderboard.test {
            assertEquals(emptyList<LeaderboardEntry>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `globalLeaderboard catches exceptions and emits empty list`() = runTest {
        val failingManager = mockk<LeaderboardManager>(relaxed = true)
        every { failingManager.getTopUsersFlow() } returns flow {
            throw RuntimeException("Firestore Crash")
        }

        val testViewModel = TradingViewModel(
            context,
            repository,
            failingManager,
            testCoroutineRule.testDispatcher,
            testCoroutineRule.testDispatcher
        )

        testViewModel.globalLeaderboard.test {
            val result = awaitItem()
            assertEquals(0, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleWatchlistCompactMode changes state`() = runTest {
        assertEquals(false, viewModel.isWatchlistCompactMode.value)
        viewModel.toggleWatchlistCompactMode()
        assertEquals(true, viewModel.isWatchlistCompactMode.value)
        viewModel.toggleWatchlistCompactMode()
        assertEquals(false, viewModel.isWatchlistCompactMode.value)
    }
}
