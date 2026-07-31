package com.ashwathai.tradelab.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.ashwathai.tradelab.ui.theme.AccentGreen
import com.ashwathai.tradelab.ui.theme.AccentRose
import com.ashwathai.tradelab.ui.theme.TextMuted

@Composable
fun CandlestickChart(
    candles: List<CandleData>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val minPrice = candles.minOf { it.low }
    val maxPrice = candles.maxOf { it.high }
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)

    val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1L)

    val priceLabelCount = 5
    val priceLabels = remember(priceRange) {
        (0 until priceLabelCount).map { i ->
            val pct = i.toDouble() / (priceLabelCount - 1)
            minPrice + (priceRange * pct)
        }
    }

    val xLabelCount = 4
    val xLabels = remember(candles.size) {
        (0 until xLabelCount).map { i ->
            val idx = (i.toDouble() / (xLabelCount - 1) * (candles.size - 1)).toInt().coerceIn(0, candles.size - 1)
            idx to java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(candles[idx].timestamp))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val candleWidth = width / (candles.size * 1.5f)
        val spacing = width / candles.size
        val axisLeftPad = 44f
        val axisBottomPad = 20f
        val chartWidth = width - axisLeftPad
        val chartHeight = height - axisBottomPad

        // Horizontal grid lines + Y-axis labels
        priceLabels.forEach { price ->
            val y = chartHeight - ((price - minPrice) / priceRange * chartHeight).toFloat()
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

        // Vertical grid lines + X-axis labels
        xLabels.forEach { (idx, label) ->
            val x = idx * spacing + spacing / 2 + axisLeftPad
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

        candles.forEachIndexed { index, candle ->
            val x = index * spacing + spacing / 2 + axisLeftPad
            
            val volHeight = (candle.volume.toFloat() / maxVolume) * (chartHeight * 0.2f)
            drawRect(
                color = if (candle.close >= candle.open) AccentGreen.copy(alpha = 0.2f) else AccentRose.copy(alpha = 0.2f),
                topLeft = Offset(x - candleWidth / 2, chartHeight - volHeight),
                size = Size(candleWidth, volHeight)
            )

            val yHigh = chartHeight - ((candle.high - minPrice) / priceRange * chartHeight).toFloat()
            val yLow = chartHeight - ((candle.low - minPrice) / priceRange * chartHeight).toFloat()
            val yOpen = chartHeight - ((candle.open - minPrice) / priceRange * chartHeight).toFloat()
            val yClose = chartHeight - ((candle.close - minPrice) / priceRange * chartHeight).toFloat()

            val isBullish = candle.close >= candle.open
            val color = if (isBullish) AccentGreen else AccentRose

            drawLine(
                color = color,
                start = Offset(x, yHigh),
                end = Offset(x, yLow),
                strokeWidth = 3f
            )

            val top = minOf(yOpen, yClose)
            val bottom = maxOf(yOpen, yClose)
            val bodyHeight = (bottom - top).coerceAtLeast(3f)

            drawRect(
                color = color,
                topLeft = Offset(x - candleWidth / 2, top),
                size = Size(candleWidth, bodyHeight)
            )
        }
    }
}
