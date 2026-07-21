package com.ashwathai.tradelab.ui.commodities

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
fun CommoditiesScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    onTickerClick: (String) -> Unit
) {
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.commoditiesUnlocked.collectAsStateWithLifecycle()
    val isPremium = stats.isPremium

    var activeSubTab by remember { mutableStateOf("MCX") } // "MCX" or "Global"
    var showSimulatedAd by remember { mutableStateOf(false) }
    var adCountdown by remember { mutableStateOf(5) }

    if (showSimulatedAd) {
        LaunchedEffect(Unit) {
            for (i in 5 downTo 1) {
                adCountdown = i
                kotlinx.coroutines.delay(1000)
            }
            showSimulatedAd = false
            viewModel.unlockCommodities()
        }

        // Full Screen Sponsor Ad Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(24.dp)
                .testTag("simulated_ad_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPONSOR ADVERTISEMENT",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE53935))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Reward in $adCountdown s",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Ad Viewport
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .border(BorderStroke(1.dp, BrandViolet.copy(alpha = 0.4f)), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(BrandViolet.copy(alpha = 0.2f))
                                .border(BorderStroke(1.dp, BrandViolet), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Academy",
                                tint = BrandViolet,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Ashwath AI Investor Academy",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Master position sizing, options decay, risk controls, and advanced global market indicators in minutes. No fluff. Real simulations.",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Learn more at ashwath.ai",
                            color = BrandViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                CircularProgressIndicator(
                    color = BrandViolet,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Unlock progress will apply automatically upon video completion.",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Check if Unlocked
        val hasAccess = isUnlocked || isPremium

        if (!hasAccess) {
            // Ad-Wall Paywall Screen (Centered vertically for consistency)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("commodities_paywall_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shield Icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFFFFB300).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Derivatives Risk Lock",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Commodity assets trade with high leverage and volatility in the real world. To protect young retail investors from excessive speculation, Trade Lab gates advanced commodity indices behind an ad-wall safety shield.",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalDivider(color = Color(0xFF1A1A1A))

                        Spacer(modifier = Modifier.height(24.dp))

                        // FREE WATCH AD OPTION
                        Button(
                            onClick = { showSimulatedAd = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("watch_ad_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Watch",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Watch Sponsor Ad to Unlock (FREE)",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Grants 12 hours of unlimited access to MCX & Global desks.",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "OR",
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PRO UPGRADE OPTION
                        OutlinedButton(
                            onClick = { viewModel.selectTab("Profile") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Pro",
                                tint = BrandViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Go Pro for Unlimited Access",
                                color = BrandViolet,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Commodities Desk UI (Unlocked)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sub tab row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C0C0C))
                        .border(1.dp, Color(0xFF1F1F1F), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    val subTabs = listOf("MCX", "Global")
                    subTabs.forEach { tab ->
                        val isSelected = activeSubTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1A1A1A) else Color.Transparent)
                                .clickable { activeSubTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tab == "MCX") "MCX (Indian Markets)" else "Global Commodities",
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Explanatory note card based on selected desk
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                    border = BorderStroke(1.dp, Color(0xFF1A1A1A))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = BrandViolet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (activeSubTab == "MCX") {
                                "Indian MCX rates are automatically converted in real-time from COMEX/NYMEX global indices at standard USD-INR exchange rate (1$ = ₹83)."
                            } else {
                                "Global NYMEX and COMEX commodity spot and futures indices denominated directly in US Dollars ($)."
                            },
                            color = TextSubtle,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of commodities
                val targetSymbols = if (activeSubTab == "MCX") {
                    listOf("MCX_GOLD", "MCX_SILVER", "MCX_CRUDE", "MCX_NATGAS", "MCX_COPPER")
                } else {
                    listOf("GLOBAL_GOLD", "GLOBAL_SILVER", "GLOBAL_CRUDE", "GLOBAL_NATGAS", "GLOBAL_COPPER")
                }

                targetSymbols.forEach { sym ->
                    val item = stockPrices.find { it.symbol == sym }
                    if (item != null) {
                        val isUp = item.dailyChangePct >= 0
                        val changeColor = if (isUp) AccentGreen else AccentRose
                        val changeSign = if (isUp) "+" else ""

                        val unitLabel = when (sym) {
                            "MCX_GOLD" -> "per 10g"
                            "MCX_SILVER" -> "per kg"
                            "MCX_CRUDE" -> "per barrel"
                            "MCX_NATGAS" -> "per MMBtu"
                            "MCX_COPPER" -> "per kg"
                            "GLOBAL_GOLD" -> "per oz"
                            "GLOBAL_SILVER" -> "per oz"
                            "GLOBAL_CRUDE" -> "per barrel"
                            "GLOBAL_NATGAS" -> "per MMBtu"
                            "GLOBAL_COPPER" -> "per lb"
                            else -> ""
                        }

                        val formattedPrice = if (activeSubTab == "MCX") {
                            formatCurrency(item.currentPrice, "INR")
                        } else {
                            formatCurrency(item.currentPrice, "USD")
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onTickerClick(sym) }
                                .testTag("commodity_item_${sym.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.symbol.replace("GLOBAL_", "").replace("MCX_", ""),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (activeSubTab == "MCX") Color(0xFF00C853).copy(alpha = 0.12f) else Color(0xFF2979FF).copy(alpha = 0.12f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (activeSubTab == "MCX") "MCX" else "GLOBAL",
                                                color = if (activeSubTab == "MCX") Color(0xFF00C853) else Color(0xFF2979FF),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.companyName,
                                        color = TextSubtle,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Sparkline placeholder canvas
                                Box(
                                    modifier = Modifier
                                        .size(width = 60.dp, height = 30.dp)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    val points = item.historyData.split(",").mapNotNull { it.toDoubleOrNull() }
                                    if (points.size > 1) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val min = points.minOrNull() ?: 0.0
                                            val max = points.maxOrNull() ?: 1.0
                                            val range = if (max == min) 1.0 else max - min
                                            val path = androidx.compose.ui.graphics.Path()

                                            points.forEachIndexed { idx, price ->
                                                val x = (size.width / (points.size - 1)) * idx
                                                val y = size.height - ((price - min) / range * size.height).toFloat()
                                                if (idx == 0) {
                                                    path.moveTo(x, y)
                                                } else {
                                                    path.lineTo(x, y)
                                                }
                                            }

                                            drawPath(
                                                path = path,
                                                color = changeColor,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formattedPrice,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$changeSign${String.format("%.2f", item.dailyChangePct)}%",
                                            color = changeColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = unitLabel,
                                            color = TextSubtle,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
