package com.ashwathai.tradelab.ui.common

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Manages video caching in external cache.
 * Videos are downloaded once from Firebase Storage and played locally thereafter.
 */
class VideoCacheManager(private val context: Context) {

    companion object {
        private const val TAG = "VideoCacheManager"
        private const val VIDEO_DIR = "cached_videos"
    }

    private val storage = FirebaseStorage.getInstance()

    private fun getVideoDir(): File {
        val dir = File(context.externalCacheDir, VIDEO_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun isVideoCached(lectureCode: String): Boolean {
        val file = File(getVideoDir(), "$lectureCode.mp4")
        return file.exists() && file.length() > 0
    }

    fun getLocalVideoPath(lectureCode: String): String? {
        val file = File(getVideoDir(), "$lectureCode.mp4")
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    /**
     * Download video from Firebase Storage URL to external cache.
     * Returns the local file path on success.
     */
    suspend fun downloadVideo(
        lectureCode: String,
        firebaseUrl: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> = runCatching {
        val localFile = File(getVideoDir(), "$lectureCode.mp4")

        if (localFile.exists() && localFile.length() > 0) {
            Log.d(TAG, "Video already cached: $lectureCode")
            return@runCatching localFile.absolutePath
        }

        Log.d(TAG, "Downloading video: $lectureCode from $firebaseUrl")

        val storageRef = storage.getReferenceFromUrl(firebaseUrl)

        // Download to temp file first, then rename (atomic operation)
        val tempFile = File(getVideoDir(), "$lectureCode.mp4.tmp")

        storageRef.getFile(tempFile).await()

        // Rename temp to final
        if (!tempFile.renameTo(localFile)) {
            // If rename fails (cross-device), copy and delete
            tempFile.copyTo(localFile, overwrite = true)
            tempFile.delete()
        }

        Log.d(TAG, "Video cached: $lectureCode (${localFile.length()} bytes)")
        localFile.absolutePath
    }

    fun clearCache() {
        val dir = getVideoDir()
        dir.listFiles()?.forEach { it.delete() }
        Log.d(TAG, "Video cache cleared")
    }

    fun getCacheSize(): Long {
        return getVideoDir().listFiles()?.sumOf { it.length() } ?: 0L
    }
}
