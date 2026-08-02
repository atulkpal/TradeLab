package com.ashwathai.tradelab.ui.academy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.ashwathai.tradelab.ui.AcademyCourse
import com.ashwathai.tradelab.ui.ChapterModule
import com.ashwathai.tradelab.ui.Lecture
import com.ashwathai.tradelab.ui.QuizQuestion
import com.ashwathai.tradelab.ui.common.DisablePremiumMotion
import com.ashwathai.tradelab.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AcademyScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun chapter(id: Int, completed: Boolean) = ChapterModule(
        id = id,
        courseId = id / 100,
        title = if (completed) "What is a Stock?" else "Chapter $id",
        topic = if (completed) "Equities" else "Topic",
        rewardAmt = 500.0,
        concept = if (completed) "A stock represents fractional ownership in a company." else "Concept for $id.",
        lectures = listOf(Lecture("L1", "Content")),
        quizzes = listOf(QuizQuestion("Q1", listOf("A", "B"), 0))
    )

    @Test
    fun academy_deck_collapsed_screenshot() {
        val courses = listOf(
            AcademyCourse(
                id = 1, title = "Stock Market Basics", tagline = "Foundations of the Indian markets.",
                iconEmoji = "📈", tier = "BEGINNER", order = 1,
                chapters = listOf(chapter(101, completed = true))
            ),
            AcademyCourse(
                id = 2, title = "Risk Management & Trading Psychology", tagline = "How orders really fill and traders stay disciplined.",
                iconEmoji = "🧠", tier = "INTERMEDIATE", order = 2,
                chapters = listOf(chapter(201, completed = false))
            )
        )
        composeTestRule.setContent {
            MyApplicationTheme {
                DisablePremiumMotion {
                    Column(modifier = Modifier.padding(20.dp)) {
                        CourseDeck(
                            courses = courses,
                            completedSet = setOf("101"),
                            currency = "₹",
                            onOpenChapter = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/academy_deck_collapsed.png")
    }

    @Test
    fun academy_deck_expanded_screenshot() {
        val courses = listOf(
            AcademyCourse(
                id = 1, title = "Stock Market Basics", tagline = "Foundations of the Indian markets.",
                iconEmoji = "📈", tier = "BEGINNER", order = 1,
                chapters = listOf(chapter(101, completed = true))
            )
        )
        composeTestRule.setContent {
            MyApplicationTheme {
                DisablePremiumMotion {
                    Column(modifier = Modifier.padding(20.dp)) {
                        CourseDeck(
                            courses = courses,
                            completedSet = setOf("101"),
                            currency = "₹",
                            onOpenChapter = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("academy_course_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/academy_deck_expanded.png")
    }
}
