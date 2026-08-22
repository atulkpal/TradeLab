package com.ashwathai.tradelab.ui.common

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ashwathai.tradelab.ui.theme.BrandViolet
import kotlinx.coroutines.launch
import java.io.File

private fun extractYouTubeVideoId(url: String): String? {
    val patterns = listOf(
        Regex("""youtube\.com/embed/([a-zA-Z0-9_-]{11})"""),
        Regex("""youtube\.com/watch\?v=([a-zA-Z0-9_-]{11})"""),
        Regex("""youtube\.com/shorts/([a-zA-Z0-9_-]{11})"""),
        Regex("""youtu\.be/([a-zA-Z0-9_-]{11})"""),
        Regex("""youtube\.com/v/([a-zA-Z0-9_-]{11})"""),
    )
    for (pattern in patterns) {
        pattern.find(url)?.let { return it.groupValues[1] }
    }
    return null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerView(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    if (videoUrl.isBlank()) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isYouTube = videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")
    val isFirebaseStorage = videoUrl.contains("firebasestorage.googleapis.com") ||
            videoUrl.startsWith("gs://")

    val cacheManager = remember { VideoCacheManager(context) }

    // State for Firebase download
    var localPath by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    // For local resource files (existing behavior)
    val localFile = remember(videoUrl) {
        if (!isYouTube && !isFirebaseStorage) {
            // Try to resolve as raw resource - handles both "video_name" and "video_name.mp4"
            val resName = if (videoUrl.endsWith(".mp4")) {
                videoUrl.substringAfterLast("/").removeSuffix(".mp4")
            } else {
                videoUrl.substringAfterLast("/")
            }
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (resId != 0) {
                val cacheDir = File(context.cacheDir, "videos")
                cacheDir.mkdirs()
                val outFile = File(cacheDir, "$resName.mp4")
                if (!outFile.exists()) {
                    context.resources.openRawResource(resId).use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                outFile
            } else null
        } else null
    }

    // Extract lecture code from Firebase URL for caching
    val lectureCode = remember(videoUrl) {
        if (isFirebaseStorage) {
            // Extract filename from URL: .../videos/course_1/lecture_1_1_1.mp4 -> lecture_1_1_1
            videoUrl.substringAfterLast("/").substringBefore(".mp4")
        } else null
    }

    // Check cache and download if needed
    LaunchedEffect(videoUrl, lectureCode) {
        if (isFirebaseStorage && lectureCode != null) {
            val cached = cacheManager.getLocalVideoPath(lectureCode)
            if (cached != null) {
                localPath = cached
            } else {
                isDownloading = true
                cacheManager.downloadVideo(lectureCode, videoUrl) { progress ->
                    downloadProgress = progress
                }.onSuccess { path ->
                    localPath = path
                }.onFailure { e ->
                    downloadError = e.message
                }
                isDownloading = false
            }
        }
    }

    // Determine the final URL to play
    val playbackUrl = remember(localPath, localFile, videoUrl) {
        localPath ?: localFile?.absolutePath ?: videoUrl
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Text(
            text = "VIDEO LECTURE",
            color = BrandViolet,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            when {
                // Show download spinner
                isDownloading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            color = BrandViolet,
                            trackColor = BrandViolet.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Downloading video... ${(downloadProgress * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Show error
                downloadError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Failed to load video",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = downloadError ?: "",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Play YouTube embed
                isYouTube -> {
                    val videoId = extractYouTubeVideoId(videoUrl)
                    if (videoId != null) {
                        YouTubeWebView(videoId)
                    }
                }

                // Play local video (Firebase cached or resource file)
                else -> {
                    LocalVideoWebView(playbackUrl)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeWebView(videoId: String) {
    AndroidView(
        factory = { ctx ->
            CookieManager.getInstance().setAcceptCookie(true)

            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                val ua = settings.userAgentString
                    .replace("; wv", "")
                    .replace("AndroidWebView", "Chrome")
                settings.userAgentString = ua

                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            * { margin: 0; padding: 0; }
                            body { background: #000; overflow: hidden; }
                            iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none; }
                        </style>
                    </head>
                    <body>
                        <iframe 
                            src="https://www.youtube.com/embed/$videoId?autoplay=1&rel=0&playsinline=1&enablejsapi=1" 
                            referrerpolicy="strict-origin-when-cross-origin"
                            allow="autoplay; encrypted-media; picture-in-picture"
                            allowfullscreen>
                        </iframe>
                    </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LocalVideoWebView(videoUrl: String) {
    val actualUrl = if (videoUrl.startsWith("/")) "file://$videoUrl" else videoUrl

    AndroidView(
        factory = { ctx ->
            CookieManager.getInstance().setAcceptCookie(true)

            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                val ua = settings.userAgentString
                    .replace("; wv", "")
                    .replace("AndroidWebView", "Chrome")
                settings.userAgentString = ua

                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            * { margin: 0; padding: 0; box-sizing: border-box; }
                            body { background: #000; display: flex; align-items: center; justify-content: center; height: 100vh; overflow: hidden; }
                            video { width: 100%; height: auto; }
                        </style>
                    </head>
                    <body>
                        <video controls autoplay playsinline preload="auto">
                            <source src="$actualUrl" type="video/mp4">
                            Your browser does not support video.
                        </video>
                    </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
    )
}
