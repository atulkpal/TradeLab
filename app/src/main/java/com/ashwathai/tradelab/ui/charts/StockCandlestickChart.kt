package com.ashwathai.tradelab.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.ashwathai.tradelab.ui.theme.AccentGreen
import com.ashwathai.tradelab.ui.theme.AccentRose

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

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val candleWidth = width / (candles.size * 1.5f)
        val spacing = width / candles.size

        candles.forEachIndexed { index, candle ->
            val x = index * spacing + spacing / 2
            
            // Draw Volume Bar (at the bottom)
            val volHeight = (candle.volume.toFloat() / maxVolume) * (height * 0.2f)
            drawRect(
                color = if (candle.close >= candle.open) AccentGreen.copy(alpha = 0.2f) else AccentRose.copy(alpha = 0.2f),
                topLeft = Offset(x - candleWidth / 2, height - volHeight),
                size = Size(candleWidth, volHeight)
            )

            // Calculate Y coordinates
            val yHigh = height - ((candle.high - minPrice) / priceRange * height).toFloat()
            val yLow = height - ((candle.low - minPrice) / priceRange * height).toFloat()
            val yOpen = height - ((candle.open - minPrice) / priceRange * height).toFloat()
            val yClose = height - ((candle.close - minPrice) / priceRange * height).toFloat()

            val isBullish = candle.close >= candle.open
            val color = if (isBullish) AccentGreen else AccentRose

            // Draw Wick
            drawLine(
                color = color,
                start = Offset(x, yHigh),
                end = Offset(x, yLow),
                strokeWidth = 3f
            )

            // Draw Body
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
