package com.ashwathai.tradelab.ui.watchlist

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
fun HomeScreen(viewModel: TradingViewModel, stats: PortfolioStats) {
    val watchlistItems by viewModel.selectedWatchlistItems.collectAsStateWithLifecycle()
    val watchlistNames by viewModel.watchlistNames.collectAsStateWithLifecycle()
    val selectedWatchlistId by viewModel.selectedWatchlistId.collectAsStateWithLifecycle()
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val isCompactMode by viewModel.isWatchlistCompactMode.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    var watchlistToRename by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        // Portfolio summary card
        PortfolioCard(stats = stats, onSimulateTick = { viewModel.simulateMarketMove() })

        Spacer(modifier = Modifier.height(16.dp))

        // --- SIMULATION SETTINGS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("simulation_mode_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, if (stats.isArcadeMode) AccentYellow.copy(alpha = 0.4f) else DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "SIMULATION ENGINE",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stats.isArcadeMode) "Arcade Simulator Active" else "Pure Realism Active",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mode Switch Toggle
                    Switch(
                        checked = stats.isArcadeMode,
                        onCheckedChange = { viewModel.setArcadeMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentYellow,
                            checkedTrackColor = AccentYellow.copy(alpha = 0.2f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.testTag("arcade_mode_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (stats.isArcadeMode) {
                    Text(
                        text = "⚠️ Manual tick updates enabled. Warning: Fast-forward simulations can lead to speculative, gamified patterns instead of patient learning habits. Practice responsibility!",
                        color = AccentYellow,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                } else {
                    Text(
                        text = "🟢 Synchronized on 15-minute delayed feeds. Builds retail patience, emotional resilience, and authentic market decision-making. Pure Realism is the best way to learn.",
                        color = AccentGreen,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "Total Return",
                value = formatPnL(stats.totalPnL, stats.currency),
                valueColor = if (stats.totalPnL >= 0) AccentGreen else AccentRose,
                percent = "${if (stats.totalPnLPct >= 0) "+" else ""}${String.format("%.2f", stats.totalPnLPct)}%",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Open Positions P/L",
                value = formatPnL(stats.openPnL, stats.currency),
                valueColor = if (stats.openPnL >= 0) AccentGreen else AccentRose,
                percent = null,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Watchlist Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MY WATCHLISTS",
                color = TextSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.toggleWatchlistCompactMode() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isCompactMode) Icons.Default.ViewStream else Icons.Default.DensityMedium,
                        contentDescription = "Toggle Compact Mode",
                        tint = BrandViolet,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "View All",
                    color = BrandViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        viewModel.selectTab("Trade")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Watchlist switcher chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(watchlistNames) { wl ->
                val isSelected = wl.id == selectedWatchlistId
                Row(
                    modifier = Modifier
                        .testTag("watchlist_chip_${wl.id}")
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) BrandViolet.copy(alpha = 0.15f) else DarkSurface)
                        .border(1.dp, if (isSelected) BrandViolet else DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectWatchlist(wl.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = wl.name,
                        color = if (isSelected) BrandViolet else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename Watchlist",
                            tint = BrandViolet,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    renameInput = wl.name
                                    watchlistToRename = wl.id
                                    showRenameDialog = true
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Watchlist Items
        val watchlistStocks = stockPrices.filter { stock ->
            watchlistItems.any { it.symbol == stock.symbol }
        }

        if (watchlistStocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = "Empty Watchlist",
                        tint = TextSubtle,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Watchlist is empty",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Star stocks in the Trade Desk to track them here.",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            watchlistStocks.forEach { stock ->
                WatchlistStockRow(
                    stock = stock,
                    currency = stats.currency,
                    isCompact = isCompactMode,
                    onClick = {
                        viewModel.selectStock(stock.symbol)
                        viewModel.selectTab("Trade")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Rename Watchlist Dialog
    if (showRenameDialog && watchlistToRename != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Watchlist", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandViolet,
                        unfocusedBorderColor = DarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        watchlistToRename?.let { id ->
                            viewModel.renameWatchlist(id, renameInput)
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandViolet)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextMuted)
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

    val rowPadding = if (isCompact) 8.dp else 16.dp
    val rowCorners = if (isCompact) 12.dp else 18.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 2.dp else 6.dp)
            .clip(RoundedCornerShape(rowCorners))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorderElevated, RoundedCornerShape(rowCorners))
            .clickable { onClick() }
            .padding(rowPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(if (isCompact) 1.2f else 1.5f)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 32.dp else 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.symbol.take(if (isCompact) 4 else 8),
                    color = Color.White,
                    fontSize = if (isCompact) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stock.symbol,
                        color = Color.White,
                        fontSize = if (isCompact) 14.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCompact) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "NSE", color = TextSubtle, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (!isCompact) {
                    Text(
                        text = stock.companyName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${stock.symbol} • ${formatCurrency(stock.currentPrice, currency)}",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Vector Line Chart - Present in both modes!
        StockLineChart(
            pricesString = stock.historyData,
            isPositive = isPositive,
            modifier = Modifier
                .width(if (isCompact) 60.dp else 70.dp)
                .height(if (isCompact) 24.dp else 28.dp)
                .padding(horizontal = 4.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(stock.currentPrice, currency),
                    color = Color.White,
                    fontSize = if (isCompact) 13.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", stock.dailyChangePct)}%",
                    color = trendColor,
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (onRemoveClick != null) {
                Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 12.dp))
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(if (isCompact) 28.dp else 36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Ticker",
                        tint = TextMuted,
                        modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun WatchlistScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
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
    var isTipDismissed by remember { mutableStateOf(false) }

    var showAdConfirmationDialog by remember { mutableStateOf(false) }
    var isAdLoading by remember { mutableStateOf(false) }
    var adLoadFailedMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val mainActivity = context as? com.ashwathai.tradelab.MainActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // 1. DENSE SHEETS SWITCHER (Multi-Watchlist Tabs) - AT THE TOP
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
                            .border(
                                1.dp,
                                if (isSelected) BrandViolet else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
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
                                        .clickable {
                                            viewModel.deleteWatchlist(wl.id)
                                        }
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
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Watchlist",
                        tint = BrandViolet,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 2. COLLAPSIBLE SEARCH OVERLAY (Inlined)
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
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSubtle, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("watchlist_search_input"),
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

                // Search Results Dropdown
                if (watchlistSearchQuery.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .zIndex(20f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
                    ) {
                        Column {
                            val filtered = stockPrices.filter {
                                it.symbol.contains(watchlistSearchQuery, ignoreCase = true) ||
                                        it.companyName.contains(watchlistSearchQuery, ignoreCase = true)
                            }

                            if (filtered.isNotEmpty()) {
                                filtered.take(4).forEach { stock ->
                                    val isAdded = watchlistItems.any { it.symbol == stock.symbol }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.toggleWatchlistV2(stock.symbol)
                                                viewModel.setWatchlistSearchQuery("") 
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(stock.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(stock.companyName, color = TextMuted, fontSize = 10.sp)
                                        }
                                        Icon(
                                            imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                            tint = if (isAdded) BrandViolet else AccentYellow,
                                            modifier = Modifier.size(16.dp),
                                            contentDescription = "Add"
                                        )
                                    }
                                }
                            }
                            
                            if (watchlistAutocompleteResults.isNotEmpty()) {
                                Text(
                                    text = "GLOBAL SUGGESTIONS",
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                watchlistAutocompleteResults.take(3).forEach { result ->
                                    val isAdded = watchlistItems.any { it.symbol == result.symbol }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.injectLiveStock(symbol = result.symbol, addToWatchlistId = selectedWatchlistId)
                                                viewModel.setWatchlistSearchQuery("")
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(result.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${result.name} • ${result.exchange}", color = TextMuted, fontSize = 10.sp)
                                        }
                                        Icon(
                                            imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                            tint = if (isAdded) BrandViolet else AccentYellow,
                                            modifier = Modifier.size(16.dp),
                                            contentDescription = "Add Live"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. DYNAMIC POPULAR TICKERS - ONLY IF LIST IS SMALL (< 5 items)
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

        // 4. TICKER LIST HEADER (Compact)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TICKERS (${watchlistItems.size})",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                // Show "Market Closed" hint if in Live mode and market is actually closed
                val anyIndianTicker = watchlistItems.any { viewModel.isIndianStockSymbol(it.symbol) }
                if (!isSimulatedMode && anyIndianTicker && !viewModel.isMarketOpen("RELIANCE.NS")) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentRose.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MARKET CLOSED",
                            color = AccentRose,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { viewModel.toggleWatchlistCompactMode() },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = if (isCompactMode) Icons.Default.ViewStream else Icons.Default.DensityMedium,
                    contentDescription = "View Mode",
                    tint = BrandViolet,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (watchlistItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Empty",
                        tint = TextSubtle,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "This watchlist is empty",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap the search icon in the title bar to add your first ticker!",
                        color = TextSubtle,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (isCompactMode) 6.dp else 10.dp)
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

        // 5. EDUCATIONAL CONTEXT TIP
        if (!isTipDismissed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (kotlin.math.abs(dragAmount) > 15f) {
                                isTipDismissed = true
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandViolet.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Tip",
                        tint = BrandViolet,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RISK EDUCATION DISCIPLINE",
                            color = BrandViolet,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Practice disciplined sizing: Never allocate more than 10% of your account (₹1,000) to a single ticker. This limits concentrations so you survive market downturns!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    IconButton(
                        onClick = { isTipDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    // Rename Watchlist Dialog
    if (showRenameDialog && watchlistToRename != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Watchlist Sheet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandViolet,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameWatchlist(watchlistToRename!!, renameInput)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("SAVE", color = BrandViolet, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("CANCEL", color = TextSubtle)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Create Watchlist Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Watchlist Sheet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = createInput,
                    onValueChange = { createInput = it },
                    placeholder = { Text("Enter sheet name...", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandViolet,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (createInput.isNotBlank()) {
                            viewModel.addNewWatchlist(createInput)
                        }
                        showCreateDialog = false
                    }
                ) {
                    Text("CREATE", color = BrandViolet, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = TextSubtle)
                }
            },
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
                    adLoadFailedMessage?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Failed to load ad: $error", color = AccentRose, fontSize = 11.sp)
                        Text("Bypassing ad so you can create your sheet!", color = AccentGreen, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isAdLoading = true
                        adLoadFailedMessage = null
                        if (mainActivity != null) {
                            mainActivity.loadAndShowRewardedAd(
                                adType = com.ashwathai.tradelab.MainActivity.AdType.WATCHLIST_CREATE,
                                onAdLoaded = { isAdLoading = false },
                                onAdFailed = { err ->
                                    isAdLoading = false
                                    adLoadFailedMessage = err
                                    // Fallback / bypass so they are never blocked
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
                            // Offline/Fallback bypass
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
    var customPriceInput by remember { mutableStateOf(String.format("%.2f", stock.currentPrice)) }
    var isExpanded by remember { mutableStateOf(false) }
    var activeEduTab by remember { mutableStateOf("Market") }

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(emptyList())
    val currentHolding = holdings.find { it.symbol == stock.symbol }
    val ownedShares = currentHolding?.shares ?: 0.0

    val shares = sharesInput.toDoubleOrNull() ?: 0.0
    val price = if (orderType != "Market") (customPriceInput.toDoubleOrNull() ?: stock.currentPrice) else stock.currentPrice
    val totalOrderValue = shares * price

    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(if (isExpanded) 0.92f else 0.65f)
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
                                    text = "Holding: ${String.format("%.2f", ownedShares)} shares",
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
                    val isPositive = stock.dailyChangePct >= 0
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.2f", stock.dailyChangePct)}%",
                        color = if (isPositive) AccentGreen else AccentRose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                                            sharesInput = String.format("%.4f", calculatedShares)
                                        }
                                    } else {
                                        sharesInput = String.format("%.4f", ownedShares * ratio)
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


@Composable
fun TradeScreen(viewModel: TradingViewModel, onDeepLinkQuiz: (Int) -> Unit) {
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val selectedStock by viewModel.selectedStock.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val autocompleteResults by viewModel.autocompleteResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val tradeSharesInput by viewModel.tradeSharesInput.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val stats by viewModel.portfolioStats.collectAsStateWithLifecycle()

    val orderType by viewModel.orderType.collectAsStateWithLifecycle()
    val triggerPriceInput by viewModel.triggerPriceInput.collectAsStateWithLifecycle()
    val selectedWatchlistId by viewModel.selectedWatchlistId.collectAsStateWithLifecycle()
    val selectedWatchlistItems by viewModel.selectedWatchlistItems.collectAsStateWithLifecycle()
    val isSimulatedMode by viewModel.isSimulatedMode.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val filteredStocks = remember(stockPrices, searchQuery) {
        if (searchQuery.isBlank()) {
            stockPrices
        } else {
            stockPrices.filter {
                it.symbol.contains(searchQuery, ignoreCase = true) ||
                        it.companyName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Sheet expansion state
    var isSheetExpanded by remember { mutableStateOf(false) }
    var showIndicators by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                placeholder = { Text("Search tickers (e.g. Reliance, SBIN, AAPL)", color = TextSubtle) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandViolet, strokeWidth = 2.dp)
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = BrandViolet,
                    unfocusedBorderColor = DarkBorder,
                    cursorColor = BrandViolet
                ),
                shape = RoundedCornerShape(20.dp)
            )

            // Live Dropdown Autocomplete results for TradeScreen
            if (searchQuery.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .zIndex(10f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.5f))
                ) {
                    if (isSearching && autocompleteResults.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(24.dp))
                        }
                    } else if (autocompleteResults.isEmpty() && filteredStocks.isEmpty()) {
                        Text(
                            text = "No stock suggestions found online",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            // 1. Matches from local SQLite DB
                            if (filteredStocks.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "AVAILABLE LOCAL SECURITIES",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(filteredStocks) { stock ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectStock(stock.symbol)
                                                viewModel.setSearchQuery("") // Reset search
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(stock.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(stock.companyName, color = TextMuted, fontSize = 11.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatCurrency(stock.currentPrice, stats.currency),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                tint = AccentYellow,
                                                contentDescription = "Trade"
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                                }
                            }

                            // 2. Matches from live online Yahoo Finance search API
                            if (autocompleteResults.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "LIVE ONLINE SUGGESTIONS (NSE/BSE & GLOBAL)",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(autocompleteResults.take(6)) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.injectLiveStock(symbol = item.symbol)
                                                viewModel.setSearchQuery("") // Reset search
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(item.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(BrandViolet.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = item.exchange,
                                                        color = BrandViolet,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(item.name, color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Trade Live",
                                                color = AccentYellow,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                tint = AccentYellow,
                                                contentDescription = "Trade"
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Tickers Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStocks) { stock ->
                    val isSelected = selectedStock?.symbol == stock.symbol

                    Box(
                        modifier = Modifier
                            .testTag("ticker_chip_${stock.symbol.lowercase()}")
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF1E1E1E) else DarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) BrandViolet else DarkBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                viewModel.selectStock(stock.symbol)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stock.symbol,
                                color = if (isSelected) BrandViolet else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatCurrency(stock.currentPrice, stats.currency),
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Standard table of filtered tickers for high-density navigation
            Text(
                text = "AVAILABLE SECURITIES",
                color = TextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredStocks) { stock ->
                    val isPositive = stock.dailyChangePct >= 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.selectStock(stock.symbol)
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stock.symbol, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stock.companyName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${stock.symbol} • ${formatCurrency(stock.currentPrice, stats.currency)}", color = TextSubtle, fontSize = 11.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatCurrency(stock.currentPrice, stats.currency), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.2f", stock.dailyChangePct)}%",
                                color = if (isPositive) AccentGreen else AccentRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- FUTURISTIC SLIDE-UP BOTTOM SHEET FOR TRADES ---
        selectedStock?.let { stock ->
            Box(modifier = Modifier.fillMaxSize()) {
                val isWatchlisted = selectedWatchlistItems.any { it.symbol == stock.symbol }
                val ownedHolding = holdings.find { it.symbol == stock.symbol }
                val isPositive = stock.dailyChangePct >= 0
                val trendColor = if (isPositive) AccentGreen else AccentRose

                val sheetHeight = if (isSheetExpanded) Modifier.fillMaxHeight() else Modifier.fillMaxHeight(0.65f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isSheetExpanded) 0.8f else 0.4f))
                        .clickable {
                            // Collapse or dismiss stock selection
                            viewModel.selectStock(null)
                        }
                )

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) {} // Prevent click-through
                ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(sheetHeight)
                        .testTag("futuristic_bottom_sheet"),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        // Sliding gesture handle / Arrow expander
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { isSheetExpanded = !isSheetExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White.copy(alpha = 0.3f))
                                  )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSheetExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Toggle Expand/Collapse",
                                        tint = BrandViolet,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSheetExpanded) "Collapse Stock Desk" else "Expand Deep Analysis",
                                        color = BrandViolet,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Sheet Header (Stock title & Watchlist toggle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stock.companyName,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.toggleWatchlistV2(stock.symbol) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isWatchlisted) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Watchlist toggle",
                                            tint = if (isWatchlisted) AccentYellow else TextMuted
                                        )
                                    }
                                }
                                Text(
                                    text = "${stock.symbol} • ${formatCurrency(stock.currentPrice, stats.currency)}",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.2f", stock.dailyChangePct)}%",
                                color = trendColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable content inside sheet
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Technical Chart and Custom Indicators
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isSheetExpanded) 180.dp else 120.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
                                    ) {
                                        StockLineChart(
                                            pricesString = stock.historyData,
                                            isPositive = isPositive,
                                            showIndicators = showIndicators,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.TopStart),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "High: ${formatCurrency(stock.highPrice, stats.currency)}",
                                                color = TextSubtle,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Low: ${formatCurrency(stock.lowPrice, stats.currency)}",
                                                color = TextSubtle,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // RSI Neon Pill on top right
                                        val prices = stock.historyData.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                                        if (prices.isNotEmpty()) {
                                            val rsi = calculateRSI(prices)
                                            val rsiStatus = when {
                                                rsi >= 70.0 -> "Overbought"
                                                rsi <= 30.0 -> "Oversold"
                                                else -> "Neutral"
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (rsi >= 70.0) AccentRose.copy(alpha = 0.15f) else AccentGreen.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "RSI: ${String.format("%.1f", rsi)} ($rsiStatus)",
                                                    color = if (rsi >= 70.0) AccentRose else AccentGreen,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Switch to show indicators
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ShowChart, contentDescription = "SMA Indicator", tint = AccentYellow, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Show Simple Moving Average (SMA)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Switch(
                                        checked = showIndicators,
                                        onCheckedChange = { isChecked ->
                                            val isUnlocked = stats.isPremium || stats.indicatorsUnlockedUntil > System.currentTimeMillis()
                                            if (isChecked) {
                                                if (isUnlocked) {
                                                    showIndicators = true
                                                } else {
                                                    viewModel.showFeedback("Sponsor Reward Needed: Go to Profile and watch a sponsor video to unlock, or Upgrade to Pro for instant access!")
                                                }
                                            } else {
                                                showIndicators = false
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = AccentYellow,
                                            checkedTrackColor = AccentYellow.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                            }

                            // Holdings summary
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "CURRENT POSITION", color = TextSubtle, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (ownedHolding != null) "${String.format("%.4f", ownedHolding.shares)} Shares" else "0.0 Shares",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (ownedHolding != null) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = "TOTAL COST BASIS", color = TextSubtle, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = formatCurrency(ownedHolding.shares * ownedHolding.averagePrice, stats.currency),
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Extended stock detail parameters (visible when sheet is expanded)
                            if (isSheetExpanded) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, DarkBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("SECURITY SUMMARY", color = TextSubtle, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            listOf(
                                                "Previous Close" to formatCurrency(stock.currentPrice * (1 - stock.dailyChangePct / 100), stats.currency),
                                                "Day High" to formatCurrency(stock.highPrice, stats.currency),
                                                "Day Low" to formatCurrency(stock.lowPrice, stats.currency),
                                                "Primary Exchange" to "NASDAQ / NYSE",
                                                "Asset Class" to "Blue-Chip Growth Equity"
                                            ).forEach { (lbl, valStr) ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(lbl, color = TextMuted, fontSize = 11.sp)
                                                    Text(valStr, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Market Status Warning Card
                            item {
                                val isOpen = viewModel.isMarketOpen(stock.symbol)
                                val isSim = isSimulatedMode
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("market_status_card"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSim) BrandViolet.copy(alpha = 0.15f) else if (isOpen) Color.White.copy(alpha = 0.02f) else AccentRoseDark.copy(alpha = 0.15f)
                                    ),
                                    border = BorderStroke(1.dp, if (isSim) BrandViolet.copy(alpha = 0.5f) else if (isOpen) AccentGreen.copy(alpha = 0.3f) else AccentRose.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSim) BrandViolet else if (isOpen) AccentGreen else AccentRose)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isSim) "Simulation Mode Active" else if (isOpen) "Market is Active" else "Market is Closed",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = if (isSim) "Simulated Ticks (Trades Open)" else if (isOpen) "Delayed Live Data Feed" else "Off-Market (Trades Blocked)",
                                            color = if (isSim) BrandViolet else if (isOpen) AccentGreen else AccentRose,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Order input card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "ORDER TRANSACTION DESK",
                                            color = TextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Available Liquid Capital:", color = TextMuted, fontSize = 11.sp)
                                            Text(formatCurrency(stats.cash, stats.currency), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Order Type Picker (Market, Limit, GTT)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("Market", "Limit", "GTT").forEach { type ->
                                                val isSel = orderType == type
                                                Button(
                                                    onClick = { viewModel.setOrderType(type) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isSel) BrandViolet else Color.White.copy(alpha = 0.05f)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text(
                                                        text = type,
                                                        color = if (isSel) Color.White else Color.White.copy(alpha = 0.6f),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (orderType != "Market") {
                                            OutlinedTextField(
                                                value = triggerPriceInput,
                                                onValueChange = { viewModel.setTriggerPrice(it) },
                                                modifier = Modifier.fillMaxWidth().testTag("trigger_price_input"),
                                                label = { Text("Trigger / Limit Price", color = TextMuted) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = DarkSurface,
                                                    unfocusedContainerColor = DarkSurface,
                                                    focusedBorderColor = BrandViolet,
                                                    unfocusedBorderColor = DarkBorder
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }

                                        OutlinedTextField(
                                            value = tradeSharesInput,
                                            onValueChange = { viewModel.setTradeShares(it) },
                                            modifier = Modifier.fillMaxWidth().testTag("shares_input"),
                                            label = { Text("Shares to Trade", color = TextMuted) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = DarkSurface,
                                                unfocusedContainerColor = DarkSurface,
                                                focusedBorderColor = BrandViolet,
                                                unfocusedBorderColor = DarkBorder
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Dynamic Sizing Warning Alert inside sheet
                                        val sharesDouble = tradeSharesInput.toDoubleOrNull() ?: 0.0
                                        val basePrice = if (orderType != "Market") {
                                            val triggerParsed = triggerPriceInput.toDoubleOrNull()
                                            if (triggerParsed != null) {
                                                if (stats.currency == "INR") triggerParsed / 83.0 else triggerParsed
                                            } else {
                                                stock.currentPrice
                                            }
                                        } else {
                                            stock.currentPrice
                                        }
                                        val calculatedCost = sharesDouble * basePrice
                                        val isOverSized = calculatedCost > stats.cash * 0.20 && sharesDouble > 0

                                        if (sharesDouble > 0) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White.copy(alpha = 0.03f))
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Estimated Cost:", color = TextMuted, fontSize = 12.sp)
                                                Text(
                                                    text = formatCurrency(calculatedCost, stats.currency),
                                                    color = if (calculatedCost <= stats.cash) Color.White else AccentRose,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }

                                        if (isOverSized && calculatedCost <= stats.cash) {
                                            val sizePct = (calculatedCost / stats.cash) * 100
                                            Card(
                                                modifier = Modifier.fillMaxWidth().testTag("sizing_warning_card"),
                                                colors = CardDefaults.cardColors(containerColor = AccentRoseDark.copy(alpha = 0.2f)),
                                                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text("⚠️ UNSAFE POSITION SIZING RISK!", color = AccentRose, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = "You are allocating ${String.format("%.1f", sizePct)}% of your capital. Standard discipline rules restrict single trades to 12% to prevent massive accounts drawdowns on sudden drops.",
                                                        color = TextMuted,
                                                        fontSize = 9.sp,
                                                        lineHeight = 12.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }

                                        // AI Check and diagnostic consultation (Visible when expanded)
                                        if (isSheetExpanded) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.selectTab("Profile")
                                                        viewModel.sendMessageToAi("What is your financial analysis on ${stock.symbol} given my current portfolio profile and risk limit?")
                                                    },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(containerColor = BrandVioletDark.copy(alpha = 0.2f)),
                                                border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Psychology, contentDescription = "AI", tint = BrandViolet, modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = if (stats.isPremium) "Get Pro AI Diagnostic on ${stock.symbol}" else "Get AI Diagnostic on ${stock.symbol}",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = if (stats.isPremium) "Pro Unlimited Access • Real-time" else "Costs 1 AI Credit (${stats.aiAuditCredits} remaining)",
                                                            color = if (stats.isPremium) AccentYellow else TextMuted,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Open Chat", tint = BrandViolet, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        val marketIsOpen = viewModel.isMarketOpen(stock.symbol)
                                        // Buy & Sell execution CTAs
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.executeBuy()
                                                    keyboardController?.hide()
                                                    focusManager.clearFocus()
                                                },
                                                modifier = Modifier.weight(1f).testTag("buy_button"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (marketIsOpen) AccentGreen else AccentGreen.copy(alpha = 0.25f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = if (marketIsOpen) "BUY ${stock.symbol}" else "CLOSED",
                                                    color = if (marketIsOpen) DarkBg else Color.White.copy(alpha = 0.5f),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.executeSell()
                                                    keyboardController?.hide()
                                                    focusManager.clearFocus()
                                                },
                                                modifier = Modifier.weight(1f).testTag("sell_button"),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = if (marketIsOpen) AccentRose else AccentRose.copy(alpha = 0.3f)
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (marketIsOpen) AccentRose else AccentRose.copy(alpha = 0.3f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = if (marketIsOpen) "SELL ${stock.symbol}" else "CLOSED",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (marketIsOpen) AccentRose else Color.White.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}


