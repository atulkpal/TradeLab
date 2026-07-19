package com.ashwathai.tradelab.ui.portfolio

import com.ashwathai.tradelab.MainActivity
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
fun PortfolioScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    onTickerClick: (String) -> Unit
) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var activeSubTab by remember { mutableStateOf("Holdings") } // "Holdings" or "Positions"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        // 1. Account value display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("portfolio_account_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL ACCOUNT VALUE",
                    color = TextSubtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formatCurrency(stats.totalValue, stats.currency),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    val isProfit = stats.totalPnL >= 0
                    val badgeBg = if (isProfit) AccentGreenDark else AccentRoseDark
                    val badgeText = if (isProfit) AccentGreen else AccentRose
                    val sign = if (isProfit) "+" else ""
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(badgeText, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$sign${String.format("%.2f", stats.totalPnLPct)}%",
                            color = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "DAY'S P&L (EST)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        var estDayPnL = 0.0
                        for (holding in holdings) {
                            val liveStock = stockPrices.find { it.symbol == holding.symbol }
                            val changePct = (liveStock?.dailyChangePct ?: 0.0) / 100.0
                            val currentValue = holding.shares * (liveStock?.currentPrice ?: holding.averagePrice)
                            estDayPnL += (currentValue * changePct)
                        }
                        val isDayProfit = estDayPnL >= 0
                        val daySign = if (isDayProfit) "+" else ""
                        Text(
                            text = "$daySign${formatPnL(estDayPnL, stats.currency)}",
                            color = if (isDayProfit) AccentGreen else AccentRose,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "PRESENT CASH IN HAND", color = TextSubtle, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(stats.cash, stats.currency),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Brokerage Shield Widget
        val shieldActive = stats.isPremium || stats.brokerageCredits >= 20
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("brokerage_shield_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (stats.isPremium) Color(0xFF1A1A2F) else if (shieldActive) Color(0xFF132A13) else Color(0xFF2B1B1B)
            ),
            border = BorderStroke(1.dp, if (stats.isPremium) BrandViolet.copy(alpha = 0.5f) else if (shieldActive) AccentGreen.copy(alpha = 0.3f) else AccentRose.copy(alpha = 0.3f))
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val mainActivity = context as? MainActivity
            var isWatchingAd by remember { mutableStateOf(false) }
            var isAdLoading by remember { mutableStateOf(false) }
            var adTimer by remember { mutableStateOf(0) }
            
            if (isAdLoading) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Connecting to AdMob Live Stream...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else if (isWatchingAd) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Streaming Sponsor Video... ${adTimer}s", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                LaunchedEffect(isWatchingAd) {
                    adTimer = 2
                    while (adTimer > 0) {
                        kotlinx.coroutines.delay(1000)
                        adTimer--
                    }
                    viewModel.earnBrokerageCredits(50)
                    isWatchingAd = false
                }
            } else {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (stats.isPremium) Icons.Default.CheckCircle else if (shieldActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Shield State",
                            tint = if (stats.isPremium) AccentYellow else if (shieldActive) AccentGreen else AccentRose,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Brokerage Shield",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (stats.isPremium) AccentYellow.copy(alpha = 0.15f)
                                            else if (shieldActive) AccentGreen.copy(alpha = 0.15f)
                                            else AccentRose.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (stats.isPremium) "PRO ACTIVE ⚡" else if (shieldActive) "PROTECTED" else "DRAINING",
                                        color = if (stats.isPremium) AccentYellow else if (shieldActive) AccentGreen else AccentRose,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (stats.isPremium) "Zero brokerage fees active. No credits consumed." else "${stats.brokerageCredits} Credits remaining (Consumes 20/trade)",
                                color = if (stats.isPremium) AccentYellow.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (stats.isPremium) AccentYellow.copy(alpha = 0.15f)
                                else if (shieldActive) Color.White.copy(alpha = 0.05f)
                                else BrandViolet
                            )
                            .border(
                                1.dp,
                                if (stats.isPremium) AccentYellow.copy(alpha = 0.3f)
                                else if (shieldActive) Color.White.copy(alpha = 0.1f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (stats.isPremium) {
                                    viewModel.showFeedback("TradeLab Pro is active! Zero brokerage fee waiver applied to all trades.")
                                } else {
                                    isAdLoading = true
                                    if (mainActivity != null) {
                                        mainActivity.loadAndShowRewardedAd(
                                            adType = MainActivity.AdType.PORTFOLIO_SHIELD,
                                            onAdLoaded = {
                                                isAdLoading = false
                                            },
                                            onAdFailed = { errorMsg ->
                                                isAdLoading = false
                                                viewModel.showFeedback("AdMob failed: $errorMsg. Launching fallback.")
                                                isWatchingAd = true
                                            },
                                            onUserEarnedReward = {
                                                viewModel.earnBrokerageCredits(50)
                                            }
                                        )
                                    } else {
                                        isAdLoading = false
                                        isWatchingAd = true
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (stats.isPremium) "Pro Waiver Active ⚡" else if (shieldActive) "Recharge (+50)" else "Shield Up 📺",
                            color = if (stats.isPremium) AccentYellow else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Sub-Tab Row: Holdings vs Positions (all trades happening)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Holdings" to "Holdings (${holdings.size})", "Positions" to "Trades/Positions (${transactions.size})").forEach { (tabId, tabTitle) ->
                val isSelected = activeSubTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BrandViolet.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { activeSubTab = tabId }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        color = if (isSelected) BrandViolet else Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Render Holdings or Positions
        if (activeSubTab == "Holdings") {
            if (holdings.isEmpty()) {
                // Empty state: show 0 / zero-state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Zero",
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Holdings: 0 Value", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Get started by adding tickers to your Watchlist and placing a trade!",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                val (optionHoldings, equityHoldings) = holdings.partition {
                    it.symbol.contains("_CE_") || it.symbol.contains("_PE_")
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (equityHoldings.isNotEmpty()) {
                        Text(
                            text = "EQUITY & ASSET HOLDINGS",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        equityHoldings.forEach { holding ->
                            val liveStock = stockPrices.find { it.symbol == holding.symbol }
                            val currentPrice = liveStock?.currentPrice ?: holding.averagePrice
                            val currentValue = holding.shares * currentPrice
                            val costBasis = holding.shares * holding.averagePrice
                            val pnl = currentValue - costBasis
                            val pnlPct = if (costBasis > 0) (pnl / costBasis) * 100.0 else 0.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                                    .clickable { onTickerClick(holding.symbol) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = holding.symbol.take(4), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = holding.symbol, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${String.format("%.2f", holding.shares)} shares",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Avg: ${formatCurrency(holding.averagePrice, stats.currency)}",
                                        color = TextSubtle,
                                        fontSize = 11.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatCurrency(currentValue, stats.currency),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val isUp = pnl >= 0
                                    Text(
                                        text = "${if (isUp) "+" else ""}${formatCurrency(pnl, stats.currency)} (${if (isUp) "+" else ""}${String.format("%.2f", pnlPct)}%)",
                                        color = if (isUp) AccentGreen else AccentRose,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (optionHoldings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Render portfolio Greeks box
                        GreeksDiagnosticsBox(optionHoldings, stockPrices, stats.currency)

                        Text(
                            text = "ACTIVE OPTIONS CONTRACTS (F&O)",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        optionHoldings.forEach { holding ->
                            val isCall = holding.symbol.contains("_CE_")
                            val separator = if (isCall) "_CE_" else "_PE_"
                            val parts = holding.symbol.split(separator)
                            val underlying = parts[0]
                            val strike = parts.getOrNull(1)?.toDoubleOrNull() ?: 100.0

                            val liveStock = stockPrices.find { it.symbol == holding.symbol }
                            val currentPrice = liveStock?.currentPrice ?: holding.averagePrice
                            val currentValue = holding.shares * currentPrice
                            val costBasis = holding.shares * holding.averagePrice
                            val pnl = currentValue - costBasis
                            val pnlPct = if (costBasis > 0) (pnl / costBasis) * 100.0 else 0.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, BrandViolet.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isCall) AccentGreen.copy(alpha = 0.15f) else AccentRose.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isCall) "CALL" else "PUT",
                                                color = if (isCall) AccentGreen else AccentRose,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "$underlying @ $strike", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Qty: ${String.format("%.0f", holding.shares)} shares (${String.format("%.2f", holding.shares / 100.0)} Lots)",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Avg Prem: ${formatCurrency(holding.averagePrice, stats.currency)}",
                                        color = TextSubtle,
                                        fontSize = 11.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatCurrency(currentValue, stats.currency),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val isUp = pnl >= 0
                                    Text(
                                        text = "${if (isUp) "+" else ""}${formatPnL(pnl, stats.currency)} (${if (isUp) "+" else ""}${String.format("%.2f", pnlPct)}%)",
                                        color = if (isUp) AccentGreen else AccentRose,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Square Off / Close Position Command Button
                                    Button(
                                        onClick = {
                                            viewModel.sellStock(holding.symbol, holding.shares)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1E1E)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("SQUARE OFF", color = AccentRose, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Positions (All trades happening)
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Zero Trades",
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Trades Done: 0 Transactions", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Any buying or selling transaction you execute will be logged here instantly.",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    transactions.forEach { tx ->
                        val liveStock = stockPrices.find { it.symbol == tx.symbol }
                        val currentPrice = liveStock?.currentPrice ?: tx.price
                        val isBuy = tx.type == "BUY"
                        
                        // Transaction P&L calculations
                        val boughtValue = tx.shares * tx.price
                        val presentValue = tx.shares * currentPrice
                        val txPnL = if (isBuy) (presentValue - boughtValue) else (boughtValue - presentValue)
                        val txPnLPct = if (boughtValue > 0) (txPnL / boughtValue) * 100.0 else 0.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                                .clickable { onTickerClick(tx.symbol) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isBuy) AccentGreen.copy(alpha = 0.1f) else AccentRose.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tx.type,
                                            color = if (isBuy) AccentGreen else AccentRose,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = tx.symbol, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%.2f", tx.shares)} units @ ${formatCurrency(tx.price, stats.currency)}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Trade Value: ${formatCurrency(boughtValue, stats.currency)}",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Current: ${formatCurrency(currentPrice, stats.currency)}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val isPositive = txPnL >= 0
                                Text(
                                    text = "${if (isPositive) "+" else ""}${formatCurrency(txPnL, stats.currency)} (${if (isPositive) "+" else ""}${String.format("%.2f", txPnLPct)}%)",
                                    color = if (isPositive) AccentGreen else AccentRose,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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


@Composable
fun AssetsScreen(viewModel: TradingViewModel, stats: PortfolioStats) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val pendingOrders by viewModel.pendingOrders.collectAsStateWithLifecycle()

    var selectedSection by remember { mutableStateOf("Holdings") } // "Holdings" or "Pending"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Portfolio Asset Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "PORTFOLIO DISTRIBUTION",
                    color = TextSubtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Holdings", color = BrandViolet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = formatCurrency(stats.holdingsValue, stats.currency), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = DarkBorder)
                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(text = "Cash Balance", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = formatCurrency(stats.cash, stats.currency), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val totalValue = maxOf(stats.totalValue, 1.0)
                val holdingsPct = (stats.holdingsValue / totalValue).toFloat()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(maxOf(holdingsPct, 0.001f))
                            .background(BrandViolet)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(maxOf(1f - holdingsPct, 0.001f))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Navigation Tab Selector (Holdings vs Pending Orders)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Holdings" to "Active Holdings", "Pending" to "Pending Orders (${pendingOrders.size})").forEach { (secId, title) ->
                val isSelected = selectedSection == secId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) BrandViolet.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedSection = secId }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) BrandViolet else Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSection == "Holdings") {
            if (holdings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "No positions",
                            tint = TextSubtle,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active holdings",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Go to the Trade tab, choose a stock, and place an order to build your virtual portfolio.",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(holdings) { holding ->
                        val liveStock = stockPrices.find { it.symbol == holding.symbol }
                        val currentPrice = liveStock?.currentPrice ?: holding.averagePrice
                        val currentValue = holding.shares * currentPrice
                        val costBasis = holding.shares * holding.averagePrice
                        val pnl = currentValue - costBasis
                        val pnlPct = if (costBasis > 0) (pnl / costBasis) * 100.0 else 0.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.selectStock(holding.symbol)
                                    viewModel.selectTab("Trade")
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = holding.symbol, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = holding.symbol, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%.4f", holding.shares)} shares",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Avg: ${formatCurrency(holding.averagePrice, stats.currency)}",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrency(currentValue, stats.currency),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val displayColor = if (pnl >= 0) AccentGreen else AccentRose
                                val sign = if (pnl >= 0) "+" else ""
                                Text(
                                    text = "$sign${formatCurrency(pnl, stats.currency)} ($sign${String.format("%.2f", pnlPct)}%)",
                                    color = displayColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // PENDING ORDERS SECTION (GTT / Limit)
            if (pendingOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "No Pending Orders",
                            tint = TextSubtle,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No pending orders",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "GTT and Limit orders will stay active here until they are triggered or manually cancelled.",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pendingOrders) { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (order.type == "BUY") AccentGreen.copy(alpha = 0.15f) else AccentRose.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${order.type} ${order.orderType}",
                                            color = if (order.type == "BUY") AccentGreen else AccentRose,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = order.symbol, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%.2f", order.shares)} Shares",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Trigger at: ${formatCurrency(order.triggerPrice, stats.currency)}",
                                    color = AccentYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deletePendingOrder(order.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Cancel Pending Order",
                                    tint = AccentRose
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PortfolioCard(stats: PortfolioStats, onSimulateTick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portfolio_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total Portfolio Value",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatCurrencyNoDecimals(stats.totalValue, stats.currency),
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp
                        )
                        val scale = if (stats.currency == "INR") 83.0 else 1.0
                        val decimalPart = ((stats.totalValue * scale) % 1) * 100
                        Text(
                            text = String.format(".%02d", decimalPart.toInt().coerceIn(0, 99)),
                            color = TextMuted,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // SIMULATE TICK BUTTON - ONLY ENABLED OR UNLOCKED VISUALLY FOR ARCADE
                if (stats.isArcadeMode) {
                    IconButton(
                        onClick = onSimulateTick,
                        modifier = Modifier
                            .testTag("simulate_tick_button")
                            .size(40.dp)
                            .background(AccentYellow.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, AccentYellow.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Simulate Market Movement",
                            tint = AccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buying power
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Cash (Buying Power)",
                        color = TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(stats.cash, stats.currency),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                val returnsPct = stats.totalPnLPct
                val badgeBg = if (returnsPct >= 0) AccentGreenDark else AccentRoseDark
                val badgeText = if (returnsPct >= 0) AccentGreen else AccentRose
                val sign = if (returnsPct >= 0) "+" else ""

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(badgeText, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$sign${String.format("%.2f", returnsPct)}%",
                        color = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun StatCard(label: String, value: String, valueColor: Color, percent: String?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = TextSubtle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (percent != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = percent,
                    color = valueColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


