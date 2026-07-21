package com.ashwathai.tradelab

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ashwathai.tradelab.data.AppDatabase
import com.ashwathai.tradelab.data.LeaderboardManager
import com.ashwathai.tradelab.data.TradingRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = TradeLabApplication::class)
class MainActivityLaunchTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Before
    fun setup() {
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.getInstance() } returns mockk(relaxed = true)
        
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

        mockkObject(AppDatabase.Companion)
        every { AppDatabase.getDatabase(any()) } returns mockk(relaxed = true)

        mockkConstructor(com.ashwathai.tradelab.ui.TradingViewModel::class)
        every { anyConstructed<com.ashwathai.tradelab.ui.TradingViewModel>().startBackgroundTasks() } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testLaunchAndNoCrash() {
        // This test might still fail if Hilt isn't fully set up for Robolectric.
        // If it does, we will move to HiltTestRule.
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.waitForIdle()
        }
    }
}
