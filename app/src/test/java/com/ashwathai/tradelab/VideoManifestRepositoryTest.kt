package com.ashwathai.tradelab

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ashwathai.tradelab.data.LectureMedia
import com.ashwathai.tradelab.data.VideoManifestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Epic 27 — remote video manifest: parsing, layered resolution
 * (remote URL > bundled raw > blank), Hindi availability, and cache-first fetch.
 *
 * Manifest state is seeded via the cache-first fetch path (fresh cache short-circuits
 * the network), which doubles as coverage for the offline-first guarantee.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VideoManifestRepositoryTest {

    private lateinit var repo: VideoManifestRepository
    private lateinit var cacheFile: File

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        cacheFile = File(app.cacheDir, "academy_video_manifest.json")
        cacheFile.delete()
        repo = VideoManifestRepository(app, Dispatchers.Unconfined)
        // Hermetic: unreachable port -> instant refusal, so the offline-survival
        // path is genuinely exercised (the real manifest is LIVE in production now!)
        repo.manifestUrl = "http://127.0.0.1:9/manifest.json"
    }

    /** Seeds manifest state through the cache-first fetch (no network touched). */
    private fun seedManifest(json: String) = runBlocking {
        cacheFile.writeText(json)
        repo.fetch(forceRefresh = false)
    }

    // ── Parsing ──

    @Test
    fun `parseManifest decodes entries keyed by bundled videoUrl`() {
        val m = repo.parseManifest(
            """
            {"version":3,"generatedAt":"2026-08-25T00:00:00Z","videos":{
              "lecture_1_10_1_final":{"en":"https://x/en.mp4","hi":"https://x/hi.mp4"},
              "lecture_2_1_2_final":{"en":"https://x/en2.mp4"}
            }}
            """.trimIndent()
        )
        assertNotNull(m)
        assertEquals(3, m!!.version)
        assertEquals(2, m.videos.size)
        assertEquals("https://x/hi.mp4", m.videos["lecture_1_10_1_final"]?.hi)
        assertNull(m.videos["lecture_2_1_2_final"]?.hi)
    }

    @Test
    fun `parseManifest returns null on malformed json`() {
        assertNull(repo.parseManifest("{not json"))
    }

    // ── Resolution: no manifest (bundled fallback) ──

    @Test
    fun `no manifest resolves bundled key passthrough with no hindi`() {
        val media = repo.lectureMedia("lecture_1_10_1_final", VideoManifestRepository.LANG_EN)
        assertEquals("lecture_1_10_1_final", media.resolvedUrl)
        assertFalse(media.hasHindi)
        assertEquals(LectureMedia.SOURCE_BUNDLED, media.source)
    }

    @Test
    fun `blank key resolves to blank`() {
        val media = repo.lectureMedia("", VideoManifestRepository.LANG_EN)
        assertEquals("", media.resolvedUrl)
        assertFalse(media.hasHindi)
    }

    // ── Resolution: manifest present ──

    @Test
    fun `manifest en is preferred over bundled for english`() {
        seedManifest(entry("https://x/en.mp4", "https://x/hi.mp4"))
        val media = repo.lectureMedia("lecture_1_10_1_final", VideoManifestRepository.LANG_EN)
        assertEquals("https://x/en.mp4", media.resolvedUrl)
        assertEquals(LectureMedia.SOURCE_REMOTE, media.source)
    }

    @Test
    fun `hindi language uses remote hi url when available`() {
        seedManifest(entry("https://x/en.mp4", "https://x/hi.mp4"))
        val media = repo.lectureMedia("lecture_1_10_1_final", VideoManifestRepository.LANG_HI)
        assertEquals("https://x/hi.mp4", media.resolvedUrl)
        assertTrue(media.hasHindi)
    }

    @Test
    fun `hindi language falls back to remote en when hi missing`() {
        seedManifest(entry("https://x/en.mp4", null))
        val media = repo.lectureMedia("lecture_1_10_1_final", VideoManifestRepository.LANG_HI)
        assertEquals("https://x/en.mp4", media.resolvedUrl)
        assertFalse(media.hasHindi)
    }

    @Test
    fun `hindi-only manifest still serves bundled english for en language`() {
        seedManifest(entry(null, "https://x/hi.mp4"))
        val media = repo.lectureMedia("lecture_1_10_1_final", VideoManifestRepository.LANG_EN)
        assertEquals("lecture_1_10_1_final", media.resolvedUrl)
        assertTrue(media.hasHindi)
    }

    @Test
    fun `manifest miss for unknown key falls back to bundled passthrough`() {
        seedManifest(entry("https://x/en.mp4", null))
        val media = repo.lectureMedia("lecture_9_9_9_final", VideoManifestRepository.LANG_EN)
        assertEquals("lecture_9_9_9_final", media.resolvedUrl)
    }

    // ── Cache-first fetch (no network when cache is fresh) ──

    @Test
    fun `fresh cache satisfies fetch without network`() = runBlocking {
        cacheFile.writeText(entry("https://cache/en.mp4", null))
        val result = repo.fetch(forceRefresh = false)
        assertNotNull(result)
        assertEquals("https://cache/en.mp4", result!!.videos["lecture_1_10_1_final"]?.en)
    }

    @Test
    fun `stale cache is replaced by network result or retained on failure`() = runBlocking {
        cacheFile.writeText(entry("https://old/en.mp4", null))
        // Backdate the cache beyond TTL → stale
        cacheFile.setLastModified(System.currentTimeMillis() - 25 * 60 * 60 * 1000L)
        // Network will fail in Robolectric (no internet) → cached (any age) must survive
        val result = repo.fetch(forceRefresh = false)
        assertNotNull("offline fallback must retain cache", result)
        assertEquals("https://old/en.mp4", result!!.videos["lecture_1_10_1_final"]?.en)
    }

    // ── helpers ──

    private fun entry(en: String?, hi: String?): String {
        val enJson = en?.let { "\"$it\"" } ?: "null"
        val hiJson = hi?.let { "\"$it\"" } ?: "null"
        return """{"version":1,"videos":{"lecture_1_10_1_final":{"en":$enJson,"hi":$hiJson}}}"""
    }
}
