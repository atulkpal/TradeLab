package com.ashwathai.tradelab.ui.charts

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.ui.theme.*
import kotlin.math.roundToInt

data class CandleData(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

@Composable
fun StockLineChart(
    pricesString: String,
    isPositive: Boolean,
    showIndicators: Boolean = false,
    mini: Boolean = false,
    modifier: Modifier = Modifier
) {
    val candles = remember(pricesString) {
        if (pricesString.contains("|")) {
            pricesString.split(";").mapNotNull { segment ->
                val parts = segment.split("|")
                if (parts.size >= 6) {
                    CandleData(
                        timestamp = parts[0].toLongOrNull() ?: 0L,
                        open = parts[1].toDoubleOrNull() ?: 0.0,
                        high = parts[2].toDoubleOrNull() ?: 0.0,
                        low = parts[3].toDoubleOrNull() ?: 0.0,
                        close = parts[4].toDoubleOrNull() ?: 0.0,
                        volume = parts[5].toLongOrNull() ?: 0L
                    )
                } else null
            }
        } else {
            // Fallback for old format
            pricesString.split(",").mapNotNull { it.trim().toDoubleOrNull() }.map { 
                CandleData(0L, it, it, it, it, 0L)
            }
        }
    }
    
    if (candles.size < 2) return

    // Zoom & Pan States
    var visibleCount by remember { mutableFloatStateOf(candles.size.toFloat()) }
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    
    val currentCandles = remember(candles, visibleCount, scrollOffset) {
        val count = visibleCount.roundToInt().coerceIn(2, candles.size)
        val maxOffset = (candles.size - count).toFloat()
        val offset = scrollOffset.coerceIn(0f, maxOffset).roundToInt()
        candles.subList(offset, (offset + count).coerceAtMost(candles.size))
    }

    val prices = remember(currentCandles) { currentCandles.map { it.close } }
    val minPrice = currentCandles.minOf { it.low }
    val maxPrice = currentCandles.maxOf { it.high }
    val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice

    var activeIndex by remember { mutableStateOf<Int?>(null) }
    var selectedIndicator by remember { mutableStateOf("None") }
    var chartMode by remember { mutableStateOf(if (showIndicators) "Candle" else "Line") }

    Column(modifier = modifier) {
        if (showIndicators) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { chartMode = if (chartMode == "Line") "Candle" else "Line" },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (chartMode == "Line") Icons.Default.ShowChart else Icons.Default.BarChart,
                        contentDescription = "Toggle Chart Mode",
                        tint = BrandViolet,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.1f)))

                val indicators = listOf("None", "SMA", "EMA", "RSI", "BOL", "MACD")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(indicators) { ind ->
                        val active = selectedIndicator == ind
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) BrandViolet.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                                .border(1.dp, if (active) BrandViolet else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { selectedIndicator = ind }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = ind, color = if (active) BrandViolet else Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Zoom Controls
                IconButton(onClick = { visibleCount = (visibleCount * 0.8f).coerceAtLeast(5f) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = { visibleCount = (visibleCount * 1.2f).coerceAtMost(candles.size.toFloat()) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                }
            }
        }

        val mainChartWeight = if (showIndicators && (selectedIndicator == "RSI" || selectedIndicator == "MACD")) 0.65f else 1.0f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(mainChartWeight)
                .pointerInput(candles.size) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val colWidth = size.width / (currentCandles.size - 1)
                            activeIndex = (offset.x / colWidth).roundToInt().coerceIn(currentCandles.indices)
                        },
                        onDragEnd = { activeIndex = null },
                        onDragCancel = { activeIndex = null },
                        onHorizontalDrag = { change, dragAmount ->
                            if (activeIndex != null) {
                                val colWidth = size.width / (currentCandles.size - 1)
                                activeIndex = (change.position.x / colWidth).roundToInt().coerceIn(currentCandles.indices)
                            } else {
                                // Pan logic
                                val panSpeed = visibleCount / size.width
                                scrollOffset -= dragAmount * panSpeed
                            }
                            change.consume()
                        }
                    )
                }
                .pointerInput(candles.size) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            val colWidth = size.width / (currentCandles.size - 1)
                            activeIndex = (offset.x / colWidth).roundToInt().coerceIn(currentCandles.indices)
                        },
                        onTap = { activeIndex = null }
                    )
                }
        ) {
            if (chartMode == "Candle") {
                CandlestickChart(candles = currentCandles, modifier = Modifier.fillMaxSize())
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val axisLeftPad = if (mini) 0f else 44f
                    val axisBottomPad = if (mini) 0f else 20f
                    val chartWidth = width - axisLeftPad
                    val chartHeight = height - axisBottomPad

                    val points = prices.mapIndexed { index, price ->
                        val x = if (mini) index * (width / (prices.size - 1).coerceAtLeast(1))
                                else index * (chartWidth / (prices.size - 1).coerceAtLeast(1)) + axisLeftPad
                        val y = chartHeight - ((price - minPrice) / priceRange * chartHeight).toFloat()
                        Offset(x, y)
                    }

                    if (!mini) {
                        // Y-axis grid lines & labels
                        val labelCount = 5
                        (0 until labelCount).forEach { i ->
                            val pct = i.toDouble() / (labelCount - 1)
                            val price = minPrice + (priceRange * pct)
                            val y = chartHeight - (pct * chartHeight).toFloat()
                            drawLine(
                                color = Color.White.copy(alpha = 0.06f),
                                start = Offset(axisLeftPad, y),
                                end = Offset(width, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format("%.1f", price),
                                axisLeftPad - 6f,
                                y + 4f,
                                android.graphics.Paint().apply {
                                    color = 0x66FFFFFF
                                    textSize = 20f
                                    textAlign = android.graphics.Paint.Align.RIGHT
                                }
                            )
                        }

                        // X-axis labels
                        val xLabelCount = 4
                        (0 until xLabelCount).forEach { i ->
                            val idx = (i.toDouble() / (xLabelCount - 1) * (currentCandles.size - 1)).toInt().coerceIn(0, currentCandles.size - 1)
                            val x = idx * (chartWidth / (currentCandles.size - 1)) + axisLeftPad
                            val label = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(currentCandles[idx].timestamp))
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(x, 0f),
                                end = Offset(x, chartHeight),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x,
                                height - 4f,
                                android.graphics.Paint().apply {
                                    color = 0x44FFFFFF
                                    textSize = 18f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }

                    val strokeColor = if (isPositive) AccentGreen else AccentRose

                    if (!mini) {
                        val gradientColor = if (isPositive) AccentGreenDark else AccentRoseDark
                        val fillPath = Path().apply {
                            moveTo(axisLeftPad, chartHeight)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(chartWidth + axisLeftPad, chartHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(gradientColor.copy(alpha = 0.3f), Color.Transparent),
                                startY = 0f,
                                endY = chartHeight
                            )
                        )
                    }

                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                    }
                    drawPath(path = linePath, color = strokeColor, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            if (!mini) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val axisLeftPad = 44f
                val chartHeight = height - 20f
                fun getPoint(price: Double, index: Int): Offset {
                    val x = index * ((width - axisLeftPad) / (currentCandles.size - 1)) + axisLeftPad
                    val y = chartHeight - ((price - minPrice) / priceRange * chartHeight).toFloat()
                    return Offset(x, y)
                }

                if (showIndicators && selectedIndicator == "SMA" && prices.size >= 5) {
                    val smaValues = mutableListOf<Double>()
                    for (i in prices.indices) {
                        if (i < 4) smaValues.add(prices[i])
                        else smaValues.add(prices.subList(i - 4, i + 1).sum() / 5.0)
                    }
                    val smaPath = Path().apply {
                        val first = getPoint(smaValues.first(), 0)
                        moveTo(first.x, first.y)
                        for (i in 1 until smaValues.size) lineTo(getPoint(smaValues[i], i).x, getPoint(smaValues[i], i).y)
                    }
                    drawPath(path = smaPath, color = AccentYellow, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }

                if (showIndicators && selectedIndicator == "EMA" && prices.size >= 5) {
                    val emaValues = calculateEma(prices, 5)
                    val emaPath = Path().apply {
                        val first = getPoint(emaValues.first(), 0)
                        moveTo(first.x, first.y)
                        for (i in 1 until emaValues.size) lineTo(getPoint(emaValues[i], i).x, getPoint(emaValues[i], i).y)
                    }
                    drawPath(path = emaPath, color = Color(0xFF00E5FF), style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }

                if (showIndicators && selectedIndicator == "BOL" && prices.size >= 20) {
                    val (mid, up, low) = calculateBollingerBands(prices)
                    val fillPath = Path().apply {
                        moveTo(getPoint(up.first(), 0).x, getPoint(up.first(), 0).y)
                        for (i in 1 until up.size) lineTo(getPoint(up[i], i).x, getPoint(up[i], i).y)
                        for (i in low.size - 1 downTo 0) lineTo(getPoint(low[i], i).x, getPoint(low[i], i).y)
                        close()
                    }
                    drawPath(path = fillPath, color = Color.White.copy(alpha = 0.05f))
                    val midPath = Path().apply {
                        moveTo(getPoint(mid.first(), 0).x, getPoint(mid.first(), 0).y)
                        for (i in 1 until mid.size) lineTo(getPoint(mid[i], i).x, getPoint(mid[i], i).y)
                    }
                    drawPath(path = midPath, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
                }

                if (activeIndex != null) {
                    val index = activeIndex!!
                    val point = getPoint(prices[index], index)
                    drawLine(color = Color.White.copy(alpha = 0.3f), start = Offset(point.x, 0f), end = Offset(point.x, chartHeight), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    drawCircle(color = if (isPositive) AccentGreen else AccentRose, radius = 10f, center = point)
                    drawCircle(color = Color.White, radius = 5f, center = point)
                }
            }

            if (activeIndex != null) {
                val index = activeIndex!!
                val candle = currentCandles[index]
                Box(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.85f)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Price: ${String.format("%.2f", candle.close)}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        if (chartMode == "Candle") {
                            Text(text = "O: ${candle.open} H: ${candle.high} L: ${candle.low}", color = TextMuted, fontSize = 8.sp)
                        }
                    }
                }
            }
            }
        }

        if (!mini && showIndicators && (selectedIndicator == "RSI" || selectedIndicator == "MACD")) {
            Spacer(modifier = Modifier.height(6.dp))
            val label = if (selectedIndicator == "RSI") "RSI (5)" else "MACD (12, 26, 9)"
            Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                if (selectedIndicator == "RSI") RsiGraph(prices, activeIndex) else MacdGraph(prices, activeIndex)
            }
        }
    }
}

