package com.ashwathai.tradelab.ui.watchlist

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ashwathai.tradelab.data.*
import com.ashwathai.tradelab.rememberLaunchPromo
import com.ashwathai.tradelab.R
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.theme.*
import com.ashwathai.tradelab.ui.common.*
import com.ashwathai.tradelab.ui.charts.*
import java.util.Locale
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

@Composable
fun WatchlistScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    latestNews: List<MarketNews>,
    onTickerClick: (String, Boolean, Boolean) -> Unit
) {
    val watchlistItems by viewModel.selectedWatchlistItems.collectAsStateWithLifecycle()
    val watchlistNames by viewModel.watchlistNames.collectAsStateWithLifecycle()
    val selectedWatchlistId by viewModel.selectedWatchlistId.collectAsStateWithLifecycle()
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val isCompactMode by viewModel.isWatchlistCompactMode.collectAsStateWithLifecycle()

    val watchlistSearchQuery by viewModel.watchlistSearchQuery.collectAsStateWithLifecycle()
    val watchlistAutocompleteResults by viewModel.watchlistAutocompleteResults.collectAsStateWithLifecycle()
    val isWatchlistSearching by viewModel.isWatchlistSearching.collectAsStateWithLifecycle()
    val isWatchlistSearchVisible by viewModel.isWatchlistSearchVisible.collectAsStateWithLifecycle()
    val isSimulatedMode by viewModel.isSimulatedMode.collectAsStateWithLifecycle()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    var watchlistToRename by remember { mutableStateOf<Int?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var createInput by remember { mutableStateOf("") }

    var showAdConfirmationDialog by remember { mutableStateOf(false) }
    var isAdLoading by remember { mutableStateOf(false) }

    val promo = rememberLaunchPromo()

    val context = androidx.compose.ui.platform.LocalContext.current
    val mainActivity = context as? com.ashwathai.tradelab.MainActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg) // Prevent ghost bleed
            .padding(horizontal = 20.dp)
    ) {
        // 0. Breaking News
        if (!LocalZenMode.current) {
            BreakingNewsTicker(latestNews = latestNews)
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 1. Multi-Watchlist Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(watchlistNames) { wl ->
                    val isSelected = wl.id == selectedWatchlistId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) DynamicPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                            .border(1.dp, if (isSelected) DynamicPrimary else Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectWatchlist(wl.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wl.name,
                                color = if (isSelected) DynamicPrimary else TextPrimary.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = if (isSelected) BrandViolet else TextMuted,
                                modifier = Modifier
                                    .size(10.dp)
                                    .clickable {
                                        watchlistToRename = wl.id
                                        renameInput = wl.name
                                        showRenameDialog = true
                                    }
                            )
                            if (wl.id > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = AccentRose.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clickable { viewModel.deleteWatchlist(wl.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (watchlistNames.size < 10) {
                IconButton(
                    onClick = {
                        val maxAllowed = if (stats.isPremium) 10 else 5
                        if (watchlistNames.size >= maxAllowed) {
                            if (!stats.isPremium) {
                                viewModel.triggerPaywall()
                                viewModel.showFeedback("Go Pro to unlock up to 10 watchlists!")
                            } else {
                                viewModel.showFeedback("Maximum of 10 watchlists reached!")
                            }
                        } else {
                            if (stats.isPremium) {
                                createInput = "Sheet ${watchlistNames.size + 1}"
                                showCreateDialog = true
                            } else {
                                if (watchlistNames.isNotEmpty()) {
                                    createInput = "Sheet ${watchlistNames.size + 1}"
                                    showAdConfirmationDialog = true
                                } else {
                                    createInput = "Sheet ${watchlistNames.size + 1}"
                                    showCreateDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .background(DynamicPrimary.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, DynamicPrimary.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, "Add Watchlist", tint = DynamicPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // 2. Search Overlay
        AnimatedVisibility(
            visible = isWatchlistSearchVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                OutlinedTextField(
                    value = watchlistSearchQuery,
                    onValueChange = { viewModel.setWatchlistSearchQuery(it) },
                    placeholder = { Text("Search and add tickers...", color = TextMuted, fontSize = 12.sp) },
                    trailingIcon = {
                        if (isWatchlistSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandViolet, strokeWidth = 2.dp)
                        } else if (watchlistSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setWatchlistSearchQuery("") }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, "Clear", tint = TextSubtle, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = DynamicPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                if (watchlistSearchQuery.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).zIndex(20f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
                    ) {
                        Column {
                            val filtered = stockPrices.filter {
                                it.symbol.contains(watchlistSearchQuery, ignoreCase = true) ||
                                        it.companyName.contains(watchlistSearchQuery, ignoreCase = true)
                            }
                            filtered.take(4).forEach { stock ->
                                val isAdded = watchlistItems.any { it.symbol == stock.symbol }
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.toggleWatchlistV2(stock.symbol)
                                        viewModel.setWatchlistSearchQuery("") 
                                    }.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(stock.symbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(stock.companyName, color = TextMuted, fontSize = 10.sp)
                                    }
                                    Icon(if (isAdded) Icons.Default.Check else Icons.Default.Add, null, tint = if (isAdded) BrandViolet else AccentYellow, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (watchlistAutocompleteResults.isNotEmpty()) {
                                Text("GLOBAL SUGGESTIONS", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                                watchlistAutocompleteResults.take(3).forEach { result ->
                                    val isAdded = watchlistItems.any { it.symbol == result.symbol }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            viewModel.injectLiveStock(symbol = result.symbol, addToWatchlistId = selectedWatchlistId)
                                            viewModel.setWatchlistSearchQuery("")
                                        }.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(result.symbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${result.name} • ${result.exchange}", color = TextMuted, fontSize = 10.sp)
                                        }
                                        Icon(if (isAdded) Icons.Default.Check else Icons.Default.Add, null, tint = if (isAdded) BrandViolet else AccentYellow, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2.5. DYNAMIC POPULAR TICKERS - ONLY IF LIST IS SMALL (< 5 items)
        if (watchlistItems.size < 5 && !isWatchlistSearchVisible) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ADD:",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                val populars = listOf("TATASTEEL", "RELIANCE", "TCS", "INFY", "HDFCBANK", "SBIN")
                populars.forEach { symbol ->
                    val isPresent = watchlistItems.any { it.symbol == symbol }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPresent) BrandViolet.copy(alpha = 0.15f) else DarkSurface)
                            .border(1.dp, if (isPresent) BrandViolet else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleWatchlistV2(symbol) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+ $symbol",
                            color = if (isPresent) DynamicPrimary else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Ticker List Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TICKERS (${watchlistItems.size})", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                val anyIndianTicker = watchlistItems.any { viewModel.isIndianStockSymbol(it.symbol) }
                if (!isSimulatedMode && anyIndianTicker && !viewModel.isMarketOpen("RELIANCE.NS")) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentRose.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("MARKET CLOSED", color = AccentRose, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            IconButton(onClick = { viewModel.toggleWatchlistCompactMode() }, modifier = Modifier.size(20.dp)) {
                Icon(if (isCompactMode) Icons.Default.ViewStream else Icons.Default.DensityMedium, null, tint = BrandViolet, modifier = Modifier.size(14.dp))
            }
        }

        if (watchlistItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FormatListBulleted, null, tint = TextSubtle, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Watchlist is empty", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (isCompactMode) 4.dp else 8.dp)
            ) {
                items(watchlistItems.take(5)) { item ->
                    val stock = stockPrices.find { it.symbol == item.symbol }
                    stock?.let { s ->
                        WatchlistStockRow(
                            stock = s,
                            currency = stats.currency,
                            isCompact = isCompactMode,
                            onRemoveClick = { viewModel.toggleWatchlistV2(s.symbol) },
                            onAction = { isBuy -> onTickerClick(s.symbol, isBuy, false) },
                            onClick = { onTickerClick(s.symbol, true, true) },
                            onChartClick = { viewModel.navigateToChart(s.symbol) }
                        )
                    }
                }


                items(watchlistItems.drop(5)) { item ->
                    val stock = stockPrices.find { it.symbol == item.symbol }
                    stock?.let { s ->
                        WatchlistStockRow(
                            stock = s,
                            currency = stats.currency,
                            isCompact = isCompactMode,
                            onRemoveClick = { viewModel.toggleWatchlistV2(s.symbol) },
                            onAction = { isBuy -> onTickerClick(s.symbol, isBuy, false) },
                            onClick = { onTickerClick(s.symbol, true, true) },
                            onChartClick = { viewModel.navigateToChart(s.symbol) }
                        )
                    }
                }
                
            }
        }

        // Educational Context Tip - Adaptive
        var isTipDismissed by remember { mutableStateOf(false) }
        val adaptiveTip by viewModel.adaptiveGuidanceText.collectAsStateWithLifecycle()
        if (!isTipDismissed && adaptiveTip.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DynamicPrimary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, DynamicPrimary.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.School, null, tint = BrandViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RISK • ACTION • DISCIPLINE", color = BrandViolet, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(adaptiveTip, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, lineHeight = 14.sp)
                    }
                    IconButton(onClick = { isTipDismissed = true }, modifier = Modifier.size(20.dp)) { Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(12.dp)) }
                }
            }
        }
    }

    // Dialogs
    if (showRenameDialog && watchlistToRename != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Watchlist", color = Color.White) },
            text = {
                OutlinedTextField(value = renameInput, onValueChange = { renameInput = it }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = BrandViolet, unfocusedBorderColor = DarkBorder))
            },
            confirmButton = { TextButton(onClick = { viewModel.renameWatchlist(watchlistToRename!!, renameInput); showRenameDialog = false }) { Text("SAVE", color = DynamicPrimary) } },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("CANCEL", color = TextMuted) } },
            containerColor = DarkSurfaceElevated
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Watchlist", color = Color.White) },
            text = {
                OutlinedTextField(value = createInput, onValueChange = { createInput = it }, placeholder = { Text("Sheet name...") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = BrandViolet, unfocusedBorderColor = DarkBorder))
            },
            confirmButton = { TextButton(onClick = { if (createInput.isNotBlank()) viewModel.addNewWatchlist(createInput); showCreateDialog = false }) { Text("CREATE", color = DynamicPrimary) } },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("CANCEL", color = TextMuted) } },
            containerColor = DarkSurfaceElevated
        )
    }

    if (showAdConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showAdConfirmationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Watch Ad",
                        tint = BrandViolet,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_status_ad),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unlock Watchlist Sheet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = "The first watchlist sheet is completely free. To unlock and create additional custom sheets (up to 5), please watch a short sponsor ad!",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
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
                                text = "Get TradeLab Pro to unlock unlimited watchlists, zero brokerage, and double rewards instantly.",
                                color = TextSubtle,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    showAdConfirmationDialog = false
                                    viewModel.openProBenefits()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.align(Alignment.End).testTag("watchlist_go_pro_button")
                            ) {
                                Text("GO PRO • ${promo.priceLabel}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isAdLoading) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Loading sponsor ad...", color = TextMuted, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isAdLoading = true
                        if (mainActivity != null) {
                            mainActivity.loadAndShowRewardedAd(
                                adType = com.ashwathai.tradelab.MainActivity.AdType.WATCHLIST_CREATE,
                                onAdLoaded = { isAdLoading = false },
                                onAdFailed = { err ->
                                    isAdLoading = false
                                    createInput = "Sheet ${watchlistNames.size + 1}"
                                    showCreateDialog = true
                                    showAdConfirmationDialog = false
                                },
                                onUserEarnedReward = {
                                    isAdLoading = false
                                    showAdConfirmationDialog = false
                                    createInput = "Sheet ${watchlistNames.size + 1}"
                                    showCreateDialog = true
                                }
                            )
                        } else {
                            isAdLoading = false
                            showAdConfirmationDialog = false
                            createInput = "Sheet ${watchlistNames.size + 1}"
                            showCreateDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAdLoading
                ) {
                    Text("WATCH AD", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAdConfirmationDialog = false },
                    enabled = !isAdLoading
                ) {
                    Text("CANCEL", color = TextSubtle)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
fun WatchlistStockRow(
    stock: StockPrice, 
    currency: String, 
    isCompact: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    onAction: (Boolean) -> Unit, // Boolean: isBuy
    onClick: () -> Unit,
    onChartClick: () -> Unit
) {
    val isPositive = stock.dailyChangePct >= 0
    val trendColor = if (isPositive) AccentGreen else AccentRose
    val rowPadding = if (isCompact) 8.dp else 12.dp

    var offsetX by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    offsetX > 50f -> AccentGreen.copy(alpha = 0.2f)
                    offsetX < -50f -> AccentRose.copy(alpha = 0.2f)
                    else -> DarkSurfaceElevated
                }
            )
    ) {
        // Background Labels
        if (offsetX > 50f) {
            Text(
                "BUY",
                color = AccentGreen,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp)
            )
        } else if (offsetX < -50f) {
            Text(
                "SELL",
                color = AccentRose,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp)
            )
        }

        Row(
            modifier = Modifier
                .offset(x = offsetX.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkBorderElevated, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            offsetX += dragAmount / 2
                            change.consume()
                        },
                        onDragEnd = {
                            if (offsetX > 100f) {
                                onAction(true)
                            } else if (offsetX < -100f) {
                                onAction(false)
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f }
                    )
                }
                .clickable { onClick() }
                .padding(rowPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
                Box(modifier = Modifier.size(if (isCompact) 28.dp else 34.dp).clip(RoundedCornerShape(6.dp)).background(TextPrimary.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                    Text(text = stock.symbol.take(4), color = TextPrimary, fontSize = if (isCompact) 8.sp else 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = stock.symbol, color = TextPrimary, fontSize = if (isCompact) 12.sp else 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (!isCompact) {
                        Text(text = stock.companyName, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                StockLineChart(pricesString = stock.historyData, isPositive = isPositive, mini = true, modifier = Modifier.width(50.dp).height(20.dp).padding(horizontal = 4.dp))
                IconButton(onClick = onChartClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.AutoGraph, null, tint = BrandViolet, modifier = Modifier.size(14.dp))
                }
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(text = formatCurrency(stock.currentPrice, currency), color = TextPrimary, fontSize = if (isCompact) 12.sp else 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.stealthBlur())
                Text(text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", stock.dailyChangePct)}%", color = trendColor, fontSize = if (isCompact) 9.sp else 11.sp, fontWeight = FontWeight.Bold)
            }
            if (onRemoveClick != null) {
                IconButton(onClick = onRemoveClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(12.dp)) }
            }
        }
    }
}

@Composable
fun BuySellBottomSheet(
    stock: StockPrice,
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    initialIsBuy: Boolean = true,
    initialIsExpanded: Boolean = false,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isBuy by remember(stock.symbol) { mutableStateOf(initialIsBuy) }
    var sharesInput by remember(stock.symbol) { mutableStateOf("") }
    var orderType by remember(stock.symbol) { mutableStateOf("Market") } // "Market", "Limit", "Stop-Loss", "GTT"
    var isDelivery by remember(stock.symbol) { mutableStateOf(true) } // CNC vs MIS
    var customPriceInput by remember(stock.symbol) { mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice)) }
    var targetPriceInput by remember(stock.symbol) { mutableStateOf("") }
    var stopLossPriceInput by remember(stock.symbol) { mutableStateOf("") }
    var isTrailing by remember(stock.symbol) { mutableStateOf(false) }
    var trailingGapInput by remember(stock.symbol) { mutableStateOf("") }
    var isBracketOrder by remember(stock.symbol) { mutableStateOf(false) }
    var isExpanded by remember(stock.symbol) { mutableStateOf(initialIsExpanded) }
    var activeEduTab by remember { mutableStateOf("Market") }
    var showIntradayUnlockDialog by remember { mutableStateOf(false) }
    var isAdLoadingInsideSheet by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle(emptyList())
    val tickerNews by viewModel.getNewsForSymbol(stock.symbol).collectAsStateWithLifecycle(emptyList())
    val currentHolding = holdings.find { it.symbol == stock.symbol }
    val ownedShares = (currentHolding?.shares ?: 0.0) + (currentHolding?.sharesT1 ?: 0.0)

    val shares = sharesInput.toDoubleOrNull() ?: 0.0
    val price = if (orderType != "Market") (customPriceInput.toDoubleOrNull() ?: stock.currentPrice) else stock.currentPrice
    val totalOrderValue = shares * price

    val scrollState = rememberScrollState()
    val isLevUnlocked = stats.isPremium || (userProfile?.leverageUnlockedUntil ?: 0L) > System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(if (isExpanded) 0.95f else 0.65f)
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20f) { if (isExpanded) isExpanded = false else onDismiss() }
                    else if (dragAmount < -20f) isExpanded = true
                }
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }
            .animateContentSize()
            .testTag("buy_sell_bottom_sheet"),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = if (isBuy) DeepProfit else DeepLoss),
        border = BorderStroke(1.dp, (if (isBuy) AccentGreen else AccentRose).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxSize()) {
            // Sliding Handle
            Box(modifier = Modifier.width(36.dp).height(4.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
            
            Spacer(modifier = Modifier.height(12.dp))

            // OVERHAULED CONDENSED HEADER
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stock.symbol, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.navigateToChart(stock.symbol) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.AutoGraph, null, tint = BrandViolet, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(text = stock.companyName, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = formatCurrency(stock.currentPrice, stats.currency), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.stealthBlur())
                    val isPositive = stock.dailyChangePct >= 0
                    Text(text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", stock.dailyChangePct)}%", color = if (isPositive) AccentGreen else AccentRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                    Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Exit button when user holds this stock
            if (ownedShares > 0.0001) {
                Button(
                    onClick = {
                        viewModel.exitPosition(stock.symbol)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = AccentRose, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXIT POSITION - ${String.format(Locale.US, "%.2f", ownedShares)} shares", color = AccentRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- SCROLLABLE BODY ---
            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState)) {
                
                if (isExpanded) {
                    // Minimized Chart and Stats in Detailed Mode
                    Card(modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
                        Box(modifier = Modifier.padding(8.dp)) { StockLineChart(pricesString = stock.historyData, isPositive = stock.dailyChangePct >= 0, showIndicators = true, modifier = Modifier.fillMaxSize()) }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Prev Close" to formatCurrency(stock.previousClose, stats.currency), "High" to formatCurrency(stock.highPrice, stats.currency), "Low" to formatCurrency(stock.lowPrice, stats.currency)).forEach { (l, v) ->
                            Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(TextPrimary.copy(alpha = 0.03f)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(l, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(v, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 1. DENSE CONTROL GRID
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(TextPrimary.copy(alpha = 0.03f)).border(1.dp, TextPrimary.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Quantity Input (Standard Text Box as requested)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("QUANTITY", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = sharesInput,
                                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) sharesInput = it },
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("trade_quantity_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = DarkBg, unfocusedContainerColor = DarkBg, focusedBorderColor = DynamicPrimary, unfocusedBorderColor = Color.Transparent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        
                        // Transaction Toggle
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ACTION", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(8.dp)).background(DarkBg).padding(2.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(if (isBuy) AccentGreen.copy(alpha = 0.2f) else Color.Transparent).clickable { isBuy = true }, contentAlignment = Alignment.Center) {
                                    Text("BUY", color = if (isBuy) AccentGreen else Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(if (!isBuy) AccentRose.copy(alpha = 0.2f) else Color.Transparent).clickable { isBuy = false }, contentAlignment = Alignment.Center) {
                                    Text("SELL", color = if (!isBuy) AccentRose else Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Product Toggle
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PRODUCT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(8.dp)).background(DarkBg).padding(2.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(if (isDelivery) Color.White.copy(alpha = 0.1f) else Color.Transparent).clickable { isDelivery = true }, contentAlignment = Alignment.Center) {
                                    Text("CNC", color = if (isDelivery) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(if (!isDelivery) Color.White.copy(alpha = 0.1f) else Color.Transparent).clickable { if (isLevUnlocked) isDelivery = false else showIntradayUnlockDialog = true }, contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("MIS", color = if (!isDelivery) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        if (!isLevUnlocked) Icon(Icons.Default.Lock, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(8.dp).padding(start = 2.dp))
                                    }
                                }
                            }
                        }

                        // Order Type
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TYPE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(8.dp)).background(DarkBg).padding(2.dp).horizontalScroll(rememberScrollState())) {
                                listOf("Market", "Limit", "Stop-Loss", "GTT").forEach { type ->
                                    val sel = orderType == type
                                    Box(modifier = Modifier.width(60.dp).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(if (sel) BrandViolet.copy(alpha = 0.2f) else Color.Transparent).clickable { orderType = type }, contentAlignment = Alignment.Center) {
                                        Text(if (type == "Stop-Loss") "Stop" else type, color = if (sel) BrandViolet else Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (orderType != "Market") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("PRICE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customPriceInput,
                                onValueChange = { customPriceInput = it },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = DarkBg, unfocusedContainerColor = DarkBg, focusedBorderColor = DynamicPrimary, unfocusedBorderColor = Color.Transparent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. HIGH PROMINENCE FINANCIAL STATUS BAR
                val isLevActive = !isDelivery && isLevUnlocked
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    val effectiveOrderValue = if (isLevActive) totalOrderValue / 5.0 else totalOrderValue
                    val sttRate = if (isDelivery) 0.001 else 0.00025
                    val stt = totalOrderValue * sttRate
                    val miscCharges = totalOrderValue * 0.0001
                    val isShielded = stats.brokerageCredits >= 20 || stats.isPremium
                    val brokerageFee = if (isShielded) 0.0 else totalOrderValue * 0.0005
                    val totalCharges = stt + miscCharges + brokerageFee

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(if (isLevActive) "MARGIN REQUIRED (5x)" else "ORDER VALUE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(formatCurrency(effectiveOrderValue, stats.currency), color = if (isLevActive) AccentGreen else TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.stealthBlur())
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CASH IN HAND", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(formatCurrency(stats.cash, stats.currency), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.stealthBlur())
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = TextPrimary.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("EST. CHARGES (TAX/FEE)", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if (isShielded) AccentGreen.copy(alpha = 0.15f) else AccentYellow.copy(alpha = 0.15f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isShielded) {
                                            Image(painter = painterResource(R.drawable.ic_status_shield), contentDescription = null, modifier = Modifier.size(9.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }
                                        Text(if (isShielded) "SHIELDED" else "FEE UNLOCKED", color = if (isShielded) AccentGreen else AccentYellow, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                            Text(formatCurrency(totalCharges, stats.currency), color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quick Sizing Buttons
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("25%", "50%", "100%").forEach { pct ->
                        val ratio = if (pct == "25%") 0.25 else if (pct == "50%") 0.5 else 1.0
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(TextPrimary.copy(alpha = 0.05f)).clickable {
                            val curPrice = if (orderType != "Market") customPriceInput.toDoubleOrNull() ?: stock.currentPrice else stock.currentPrice
                            if (isBuy) {
                                val allocated = stats.cash * (if (pct == "100%") 0.95 else ratio)
                                sharesInput = if (curPrice > 0) (allocated / curPrice).toInt().toString() else ""
                            } else sharesInput = (ownedShares * ratio).toInt().toString()
                        }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(pct, color = TextPrimary.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // BRACKET / INSTITUTIONAL SECTION
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("BRACKET ORDER (PRO)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Switch(checked = isBracketOrder, onCheckedChange = { if (stats.isPremium) { isBracketOrder = it; if (it && orderType == "Market") orderType = "Limit" } else viewModel.triggerPaywall() }, colors = SwitchDefaults.colors(checkedThumbColor = DynamicPrimary, checkedTrackColor = DynamicPrimary.copy(alpha = 0.3f)))
                    }
                    
                    if (isBracketOrder) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("TARGET" to targetPriceInput, "STOP LOSS" to stopLossPriceInput).forEach { (l, v) ->
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(l, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(value = v, onValueChange = { if (l == "TARGET") targetPriceInput = it else stopLossPriceInput = it }, modifier = Modifier.fillMaxWidth().height(44.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = DarkBg, unfocusedContainerColor = DarkBg, focusedBorderColor = if (l == "TARGET") AccentGreen else AccentRose, unfocusedBorderColor = DarkBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), shape = RoundedCornerShape(10.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                }
                            }
                        }
                    }

                    if (tickerNews.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("INSIGHTS", color = BrandViolet, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        tickerNews.take(2).forEach { news ->
                            Column(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TextPrimary.copy(alpha = 0.02f)).border(1.dp, TextPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(10.dp)) {
                                Text(news.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(news.summary, color = TextSubtle, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }

            // FIXED FOOTER
            val canExec = shares > 0 && (isBuy.not() || totalOrderValue <= stats.cash)
            Button(
                onClick = {
                    viewModel.setTradeShares(sharesInput); viewModel.setOrderType(orderType); viewModel.setDeliveryMode(isDelivery)
                    if (orderType != "Market") viewModel.setTriggerPrice(customPriceInput)
                    if (isBracketOrder) { viewModel.setTargetPrice(targetPriceInput); viewModel.setStopLossPrice(stopLossPriceInput) }
                    viewModel.setIsTrailing(isTrailing); if (isTrailing) viewModel.setTrailingGap(trailingGapInput)
                    if (isBuy) viewModel.executeBuy() else viewModel.executeSell()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("execute_trade_button"),
                enabled = canExec,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isBuy) AccentGreen else AccentRose, disabledContainerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Text(text = if (isBuy) "CONFIRM BUY" else "CONFIRM SELL", color = if (canExec) Color.Black else TextMuted, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }

        // Just-in-Time Intraday Suite Unlock Dialog
        if (showIntradayUnlockDialog) {
            AlertDialog(
                onDismissRequest = { if (!isAdLoadingInsideSheet) showIntradayUnlockDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = BrandViolet, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.ic_status_pro),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unlock Intraday Suite", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Practice like a pro! 1 ad unlocks the full Intraday Suite (MIS mode + 5x Buying Power) for the rest of today's market session.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        if (isAdLoadingInsideSheet) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Connecting to sponsor...", color = BrandViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    val currentContext = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            isAdLoadingInsideSheet = true
                            val activity = currentContext as? com.ashwathai.tradelab.MainActivity
                            if (activity != null) {
                                activity.loadAndShowRewardedAd(
                                    adType = com.ashwathai.tradelab.MainActivity.AdType.PROFILE_LEVERAGE,
                                    onAdLoaded = { isAdLoadingInsideSheet = false },
                                    onAdFailed = { err: String ->
                                        isAdLoadingInsideSheet = false
                                        viewModel.showFeedback("Ad Failed: $err. Unlocking via offline fallback.")
                                        viewModel.unlockIntradaySession()
                                        showIntradayUnlockDialog = false
                                    },
                                    onUserEarnedReward = {
                                        viewModel.unlockIntradaySession()
                                        showIntradayUnlockDialog = false
                                    }
                                )
                            } else {
                                isAdLoadingInsideSheet = false
                                viewModel.unlockIntradaySession()
                                showIntradayUnlockDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAdLoadingInsideSheet
                    ) {
                        Text("WATCH AD", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showIntradayUnlockDialog = false }, enabled = !isAdLoadingInsideSheet) {
                        Text("CANCEL", color = TextSubtle)
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }
    }
}

