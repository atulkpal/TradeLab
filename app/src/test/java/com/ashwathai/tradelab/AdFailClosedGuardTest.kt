package com.ashwathai.tradelab

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Epic 26 backlog guard — fail-closed ad surfaces.
 *
 * Scans the ad call sites and asserts that NO `onAdFailed` handler grants a
 * reward or opens a reward-gated flow. Ad failures must surface feedback only.
 *
 * Regression context (2026-08-25): three surfaces silently granted rewards on
 * ad failure (free watchlist sheets, free portfolio resets, free intraday
 * unlocks). This test pins the fail-closed contract.
 */
class AdFailClosedGuardTest {

    private val sourceFiles = listOf(
        "src/main/java/com/ashwathai/tradelab/ui/watchlist/WatchlistScreen.kt",
        "src/main/java/com/ashwathai/tradelab/ui/profile/ProfileScreen.kt",
        "src/main/java/com/ashwathai/tradelab/ui/portfolio/PortfolioScreen.kt",
        "src/main/java/com/ashwathai/tradelab/ui/academy/LectureScreen.kt"
    )

    /** Tokens that must NEVER appear inside an onAdFailed handler. */
    private val forbiddenGrants = listOf(
        "resetPortfolio(", // free portfolio resets
        "unlockIntradaySession(", // free intraday unlock
        "unlockPremiumIndicators(", // free indicator pack
        "earnEmergencyCash(", // free emergency cash
        "earnAiAuditCredit(", // free AI audit
        "earnBrokerageCredits(", // free brokerage credits
        "unlockIntraday", // any intraday unlock variant
        "showCreateDialog = true" // watchlist sheet granted on failure
    )

    /** Extracts the balanced-brace block starting at [start] (index of '{'). */
    private fun blockFrom(source: String, start: Int): String {
        var depth = 0
        var i = start
        var inString = false
        var inComment = false
        while (i < source.length) {
            val c = source[i]
            when {
                c == '/' && i + 1 < source.length && source[i + 1] == '/' -> inComment = true
                c == '\n' -> inComment = false
                !inComment && !inString && c == '"' -> inString = true
                !inComment && inString && c == '"' && source[i - 1] != '\\' -> inString = false
                !inComment && !inString && c == '{' -> depth++
                !inComment && !inString && c == '}' -> {
                    depth--
                    if (depth == 0) return source.substring(start, i + 1)
                }
            }
            i++
        }
        return source.substring(start)
    }

    @Test
    fun `onAdFailed handlers are fail-closed - no grants on ad failure`() {
        val violations = mutableListOf<String>()

        for (path in sourceFiles) {
            val file = File(path)
            if (!file.exists()) {
                violations.add("MISSING SOURCE FILE: $path (run from app/ module dir)")
                continue
            }
            val source = file.readText()
            var idx = source.indexOf("onAdFailed")
            while (idx != -1) {
                val brace = source.indexOf('{', idx)
                if (brace != -1) {
                    val block = blockFrom(source, brace)
                    for (token in forbiddenGrants) {
                        if (block.contains(token)) {
                            val line = source.substring(0, idx).count { it == '\n' } + 1
                            violations.add("$path:$line onAdFailed grants '$token'")
                        }
                    }
                }
                idx = source.indexOf("onAdFailed", idx + 1)
            }
        }

        assertTrue(
            "Fail-closed contract violated:\n" + violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun `ad config production ids are wired`() {
        val cfg = File("src/main/java/com/ashwathai/tradelab/ui/common/AdConfig.kt")
        assertTrue("AdConfig.kt missing", cfg.exists())
        val source = cfg.readText()
        assertTrue("USE_TEST_ADS must derive from BuildConfig.DEBUG (debug=test, release=prod)", source.contains("BuildConfig.DEBUG"))
        assertTrue("rewarded_v2 unit must be wired", source.contains("349kle4725uh1kfa"))
        assertTrue("interstitial unit must be wired", source.contains("0pv0ggz19gmfkp18"))
    }
}
