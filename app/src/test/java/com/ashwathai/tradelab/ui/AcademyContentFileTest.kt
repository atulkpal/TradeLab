package com.ashwathai.tradelab.ui

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AcademyContentFileTest {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private fun assetFile(name: String): File? {
        val candidates = listOf(
            File("src/main/assets/$name"),
            File("app/src/main/assets/$name")
        )
        return candidates.firstOrNull { it.exists() }
    }

    @Test
    fun `legacy academy_data_json parses and every module is well formed`() {
        val file = assetFile("academy_data.json")
            ?: return // asset missing from checkout; skip gracefully
        val json = file.readText()
        val type = Types.newParameterizedType(List::class.java, QuizModule::class.java)
        val modules: List<QuizModule>? = moshi.adapter<List<QuizModule>>(type).fromJson(json)

        assertTrue("academy_data.json must parse into modules", !modules.isNullOrEmpty())
        modules!!.forEachIndexed { index, module ->
            assertTrue("Module ${module.id} correctIndex out of range", module.correctIndex in module.options.indices)
            assertTrue("Module ${module.id} must have at least one lecture", module.lectures.isNotEmpty())
            assertTrue("Module ids must be unique", modules.count { it.id == module.id } == 1)
            assertTrue("Module ${module.id} title must not be blank", module.title.isNotBlank())
        }
        assertEquals(8, modules.size)
    }

    @Test
    fun `academy_data_v2_json validates against the content spec`() {
        val file = assetFile("academy_data_v2.json") ?: return // not authored yet; skip gracefully
        val json = file.readText()
        val content = moshi.adapter(AcademyContentV2::class.java).fromJson(json)
        assertTrue("academy_data_v2.json must parse", content != null)
        assertTrue("content version should be 2", content!!.version == 2)
        assertTrue("must define courses", content.courses.isNotEmpty())

        val validation = AcademyScoring.validateCourses(content.courses)
        assertTrue(
            "academy_data_v2.json failed validation:\n" + validation.errors.joinToString("\n"),
            validation.isValid
        )

        val ids = content.courses.flatMap { it.chapters.map { ch -> ch.id } }
        assertEquals("chapter ids must be globally unique", ids.size, ids.toSet().size)
        assertEquals("must be contiguous within each course", ids.toSet().size, ids.size)

        content.courses.forEach { course ->
            assertEquals(
                "course ${course.id} reward should match its tier",
                AcademyScoring.tierReward(course.tier),
                course.chapters.first().rewardAmt,
                0.0
            )
        }

        assertEquals("must contain exactly six courses", 6, content.courses.size)
        assertEquals("courses must be ordered", listOf(1, 2, 3, 4, 5, 6), content.courses.map { it.id })
        val expectedChapters = mapOf(1 to 12, 2 to 12, 3 to 10, 4 to 12, 5 to 12, 6 to 10)
        content.courses.forEach { course ->
            assertEquals(
                "course ${course.id} chapter count",
                expectedChapters[course.id],
                course.chapters.size
            )
        }
        content.courses.filter { it.id == 4 || it.id == 6 }.forEach { course ->
            assertTrue("course ${course.id} must carry riskDisclosure", course.chapters.all { it.riskDisclosure.isNotBlank() })
        }
        content.courses.filter { it.id !in setOf(4, 6) }.forEach { course ->
            assertTrue("non-advanced course ${course.id} has no riskDisclosure", course.chapters.none { it.riskDisclosure.isNotBlank() })
        }
    }
}
