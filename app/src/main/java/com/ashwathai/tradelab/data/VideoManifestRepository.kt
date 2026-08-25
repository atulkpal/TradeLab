package com.ashwathai.tradelab.data

import android.content.Context
import com.ashwathai.tradelab.di.IoDispatcher
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/** Per-lecture remote video URLs keyed by language ("en" / "hi"). */
@JsonClass(generateAdapter = true)
data class VideoManifestEntry(
    val en: String? = null,
    val hi: String? = null
)

/**
 * Remote video manifest fetched from Firebase Storage (`videos/manifest.json`).
 * Keys match the bundled `videoUrl` values in `academy_data_v2.json`
 * (e.g. "lecture_1_10_1_final") so lookup is a direct map hit.
 */
@JsonClass(generateAdapter = true)
data class VideoManifest(
    val version: Int = 1,
    val generatedAt: String = "",
    val videos: Map<String, VideoManifestEntry> = emptyMap()
)

/** Resolved playback info for a single lecture. */
data class LectureMedia(
    val resolvedUrl: String,
    val hasHindi: Boolean,
    val source: String = SOURCE_BUNDLED
) {
    companion object {
        const val SOURCE_BUNDLED = "bundled"
        const val SOURCE_REMOTE = "remote"
    }
}

/**
 * Epic 27: Remote video manifest delivery with layered fallback.
 *
 * Resolution order per lecture (Epic 27 spec):
 *   1. Manifest URL for the selected language (Firebase Storage, public read)
 *   2. Bundled raw resource (en: original key; hi: key + "_hi" suffix)
 *   3. Blank (caller shows the "coming soon" empty state)
 *
 * Caching: cache-first (any age) so playback works fully offline; network
 * refresh only when the cached manifest is older than [CACHE_TTL_MS].
 * Fetch is a one-shot fire-and-forget — no polling loops (Background Tasks Rule).
 */
@Singleton
class VideoManifestRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    // Test seam: override with an unreachable URL to keep network tests hermetic.
    // (Not a constructor param — Dagger cannot bind a raw String default.)
    var manifestUrl: String = MANIFEST_URL
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val manifestAdapter = Moshi.Builder().build().adapter(VideoManifest::class.java)

    private val _manifest = MutableStateFlow<VideoManifest?>(null)
    val manifest: StateFlow<VideoManifest?> = _manifest.asStateFlow()

    private val fetchStarted = AtomicBoolean(false)

    private val fetchScope = CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + ioDispatcher
    )

    /** One-shot fetch: cache-first, then network refresh if stale. */
    fun fetchIfNeeded() {
        if (!fetchStarted.compareAndSet(false, true)) return
        fetchScope.launch { fetch(forceRefresh = false) }
    }

    /** Loads the manifest: cache immediately, network refresh when stale or missing. */
    suspend fun fetch(forceRefresh: Boolean): VideoManifest? = withContext(ioDispatcher) {
        val cache = cacheFile()
        val cached = readCache(cache)

        if (cached != null) {
            _manifest.value = cached
        }

        val isStale = cached == null ||
            System.currentTimeMillis() - cache.lastModified() > CACHE_TTL_MS
        if (!forceRefresh && !isStale) return@withContext cached

        try {
            val body = client.newCall(Request.Builder().url(manifestUrl).build())
                .execute().use { resp ->
                    if (!resp.isSuccessful) null else resp.body?.string()
                }
            val parsed = body?.let { parseManifest(it) }
            if (parsed != null) {
                _manifest.value = parsed
                try {
                    cache.writeText(body)
                } catch (_: Exception) {
                    // cache write failure is non-fatal
                }
            }
            parsed ?: cached
        } catch (_: Exception) {
            cached // offline: cache (any age) beats nothing
        }
    }

    /** Resolved playback info for a bundled lecture key, honoring the selected language. */
    fun lectureMedia(bundledVideoUrl: String, language: String): LectureMedia {
        if (bundledVideoUrl.isBlank()) return LectureMedia("", hasHindi = false)
        val entry = _manifest.value?.videos?.get(bundledVideoUrl)
        val enRemote = entry?.en?.takeIf { it.isNotBlank() }
        val hiRemote = entry?.hi?.takeIf { it.isNotBlank() }
        val hiBundled = rawResourceExists(hiRawName(bundledVideoUrl))
        val hasHindi = hiRemote != null || hiBundled

        val resolved = when {
            language == LANG_HI && hiRemote != null -> hiRemote
            language == LANG_HI && hiBundled -> hiRawName(bundledVideoUrl)
            enRemote != null -> enRemote
            else -> bundledVideoUrl
        }
        val source = if (entry != null && (enRemote != null || hiRemote != null)) {
            LectureMedia.SOURCE_REMOTE
        } else {
            LectureMedia.SOURCE_BUNDLED
        }
        return LectureMedia(resolved, hasHindi, source)
    }

    /** Whether a Hindi variant exists (remote manifest or bundled raw resource). */
    fun hasHindi(bundledVideoUrl: String): Boolean =
        lectureMedia(bundledVideoUrl, LANG_HI).hasHindi

    internal fun parseManifest(json: String): VideoManifest? = try {
        manifestAdapter.fromJson(json)
    } catch (_: Exception) {
        null
    }

    private fun readCache(cache: File): VideoManifest? = try {
        if (cache.exists()) parseManifest(cache.readText()) else null
    } catch (_: Exception) {
        null
    }

    private fun cacheFile(): File = File(context.cacheDir, CACHE_FILE)

    /** Hindi raw resource names must be lowercase (Android resource naming). */
    private fun hiRawName(key: String): String =
        key.removeSuffix(".mp4").substringAfterLast("/") + "_hi"

    private fun rawResourceExists(name: String): Boolean =
        name.isNotBlank() &&
            context.resources.getIdentifier(name, "raw", context.packageName) != 0

    companion object {
        const val MANIFEST_URL =
            "https://firebasestorage.googleapis.com/v0/b/tradelab-4f858.firebasestorage.app" +
                "/o/videos%2Fmanifest.json?alt=media"
        const val LANG_EN = "en"
        const val LANG_HI = "hi"
        private const val CACHE_FILE = "academy_video_manifest.json"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L
    }
}
