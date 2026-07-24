package com.ashwathai.tradelab.ui.portfolio

import com.ashwathai.tradelab.MainActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ashwathai.tradelab.BuildConfig
import com.ashwathai.tradelab.data.*
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.charts.StockLineChart
import com.ashwathai.tradelab.ui.common.*
import com.ashwathai.tradelab.ui.theme.*
import com.ashwathai.tradelab.ui.derivatives.GreeksDiagnosticsBox
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun PortfolioScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    latestNews: List<MarketNews>,
    onTickerClick: (String) -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val userLeague by viewModel.userLeague.collectAsStateWithLifecycle()
    val xpToNextLeague by viewModel.xpToNextLeague.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val topGainers by viewModel.topGainers.collectAsStateWithLifecycle()
    val topLosers by viewModel.topLosers.collectAsStateWithLifecycle()
    val accountSnapshots by viewModel.accountSnapshots.collectAsStateWithLifecycle()
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val shareHook = remember(stats.totalPnLPct) { ShareHooks.getRandomHook(stats) }

    var activeSubTab by remember { mutableStateOf("Holdings") }

    // Docking Footer logic: appears when main account card scrolls past
    val showDockedFooter = scrollState.value > 450

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 0. Breaking News (Internalized)
            BreakingNewsTicker(latestNews = latestNews)

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Market Dashboard (Movers)
            MarketDashboardWidget(topGainers, topLosers)
            
            Spacer(modifier = Modifier.height(12.dp))

            // 2. Account Card - ULTRA DENSE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("portfolio_account_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL PORTFOLIO VALUE", color = TextSubtle, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userLeague.uppercase(),
                                    color = when(userLeague) {
                                        "Diamond" -> Color(0xFFB9F2FF)
                                        "Platinum" -> Color(0xFFE5E4E2)
                                        "Gold" -> AccentYellow
                                        "Silver" -> Color(0xFFC0C0C0)
                                        else -> Color(0xFFCD7F32)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                if (xpToNextLeague > 0) {
                                    Text(" • $xpToNextLeague XP", color = TextMuted, fontSize = 8.sp)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            userProfile?.let { profile ->
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(AccentRose.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Whatshot, null, tint = AccentRose, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(profile.dailyStreak.toString(), color = AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            // Point 11: Ensure layer is captured with size
                                            if (graphicsLayer.size.width > 0 && graphicsLayer.size.height > 0) {
                                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                                val shareText = "$shareHook \n\nI turned my portfolio into ${formatCurrency(stats.totalValue, stats.currency)} on Trade Lab! 🔥"
                                                ShareUtils.shareImage(context, bitmap, shareText)
                                            } else {
                                                viewModel.showFeedback("Snapshot ready. Please try sharing again.")
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            viewModel.showFeedback("Error: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Share, null, tint = BrandViolet, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatCurrency(stats.totalValue, stats.currency),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("LIFETIME P&L", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            val isTotalUp = stats.totalPnL >= 0
                            Text(
                                text = "${if (isTotalUp) "+" else ""}${formatPnL(stats.totalPnL, stats.currency)} (${String.format(Locale.US, "%.1f", stats.totalPnLPct)}%)",
                                color = if (isTotalUp) AccentGreen else AccentRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("TODAY'S P&L", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            val isDayUp = stats.todayPnL >= 0
                            Text(
                                text = "${if (isDayUp) "+" else ""}${formatPnL(stats.todayPnL, stats.currency)} (${String.format(Locale.US, "%.1f", stats.todayPnLPct)}%)",
                                color = if (isDayUp) AccentGreen else AccentRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. INSTITUTIONAL ANALYTICS (Heatmap)
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectorHeatmapWidget(holdings, stockPrices, stats)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Brokerage Shield Widget
            val shieldActive = stats.isPremium || stats.brokerageCredits >= 20
            var showShieldDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (stats.isPremium) Color(0xFF151525) else if (shieldActive) Color(0xFF102010) else Color(0xFF251010)),
                border = BorderStroke(1.dp, if (stats.isPremium) BrandViolet.copy(alpha = 0.4f) else if (shieldActive) AccentGreen.copy(alpha = 0.3f) else AccentRose.copy(alpha = 0.3f))
            ) {
                val mainActivity = context as? MainActivity
                var isWatchingAd by remember { mutableStateOf(false) }
                var isAdLoading by remember { mutableStateOf(false) }
                var adTimer by remember { mutableIntStateOf(0) }
                
                if (isAdLoading || isWatchingAd) {
                    Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BrandViolet)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(if (isAdLoading) "Connecting..." else "Watching... ${adTimer}s", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (isWatchingAd) {
                        LaunchedEffect(Unit) {
                            adTimer = 2
                            while (adTimer > 0) { kotlinx.coroutines.delay(1000); adTimer-- }
                            viewModel.earnBrokerageCredits(50); isWatchingAd = false
                        }
                    }
                } else {
                    Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (shieldActive) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (shieldActive) AccentGreen else AccentRose, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Brokerage Shield", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(if (stats.isPremium) "PRO Waiver Active" else "${stats.brokerageCredits} Credits", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (stats.isPremium) AccentYellow.copy(alpha = 0.1f) else BrandViolet).clickable {
                            if (stats.isPremium) viewModel.showFeedback("Pro Active!")
                            else {
                                if (stats.shouldShowShieldDialog) showShieldDialog = true
                                else { isAdLoading = true; mainActivity?.loadAndShowRewardedAd(MainActivity.AdType.PORTFOLIO_SHIELD, { isAdLoading = false }, { isAdLoading = false; isWatchingAd = true }, { viewModel.earnBrokerageCredits(20) }) ?: run { isAdLoading = false; isWatchingAd = true } }
                            }
                        }.padding(horizontal = 8.dp, vertical = 5.dp)) {
                            Text(if (stats.isPremium) "PRO ⚡" else "Shield Up 📺", color = if (stats.isPremium) AccentYellow else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (showShieldDialog) {
                    BrokerageShieldDialog(
                        onDismiss = { showShieldDialog = false },
                        onGoPro = { showShieldDialog = false; viewModel.openBillingFlow() },
                        onWatchAd = { 
                            showShieldDialog = false
                            isAdLoading = true
                            mainActivity?.loadAndShowRewardedAd(MainActivity.AdType.PORTFOLIO_SHIELD, { isAdLoading = false }, { isAdLoading = false; isWatchingAd = true }, { viewModel.earnBrokerageCredits(20) }) ?: run { isAdLoading = false; isWatchingAd = true }
                        },
                        onDoNotShowAgain = { viewModel.setShouldShowShieldDialog(false) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Tabs (Point 7 & 12)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)).background(DarkSurface).padding(4.dp)) {
                listOf(
                    "Holdings" to "Holdings (${holdings.filter { it.shares > 0 }.size})", 
                    "Positions" to "Positions (${holdings.filter { it.sharesT1 > 0 }.size})",
                    "Pending" to "Pending"
                ).forEach { (tabId, tabTitle) ->
                    val isSel = activeSubTab == tabId
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSel) BrandViolet.copy(alpha = 0.15f) else Color.Transparent).clickable { activeSubTab = tabId }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(tabTitle, color = if (isSel) BrandViolet else Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lists
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                when (activeSubTab) {
                    "Holdings" -> HoldingsList(holdings.filter { it.shares > 0 }, stockPrices, stats, onTickerClick, viewModel)
                    "Positions" -> PositionsList(holdings.filter { it.sharesT1 > 0 }, transactions, stockPrices, stats, onTickerClick, viewModel)
                    "Pending" -> PendingOrdersList(viewModel)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.padding(horizontal = 20.dp)) { EquityCurveWidget(accountSnapshots, stats.currency) }
            Spacer(modifier = Modifier.height(80.dp))
        }

        // 🟢 DOCKED FOOTER (SPRING ANIMATION)
        androidx.compose.animation.AnimatedVisibility(
            visible = showDockedFooter,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C).copy(alpha = 0.98f)),
                border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp).fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PORTFOLIO VALUE", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(formatCurrency(stats.totalValue, stats.currency), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("LIFETIME", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        val isTotalUp = stats.totalPnL >= 0
                        Text("${formatPnL(stats.totalPnL, stats.currency)} (${if (isTotalUp) "+" else ""}${String.format(Locale.US, "%.1f", stats.totalPnLPct)}%)", color = if (isTotalUp) AccentGreen else AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Text("TODAY", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        val isDayUp = stats.todayPnL >= 0
                        Text("${formatPnL(stats.todayPnL, stats.currency)} (${if (isDayUp) "+" else ""}${String.format(Locale.US, "%.1f", stats.todayPnLPct)}%)", color = if (isDayUp) AccentGreen else AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 🟢 SECURE CAPTURE (measured but not placed)
        Box(
            modifier = Modifier.layout { measurable, constraints ->
                val width = 360.dp.roundToPx(); val height = 480.dp.roundToPx()
                measurable.measure(constraints.copy(minWidth = width, maxWidth = width, minHeight = height, maxHeight = height))
                layout(0, 0) {} 
            }.drawWithContent {
                graphicsLayer.record(density = density, layoutDirection = layoutDirection, size = IntSize(360.dp.roundToPx(), 480.dp.roundToPx())) { this@drawWithContent.drawContent() }
            }
        ) { Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) { PortfolioShareCard(stats = stats, hookText = shareHook) } }
    }
}

@Composable
fun BrokerageShieldDialog(
    onDismiss: () -> Unit,
    onGoPro: () -> Unit,
    onWatchAd: () -> Unit,
    onDoNotShowAgain: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 1. Header with Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Watch Ad",
                        tint = BrandViolet,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recharge Brokerage Shield 📺", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Educational Summary
                Text(
                    text = "Accumulate brokerage credits by watching ads anytime (even off-market)! Each trade costs 20 credits to waive fees.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // 3. PRO Advantage Promotional Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandViolet.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Pro Option",
                                tint = AccentYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SKIP ALL ADS WITH PRO",
                                color = AccentYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get TradeLab Pro to unlock zero brokerage permanently and skip all sponsor ads instantly.",
                            color = TextSubtle,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onGoPro,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("GO PRO • ₹99/mo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Primary and Secondary Actions
                Button(
                    onClick = onWatchAd,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("WATCH SPONSOR AD (+50)", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // 5. Preference Toggle & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { 
                                checked = it
                                onDoNotShowAgain(it)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BrandViolet)
                        )
                        Text("Don't show again", color = TextMuted, fontSize = 11.sp)
                    }
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = TextSubtle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HoldingsList(holdings: List<Holding>, stockPrices: List<StockPrice>, stats: PortfolioStats, onTickerClick: (String) -> Unit, viewModel: TradingViewModel) {
    if (holdings.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = TextMuted, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No Holdings Found", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        val (optionHoldings, equityHoldings) = holdings.partition { it.symbol.contains("_CE_") || it.symbol.contains("_PE_") }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            equityHoldings.forEach { HoldingItem(it, stockPrices, stats, onTickerClick) }
            if (optionHoldings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                GreeksDiagnosticsBox(optionHoldings, stockPrices, stats.currency)
                optionHoldings.forEach { OptionHoldingItem(it, stockPrices, stats, viewModel) }
            }
        }
    }
}

@Composable
fun HoldingItem(holding: Holding, stockPrices: List<StockPrice>, stats: PortfolioStats, onTickerClick: (String) -> Unit) {
    val liveStock = stockPrices.find { it.symbol == holding.symbol }
    val currentPrice = liveStock?.currentPrice ?: holding.averagePrice
    val totalShares = holding.shares + holding.sharesT1
    val currentValue = totalShares * currentPrice
    val costBasis = totalShares * holding.averagePrice
    val pnl = currentValue - costBasis
    val pnlPct = if (costBasis > 0) (pnl / costBasis) * 100.0 else 0.0
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DarkSurface).border(1.dp, DarkBorder, RoundedCornerShape(16.dp)).clickable { onTickerClick(holding.symbol) }.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(holding.symbol, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("${String.format(Locale.US, "%.2f", totalShares)} shares", color = TextMuted, fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatCurrency(currentValue, stats.currency), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            val isUp = pnl >= 0
            Text("${if (isUp) "+" else ""}${String.format(Locale.US, "%.2f", pnlPct)}%", color = if (isUp) AccentGreen else AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OptionHoldingItem(holding: Holding, stockPrices: List<StockPrice>, stats: PortfolioStats, viewModel: TradingViewModel) {
    val isCall = holding.symbol.contains("_CE_")
    val liveStock = stockPrices.find { it.symbol == holding.symbol }
    val currentPrice = liveStock?.currentPrice ?: holding.averagePrice
    val totalShares = holding.shares + holding.sharesT1
    val currentValue = totalShares * currentPrice
    val costBasis = totalShares * holding.averagePrice
    val pnl = currentValue - costBasis
    val pnlPct = if (costBasis > 0) (pnl / costBasis) * 100.0 else 0.0

    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DarkSurface).border(1.dp, BrandViolet.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (isCall) AccentGreen.copy(alpha = 0.15f) else AccentRose.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(if (isCall) "CALL" else "PUT", color = if (isCall) AccentGreen else AccentRose, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp)); Text(holding.symbol, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text("Qty: ${totalShares.toInt()}${if (holding.sharesT1 > 0) " (${holding.sharesT1.toInt()} T1)" else ""}", color = TextMuted, fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatCurrency(currentValue, stats.currency), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            val isUp = pnl >= 0
            Text("${if (isUp) "+" else ""}${String.format(Locale.US, "%.1f", pnlPct)}%", color = if (isUp) AccentGreen else AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = { viewModel.sellStock(holding.symbol, totalShares) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1E1E)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp), modifier = Modifier.height(24.dp)) {
                Text("SQUARE OFF", color = AccentRose, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PositionsList(t1Holdings: List<Holding>, transactions: List<Transaction>, stockPrices: List<StockPrice>, stats: PortfolioStats, onTickerClick: (String) -> Unit, viewModel: TradingViewModel) {
    if (t1Holdings.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Timer, null, tint = TextMuted, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No Open Positions (T1)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Stocks bought today appear here until T+1 settlement.", color = TextMuted, fontSize = 11.sp)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            t1Holdings.forEach { holding ->
                val liveStock = stockPrices.find { it.symbol == holding.symbol }
                val currentPrice = liveStock?.currentPrice ?: holding.averagePrice
                val convertedPrice = viewModel.getConvertedStockPrice(currentPrice, holding.symbol, stats.currency)
                val pnl = holding.sharesT1 * (convertedPrice - holding.averagePrice)
                val pnlPct = if (holding.averagePrice > 0) (pnl / (holding.sharesT1 * holding.averagePrice)) * 100.0 else 0.0

                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DarkSurface).border(1.dp, BrandViolet.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).clickable { onTickerClick(holding.symbol) }.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(BrandViolet.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("T1", color = BrandViolet, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(holding.symbol, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${holding.sharesT1.toInt()} shares @ ${formatCurrency(holding.averagePrice, stats.currency)}", color = TextMuted, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatCurrency(holding.sharesT1 * convertedPrice, stats.currency), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        val isUp = pnl >= 0
                        Text("${if (isUp) "+" else ""}${String.format(Locale.US, "%.2f", pnlPct)}%", color = if (isUp) AccentGreen else AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingOrdersList(viewModel: TradingViewModel) {
    val pendingOrders by viewModel.activePendingOrders.collectAsStateWithLifecycle()
    val stats by viewModel.portfolioStats.collectAsStateWithLifecycle()

    if (pendingOrders.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.HourglassEmpty, null, tint = TextMuted, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No Pending Orders", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Active Limit and GTT orders will appear here.", color = TextMuted, fontSize = 11.sp)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pendingOrders.forEach { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (order.type == "BUY") AccentGreen.copy(alpha = 0.1f) else AccentRose.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(order.type, color = if (order.type == "BUY") AccentGreen else AccentRose, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(order.symbol, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(order.orderType, color = BrandViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${order.shares.toInt()} shares @ ${formatCurrency(order.triggerPrice, stats.currency)}", color = TextSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = { viewModel.deletePendingOrder(order.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = AccentRose.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TransactionsList(transactions: List<Transaction>, stockPrices: List<StockPrice>, stats: PortfolioStats, onTickerClick: (String) -> Unit) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SwapVert, null, tint = TextMuted, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No Trades Found", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            transactions.forEach { tx ->
                val liveStock = stockPrices.find { it.symbol == tx.symbol }
                val currentPrice = liveStock?.currentPrice ?: tx.price
                val isBuy = tx.type == "BUY"
                val presentValue = tx.shares * currentPrice
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DarkSurface).border(1.dp, DarkBorder, RoundedCornerShape(16.dp)).clickable { onTickerClick(tx.symbol) }.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (isBuy) AccentGreen.copy(alpha = 0.1f) else AccentRose.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(tx.type, color = if (isBuy) AccentGreen else AccentRose, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp)); Text(tx.symbol, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${tx.shares.toInt()} @ ${formatCurrency(tx.price, stats.currency)}", color = TextMuted, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) { Text(formatCurrency(presentValue, stats.currency), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
fun SectorHeatmapWidget(
    holdings: List<Holding>,
    stockPrices: List<StockPrice>,
    stats: PortfolioStats
) {
    if (holdings.isEmpty()) return
    var isExpanded by remember { mutableStateOf(false) }
    val sectorMap = remember(holdings, stockPrices) {
        val map = mutableMapOf<String, Double>()
        holdings.forEach { h ->
            val live = stockPrices.find { it.symbol == h.symbol }
            val price = live?.currentPrice ?: h.averagePrice
            val industry = mapTickerToIndustry(h.symbol)
            map[industry] = map.getOrDefault(industry, 0.0) + ((h.shares + h.sharesT1) * price)
        }
        map.toList().sortedByDescending { it.second }
    }
    Card(modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SECTOR ALLOCATION", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = TextMuted, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(26.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = 0.05f))) {
                Row(modifier = Modifier.fillMaxSize()) {
                    val totalValue = maxOf(stats.holdingsValue, 1.0)
                    sectorMap.forEachIndexed { index, (sector, value) ->
                        val weight = (value / totalValue).toFloat().coerceAtLeast(0.01f)
                        val color = when (index % 5) { 0 -> BrandViolet; 1 -> AccentGreen; 2 -> Color(0xFF2196F3); 3 -> AccentYellow; else -> AccentRose }
                        Box(modifier = Modifier.fillMaxHeight().weight(weight).background(color), contentAlignment = Alignment.Center) {
                            Text(sector.take(4).uppercase(), color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Clip, modifier = Modifier.padding(horizontal = 2.dp))
                        }
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    sectorMap.forEachIndexed { index, (sector, value) ->
                        val pct = (value / maxOf(stats.holdingsValue, 1.0)) * 100.0
                        val color = when (index % 5) { 0 -> BrandViolet; 1 -> AccentGreen; 2 -> Color(0xFF2196F3); 3 -> AccentYellow; else -> AccentRose }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(10.dp)); Text(sector, color = Color.White, fontSize = 12.sp)
                            }
                            Text("${String.format(Locale.US, "%.1f", pct)}%", color = TextSubtle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun mapTickerToIndustry(symbol: String): String {
    val clean = symbol.substringBefore(".NS").substringBefore(".BO").uppercase()
    return when {
        clean.contains("RELIANCE") || clean.contains("ONGC") || clean.contains("BPCL") -> "Energy"
        clean.contains("TCS") || clean.contains("INFY") || clean.contains("WIPRO") || clean.contains("HCL") -> "IT"
        clean.contains("BANK") || clean.contains("SBIN") || clean.contains("PFC") -> "Finance"
        clean.contains("TATASTEEL") || clean.contains("JSW") || clean.contains("HINDALCO") -> "Metals"
        clean.contains("AAPL") || clean.contains("MSFT") || clean.contains("GOOG") -> "Global"
        clean.contains("BTC") || clean.contains("ETH") -> "Crypto"
        clean.contains("GOLD") || clean.contains("CRUDE") -> "Commodity"
        else -> "Misc"
    }
}

@Composable
fun EquityCurveWidget(snapshots: List<AccountSnapshot>, currency: String) {
    var showDetails by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().clickable { showDetails = true }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("EQUITY CURVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Performance analytics", color = TextMuted, fontSize = 8.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = TextMuted, modifier = Modifier.size(14.dp))
        }
    }
    if (showDetails) {
        Dialog(onDismissRequest = { showDetails = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated), border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("PERFORMANCE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showDetails = false }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = TextMuted) }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    if (snapshots.size < 2) {
                        Column(modifier = Modifier.fillMaxWidth().height(160.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DataUsage, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("NOT ENOUGH DATA", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Typical history required: 30 days.", color = TextSubtle, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        val pricesString = snapshots.joinToString(",") { it.totalValue.toString() }
                        val isPositive = snapshots.last().totalValue >= snapshots.first().totalValue
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) { StockLineChart(pricesString = pricesString, isPositive = isPositive, showIndicators = false, modifier = Modifier.fillMaxSize()) }
                        Spacer(modifier = Modifier.height(16.dp))
                        val gain = snapshots.last().totalValue - snapshots.first().totalValue
                        val gainPct = if (snapshots.first().totalValue > 0) (gain / snapshots.first().totalValue) * 100.0 else 0.0
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("NET GROWTH", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = formatCurrency(gain, currency), color = if (gain >= 0) AccentGreen else AccentRose, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TOTAL RETURN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${if (gainPct >= 0) "+" else ""}${String.format(Locale.US, "%.2f", gainPct)}%", color = if (gainPct >= 0) AccentGreen else AccentRose, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showDetails = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BrandViolet), shape = RoundedCornerShape(12.dp)) { Text("Close View", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
