package com.ashwathai.tradelab.ui.derivatives

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
fun FoDeskScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val mainActivity = context as? MainActivity

    // 1. Check Academic Gating (Requires levels 1, 2, and 3)
    val completedSet = remember(stats.completedLevels) { 
        stats.completedLevels.split(",").filter { it.isNotBlank() }.toSet() 
    }
    val isAcademicUnlocked = completedSet.contains("1") && completedSet.contains("2") && completedSet.contains("3")

    if (!isAcademicUnlocked) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("academic_gate_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Academic Lock",
                        tint = AccentRose,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Academic Gate Active",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Futures & Options (F&O) involve severe leverage and risk. To protect your virtual capital, you must first study and complete the basic curriculum.",
                        color = TextSubtle,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Checklist of completed levels
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1" to "Module 1: Equity Basics", "2" to "Module 2: Market Order Mechanics", "3" to "Module 3: Portfolio Asset Classes").forEach { (id, title) ->
                            val done = completedSet.contains(id)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (done) AccentGreen else TextSubtle,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = title,
                                    color = if (done) Color.White else TextSubtle,
                                    fontSize = 12.sp,
                                    fontWeight = if (done) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.selectTab("Academy")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("GO TO ACADEMY HUB 🎓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // 2. Check monetization paywall: requires Trade Lab Pro OR F&O Tokens
    val hasAccess = stats.isPremium || stats.fnoTokens > 0

    if (!hasAccess) {
        var isWatchingAd by remember { mutableStateOf(false) }
        var isAdLoading by remember { mutableStateOf(false) }
        var adTimer by remember { mutableStateOf(0) }

        if (isAdLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Connecting to sponsored ad network...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        } else if (isWatchingAd) {
            LaunchedEffect(Unit) {
                adTimer = 3
                while (adTimer > 0) {
                    kotlinx.coroutines.delay(1000)
                    adTimer--
                }
                viewModel.earnFnoTokens(3)
                isWatchingAd = false
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(BrandViolet.copy(alpha = 0.1f))
                        .border(2.dp, BrandViolet, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$adTimer",
                        color = BrandViolet,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Streaming Sponsor Video Promo...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Hold tight! Unlocking your +3 Free F&O Tokens in a moment.", color = TextSubtle, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("fno_paywall_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "F&O SPECIAL DESK ACCESS",
                            color = BrandViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Unshackle Advanced Options Chain 👑",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Premium derivative tools require high exchange margins and specialized infrastructure. Unlock professional desking instantly below.",
                            color = TextSubtle,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Option 1: Premium Buyout
                        Button(
                            onClick = {
                                viewModel.simulatePremiumPurchase()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("UPGRADE TO TRADE LAB PRO 👑", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("OR", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Option 2: Ad unlock
                        Button(
                            onClick = {
                                isAdLoading = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    isAdLoading = false
                                    isWatchingAd = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, BrandViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("WATCH SPONSORED VIDEO (CLAIM +3 TOKENS) 📺", color = BrandViolet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    // 3. Option Chain Trading Desk
    val tickers = listOf("RELIANCE", "TCS", "INFY", "HDFCBANK", "AAPL", "TSLA", "MSFT")
    var selectedTicker by remember { mutableStateOf("RELIANCE") }

    val liveStock = stockPrices.find { it.symbol == selectedTicker }
    val currentPrice = liveStock?.currentPrice ?: 100.0

    // Generate option strikes spaced around current price
    val strikes = remember(currentPrice) {
        listOf(0.95, 0.98, 0.99, 1.0, 1.01, 1.02, 1.05).map {
            val mult = if (selectedTicker == "AAPL" || selectedTicker == "TSLA" || selectedTicker == "MSFT") 1.0 else 5.0
            Math.round(currentPrice * it / mult).toDouble() * mult
        }.distinct().sorted()
    }

    // Option state details
    var activeOptionStrike by remember { mutableStateOf(0.0) }
    var activeOptionIsCall by remember { mutableStateOf(true) }
    var activeOptionPremium by remember { mutableStateOf(0.0) }
    var activeOptionSymbol by remember { mutableStateOf<String?>(null) }
    var orderLots by remember { mutableStateOf(1) }
    var isBuyOrder by remember { mutableStateOf(true) }
    var isDelivery by remember { mutableStateOf(true) } // Carry Forward vs Intraday

    // Synchronize selected strike details if ticker changes
    LaunchedEffect(selectedTicker, currentPrice) {
        val middleStrike = strikes.getOrNull(strikes.size / 2) ?: currentPrice
        activeOptionStrike = middleStrike
        activeOptionIsCall = true
        activeOptionPremium = calculateOptionPremiumStatic(currentPrice, middleStrike, true, 7)
        activeOptionSymbol = "${selectedTicker}_CE_${middleStrike.toInt()}"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        // Ticker Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tickers.forEach { ticker ->
                val isSelected = selectedTicker == ticker
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BrandViolet.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) BrandViolet else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedTicker = ticker }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = ticker, color = if (isSelected) BrandViolet else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Ticker pricing Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "$selectedTicker UNDERLYING", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = formatCurrency(currentPrice, stats.currency), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                
                // Show token badge if using token
                if (!stats.isPremium) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandViolet.copy(alpha = 0.15f))
                            .border(1.dp, BrandViolet, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎫 TOKENS: ${stats.fnoTokens}",
                            color = BrandViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B2E1E))
                            .border(1.dp, AccentGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "👑 PRO UNLOCKED",
                            color = AccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Option Chain Header Labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("CALL PREMIUM (CE)", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("STRIKE PRICE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("PUT PREMIUM (PE)", color = AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Strike List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkBg),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                strikes.forEach { strike ->
                    val cePremium = calculateOptionPremiumStatic(currentPrice, strike, true, 7)
                    val pePremium = calculateOptionPremiumStatic(currentPrice, strike, false, 7)

                    val isCeSelected = activeOptionStrike == strike && activeOptionIsCall
                    val isPeSelected = activeOptionStrike == strike && !activeOptionIsCall

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCeSelected || isPeSelected) Color.White.copy(alpha = 0.04f) else Color.Transparent)
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // CE Premium Button
                        Button(
                            onClick = {
                                activeOptionStrike = strike
                                activeOptionIsCall = true
                                activeOptionPremium = cePremium
                                activeOptionSymbol = "${selectedTicker}_CE_${strike.toInt()}"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCeSelected) AccentGreen.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = BorderStroke(1.dp, if (isCeSelected) AccentGreen else AccentGreen.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.width(96.dp).height(32.dp).testTag("strike_ce_${strike.toInt()}")
                        ) {
                            Text(text = formatCurrency(cePremium, stats.currency), color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Centered Strike Price
                        Text(
                            text = strike.toInt().toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // PE Premium Button
                        Button(
                            onClick = {
                                activeOptionStrike = strike
                                activeOptionIsCall = false
                                activeOptionPremium = pePremium
                                activeOptionSymbol = "${selectedTicker}_PE_${strike.toInt()}"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPeSelected) AccentRose.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = BorderStroke(1.dp, if (isPeSelected) AccentRose else AccentRose.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.width(96.dp).height(32.dp).testTag("strike_pe_${strike.toInt()}")
                        ) {
                            Text(text = formatCurrency(pePremium, stats.currency), color = AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Execution Ticket Card
        activeOptionSymbol?.let { symbol ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("fno_order_ticket"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "F&O DERIVATIVE ORDER TICKET",
                        color = BrandViolet,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (activeOptionIsCall) AccentGreen.copy(alpha = 0.15f) else AccentRose.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (activeOptionIsCall) "CALL (CE)" else "PUT (PE)",
                                        color = if (activeOptionIsCall) AccentGreen else AccentRose,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$selectedTicker @ ${activeOptionStrike.toInt()}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Premium Per Share: ${formatCurrency(activeOptionPremium, stats.currency)}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Buy/Sell Direction Toggle Slider
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(2.dp)
                        ) {
                            listOf(true to "BUY", false to "SELL").forEach { (buyVal, title) ->
                                val isSel = isBuyOrder == buyVal
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSel) {
                                                if (buyVal) AccentGreen.copy(alpha = 0.2f) else AccentRose.copy(alpha = 0.2f)
                                            } else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isSel) (if (buyVal) AccentGreen else AccentRose) else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { isBuyOrder = buyVal }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSel) (if (buyVal) AccentGreen else AccentRose) else TextSubtle,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // NEW: PRODUCT TYPE TOGGLE (NRML vs MIS)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("PRODUCT TYPE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkBg).padding(2.dp)) {
                            listOf(true to "NRML (Carry Forward)", false to "MIS (Intraday)").forEach { (isDel, label) ->
                                val selected = isDelivery == isDel
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (selected) Color.White.copy(alpha = 0.08f) else Color.Transparent).clickable { isDelivery = isDel }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(label, color = if (selected) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        // Educational Warning for MIS
                        if (!isDelivery) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "⚠️ MIS/Intraday: Position will be auto-squared off at 3:20 PM IST today.", color = AccentYellow, fontSize = 10.sp, lineHeight = 13.sp)
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "💎 NRML/Carry Forward: Hold position until contract expiry. Higher margin may be required for overnight holdings.", color = BrandViolet, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Lot Selector Row
                    Text("SELECT CONTRACT VOLUME (LOTS)", color = TextSubtle, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 5, 10).forEach { lots ->
                            val isSel = orderLots == lots
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BrandViolet.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, if (isSel) BrandViolet else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { orderLots = lots }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$lots Lot\n(${lots * 100} qty)",
                                    color = if (isSel) BrandViolet else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium outflows or Collateral Details
                    val shareQty = orderLots * 100.0
                    val totalPremiumCost = shareQty * activeOptionPremium
                    val collateralCost = if (!isBuyOrder) activeOptionStrike * shareQty * 0.1 else 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBuyOrder) "PREMIUM OUTFLOW" else "COLLATERAL MARGIN REQUIRED",
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCurrency(if (isBuyOrder) totalPremiumCost else collateralCost, stats.currency),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Premium cost info
                        if (!isBuyOrder) {
                            Text(
                                text = "10% Margin",
                                color = AccentRose,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "@ ${formatCurrency(activeOptionPremium, stats.currency)} / share",
                                color = AccentGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Spend details / free trade token warning
                    if (!stats.isPremium && stats.fnoTokens > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = "Token", tint = BrandViolet, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Spent: 1 F&O Free Trade Token (Remaining: ${stats.fnoTokens - 1})",
                                color = BrandViolet,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Execute Button
                    Button(
                        onClick = {
                            val cashRequired = if (isBuyOrder) totalPremiumCost else collateralCost
                            if (stats.cash < cashRequired) {
                                viewModel.showFeedback("Failed: Insufficient Cash Balance for this derivative order!")
                                return@Button
                            }

                            viewModel.setDeliveryMode(isDelivery)

                            if (!stats.isPremium && stats.fnoTokens > 0) {
                                // Spend token first
                                viewModel.useFnoToken { success ->
                                    if (success) {
                                        viewModel.executeOptionOrder(
                                            optionSymbol = symbol,
                                            isBuy = isBuyOrder,
                                            shares = shareQty,
                                            premium = activeOptionPremium,
                                            strike = activeOptionStrike,
                                            isCall = activeOptionIsCall,
                                            onSuccess = {
                                                // Reset order input
                                                orderLots = 1
                                            }
                                        )
                                    }
                                }
                            } else {
                                // Premium user - Unlimited access
                                viewModel.executeOptionOrder(
                                    optionSymbol = symbol,
                                    isBuy = isBuyOrder,
                                    shares = shareQty,
                                    premium = activeOptionPremium,
                                    strike = activeOptionStrike,
                                    isCall = activeOptionIsCall,
                                    onSuccess = {
                                        orderLots = 1
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBuyOrder) AccentGreen else AccentRose
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_option_order")
                    ) {
                        Text(
                            text = "EXECUTE F&O ${if (isBuyOrder) "BUY" else "SELL"} ORDER 🚀",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun GreeksDiagnosticsBox(
    optionHoldings: List<Holding>,
    stockPrices: List<StockPrice>,
    currency: String
) {
    var totalDelta = 0.0
    var totalTheta = 0.0

    for (h in optionHoldings) {
        val isCall = h.symbol.contains("_CE_")
        val separator = if (isCall) "_CE_" else "_PE_"
        val parts = h.symbol.split(separator)
        val underlyingSymbol = parts[0]
        val strikePrice = parts.getOrNull(1)?.toDoubleOrNull() ?: 100.0

        val liveStock = stockPrices.find { it.symbol == underlyingSymbol }
        val underlyingPrice = liveStock?.currentPrice ?: strikePrice

        val deltaOneShare = if (isCall) {
            if (underlyingPrice == strikePrice) 0.5
            else if (underlyingPrice > strikePrice) {
                0.5 + 0.5 * minOf(1.0, (underlyingPrice - strikePrice) / (strikePrice * 0.05))
            } else {
                0.5 - 0.5 * minOf(1.0, (strikePrice - underlyingPrice) / (strikePrice * 0.05))
            }
        } else {
            val callDelta = if (underlyingPrice == strikePrice) 0.5
            else if (underlyingPrice > strikePrice) {
                0.5 + 0.5 * minOf(1.0, (underlyingPrice - strikePrice) / (strikePrice * 0.05))
            } else {
                0.5 - 0.5 * minOf(1.0, (strikePrice - underlyingPrice) / (strikePrice * 0.05))
            }
            callDelta - 1.0
        }

        val premiumToday = calculateOptionPremiumStatic(underlyingPrice, strikePrice, isCall, 7)
        val premiumTomorrow = calculateOptionPremiumStatic(underlyingPrice, strikePrice, isCall, 6)
        val thetaOneShare = premiumTomorrow - premiumToday

        totalDelta += (deltaOneShare * h.shares)
        totalTheta += (thetaOneShare * h.shares)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("greeks_diagnostics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = "Greeks", tint = BrandViolet, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("F&O Portfolio Risk Diagnostics", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("PORTFOLIO DELTA (BIAS)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${if (totalDelta >= 0) "+" else ""}${String.format("%.2f", totalDelta)}",
                        color = if (totalDelta >= 0.5) AccentGreen else if (totalDelta <= -0.5) AccentRose else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val biasText = when {
                        totalDelta >= 5.0 -> "Bullish Bias 📈"
                        totalDelta <= -5.0 -> "Bearish Bias 📉"
                        else -> "Delta Neutral ⚖️"
                    }
                    Text(biasText, color = TextSubtle, fontSize = 10.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.End) {
                    Text("DAILY THETA DECAY (BLEED)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatPnL(totalTheta, currency),
                        color = AccentRose,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Bleeding premium per day", color = TextSubtle, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💡 Delta indicates sensitivity to direction: +Delta gains if the market rallies, -Delta gains if the market falls. Theta represents the silent time-decay of option premiums. Square off contracts before they bleed dry!",
                color = TextSubtle,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}


fun calculateOptionPremiumStatic(
    underlyingPrice: Double,
    strike: Double,
    isCall: Boolean,
    dte: Int = 7
): Double {
    val dteFactor = dte.coerceAtLeast(1) / 30.0
    val volatility = 0.25
    val intrinsicValue = if (isCall) {
        (underlyingPrice - strike).coerceAtLeast(0.0)
    } else {
        (strike - underlyingPrice).coerceAtLeast(0.0)
    }
    val stdDev = (underlyingPrice * volatility * kotlin.math.sqrt(dteFactor)).coerceAtLeast(0.01)
    val distance = underlyingPrice - strike
    val exponent = - (distance * distance) / (2.0 * stdDev * stdDev)
    val extrinsicValue = (underlyingPrice * 0.05 * kotlin.math.sqrt(dteFactor)) * kotlin.math.exp(exponent)
    val rawPremium = intrinsicValue + extrinsicValue
    return (rawPremium).coerceAtLeast(0.01)
}