@Composable
fun RsiGraph(prices: List<Double>, activeIndex: Int?) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val y70 = height * 0.3f
        val y30 = height * 0.7f
        drawRect(color = Color(0xFF9C27B0).copy(alpha = 0.08f), topLeft = Offset(0f, y70), size = Size(width, y30 - y70))
        drawLine(color = Color(0xFF9C27B0).copy(alpha = 0.3f), start = Offset(0f, y70), end = Offset(width, y70), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
        drawLine(color = Color(0xFF9C27B0).copy(alpha = 0.3f), start = Offset(0f, y30), end = Offset(width, y30), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))

        val rsiValues = mutableListOf<Double>()
        for (i in prices.indices) {
            if (i < 5) { rsiValues.add(50.0); continue }
            var gains = 0.0; var losses = 0.0
            for (j in (i - 4)..i) {
                val diff = prices[j] - prices[j-1]
                if (diff > 0) gains += diff else losses -= diff
            }
            val rs = if (losses == 0.0) 100.0 else gains / losses
            rsiValues.add(100.0 - (100.0 / (1.0 + rs)))
        }

        val points = rsiValues.mapIndexed { i, r -> Offset(i * (width / (prices.size - 1)), height - (r.toFloat() / 100f * height)) }
        val path = Path().apply { moveTo(points.first().x, points.first().y); for (i in 1 until points.size) lineTo(points[i].x, points[i].y) }
        drawPath(path = path, color = Color(0xFFE040FB), style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        if (activeIndex != null) drawLine(color = Color.White.copy(alpha = 0.3f), start = Offset(activeIndex * (width / (prices.size - 1)), 0f), end = Offset(activeIndex * (width / (prices.size - 1)), height), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    }
}

