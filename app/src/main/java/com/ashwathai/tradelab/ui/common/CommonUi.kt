package com.ashwathai.tradelab.ui.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Size
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ashwathai.tradelab.data.*
import kotlinx.coroutines.launch
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.QuizModule
import com.ashwathai.tradelab.ui.Lecture
import com.ashwathai.tradelab.ui.Mission
import com.ashwathai.tradelab.ui.theme.*
import com.ashwathai.tradelab.ui.AuthScreen
import com.ashwathai.tradelab.BuildConfig
import com.ashwathai.tradelab.ui.common.*
import com.ashwathai.tradelab.ui.charts.*
import com.ashwathai.tradelab.ui.portfolio.*
import com.ashwathai.tradelab.ui.watchlist.*
import com.ashwathai.tradelab.ui.academy.*
import com.ashwathai.tradelab.ui.derivatives.*
import com.ashwathai.tradelab.ui.commodities.*
import com.ashwathai.tradelab.ui.profile.*

@Composable
fun DataModeToggle(
    isSimulatedMode: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(20.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Live Button (L)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (!isSimulatedMode) AccentGreen.copy(alpha = 0.2f) else Color.Transparent)
                .clickable { onToggle(false) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LIVE",
                color = if (!isSimulatedMode) AccentGreen else TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Simulated Button (S)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSimulatedMode) BrandViolet.copy(alpha = 0.2f) else Color.Transparent)
                .clickable { onToggle(true) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SIM",
                color = if (isSimulatedMode) BrandViolet else TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun HeaderBar(
    title: String,
    riskLevel: String,
    isSimulatedMode: Boolean = true,
    onToggleSimulated: (Boolean) -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val avatarGradient = remember(riskLevel) {
        when (riskLevel) {
            "Aggressive" -> Brush.linearGradient(listOf(Color(0xFFFB7185), Color(0xFFE11D48)))
            "Conservative" -> Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB)))
            else -> Brush.linearGradient(listOf(Color(0xFFC084FC), Color(0xFF9333EA)))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // Fixed height to prevent vertical jitter between tabs
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Title and Metadata
        Column(modifier = Modifier.weight(1.5f)) { // Increased weight for title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false) // Allow title to take only what it needs
                )
                if (BuildConfig.DEBUG) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentRose.copy(alpha = 0.15f))
                            .border(0.5.dp, AccentRose.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "DEV",
                            color = AccentRose,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            Text(
                text = "PAPER • $riskLevel Risk",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp
            )
        }

        // Center: Data Mode Toggle (Developer-only)
        if (BuildConfig.DEBUG) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                DataModeToggle(
                    isSimulatedMode = isSimulatedMode,
                    onToggle = onToggleSimulated
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Right: Actions (Extreme Right) and Avatar
        Row(
            modifier = Modifier.weight(1f), // Reduced weight
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            actions()
            
            if (!BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(avatarGradient)
                        .border(1.dp, Color(0xFF222222), CircleShape)
                )
            }
        }
    }
}


data class TabItem(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

val MainTabs = listOf(
    TabItem("Portfolio", Icons.Default.Home),
    TabItem("Watchlist", Icons.Default.FormatListBulleted),
    TabItem("Commodities", Icons.Default.TrendingUp),
    TabItem("F&O", Icons.Default.Analytics),
    TabItem("Academy", Icons.Default.School),
    TabItem("Profile", Icons.Default.Person)
)

@Composable
fun BottomNavBar(currentTab: String, onTabSelected: (String) -> Unit) {
    Surface(
        color = DarkBg,
        border = BorderStroke(1.dp, Color(0xFF1F1F1F)),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTabs.forEach { tab ->
                val isSelected = currentTab == tab.name
                val tintColor = if (isSelected) BrandViolet else Color.White.copy(alpha = 0.4f)

                Column(
                    modifier = Modifier
                        .testTag("nav_tab_${tab.name.lowercase().replace(" ", "_")}")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab.name) }
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1A1A1A) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) BrandViolet.copy(alpha = 0.3f) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.name,
                            tint = tintColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.name,
                        color = tintColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ConfettiOverlay(trigger: Long) {
    if (trigger == 0L) return

    var particles by remember(trigger) {
        mutableStateOf(List(100) {
            ParticleItem(
                x = (0..1000).random().toFloat() / 1000f,
                y = -0.1f - (0..800).random().toFloat() / 1000f,
                vx = ((-150..150).random().toFloat() / 1000f) * 0.05f,
                vy = (120..350).random().toFloat() / 1000f * 0.12f,
                color = listOf(
                    Color(0xFFFBBF24), // Gold / Glitter
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFFEC4899), // Pink
                    Color(0xFF10B981), // Emerald
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFFFF3B30), // Red
                    Color(0xFFFFD700)  // Brilliant Gold
                ).random(),
                size = (10..24).random().toFloat(),
                rotation = (0..360).random().toFloat(),
                rotationSpeed = (-18..18).random().toFloat(),
                shape = (0..2).random()
            )
        })
    }

    var isRunning by remember(trigger) { mutableStateOf(true) }

    LaunchedEffect(trigger) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 3500) {
            androidx.compose.runtime.withFrameMillis { frameTime ->
                particles = particles.map { p ->
                    p.copy(
                        x = (p.x + p.vx).coerceIn(-0.1f, 1.1f),
                        y = p.y + p.vy,
                        rotation = (p.rotation + p.rotationSpeed) % 360f,
                        vy = p.vy + 0.0015f
                    )
                }
            }
        }
        isRunning = false
    }

    if (isRunning) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            particles.forEach { p ->
                val px = p.x * width
                val py = p.y * height
                if (py in -100f..height + 100f) {
                    drawContext.canvas.save()
                    drawContext.transform.rotate(p.rotation, Offset(px, py))
                    when (p.shape) {
                        0 -> {
                            drawCircle(
                                color = p.color,
                                radius = p.size / 2,
                                center = Offset(px, py)
                            )
                        }
                        1 -> {
                            drawRect(
                                color = p.color,
                                topLeft = Offset(px - p.size / 2, py - p.size / 2),
                                size = Size(p.size, p.size * 0.6f)
                            )
                        }
                        else -> {
                            val path = Path().apply {
                                moveTo(px, py - p.size / 2)
                                lineTo(px - p.size / 2, py + p.size / 2)
                                lineTo(px + p.size / 2, py + p.size / 2)
                                close()
                            }
                            drawPath(path = path, color = p.color)
                        }
                    }
                    drawContext.canvas.restore()
                }
            }
        }
    }
}

data class ParticleItem(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: Int
)


