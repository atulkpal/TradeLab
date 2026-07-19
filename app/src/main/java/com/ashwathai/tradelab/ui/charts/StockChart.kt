package com.ashwathai.tradelab.ui.charts

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
fun StockLineChart(
    pricesString: String,
    isPositive: Boolean,
    showIndicators: Boolean = false,
    modifier: Modifier = Modifier
) {
    val prices = remember(pricesString) {
        pricesString.split(",").mapNotNull { it.trim().toDoubleOrNull() }
    }
    if (prices.size < 2) return

    val minPrice = prices.minOrNull() ?: 0.0
    val maxPrice = prices.maxOrNull() ?: 1.0
    val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice

    var activeIndex by remember { mutableStateOf<Int?>(null) }
    var selectedIndicator by remember { mutableStateOf("SMA") } // "None", "SMA", "EMA", "RSI"

    Column(modifier = modifier) {
        // Indicator selection tabs (only if showIndicators is true)
        if (showIndicators) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("None", "SMA", "EMA", "RSI").forEach { ind ->
                    val active = selectedIndicator == ind
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) BrandViolet.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                            .border(1.dp, if (active) BrandViolet else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { selectedIndicator = ind }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ind,
                            color = if (active) BrandViolet else Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        val mainChartWeight = if (showIndicators && selectedIndicator == "RSI") 0.65f else 1.0f

        // Main Price Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(mainChartWeight)
                .pointerInput(prices) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val colWidth = size.width / (prices.size - 1)
                        var idx = (down.position.x / colWidth).roundToInt().coerceIn(prices.indices)
                        activeIndex = idx
                        down.consume()

                        while (true) {
                            val event = awaitPointerEvent()
                            val anyPressed = event.changes.any { it.pressed }
                            if (!anyPressed) {
                                activeIndex = null
                                break
                            }
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                idx = (change.position.x / colWidth).roundToInt().coerceIn(prices.indices)
                                activeIndex = idx
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val points = prices.mapIndexed { index, price ->
                    val x = index * (width / (prices.size - 1))
                    val y = height - ((price - minPrice) / priceRange * height).toFloat()
                    Offset(x, y)
                }

                val strokeColor = if (isPositive) AccentGreen else AccentRose
                val gradientColor = if (isPositive) AccentGreenDark else AccentRoseDark

                val fillPath = Path().apply {
                    moveTo(0f, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(width, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientColor.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = strokeColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw 5-period SMA
                if (showIndicators && selectedIndicator == "SMA" && prices.size >= 5) {
                    val smaValues = mutableListOf<Double>()
                    for (i in prices.indices) {
                        if (i < 4) {
                            smaValues.add(prices[i])
                        } else {
                            val sum = prices.subList(i - 4, i + 1).sum()
                            smaValues.add(sum / 5.0)
                        }
                    }
                    val smaPoints = smaValues.mapIndexed { index, price ->
                        val x = index * (width / (prices.size - 1))
                        val y = height - ((price - minPrice) / priceRange * height).toFloat()
                        Offset(x, y)
                    }

                    val smaPath = Path().apply {
                        moveTo(smaPoints.first().x, smaPoints.first().y)
                        for (i in 1 until smaPoints.size) {
                            lineTo(smaPoints[i].x, smaPoints[i].y)
                        }
                    }
                    drawPath(
                        path = smaPath,
                        color = AccentYellow,
                        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Draw 5-period EMA
                if (showIndicators && selectedIndicator == "EMA" && prices.size >= 5) {
                    val emaValues = mutableListOf<Double>()
                    val k = 2.0 / (5 + 1)
                    var prevEma = prices.first()
                    emaValues.add(prevEma)
                    for (i in 1 until prices.size) {
                        val currentEma = prices[i] * k + prevEma * (1 - k)
                        emaValues.add(currentEma)
                        prevEma = currentEma
                    }
                    val emaPoints = emaValues.mapIndexed { index, price ->
                        val x = index * (width / (prices.size - 1))
                        val y = height - ((price - minPrice) / priceRange * height).toFloat()
                        Offset(x, y)
                    }

                    val emaPath = Path().apply {
                        moveTo(emaPoints.first().x, emaPoints.first().y)
                        for (i in 1 until emaPoints.size) {
                            lineTo(emaPoints[i].x, emaPoints[i].y)
                        }
                    }
                    drawPath(
                        path = emaPath,
                        color = Color(0xFF00E5FF),
                        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Draw scrubbing cursor and dots
                if (activeIndex != null) {
                    val index = activeIndex!!
                    val point = points[index]
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(point.x, 0f),
                        end = Offset(point.x, height),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawCircle(
                        color = strokeColor,
                        radius = 10f,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = point
                    )
                }
            }

            // Drag indicator overlay pill
            if (activeIndex != null) {
                val index = activeIndex!!
                val scrubbedPrice = prices[index]
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Price: ${String.format("%.2f", scrubbedPrice)} • Tick ${index + 1}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // RSI Sub-graph Oscillator panel
        if (showIndicators && selectedIndicator == "RSI") {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Relative Strength Index (RSI-5)",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw boundaries (70 and 30 levels)
                    val y70 = height * 0.3f
                    val y30 = height * 0.7f

                    // Oversold / Overbought band
                    drawRect(
                        color = Color(0xFF9C27B0).copy(alpha = 0.08f),
                        topLeft = Offset(0f, y70),
                        size = Size(width, y30 - y70)
                    )

                    drawLine(
                        color = Color(0xFF9C27B0).copy(alpha = 0.3f),
                        start = Offset(0f, y70),
                        end = Offset(width, y70),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                    drawLine(
                        color = Color(0xFF9C27B0).copy(alpha = 0.3f),
                        start = Offset(0f, y30),
                        end = Offset(width, y30),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )

                    // Calculate RSI-5
                    val rsiValues = mutableListOf<Double>()
                    for (i in prices.indices) {
                        if (i < 5) {
                            rsiValues.add(50.0)
                            continue
                        }

                        var gainsSum = 0.0
                        var lossesSum = 0.0

                        for (j in (i - 5 + 1)..i) {
                            val change = prices[j] - prices[j - 1]
                            if (change > 0) {
                                gainsSum += change
                            } else {
                                lossesSum -= change
                            }
                        }

                        val avgGain = gainsSum / 5
                        val avgLoss = lossesSum / 5

                        if (avgLoss == 0.0) {
                            rsiValues.add(100.0)
                        } else {
                            val rs = avgGain / avgLoss
                            val currentRsi = 100.0 - (100.0 / (1.0 + rs))
                            rsiValues.add(currentRsi)
                        }
                    }

                    val rsiPoints = rsiValues.mapIndexed { index, valRsi ->
                        val x = index * (width / (prices.size - 1))
                        val y = height - (valRsi.toFloat() / 100f * height)
                        Offset(x, y)
                    }

                    val rsiPath = Path().apply {
                        moveTo(rsiPoints.first().x, rsiPoints.first().y)
                        for (i in 1 until rsiPoints.size) {
                            lineTo(rsiPoints[i].x, rsiPoints[i].y)
                        }
                    }

                    drawPath(
                        path = rsiPath,
                        color = Color(0xFFE040FB),
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw vertical scrubbing cursor if active
                    if (activeIndex != null) {
                        val index = activeIndex!!
                        val pointX = index * (width / (prices.size - 1))
                        val rsiVal = rsiValues[index]
                        val pointY = height - (rsiVal.toFloat() / 100f * height)

                        drawLine(
                            color = Color.White.copy(alpha = 0.3f),
                            start = Offset(pointX, 0f),
                            end = Offset(pointX, height),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawCircle(
                            color = Color(0xFFE040FB),
                            radius = 6f,
                            center = Offset(pointX, pointY)
                        )
                    }
                }

                // RSI text reading display
                val rsiValuesList = remember(prices) {
                    val rsiValues = mutableListOf<Double>()
                    for (i in prices.indices) {
                        if (i < 5) {
                            rsiValues.add(50.0)
                            continue
                        }

                        var gainsSum = 0.0
                        var lossesSum = 0.0

                        for (j in (i - 5 + 1)..i) {
                            val change = prices[j] - prices[j - 1]
                            if (change > 0) {
                                gainsSum += change
                            } else {
                                lossesSum -= change
                            }
                        }

                        val avgGain = gainsSum / 5
                        val avgLoss = lossesSum / 5

                        if (avgLoss == 0.0) {
                            rsiValues.add(100.0)
                        } else {
                            val rs = avgGain / avgLoss
                            val currentRsi = 100.0 - (100.0 / (1.0 + rs))
                            rsiValues.add(currentRsi)
                        }
                    }
                    rsiValues
                }

                val displayRsiVal = if (activeIndex != null) {
                    rsiValuesList.getOrNull(activeIndex!!) ?: 50.0
                } else {
                    rsiValuesList.lastOrNull() ?: 50.0
                }

                Text(
                    text = "RSI: ${String.format("%.1f", displayRsiVal)} ${if (displayRsiVal >= 70.0) "🔥 OVERBOUGHT" else if (displayRsiVal <= 30.0) "❄️ OVERSOLD" else "NORMAL"}",
                    color = if (displayRsiVal >= 70.0) AccentRose else if (displayRsiVal <= 30.0) AccentGreen else Color.White.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp)
                )
            }
        }
    }
}


