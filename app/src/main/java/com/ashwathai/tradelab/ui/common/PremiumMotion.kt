package com.ashwathai.tradelab.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ashwathai.tradelab.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * App-wide premium motion foundation. All infinite animations are gated behind
 * [LocalPremiumMotionEnabled] so tests and reduced-motion environments can turn
 * them off without blocking composition idleness.
 */
val LocalPremiumMotionEnabled = staticCompositionLocalOf { true }

object PremiumSpecs {
    val BouncySpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val GentleSpring = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    val SnappySpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
    val StandardTween = tween<Float>(durationMillis = 320, easing = FastOutSlowInEasing)
    const val EntranceStaggerMs = 70L
}

/**
 * Staggered fade + slide-in entrance. Each successive composable (by [index])
 * appears a little later, giving lists a premium cascade feel.
 */
fun Modifier.premiumEntrance(
    index: Int,
    staggerMs: Long = PremiumSpecs.EntranceStaggerMs,
    offsetY: Dp = 24.dp
): Modifier = composed {
    val motion = LocalPremiumMotionEnabled.current
    val progress = remember { Animatable(if (motion) 0f else 1f) }
    val density = LocalDensity.current
    val yPx = with(density) { offsetY.toPx() }
    androidx.compose.runtime.LaunchedEffect(index, motion) {
        if (motion) {
            delay(index * staggerMs)
            progress.animateTo(1f, animationSpec = PremiumSpecs.GentleSpring)
        }
    }
    graphicsLayer {
        alpha = progress.value
        translationY = yPx * (1f - progress.value)
    }
}

/**
 * Pulsing neon border glow. When disabled (or motion is globally off) it renders
 * a static, low-alpha border so tests stay deterministic.
 */
@Composable
private fun neonGlowColor(color: Color, enabled: Boolean): Color {
    if (!enabled) return color.copy(alpha = 0.18f)
    if (!LocalPremiumMotionEnabled.current) return color.copy(alpha = 0.45f)
    val infinite = rememberInfiniteTransition(label = "neonGlow")
    val pulse by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(animation = tween(1100), repeatMode = RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    return color.copy(alpha = pulse)
}

fun Modifier.neonGlowPulse(
    color: Color,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp)
): Modifier = composed {
    val borderColor by animateColorAsState(
        targetValue = neonGlowColor(color, enabled),
        animationSpec = tween(300),
        label = "neonBorder"
    )
    border(1.5.dp, borderColor, shape)
}

@Composable
private fun shimmerSweepPosition(motion: Boolean): Float {
    if (!motion) return 0f
    val infinite = rememberInfiniteTransition(label = "shimmer")
    return infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1600), repeatMode = RepeatMode.Reverse),
        label = "shimmerX"
    ).value
}

/**
 * Custom progress bar with a soft shimmer sweep across the filled region.
 */
@Composable
fun ShimmerProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0x0DFFFFFF)
) {
    val motion = LocalPremiumMotionEnabled.current
    val x = shimmerSweepPosition(motion)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val radius = CornerRadius(h / 2f, h / 2f)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        val fillW = w * progress.coerceIn(0f, 1f)
        if (fillW > 0f) {
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(fillW, h),
                cornerRadius = radius
            )
            if (motion) {
                val sweep = w * 0.45f
                val cx = w * x
                val brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent)
                )
                drawRect(
                    brush = brush,
                    topLeft = Offset(cx - sweep / 2f, 0f),
                    size = Size(sweep, h)
                )
            }
        }
    }
}

/**
 * Animated 180-degree chevron that flips to indicate expand/collapse state.
 */
@Composable
fun RotatingChevron(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = TextMuted
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = PremiumSpecs.SnappySpring,
        label = "chevron"
    )
    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = if (expanded) "Collapse" else "Expand",
        tint = tint,
        modifier = modifier.graphicsLayer { rotationZ = rotation }
    )
}

/**
 * Drives a horizontal shake for locked/invalid interactions. Call [shake] and
 * apply the controller via [Modifier.lockShake] to the target composable.
 */
class LockShakeController {
    private val animatable = Animatable(0f)

    suspend fun shake() {
        if (animatable.value != 0f) animatable.snapTo(0f)
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 360
                -14f at 40
                14f at 120
                -9f at 200
                7f at 280
                0f at 360
            }
        )
    }

    internal val offset: Float
        get() = animatable.value
}

@Composable
fun rememberLockShakeController(): LockShakeController = remember { LockShakeController() }

fun Modifier.lockShake(controller: LockShakeController): Modifier = composed {
    graphicsLayer { translationX = controller.offset }
}

/**
 * One-shot sparkle burst radiating from the center. Finite animation so it does
 * not interfere with test idleness.
 */
@Composable
fun SparkleBurst(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    color: Color = DynamicPrimary,
    particleCount: Int = 10
) {
    val progress = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(trigger) {
        if (trigger) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        }
    }
    val motion = LocalPremiumMotionEnabled.current
    val seed = remember(trigger) { Random.nextInt() }
    Canvas(modifier.fillMaxSize()) {
        if (!motion) return@Canvas
        val cx = size.width / 2f
        val cy = size.height / 2f
        val random = Random(seed)
        val radius = size.minDimension * (0.25f + 0.45f * progress.value)
        val alpha = (1f - progress.value).coerceIn(0f, 1f)
        repeat(particleCount) { i ->
            val angle = 2f * PI.toFloat() * i / particleCount + (seed % 7)
            val len = 4f + random.nextFloat() * 10f
            val end = Offset(cx + cos(angle) * radius, cy + sin(angle) * radius)
            val start = Offset(cx + cos(angle) * radius * 0.6f, cy + sin(angle) * radius * 0.6f)
            drawLine(
                color = color.copy(alpha = alpha),
                start = start,
                end = end,
                strokeWidth = len * (1f - progress.value).coerceIn(0f, 1f) + 0.5f
            )
        }
    }
}

/**
 * Finite falling-confetti overlay for celebratory moments (e.g., quiz pass).
 * Caller controls visibility; the burst self-completes.
 */
@Composable
fun ConfettiOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(DynamicPrimary, AccentYellow, AccentGreen, AccentRose)
) {
    if (!visible) return
    val progress = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(visible) {
        if (visible) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(2400))
        }
    }
    val motion = LocalPremiumMotionEnabled.current
    val seed = remember { Random.nextInt() }
    Canvas(modifier.fillMaxWidth().zIndex(1f)) {
        if (!motion) return@Canvas
        val random = Random(seed)
        val count = 42
        val t = progress.value
        repeat(count) { i ->
            val baseX = (i * 0.024f * size.width) % size.width
            val drift = sin((i * 1.7f) + t * 4f) * 14f
            val x = baseX + drift
            val y = ((t * 1.15f + i * 0.03f) % 1f) * size.height
            val width = 4f + random.nextFloat() * 5f
            val height = 6f + random.nextFloat() * 8f
            val alpha = if (t > 0.85f) (1f - t) / 0.15f else 1f
            rotate(degrees = t * 360f * (0.5f + random.nextFloat()), pivot = Offset(x, y)) {
                drawRect(
                    color = colors[i % colors.size].copy(alpha = alpha.coerceIn(0f, 1f)),
                    topLeft = Offset(x - width / 2f, y - height / 2f),
                    size = Size(width, height)
                )
            }
        }
    }
}

/**
 * Convenience wrapper for composables that want reduced-motion off at a subtree level.
 */
@Composable
fun DisablePremiumMotion(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPremiumMotionEnabled provides false) {
        content()
    }
}