@Composable
fun MacdGraph(prices: List<Double>, activeIndex: Int?) {
    val (macd, signal, hist) = remember(prices) { calculateMacd(prices) }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val maxAbs = (macd + signal + hist).map { kotlin.math.abs(it) }.maxOrNull() ?: 1.0
        fun getY(v: Double) = (height / 2) - (v.toFloat() / maxAbs.toFloat() * (height / 2))
        val spacing = width / (prices.size - 1)
        hist.forEachIndexed { i, h -> drawLine(color = if (h >= 0) AccentGreen.copy(alpha = 0.5f) else AccentRose.copy(alpha = 0.5f), start = Offset(i * spacing, height / 2), end = Offset(i * spacing, getY(h)), strokeWidth = 4f) }
        if (macd.isNotEmpty()) {
            val p = Path().apply { moveTo(0f, getY(macd.first())); for (i in 1 until macd.size) lineTo(i * spacing, getY(macd[i])) }
            drawPath(path = p, color = Color(0xFF2196F3), style = Stroke(width = 2f))
        }
        if (signal.isNotEmpty()) {
            val p = Path().apply { moveTo(0f, getY(signal.first())); for (i in 1 until signal.size) lineTo(i * spacing, getY(signal[i])) }
            drawPath(path = p, color = AccentYellow, style = Stroke(width = 2f))
        }
        if (activeIndex != null) drawLine(color = Color.White.copy(alpha = 0.3f), start = Offset(activeIndex * spacing, 0f), end = Offset(activeIndex * spacing, height), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    }
}

