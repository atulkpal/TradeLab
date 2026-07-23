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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ashwathai.tradelab.data.*
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.theme.*
import com.ashwathai.tradelab.ui.common.*
import com.ashwathai.tradelab.ui.charts.*
import java.util.Locale
import androidx.compose.foundation.gestures.detectVerticalDragGestures

@Composable
fun WatchlistScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    latestNews: List<MarketNews>,
    onTickerClick: (String) -> Unit
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val mainActivity = context as? com.ashwathai.tradelab.MainActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg) // Prevent ghost bleed
            .padding(horizontal = 20.dp)
    ) {
        // 0. Breaking News
        BreakingNewsTicker(latestNews = latestNews)
        
        Spacer(modifier = Modifier.height(8.dp))

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
                            .background(if (isSelected) BrandViolet.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                            .border(1.dp, if (isSelected) BrandViolet else Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectWatchlist(wl.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wl.name,
                                color = if (isSelected) BrandViolet else Color.White.copy(alpha = 0.6f),
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
                        .background(BrandViolet.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, BrandViolet.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, "Add Watchlist", tint = BrandViolet, modifier = Modifier.size(16.dp))
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
                        focusedBorderColor = BrandViolet,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
                                        Text(stock.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                            Text(result.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            color = if (isPresent) BrandViolet else Color.White,
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
                    Text("Watchlist is empty", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (isCompactMode) 4.dp else 8.dp)
            ) {
                items(watchlistItems) { item ->
                    val stock = stockPrices.find { it.symbol == item.symbol }
                    stock?.let { s ->
                        WatchlistStockRow(
                            stock = s,
                            currency = stats.currency,
                            isCompact = isCompactMode,
                            onRemoveClick = { viewModel.toggleWatchlistV2(s.symbol) },
                            onClick = { onTickerClick(s.symbol) }
                        )
                    }
                }
            }
        }

        // Educational Context Tip
        var isTipDismissed by remember { mutableStateOf(false) }
        if (!isTipDismissed) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandViolet.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.School, null, tint = BrandViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RISK EDUCATION DISCIPLINE", color = BrandViolet, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Practice disciplined sizing: Never allocate more than 10% of your account to a single ticker.", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, lineHeight = 14.sp)
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
            confirmButton = { TextButton(onClick = { viewModel.renameWatchlist(watchlistToRename!!, renameInput); showRenameDialog = false }) { Text("SAVE", color = BrandViolet) } },
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
            confirmButton = { TextButton(onClick = { if (createInput.isNotBlank()) viewModel.addNewWatchlist(createInput); showCreateDialog = false }) { Text("CREATE", color = BrandViolet) } },
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
                    Text("Unlock Watchlist Sheet 📺", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                Text("GO PRO • ₹99/mo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    onClick: () -> Unit
) {
    val isPositive = stock.dailyChangePct >= 0
    val trendColor = if (isPositive) AccentGreen else AccentRose
    val rowPadding = if (isCompact) 8.dp else 12.dp

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (isCompact) 1.dp else 4.dp).clip(RoundedCornerShape(12.dp)).background(DarkSurfaceElevated).border(1.dp, DarkBorderElevated, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(rowPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
            Box(modifier = Modifier.size(if (isCompact) 28.dp else 34.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                Text(text = stock.symbol.take(4), color = Color.White, fontSize = if (isCompact) 8.sp else 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = stock.symbol, color = Color.White, fontSize = if (isCompact) 12.sp else 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!isCompact) {
                    Text(text = stock.companyName, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        StockLineChart(pricesString = stock.historyData, isPositive = isPositive, modifier = Modifier.width(60.dp).height(20.dp).padding(horizontal = 4.dp))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(text = formatCurrency(stock.currentPrice, currency), color = Color.White, fontSize = if (isCompact) 12.sp else 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", stock.dailyChangePct)}%", color = trendColor, fontSize = if (isCompact) 9.sp else 11.sp, fontWeight = FontWeight.Bold)
        }
        if (onRemoveClick != null) {
            IconButton(onClick = onRemoveClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(12.dp)) }
        }
    }
}

@Composable
fun BuySellBottomSheet(
    stock: StockPrice,
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isBuy by remember { mutableStateOf(true) }
    var sharesInput by remember { mutableStateOf("") }
    var orderType by remember { mutableStateOf("Market") } // "Market", "Limit", "Stop-Loss", "GTT"
    var isDelivery by remember { mutableStateOf(true) } // CNC vs MIS
    var customPriceInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice)) }
    var isExpanded by remember { mutableStateOf(false) }
    var activeEduTab by remember { mutableStateOf("Market") }

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(emptyList())
    val tickerNews by viewModel.getNewsForSymbol(stock.symbol).collectAsStateWithLifecycle(emptyList())
    val currentHolding = holdings.find { it.symbol == stock.symbol }
    val ownedShares = (currentHolding?.shares ?: 0.0) + (currentHolding?.sharesT1 ?: 0.0)

    val shares = sharesInput.toDoubleOrNull() ?: 0.0
    val price = if (orderType != "Market") (customPriceInput.toDoubleOrNull() ?: stock.currentPrice) else stock.currentPrice
    val totalOrderValue = shares * price

    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(if (isExpanded) 0.92f else 0.72f) // Extra room for the new product toggle
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 15f) {
                        if (isExpanded) {
                            isExpanded = false
                        } else {
                            onDismiss()
                        }
                    } else if (dragAmount < -15f) {
                        isExpanded = true
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .animateContentSize()
            .testTag("buy_sell_bottom_sheet"),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBuy) Color(0xFF062319) else Color(0xFF280C11)
        ),
        border = BorderStroke(1.5.dp, if (isBuy) AccentGreen.copy(alpha = 0.6f) else AccentRose.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
                .fillMaxSize()
        ) {
            // Sliding handle / expand indicator
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
                    .clickable { isExpanded = !isExpanded }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Expand/Collapse Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Toggle Expand",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = if (isExpanded) "Detailed Trade Desk" else "Quick Trade Desk",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Close", color = TextSubtle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Header info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = stock.symbol, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        if (ownedShares > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Holding: ${String.format(Locale.US, "%.2f", ownedShares)} shares",
                                    color = AccentGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(text = stock.companyName, color = TextMuted, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(stock.currentPrice, stats.currency),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (stock.sentimentBias != 0.0) {
                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                val bias = stock.sentimentBias.toFloat()
                                val alignment = if (bias >= 0) Alignment.CenterEnd else Alignment.CenterStart
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(kotlin.math.abs(bias).coerceIn(0.1f, 1f))
                                        .align(alignment)
                                        .background(if (bias >= 0) AccentGreen else AccentRose)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (stock.sentimentBias > 0.3) "BULLISH" else if (stock.sentimentBias < -0.3) "BEARISH" else "NEUTRAL",
                                color = if (stock.sentimentBias > 0.3) AccentGreen else if (stock.sentimentBias < -0.3) AccentRose else TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        val isPositive = stock.dailyChangePct >= 0
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", stock.dailyChangePct)}%",
                            color = if (isPositive) AccentGreen else AccentRose,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SCROLLABLE MIDDLE BODY ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Expanded Mode Section
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom StockLineChart showing SMA indicators
                    Text(
                        text = "Historical Trend (with 5-Period Simple Moving Average)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            StockLineChart(
                                pricesString = stock.historyData,
                                isPositive = stock.dailyChangePct >= 0,
                                showIndicators = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detailed stock indicators Grid
                    Text(
                        text = "Key Market Statistics",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Prev Close" to formatCurrency(stock.previousClose, stats.currency),
                            "Daily High" to formatCurrency(stock.highPrice, stats.currency),
                            "Daily Low" to formatCurrency(stock.lowPrice, stats.currency)
                        ).forEach { (label, value) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Market Cap" to getMarketCap(stock.symbol, stats.currency),
                            "Avg Volume" to getVolume(stock.symbol),
                            "Volatility" to if (stock.symbol == "TSLA" || stock.symbol == "BTC" || stock.symbol == "ETH") "High" else "Moderate"
                        ).forEach { (label, value) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Educational Academy tabs
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Academy info", tint = BrandViolet, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TradeLab Academy: Understanding Order Types", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Scrollable/clickable tab row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Market", "Limit", "Stop-Loss", "GTT").forEach { tab ->
                                    val selected = activeEduTab == tab
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) BrandViolet.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                                            .border(1.dp, if (selected) BrandViolet else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { activeEduTab = tab }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(tab, color = if (selected) BrandViolet else Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val explanation = when (activeEduTab) {
                                "Market" -> "Executes immediately at the best available market price. Perfect for rapid entry but vulnerable to sudden slippage."
                                "Limit" -> "Executes only if the price drops to or below your limit target (when buying) or rises to your limit target (when selling). Guarantees the price, but may never trigger if market misses your target."
                                "Stop-Loss" -> "Automated trigger that executes a trade once price hits your threshold. Crucial for locking in profits or cutting losses short before they spiral."
                                "GTT" -> "Good-Till-Triggered order remains active indefinitely (until custom trigger price hits or you cancel), rather than expiring at the end of the day."
                                else -> ""
                            }
                            Text(explanation, color = TextSubtle, fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }

                    if (tickerNews.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Specific Ticker Insights",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        tickerNews.take(3).forEach { news ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                border = BorderStroke(1.dp, if (news.isAiRefined) BrandViolet.copy(alpha = 0.3f) else DarkBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (news.sentiment == "BULLISH") AccentGreen.copy(alpha = 0.2f) else AccentRose.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = news.sentiment,
                                                color = if (news.sentiment == "BULLISH") AccentGreen else AccentRose,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (news.isAiRefined) {
                                            Text("👑 PRO INSIGHT", color = AccentYellow, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(news.source, color = BrandViolet, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(news.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (news.isAiRefined && stats.isPremium) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = news.summary,
                                            color = TextSubtle,
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(BrandViolet.copy(alpha = 0.05f))
                                                .padding(6.dp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(news.summary, color = TextSubtle, fontSize = 10.sp, lineHeight = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BUY / SELL TAB SWITCH (Neon green for buy, Neon rose for sell)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkBg)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBuy) AccentGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isBuy) AccentGreen else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { isBuy = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BUY",
                            color = if (isBuy) AccentGreen else Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBuy) AccentRose.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (!isBuy) AccentRose else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { isBuy = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SELL",
                            color = if (!isBuy) AccentRose else Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // NEW: PRODUCT TYPE TOGGLE (CNC vs MIS)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PRODUCT TYPE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkBg).padding(2.dp)) {
                        listOf(true to "CNC (Delivery)", false to "MIS (Intraday)").forEach { (isDel, label) ->
                            val selected = isDelivery == isDel
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (selected) Color.White.copy(alpha = 0.08f) else Color.Transparent).clickable { isDelivery = isDel }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = if (selected) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Educational Warning for MIS
                    if (!isDelivery) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ MIS/Intraday: Used for same-day trading with higher leverage. Positions are auto-squared off before market close (3:20 PM IST). Penalty applies for failed auto-squareoff.",
                            color = AccentYellow,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💎 CNC/Delivery: Pay full value to hold shares for multiple days. T+1 Settlement cycle applies. You can still exit on the same day if desired.",
                            color = BrandViolet,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // INPUTS: Order Type, Units (Shares), Custom price if Limit/Stop/GTT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Order Type Selector (Market, Limit, Stop-Loss, GTT)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "ORDER TYPE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBg)
                                .padding(2.dp)
                        ) {
                            listOf("Market", "Limit", "Stop-Loss", "GTT").forEach { type ->
                                val selected = orderType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) Color.White.copy(alpha = 0.08f) else Color.Transparent)
                                        .clickable { orderType = type }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (type == "Stop-Loss") "Stop" else type,
                                        color = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Number of units input
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(text = "QUANTITY (SHARES)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = sharesInput,
                            onValueChange = { sharesInput = it },
                            placeholder = { Text("0.0", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg,
                                focusedBorderColor = if (isBuy) AccentGreen else AccentRose,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("trade_quantity_input")
                        )
                    }

                    // Custom Price Input for Limit/Stop/GTT Order
                    if (orderType != "Market") {
                        Column(modifier = Modifier.weight(1f)) {
                            val fieldLabel = when (orderType) {
                                "Limit" -> "LIMIT PRICE"
                                "Stop-Loss" -> "STOP PRICE"
                                else -> "GTT TRIGGER"
                            }
                            Text(text = fieldLabel, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customPriceInput,
                                onValueChange = { customPriceInput = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkBg,
                                    unfocusedContainerColor = DarkBg,
                                    focusedBorderColor = if (isBuy) AccentGreen else AccentRose,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("trade_limit_price_input")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Sizing Percentage Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("QUICK SIZE:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    listOf("25%", "50%", "100%").forEach { pct ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable {
                                    val ratio = when (pct) {
                                        "25%" -> 0.25
                                        "50%" -> 0.50
                                        else -> 1.0
                                    }
                                    val currentPrice = if (orderType != "Market") {
                                        customPriceInput.toDoubleOrNull() ?: stock.currentPrice
                                    } else {
                                        stock.currentPrice
                                    }
                                    if (isBuy) {
                                        val safeRatio = if (pct == "100%") 0.95 else ratio
                                        val allocatedCash = stats.cash * safeRatio
                                        if (currentPrice > 0) {
                                            val calculatedShares = allocatedCash / currentPrice
                                            sharesInput = String.format(Locale.US, "%.4f", calculatedShares)
                                        }
                                    } else {
                                        sharesInput = String.format(Locale.US, "%.4f", ownedShares * ratio)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pct, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Order Metrics calculation card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Order Value", color = TextSubtle, fontSize = 12.sp)
                            Text(
                                text = formatCurrency(totalOrderValue, stats.currency),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Available Cash", color = TextSubtle, fontSize = 12.sp)
                            Text(
                                text = formatCurrency(stats.cash, stats.currency),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Brokerage Shield Wallet Overlay
                        Spacer(modifier = Modifier.height(8.dp))
                        val isShielded = stats.brokerageCredits >= 20
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Brokerage Fees Shield", color = TextSubtle, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isShielded) AccentGreen.copy(alpha = 0.15f) else AccentYellow.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isShielded) "ACTIVE" else "NO CREDITS",
                                        color = if (isShielded) AccentGreen else AccentYellow,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = if (isShielded) "Waived (Spent 20🎫)" else "0.05% Deduct cash",
                                color = if (isShielded) AccentGreen else TextSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Concentration / Sizing Warning to enforce realistic practicing
                        if (isBuy && totalOrderValue > (stats.totalValue * 0.15)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentYellow.copy(alpha = 0.1f))
                                    .border(1.dp, AccentYellow.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "⚠️ Risk Warning: This single order represents over 15% of your total practising budget. We recommend sizing smaller (e.g. ₹1,000) to protect against unexpected downturns.",
                                    color = AccentYellow,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- FIXED FOOTER ---
            // Action Execution Button
            val hasValidAmount = shares > 0
            val hasEnoughCash = !isBuy || (totalOrderValue <= stats.cash)
            val canExecute = hasValidAmount && hasEnoughCash

            Button(
                onClick = {
                    viewModel.setTradeShares(sharesInput)
                    viewModel.setOrderType(orderType)
                    viewModel.setDeliveryMode(isDelivery)
                    if (orderType != "Market") {
                        viewModel.setTriggerPrice(customPriceInput)
                    }
                    if (isBuy) {
                        viewModel.executeBuy()
                    } else {
                        viewModel.executeSell()
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("execute_trade_button"),
                enabled = canExecute,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuy) AccentGreen else AccentRose,
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Text(
                    text = if (isBuy) "CONFIRM BUY ORDER" else "CONFIRM SELL ORDER",
                    color = if (canExecute) DarkBg else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "CANCEL", color = TextSubtle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

