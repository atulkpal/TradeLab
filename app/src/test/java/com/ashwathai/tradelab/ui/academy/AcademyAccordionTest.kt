package com.ashwathai.tradelab.ui.academy

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ashwathai.tradelab.ui.AcademyCourse
import com.ashwathai.tradelab.ui.ChapterModule
import com.ashwathai.tradelab.ui.Lecture
import com.ashwathai.tradelab.ui.QuizQuestion
import com.ashwathai.tradelab.ui.common.DisablePremiumMotion
import com.ashwathai.tradelab.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class AcademyAccordionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun chapter(id: Int) = ChapterModule(
        id = id,
        courseId = id / 100,
        title = "Chapter $id",
        topic = "Topic",
        rewardAmt = 500.0,
        concept = "Concept for $id.",
        lectures = listOf(Lecture("L1", "Content")),
        quizzes = listOf(QuizQuestion("Q1", listOf("A", "B"), 0))
    )

    private fun course(id: Int, order: Int, chapterIds: List<Int>) = AcademyCourse(
        id = id,
        title = "Course $id",
        tagline = "Tagline $id",
        iconEmoji = "📘",
        tier = "BEGINNER",
        order = order,
        chapters = chapterIds.map { chapter(it) }
    )

    private fun setDeck(
        courses: List<AcademyCourse>,
        completedSet: Set<String> = emptySet()
    ) {
        composeTestRule.setContent {
            MyApplicationTheme {
                DisablePremiumMotion {
                    androidx.compose.foundation.layout.Column {
                        CourseDeck(
                            courses = courses,
                            completedSet = completedSet,
                            currency = "₹",
                            onOpenChapter = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `course headers render for every course`() {
        val courses = listOf(course(1, 1, listOf(101)), course(2, 2, listOf(201)))
        setDeck(courses)

        composeTestRule.onNodeWithTag("academy_course_1").assertExists()
        composeTestRule.onNodeWithTag("academy_course_2").assertExists()
    }

    @Test
    fun `chapters are hidden until the course header is expanded`() {
        val courses = listOf(course(1, 1, listOf(101)), course(2, 2, listOf(201)))
        setDeck(courses)

        composeTestRule.onNodeWithTag("academy_module_101").assertDoesNotExist()

        composeTestRule.onNodeWithTag("academy_course_1").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("academy_module_101").assertExists()
    }

    @Test
    fun `tapping an open header collapses the course`() {
        val courses = listOf(course(1, 1, listOf(101)))
        setDeck(courses)

        composeTestRule.onNodeWithTag("academy_course_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("academy_module_101").assertExists()

        composeTestRule.onNodeWithTag("academy_course_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("academy_module_101").assertDoesNotExist()
    }

    @Test
    fun `expanding a second course collapses the first single-open accordion`() {
        val courses = listOf(course(1, 1, listOf(101)), course(2, 2, listOf(201)))
        setDeck(courses, completedSet = setOf("101"))

        composeTestRule.onNodeWithTag("academy_course_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("academy_module_101").assertExists()

        composeTestRule.onNodeWithTag("academy_course_2").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("academy_module_101").assertDoesNotExist()
        composeTestRule.onNodeWithTag("academy_module_201").assertExists()
    }

    @Test
    fun `locked course expands to reveal locked preview chapters`() {
        val courses = listOf(course(1, 1, listOf(101)), course(2, 2, listOf(201)))
        setDeck(courses)

        composeTestRule.onNodeWithText("Preview — complete the previous course to earn rewards").assertExists()
        composeTestRule.onNodeWithTag("academy_module_201").assertDoesNotExist()

        composeTestRule.onNodeWithTag("academy_course_2").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("academy_module_201").assertExists()
        composeTestRule.onNodeWithText("Preview").assertExists()
        composeTestRule.onNodeWithText("+₹5,000").assertDoesNotExist()
    }

    @Test
    fun `completed previous course unlocks the next one`() {
        val courses = listOf(course(1, 1, listOf(101, 102)), course(2, 2, listOf(201)))
        setDeck(courses, completedSet = setOf("101", "102"))

        composeTestRule.onNodeWithTag("academy_course_2").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("academy_module_201").assertExists()
    }

    @Test
    fun `partially completed previous course keeps next in locked preview`() {
        val courses = listOf(course(1, 1, listOf(101, 102)), course(2, 2, listOf(201)))
        setDeck(courses, completedSet = setOf("101"))

        composeTestRule.onNodeWithTag("academy_course_2").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("academy_module_201").assertExists()
        composeTestRule.onNodeWithText("Preview").assertExists()
        composeTestRule.onNodeWithText("+₹5,000").assertDoesNotExist()
    }

    @Test
    fun `unlocked course click reports chapter open`() {
        val courses = listOf(course(1, 1, listOf(101)))
        var openedChapter: Int? = null
        composeTestRule.setContent {
            MyApplicationTheme {
                DisablePremiumMotion {
                    androidx.compose.foundation.layout.Column {
                        CourseDeck(
                            courses = courses,
                            completedSet = emptySet(),
                            currency = "₹",
                            onOpenChapter = { openedChapter = it }
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("academy_course_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("academy_module_101").performClick()
        composeTestRule.waitForIdle()

        assertEquals(101, openedChapter)
    }
}
