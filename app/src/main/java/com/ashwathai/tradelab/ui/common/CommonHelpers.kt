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

fun isIndianStockSymbol(symbol: String): Boolean {
    val upper = symbol.uppercase().trim()
    val indianTickers = listOf(
        "RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK", "SBIN", "BHARTIARTL", "ITC",
        "LICI", "LT", "KOTAKBANK", "AXISBANK", "WIPRO", "ASIANPAINT", "HINDUNILVR",
        "MARUTI", "TATASTEEL", "M&M", "ADANIENT", "SUNPHARMA", "JSWSTEEL", "ONGC",
        "COALINDIA", "NTPC", "POWERGRID", "ULTRACEMCO", "TATAMOTORS", "BAJFINANCE", "HINDALCO"
    )
    return upper.endsWith(".NS") || indianTickers.contains(upper)
}


fun getDisplayStockPrice(priceInNativeCurrency: Double, symbol: String, targetCurrency: String): Double {
    val nativeCurrency = if (isIndianStockSymbol(symbol)) "INR" else "USD"
    if (nativeCurrency == targetCurrency) {
        return priceInNativeCurrency
    }
    return if (nativeCurrency == "USD" && targetCurrency == "INR") {
        priceInNativeCurrency * 83.0
    } else {
        priceInNativeCurrency / 83.0
    }
}


fun formatCurrency(value: Double, currency: String): String {
    val symbol = if (currency == "INR") "₹" else "$"
    return "$symbol${String.format("%,.2f", value)}"
}


fun formatCurrencyNoDecimals(value: Double, currency: String): String {
    val symbol = if (currency == "INR") "₹" else "$"
    return "$symbol${String.format("%,.0f", value)}"
}


fun formatPnL(value: Double, currency: String): String {
    val symbol = if (currency == "INR") "₹" else "$"
    val sign = if (value >= 0) "+" else ""
    return "$sign$symbol${String.format("%,.2f", value)}"
}


fun calculateRSI(prices: List<Double>, period: Int = 14): Double {
    if (prices.size <= period) return 50.0
    var gains = 0.0
    var losses = 0.0

    for (i in 1..period) {
        val difference = prices[i] - prices[i - 1]
        if (difference >= 0) {
            gains += difference
        } else {
            losses -= difference
        }
    }

    var avgGain = gains / period
    var avgLoss = losses / period

    for (i in (period + 1) until prices.size) {
        val difference = prices[i] - prices[i - 1]
        val gain = if (difference >= 0) difference else 0.0
        val loss = if (difference < 0) -difference else 0.0

        avgGain = (avgGain * (period - 1) + gain) / period
        avgLoss = (avgLoss * (period - 1) + loss) / period
    }

    if (avgLoss == 0.0) return 100.0
    val rs = avgGain / avgLoss
    return 100.0 - (100.0 / (1.0 + rs))
}


fun getMarketCap(symbol: String, currency: String): String {
    val isINR = currency == "INR"
    return when (symbol.uppercase().trim()) {
        "RELIANCE" -> if (isINR) "₹19.8L Cr" else "$238B"
        "TCS" -> if (isINR) "₹13.9L Cr" else "$167B"
        "INFY" -> if (isINR) "₹6.3L Cr" else "$75B"
        "TATASTEEL" -> if (isINR) "₹2.1L Cr" else "$25B"
        "HDFCBANK" -> if (isINR) "₹12.2L Cr" else "$146B"
        "AAPL" -> if (isINR) "₹250L Cr" else "$3.01T"
        "TSLA" -> if (isINR) "₹55L Cr" else "$660B"
        "NVDA" -> if (isINR) "₹240L Cr" else "$2.90T"
        "GOOG" -> if (isINR) "₹165L Cr" else "$1.98T"
        "MSFT" -> if (isINR) "₹260L Cr" else "$3.15T"
        "AMZN" -> if (isINR) "₹155L Cr" else "$1.87T"
        "BTC" -> if (isINR) "₹102L Cr" else "$1.23T"
        "ETH" -> if (isINR) "₹34L Cr" else "$410B"
        else -> if (isINR) "₹1.5L Cr" else "$18B"
    }
}


fun getVolume(symbol: String): String {
    return when (symbol.uppercase().trim()) {
        "RELIANCE" -> "3.4M"
        "TCS" -> "1.1M"
        "INFY" -> "5.6M"
        "TATASTEEL" -> "22.4M"
        "HDFCBANK" -> "14.2M"
        "AAPL" -> "52.3M"
        "TSLA" -> "84.1M"
        "NVDA" -> "41.5M"
        "GOOG" -> "28.7M"
        "MSFT" -> "19.9M"
        "AMZN" -> "33.2M"
        "BTC" -> "25.1K"
        "ETH" -> "180.5K"
        else -> "4.5M"
    }
}


