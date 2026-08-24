package com.ashwathai.tradelab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ashwathai.tradelab.ui.Lecture
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.QuizQuestion
import com.ashwathai.tradelab.ui.ChapterModule
import com.ashwathai.tradelab.ui.academy.LectureScreen
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Epic 25.3 â€” full-screen lecture destination: render states, knowledge-check
 * flow, and standard reward claim.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LectureScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var standardClaimed = false

    @Before
    fun reset() {
        standardClaimed = false
    }

    private fun quiz() = ChapterModule(
        id = 101,
        courseId = 1,
        title = "Lecture 1.1.1: Fractional Ownership",
        topic = "Market Basics",
        rewardAmt = 1000.0,
        concept = "Owning a slice of a business.",
        lectures = listOf(
            Lecture(
                title = "Lecture 1.1.1: Fractional Ownership Explained",
                content = "When you buy a stock, you are buying a tiny slice of a real business.",
                videoUrl = "" // no video â†’ "coming soon" empty state, no network
            )
        ),
        quizzes = listOf(
            QuizQuestion(
                question = "Do stocks represent ownership?",
                options = listOf("Yes", "No"),
                correctIndex = 0
            )
        )
    )

    private fun setContent(
        locked: Boolean = false,
        alreadyCompleted: Boolean = false
    ) {
        composeRule.setContent {
            LectureScreen(
                quiz = quiz(),
                isChapterLocked = locked,
                isAlreadyCompleted = alreadyCompleted,
                stats = PortfolioStats(),
                onDismiss = {},
                onCompleteStandard = { standardClaimed = true },
                onCompleteDouble = {},
                onFeedback = {},
                onLaunchDoubleRewardAd = { _, _, _ -> }
            )
        }
    }

    @Test
    fun `renders lecture content with video-coming-soon state`() {
        setContent()
        composeRule.onNodeWithText("Lecture 1.1.1: Fractional Ownership Explained").assertIsDisplayed()
        composeRule.onNodeWithText("Video lecture coming soon for this lesson.").assertIsDisplayed()
        composeRule.onNodeWithTag("start_knowledge_check").assertIsDisplayed()
    }

    @Test
    fun `locked chapter shows locked assessment`() {
        setContent(locked = true)
        composeRule.onNodeWithText("Assessment Locked").assertIsDisplayed()
    }

    @Test
    fun `knowledge check flow completes and claims standard reward`() {
        setContent()
        composeRule.onNodeWithTag("start_knowledge_check").performScrollTo().performClick()
        composeRule.onNodeWithText("Do stocks represent ownership?").assertIsDisplayed()
        composeRule.onNodeWithText("Yes").performScrollTo().performClick()
        composeRule.onNodeWithText("Check Answer").performScrollTo().performClick()
        composeRule.onNodeWithText("CORRECT!").assertIsDisplayed()
        composeRule.onNodeWithText("See My Results").performScrollTo().performClick()
        composeRule.onNodeWithText("Your Score: 1 / 1").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Claim Standard", substring = true).performScrollTo().performClick()
        assertTrue("onCompleteStandard should fire on claim", standardClaimed)
    }

    @Test
    fun `already completed chapter shows close instead of claim`() {
        setContent(alreadyCompleted = true)
        composeRule.onNodeWithTag("start_knowledge_check").performScrollTo().performClick()
        composeRule.onNodeWithText("Yes").performScrollTo().performClick()
        composeRule.onNodeWithText("Check Answer").performScrollTo().performClick()
        composeRule.onNodeWithText("See My Results").performScrollTo().performClick()
        composeRule.onNodeWithText("Close Quiz").performScrollTo().assertIsDisplayed()
    }
}
