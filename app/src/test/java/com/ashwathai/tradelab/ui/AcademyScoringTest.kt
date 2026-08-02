package com.ashwathai.tradelab.ui

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademyScoringTest {

    private fun q(question: String, options: List<String>, correct: Int, explanation: String = "Because of X.") =
        QuizQuestion(question, options, correct, explanation)

    private val sampleQuestions = listOf(
        q("What does a stock represent?", listOf("A", "Ownership", "C"), 1),
        q("What is T+1?", listOf("Settlement", "B", "C", "D"), 0),
        q("Which order type is GTT?", listOf("A", "B", "Good Till Triggered", "D"), 2),
        q("What is STT?", listOf("A", "Securities Transaction Tax", "C", "D"), 1),
        q("What is position sizing?", listOf("A", "B", "C", "Limiting single-trade risk"), 3)
    )

    @Test
    fun `score counts correct answers against selected indices`() {
        val answers = mapOf(0 to 1, 1 to 0, 2 to 2, 3 to 1, 4 to 3)
        val (correct, total) = AcademyScoring.score(sampleQuestions, answers)
        assertEquals(5, correct)
        assertEquals(5, total)
    }

    @Test
    fun `score ignores unanswered questions and wrong answers`() {
        val answers = mapOf(0 to 1, 1 to 2)
        val (correct, total) = AcademyScoring.score(sampleQuestions, answers)
        assertEquals(1, correct)
        assertEquals(5, total)
    }

    @Test
    fun `passes requires at least 60 percent correct`() {
        assertTrue(AcademyScoring.passes(sampleQuestions, mapOf(0 to 1, 1 to 0, 2 to 2, 3 to 1, 4 to 3))) // 5/5
        assertTrue(AcademyScoring.passes(sampleQuestions, mapOf(0 to 1, 1 to 0, 2 to 2, 3 to 1))) // 4/5 = 80%
        assertTrue(AcademyScoring.passes(sampleQuestions, mapOf(0 to 1, 1 to 0, 2 to 2))) // 3/5 = 60% exactly
        assertFalse(AcademyScoring.passes(sampleQuestions, mapOf(0 to 1, 1 to 0))) // 2/5 = 40%
        assertFalse(AcademyScoring.passes(sampleQuestions, emptyMap()))
    }

    @Test
    fun `passes on empty quiz list is false`() {
        assertFalse(AcademyScoring.passes(emptyList(), emptyMap()))
    }

    @Test
    fun `isCorrect matches the question's correct index`() {
        assertTrue(AcademyScoring.isCorrect(sampleQuestions[0], 1))
        assertFalse(AcademyScoring.isCorrect(sampleQuestions[0], 0))
        assertFalse(AcademyScoring.isCorrect(sampleQuestions[0], -1))
    }

    @Test
    fun `courseIcon maps every course id to a unique non-zero drawable`() {
        val mapped = (1..6).associateWith { AcademyScoring.courseIcon(it) }
        assertEquals(6, mapped.size)
        mapped.values.forEach { assertTrue(it != 0) }
        assertEquals(6, mapped.values.toSet().size)
        assertEquals(mapped[1], AcademyScoring.courseIcon(999))
    }

    @Test
    fun `biasIcon maps delta thresholds to distinct non-zero drawables`() {
        val up = AcademyScoring.biasIcon(6.0)
        val down = AcademyScoring.biasIcon(-6.0)
        val neutral = AcademyScoring.biasIcon(0.0)
        assertTrue(up != 0)
        assertTrue(down != 0)
        assertTrue(neutral != 0)
        assertEquals(setOf(up, down, neutral).size, 3)
        assertEquals(neutral, AcademyScoring.biasIcon(4.9))
        assertEquals(neutral, AcademyScoring.biasIcon(-4.9))
    }

    @Test
    fun `fno academic gate unlocks with v2 beginner chapters`() {
        assertFalse(AcademyScoring.fnoAcademicUnlocked(emptySet()))
        assertFalse(AcademyScoring.fnoAcademicUnlocked(setOf("101", "102")))
        assertTrue(AcademyScoring.fnoAcademicUnlocked(setOf("101", "102", "103")))
        assertTrue(AcademyScoring.fnoAcademicUnlocked(setOf("101", "102", "103", "104")))
    }

    @Test
    fun `fno academic gate still accepts legacy level ids`() {
        assertFalse(AcademyScoring.fnoAcademicUnlocked(setOf("1", "2")))
        assertTrue(AcademyScoring.fnoAcademicUnlocked(setOf("1", "2", "3")))
    }

    @Test
    fun `tier reward maps to expected capital amounts`() {        assertEquals(500.0, AcademyScoring.tierReward("BEGINNER"), 0.0)
        assertEquals(750.0, AcademyScoring.tierReward("INTERMEDIATE"), 0.0)
        assertEquals(1000.0, AcademyScoring.tierReward("ADVANCED"), 0.0)
        assertEquals(500.0, AcademyScoring.tierReward("UNKNOWN"), 0.0)
        assertEquals(1000.0, AcademyScoring.tierReward("advanced"), 0.0)
    }

    @Test
    fun `tierFor normalizes tier strings`() {
        assertEquals("BEGINNER", AcademyScoring.tierFor("beginner"))
        assertEquals("INTERMEDIATE", AcademyScoring.tierFor("Intermediate"))
        assertEquals("ADVANCED", AcademyScoring.tierFor("advanced"))
        assertEquals("BEGINNER", AcademyScoring.tierFor("bogus"))
    }

    private fun chapter(id: Int) = ChapterModule(
        id = id, title = "C$id", topic = "T", rewardAmt = 500.0, concept = "X",
        lectures = listOf(Lecture("L1", "Content here")),
        quizzes = listOf(q("Q1", listOf("A", "B"), 0), q("Q2", listOf("A", "B", "C"), 1), q("Q3", listOf("A", "B", "C", "D"), 2))
    )

    private fun course(id: Int, order: Int, chapterIds: List<Int>) =
        AcademyCourse(id = id, title = "Course $id", tier = "BEGINNER", order = order, chapters = chapterIds.map { chapter(it) })

    @Test
    fun `unlockedCourseIds unlocks first course always`() {
        val courses = listOf(course(1, 1, listOf(101, 102)), course(2, 2, listOf(201)))
        assertEquals(setOf(1), AcademyScoring.unlockedCourseIds(courses, emptySet()))
    }

    @Test
    fun `unlockedCourseIds requires full previous course completion`() {
        val courses = listOf(course(1, 1, listOf(101, 102)), course(2, 2, listOf(201)))
        assertEquals(setOf(1), AcademyScoring.unlockedCourseIds(courses, setOf("101")))
        assertEquals(setOf(1, 2), AcademyScoring.unlockedCourseIds(courses, setOf("101", "102")))
    }

    @Test
    fun `unlockedCourseIds chains across multiple courses`() {
        val courses = listOf(
            course(1, 1, listOf(101)),
            course(2, 2, listOf(201)),
            course(3, 3, listOf(301))
        )
        assertEquals(setOf(1), AcademyScoring.unlockedCourseIds(courses, emptySet()))
        assertEquals(setOf(1, 2), AcademyScoring.unlockedCourseIds(courses, setOf("101")))
        assertEquals(setOf(1, 2, 3), AcademyScoring.unlockedCourseIds(courses, setOf("101", "201")))
    }

    @Test
    fun `unlockedCourseIds handles empty input`() {
        assertEquals(emptySet<Int>(), AcademyScoring.unlockedCourseIds(emptyList(), emptySet()))
    }

    @Test
    fun `validateCourses rejects duplicate and out-of-range chapter ids`() {
        val courses = listOf(
            AcademyCourse(
                id = 1,
                title = "Course A",
                tier = "BEGINNER",
                order = 1,
                chapters = listOf(
                    ChapterModule(id = 101, courseId = 1, title = "C1", topic = "T", rewardAmt = 500.0, concept = "X", lectures = listOf(Lecture("L1", "Content here")), quizzes = listOf(q("Q1", listOf("A", "B"), 0))),
                    ChapterModule(id = 101, courseId = 1, title = "C2", topic = "T", rewardAmt = 500.0, concept = "X", lectures = listOf(Lecture("L1", "Content here")), quizzes = listOf(q("Q1", listOf("A", "B"), 0)))
                )
            )
        )
        val result = AcademyScoring.validateCourses(courses)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Duplicate") })
    }

    @Test
    fun `validateCourses rejects bad correctIndex and missing explanations`() {
        val courses = listOf(
            AcademyCourse(
                id = 1,
                title = "Course A",
                tier = "BEGINNER",
                order = 1,
                chapters = listOf(
                    ChapterModule(
                        id = 101, courseId = 1, title = "C1", topic = "T", rewardAmt = 500.0, concept = "X",
                        lectures = listOf(Lecture("L1", "Content here")),
                        quizzes = listOf(QuizQuestion("Q1", listOf("A", "B"), 5, ""))
                    )
                )
            )
        )
        val result = AcademyScoring.validateCourses(courses)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("invalid correctIndex") })
        assertTrue(result.errors.any { it.contains("missing explanation") })
    }

    @Test
    fun `validateCourses accepts a well formed curriculum`() {
        val courses = listOf(
            AcademyCourse(
                id = 1,
                title = "Basics",
                tagline = "Learn the foundations",
                iconEmoji = "📈",
                tier = "BEGINNER",
                order = 1,
                chapters = listOf(
                    ChapterModule(
                        id = 101, courseId = 1, title = "What is a Stock", topic = "Equities", rewardAmt = 500.0, concept = "Ownership basics",
                        lectures = listOf(Lecture("L1", "A stock is a unit of ownership. Companies issue shares to raise capital."), Lecture("L2", "Shareholders benefit from profits and dividends.")),
                        quizzes = listOf(
                            q("What does a stock represent?", listOf("A", "Ownership", "C"), 1),
                            q("Who gets dividends?", listOf("Shareholders", "B", "C", "D"), 0),
                            q("What raises capital?", listOf("A", "Issuing shares", "C"), 1)
                        )
                    )
                )
            )
        )
        val result = AcademyScoring.validateCourses(courses)
        assertTrue(result.isValid)
        assertEquals(0, result.errors.size)
    }

    @Test
    fun `validateCourses rejects missing riskDisclosure on advanced courses`() {
        fun chapter(id: Int, disclosure: String = "") = ChapterModule(
            id = id, courseId = 4, title = "C$id", topic = "T", rewardAmt = 1000.0, concept = "X",
            lectures = listOf(Lecture("L1", "Content here")),
            quizzes = listOf(
                q("Q1", listOf("A", "B"), 0),
                q("Q2", listOf("A", "B", "C"), 1),
                q("Q3", listOf("A", "B", "C", "D"), 2)
            ),
            riskDisclosure = disclosure
        )

        val fnoMissing = listOf(AcademyCourse(id = 4, title = "F&O", tier = "ADVANCED", order = 4, chapters = listOf(chapter(401))))
        val fnoResult = AcademyScoring.validateCourses(fnoMissing)
        assertFalse(fnoResult.isValid)
        assertTrue(fnoResult.errors.any { it.contains("missing riskDisclosure") })

        val taxMissing = listOf(AcademyCourse(id = 6, title = "Taxation", tier = "ADVANCED", order = 6, chapters = listOf(chapter(601))))
        val taxResult = AcademyScoring.validateCourses(taxMissing)
        assertFalse(taxResult.isValid)
        assertTrue(taxResult.errors.any { it.contains("missing riskDisclosure") })

        val fnoPresent = listOf(AcademyCourse(id = 4, title = "F&O", tier = "ADVANCED", order = 4, chapters = listOf(chapter(402, disclosure = "Derivatives are high-risk."))))
        val presentResult = AcademyScoring.validateCourses(fnoPresent)
        assertTrue(presentResult.isValid)

        val beginner = listOf(AcademyCourse(id = 1, title = "Basics", tier = "BEGINNER", order = 1, chapters = listOf(chapter(101, disclosure = ""))))
        assertTrue(AcademyScoring.validateCourses(beginner).isValid)
    }

    @Test
    fun `legacy QuizModule maps into a single question chapter`() {
        val legacy = QuizModule(
            id = 1,
            title = "Level 1: What is a Stock?",
            topic = "Equities",
            rewardAmt = 500.0,
            concept = "A stock is fractional ownership.",
            question = "What does buying a stock represent?",
            options = listOf("A loan", "Ownership", "Coupon"),
            correctIndex = 1
        )
        val chapter = legacy.toChapterModule(courseId = 1)
        assertEquals(1, chapter.courseId)
        assertEquals(1, chapter.quizzes.size)
        assertEquals("What does buying a stock represent?", chapter.quizzes[0].question)
        assertEquals(1, chapter.quizzes[0].correctIndex)
        assertEquals(500.0, chapter.rewardAmt, 0.0)
    }

    @Test
    fun `v2 JSON parses into courses via Moshi`() {
        val json = """
            {
              "version": 2,
              "courses": [
                {
                  "id": 1,
                  "title": "Stock Market Basics",
                  "tagline": "Foundations",
                  "iconEmoji": "📈",
                  "tier": "BEGINNER",
                  "order": 1,
                  "chapters": [
                    {
                      "id": 101,
                      "title": "Chapter 1.1: What is a Stock?",
                      "topic": "Equities Fundamentals",
                      "concept": "Ownership basics.",
                      "lectures": [
                        { "title": "Lecture 1", "content": "A stock represents fractional ownership in a company." }
                      ],
                      "quizzes": [
                        {
                          "question": "What does buying a stock represent?",
                          "options": ["A loan", "Ownership", "Coupon"],
                          "correctIndex": 1,
                          "explanation": "A share is a fractional ownership claim."
                        }
                      ],
                      "rewardAmt": 500.0
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val content = moshi.adapter(AcademyContentV2::class.java).fromJson(json)
        assertEquals(2, content?.version)
        assertEquals(1, content?.courses?.size)
        assertEquals("Stock Market Basics", content?.courses?.get(0)?.title)
        assertEquals(1, content?.courses?.get(0)?.chapters?.size)
        val chapter = content?.courses?.get(0)?.chapters?.get(0)
        assertEquals(101, chapter?.id)
        assertEquals(1, chapter?.quizzes?.size)
        assertEquals("A share is a fractional ownership claim.", chapter?.quizzes?.get(0)?.explanation)
        assertEquals("", chapter?.riskDisclosure)
    }

    private fun mission(
        id: Int,
        identifier: String,
        rewardAmt: Double = 1000.0,
        targetCount: Int? = null,
        targetCourseId: Int? = null
    ) = Mission(
        id = id,
        title = "Mission $id",
        desc = "Do a thing",
        reward = "+₹1,000 Virtual Cash",
        identifier = identifier,
        rewardAmt = rewardAmt,
        targetCount = targetCount,
        targetCourseId = targetCourseId
    )

    private fun stats(cash: Double = 25000.0, startingCash: Double = 25000.0, riskLevel: String = "Moderate", holdingsValue: Double = 0.0) =
        PortfolioStats(
            totalValue = cash + holdingsValue,
            cash = cash,
            startingCash = startingCash,
            holdingsValue = holdingsValue,
            riskLevel = riskLevel
        )

    @Test
    fun `evaluateMission has_traded flips on any trade activity`() {
        val notTraded = AcademyScoring.evaluateMission(
            mission(1, "has_traded"), emptySet(), emptyList(), emptySet(), stats()
        )
        assertFalse(notTraded.isCompleted)

        val tradedHoldings = AcademyScoring.evaluateMission(
            mission(1, "has_traded"), emptySet(), emptyList(), emptySet(), stats(holdingsValue = 5000.0)
        )
        assertTrue(tradedHoldings.isCompleted)

        val spentCash = AcademyScoring.evaluateMission(
            mission(1, "has_traded"), emptySet(), emptyList(), emptySet(), stats(cash = 20000.0, startingCash = 25000.0)
        )
        assertTrue(spentCash.isCompleted)
    }

    @Test
    fun `evaluateMission completed_3_modules uses completed chapter count`() {
        val eval0 = AcademyScoring.evaluateMission(mission(2, "completed_3_modules", targetCount = 3), emptySet(), emptyList(), emptySet(), stats())
        assertEquals(0, eval0.progress)
        assertFalse(eval0.isCompleted)

        val eval2 = AcademyScoring.evaluateMission(mission(2, "completed_3_modules", targetCount = 3), setOf("101", "102"), emptyList(), emptySet(), stats())
        assertEquals(2, eval2.progress)
        assertFalse(eval2.isCompleted)

        val eval3 = AcademyScoring.evaluateMission(mission(2, "completed_3_modules", targetCount = 3), setOf("101", "102", "103"), emptyList(), emptySet(), stats())
        assertEquals(3, eval3.progress)
        assertTrue(eval3.isCompleted)
    }

    @Test
    fun `evaluateMission has_calibrated flips on profiler changes`() {
        val default = AcademyScoring.evaluateMission(mission(3, "has_calibrated"), emptySet(), emptyList(), emptySet(), stats())
        assertFalse(default.isCompleted)

        val changedRisk = AcademyScoring.evaluateMission(mission(3, "has_calibrated"), emptySet(), emptyList(), emptySet(), stats(riskLevel = "Aggressive"))
        assertTrue(changedRisk.isCompleted)

        val changedBudget = AcademyScoring.evaluateMission(mission(3, "has_calibrated"), emptySet(), emptyList(), emptySet(), stats(startingCash = 10000.0))
        assertTrue(changedBudget.isCompleted)
    }

    @Test
    fun `evaluateMission completed_course_1 tracks chapters within the course`() {
        val courses = listOf(course(1, 1, listOf(101, 102)), course(2, 2, listOf(201)))
        val partial = AcademyScoring.evaluateMission(mission(4, "completed_course_1", targetCourseId = 1), setOf("101"), courses, emptySet(), stats())
        assertEquals(1, partial.progress)
        assertEquals(2, partial.target)
        assertFalse(partial.isCompleted)

        val full = AcademyScoring.evaluateMission(mission(4, "completed_course_1", targetCourseId = 1), setOf("101", "102", "201"), courses, emptySet(), stats())
        assertEquals(2, full.progress)
        assertTrue(full.isCompleted)
    }

    @Test
    fun `evaluateMission completed_course_1 handles missing course gracefully`() {
        val eval = AcademyScoring.evaluateMission(mission(4, "completed_course_1", targetCourseId = 9), emptySet(), emptyList(), emptySet(), stats())
        assertEquals(0, eval.progress)
        assertFalse(eval.isCompleted)
    }

    @Test
    fun `evaluateMission unlocked_advanced_course requires more than one unlocked course`() {
        val courses = listOf(course(1, 1, listOf(101)), course(2, 2, listOf(201)))
        val oneUnlocked = AcademyScoring.evaluateMission(mission(5, "unlocked_advanced_course"), emptySet(), courses, setOf(1), stats())
        assertFalse(oneUnlocked.isCompleted)

        val twoUnlocked = AcademyScoring.evaluateMission(mission(5, "unlocked_advanced_course"), setOf("101"), courses, setOf(1, 2), stats())
        assertTrue(twoUnlocked.isCompleted)
    }

    @Test
    fun `evaluateMission earned_certificate counts total chapters across courses`() {
        val courses = listOf(course(1, 1, listOf(101, 102)), course(2, 2, listOf(201)))
        val partial = AcademyScoring.evaluateMission(mission(7, "earned_certificate", targetCount = 68), setOf("101"), courses, emptySet(), stats())
        assertEquals(1, partial.progress)
        assertEquals(3, partial.target)
        assertFalse(partial.isCompleted)

        val full = AcademyScoring.evaluateMission(mission(7, "earned_certificate", targetCount = 68), setOf("101", "102", "201"), courses, emptySet(), stats())
        assertEquals(3, full.progress)
        assertTrue(full.isCompleted)
    }

    @Test
    fun `evaluateMission falls back to targetCount when academy is empty`() {
        val eval = AcademyScoring.evaluateMission(mission(7, "earned_certificate", targetCount = 68), emptySet(), emptyList(), emptySet(), stats())
        assertEquals(68, eval.target)
        assertFalse(eval.isCompleted)
    }

    @Test
    fun `evaluateMission unknown identifier is never complete`() {
        val eval = AcademyScoring.evaluateMission(mission(99, "mystery_identifier"), setOf("101"), emptyList(), emptySet(), stats())
        assertEquals(0, eval.progress)
        assertFalse(eval.isCompleted)
    }
}
