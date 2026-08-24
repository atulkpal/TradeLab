package com.ashwathai.tradelab

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Epic 25.1 — Theme Engine Compliance guard.
 *
 * Source-scan test: the Academy composables must consume theme-aware tokens
 * (DynamicPrimary / TextOnAccent / theme alpha getters) instead of hardcoded
 * literal colors, so the screen respects all 5 ThemeModes.
 */
class AcademyThemeTest {

    private fun readSource(vararg candidates: String): String {
        for (candidate in candidates) {
            val f = File(candidate)
            if (f.exists()) return f.readText()
        }
        throw IllegalStateException("Source file not found: ${candidates.first()}")
    }

    private val academyScreen get() = readSource(
        "src/main/java/com/ashwathai/tradelab/ui/academy/AcademyScreen.kt",
        "app/src/main/java/com/ashwathai/tradelab/ui/academy/AcademyScreen.kt"
    )

    private val lectureScreen get() = readSource(
        "src/main/java/com/ashwathai/tradelab/ui/academy/LectureScreen.kt",
        "app/src/main/java/com/ashwathai/tradelab/ui/academy/LectureScreen.kt"
    )

    @Test
    fun `academy screen has no hardcoded violet hex literals`() {
        assertFalse(academyScreen.contains("Color(0xFF8B5CF6)"))
    }

    @Test
    fun `academy screen has no raw Color White literals`() {
        assertFalse(academyScreen.contains("Color.White"))
    }

    @Test
    fun `academy screen uses theme accent token`() {
        assertTrue(academyScreen.contains("DynamicPrimary"))
    }

    @Test
    fun `brand violet only remains as semantic tier and identity color`() {
        // Allowed: BEGINNER tier identity + per-course identity palette.
        val uses = Regex("BrandViolet").findAll(academyScreen).count()
        assertTrue("BrandViolet usages: $uses (expected <= 3 semantic uses)", uses <= 3)
        assertFalse(academyScreen.contains("BrandViolet.copy"))
    }

    @Test
    fun `lecture screen is fully theme compliant`() {
        assertFalse(lectureScreen.contains("BrandViolet"))
        assertFalse(lectureScreen.contains("Color(0xFF8B5CF6)"))
        assertFalse(lectureScreen.contains("Color.White"))
        assertTrue(lectureScreen.contains("DynamicPrimary"))
        assertTrue(lectureScreen.contains("TextOnAccent"))
    }

    @Test
    fun `sub tab state survives config changes`() {
        assertTrue(academyScreen.contains("activeSubTab by rememberSaveable"))
    }
}