fun calculateBollingerBands(prices: List<Double>): Triple<List<Double>, List<Double>, List<Double>> {
    val mid = mutableListOf<Double>(); val up = mutableListOf<Double>(); val low = mutableListOf<Double>()
    for (i in prices.indices) {
        if (i < 19) { mid.add(prices[i]); up.add(prices[i]); low.add(prices[i]) }
        else {
            val sub = prices.subList(i - 19, i + 1)
            val avg = sub.average(); val sd = kotlin.math.sqrt(sub.map { (it - avg) * (it - avg) }.average())
            mid.add(avg); up.add(avg + 2 * sd); low.add(avg - 2 * sd)
        }
    }
    return Triple(mid, up, low)
}

fun calculateMacd(prices: List<Double>): Triple<List<Double>, List<Double>, List<Double>> {
    val e12 = calculateEma(prices, 12); val e26 = calculateEma(prices, 26)
    val macd = e12.zip(e26) { a, b -> a - b }; val sig = calculateEma(macd, 9)
    return Triple(macd, sig, macd.zip(sig) { a, b -> a - b })
}

fun calculateEma(prices: List<Double>, period: Int): List<Double> {
    val ema = mutableListOf<Double>(); if (prices.isEmpty()) return ema
    val k = 2.0 / (period + 1); var prev = prices.first(); ema.add(prev)
    for (i in 1 until prices.size) { val curr = prices[i] * k + prev * (1 - k); ema.add(curr); prev = curr }
    return ema
}
