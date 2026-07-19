package com.ashwathai.tradelab.ui.profile

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
fun ProfileScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val quizModules by viewModel.quizModules.collectAsStateWithLifecycle()

    var showAiCoachDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var isAdLoading by remember { mutableStateOf(false) }
    var activeAdRewardType by remember { mutableStateOf<String?>(null) }
    var adTimerSec by remember { mutableStateOf(0) }
    var adLoadFailedMessage by remember { mutableStateOf<String?>(null) }
    var adFailedType by remember { mutableStateOf<String?>(null) }
    var requestedRefundAmount by remember { mutableStateOf<Double?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val mainActivity = context as? MainActivity

    // Map of completed levels set
    val completedSet = remember(stats.completedLevels) {
        stats.completedLevels.split(",").filter { it.isNotBlank() }.toSet()
    }

    var isDarkMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        // 1. Personal Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("profile_user_info_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(listOf(BrandViolet, BrandVioletDark)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userProfile?.userName?.isNotBlank() == true) userProfile!!.userName.take(1).uppercase() else "A",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (userProfile?.userName?.isNotBlank() == true) userProfile!!.userName else "Ashwath Trader",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (userProfile?.userEmail?.isNotBlank() == true) userProfile!!.userEmail else "ashwath@ashwathai.com",
                            color = TextSubtle,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "+91 98765 43210",
                            color = TextSubtle,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // Subscription/Membership status block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MEMBERSHIP PLAN",
                            color = BrandViolet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stats.isPremium) "TradeLab Pro ⚡" else "TradeLab Free Tier",
                            color = if (stats.isPremium) AccentYellow else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (stats.isPremium) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentYellow.copy(alpha = 0.15f))
                                .border(1.dp, AccentYellow.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.showFeedback("Your subscription is active. Billed ₹99/mo via Google Play.") }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "MANAGE SUBSCRIPTION",
                                color = AccentYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.openProBenefits() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "UNLOCK PRO • ₹99/mo",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                
                if (!stats.isPremium) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pro tier includes 15-Day Free Trial, ₹0 Brokerage fees, unlimited watchlist sheets, and double quiz rewards.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 2. Gamification Status Card
        val levelName = when (completedSet.size) {
            0 -> "Level 1: Novice Practitioner"
            1, 2 -> "Level 2: Disciplined Risk-Taker"
            3, 4 -> "Level 3: Strategic Wealth Planner"
            5 -> "Level 4: Advanced Portfolio Architect"
            else -> "Level 5: Master Risk Manager 🏆"
        }
        val rankNumber = when (completedSet.size) {
            0 -> "Rank #15"
            1, 2 -> "Rank #8"
            3, 4 -> "Rank #4"
            else -> "Rank #2"
        }
        val userScore = completedSet.size * 1000 + 1500

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("profile_gamification_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "GAMIFICATION & LEVEL",
                    color = BrandViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = levelName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Score: $userScore XP",
                        color = TextSubtle,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Leaderboard: $rankNumber",
                        color = BrandViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { completedSet.size / quizModules.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BrandViolet,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
            }
        }

        // 2.5 sponsor video / monetization terminal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("monetization_station_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.4f))
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val mainActivity = context as? MainActivity
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRADELAB REWARDS STATION",
                            color = BrandViolet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sponsor Station by Ashwath AI",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Ad rewards",
                        tint = BrandViolet,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Unlock high-value premium features or get emergency cash recharges instantly by supporting our sponsors with a quick video clip.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Reward 1: Emergency cash
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (stats.isPremium) {
                                        viewModel.earnEmergencyCash(1000.0)
                                        viewModel.showFeedback("Pro Advantage: ₹1,000 Capital credited instantly!")
                                    } else {
                                        isAdLoading = true
                                        adLoadFailedMessage = null
                                        adFailedType = "CASH"
                                        if (mainActivity != null) {
                                            mainActivity.loadAndShowRewardedAd(
                                                adType = MainActivity.AdType.PROFILE_EMERGENCY_CASH,
                                                onAdLoaded = { isAdLoading = false },
                                                onAdFailed = { err ->
                                                    isAdLoading = false
                                                    adLoadFailedMessage = err
                                                },
                                                onUserEarnedReward = {
                                                    viewModel.earnEmergencyCash(1000.0)
                                                }
                                            )
                                        } else {
                                            isAdLoading = false
                                            activeAdRewardType = "CASH"
                                        }
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AttachMoney, contentDescription = "Cash reward", tint = AccentGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    val amtStr = if (stats.currency == "INR") "₹1,000 Capital" else "$1,000 Capital"
                                    Text("Emergency Wallet Cash", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Instantly adds $amtStr to cash balance", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = if (stats.isPremium) "PRO INSTANT ⚡" else "FREE 📺",
                                color = if (stats.isPremium) AccentYellow else BrandViolet,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Reward 2: Brokerage credits
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (stats.isPremium) {
                                        viewModel.showFeedback("Pro Active: Your brokerage is already fully waived!")
                                    } else {
                                        isAdLoading = true
                                        adLoadFailedMessage = null
                                        adFailedType = "SHIELD"
                                        if (mainActivity != null) {
                                            mainActivity.loadAndShowRewardedAd(
                                                adType = MainActivity.AdType.PROFILE_SHIELD_MAX,
                                                onAdLoaded = { isAdLoading = false },
                                                onAdFailed = { err ->
                                                    isAdLoading = false
                                                    adLoadFailedMessage = err
                                                },
                                                onUserEarnedReward = {
                                                    viewModel.earnBrokerageCredits(100)
                                                }
                                            )
                                        } else {
                                            isAdLoading = false
                                            activeAdRewardType = "SHIELD"
                                        }
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AccentYellow.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Shield credits", tint = AccentYellow, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Brokerage Shield Recharge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (stats.isPremium) "Unlimited waivers active" else "Claim +100 Credits (${stats.brokerageCredits} active)",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = if (stats.isPremium) "PRO ACTIVE ⚡" else "FREE 📺",
                                color = if (stats.isPremium) AccentYellow else BrandViolet,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Reward 3: AI Diagnostic Audit credit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (stats.isPremium) {
                                        viewModel.earnAiAuditCredit()
                                        viewModel.showFeedback("Pro Advantage: AI Advisor Credit credited instantly!")
                                    } else {
                                        isAdLoading = true
                                        adLoadFailedMessage = null
                                        adFailedType = "AI"
                                        if (mainActivity != null) {
                                            mainActivity.loadAndShowRewardedAd(
                                                adType = MainActivity.AdType.PROFILE_AI_ADVISOR,
                                                onAdLoaded = { isAdLoading = false },
                                                onAdFailed = { err ->
                                                    isAdLoading = false
                                                    adLoadFailedMessage = err
                                                },
                                                onUserEarnedReward = {
                                                    viewModel.earnAiAuditCredit()
                                                }
                                            )
                                        } else {
                                            isAdLoading = false
                                            activeAdRewardType = "AI"
                                        }
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BrandViolet.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "AI credits", tint = BrandViolet, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Unlock AI Advisor Credits", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (stats.isPremium) "Claim +1 AI Consultation Audit (Unlimited available)" else "Claim +1 AI Consultation Audit (${stats.aiAuditCredits} active)",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = if (stats.isPremium) "PRO INSTANT ⚡" else "FREE 📺",
                                color = if (stats.isPremium) AccentYellow else BrandViolet,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Reward 4: Premium indicators unlock
                        val indUnl = stats.isPremium || stats.indicatorsUnlockedUntil > System.currentTimeMillis()
                        val expiryLabel = if (stats.isPremium) {
                            "✓ PRO ACTIVE: Indicators fully unlocked."
                        } else if (indUnl) {
                            val diff = stats.indicatorsUnlockedUntil - System.currentTimeMillis()
                            val hrs = diff / (60 * 60 * 1000L)
                            "UNLOCKED (${hrs}h remaining)"
                        } else {
                            "Locked (Unlock via sponsor support)"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (stats.isPremium) {
                                        viewModel.showFeedback("Pro Active: All chart indicators are permanently unlocked!")
                                    } else {
                                        isAdLoading = true
                                        adLoadFailedMessage = null
                                        adFailedType = "INDICATORS"
                                        if (mainActivity != null) {
                                            mainActivity.loadAndShowRewardedAd(
                                                adType = MainActivity.AdType.PROFILE_INDICATORS,
                                                onAdLoaded = { isAdLoading = false },
                                                onAdFailed = { err ->
                                                    isAdLoading = false
                                                    adLoadFailedMessage = err
                                                },
                                                onUserEarnedReward = {
                                                    viewModel.unlockPremiumIndicators(24)
                                                }
                                            )
                                        } else {
                                            isAdLoading = false
                                            activeAdRewardType = "INDICATORS"
                                        }
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AccentRose.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = "Premium indicators", tint = AccentRose, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Premium Indicators Unlock", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(expiryLabel, color = if (indUnl) AccentGreen else TextMuted, fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = if (stats.isPremium) "PRO ACTIVE ⚡" else "FREE 📺",
                                color = if (stats.isPremium) AccentYellow else BrandViolet,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

        // 3. Theme Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("settings_theme_card"),
            shape = RoundedCornerShape(24.dp),
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
                Column {
                    Text(
                        text = "THEME MODE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDarkMode) "Sophisticated Dark" else "Neon Light (Experimental)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { 
                        isDarkMode = it
                        if (!it) {
                            viewModel.showFeedback("Ashwath AI recommends Sophisticated Dark mode to shield your eyes during intense simulation!")
                            isDarkMode = true // revert back automatically to keep it dark
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BrandViolet,
                        checkedTrackColor = BrandViolet.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stats Summary Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "LIFETIME METRICS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Starting Cash", color = TextSubtle, fontSize = 11.sp)
                        Text(formatCurrencyNoDecimals(stats.startingCash, stats.currency), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Trades", color = TextSubtle, fontSize = 11.sp)
                        Text("${transactions.size} orders", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Active Holdings", color = TextSubtle, fontSize = 11.sp)
                        Text("${holdings.size} positions", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Lifetime Return", color = TextSubtle, fontSize = 11.sp)
                        val returnColor = if (stats.totalPnL >= 0) AccentGreen else AccentRose
                        Text(
                            text = "${if (stats.totalPnL >= 0) "+" else ""}${String.format("%.2f", stats.totalPnLPct)}%",
                            color = returnColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Strategy Coach Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAiCoachDialog = true },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BrandVioletDark.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Strategic Assistant",
                    tint = BrandViolet,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Portfolio Coach",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Get real-time insights based on your starting capital, current holdings, and selected risk appetite.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Consult Coach",
                    tint = BrandViolet,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- INTERACTIVE STRATEGY & REALISTIC BUDGET PROFILER ---
        var profilerStep by remember { mutableStateOf(0) }
        var pGoal by remember { mutableStateOf("") }
        var pCapital by remember { mutableStateOf(25000.0) }
        var pRisk by remember { mutableStateOf("Moderate") }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("interactive_profiler_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, if (profilerStep > 0) BrandViolet.copy(alpha = 0.5f) else DarkBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REALISTIC INVESTOR PROFILER",
                        color = if (profilerStep > 0) BrandViolet else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (profilerStep in 1..3) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandViolet.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "STEP $profilerStep OF 3",
                                color = BrandViolet,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(
                    targetState = profilerStep,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "profiler_animation"
                ) { step ->
                    when (step) {
                        0 -> {
                            Column {
                                Text(
                                    text = "Before you jump into the real market waters, let's test if you can swim in a realistic swimming pool.",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Extravagant paper funds look fun but gamify your psychology, leading to reckless real-world habits. Take our 60-second interview to align your virtual wallet with your real-world readiness.",
                                    color = TextSubtle,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { profilerStep = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Begin Psychological Calibration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        1 -> {
                            Column {
                                Text(
                                    text = "What is your primary intent on Trade Lab?",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (pGoal == "Learning") Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                        .border(1.dp, if (pGoal == "Learning") BrandViolet.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .clickable { pGoal = "Learning" }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = pGoal == "Learning",
                                        onClick = { pGoal = "Learning" },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandViolet, unselectedColor = TextSubtle)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Genuine Skill Learning", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Calibrate to realistic savings & budgets. Learn true risk mitigation.", color = TextSubtle, fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (pGoal == "Gamification") Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                        .border(1.dp, if (pGoal == "Gamification") BrandViolet.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .clickable { pGoal = "Gamification" }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = pGoal == "Gamification",
                                        onClick = { pGoal = "Gamification" },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandViolet, unselectedColor = TextSubtle)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Gamified Playground", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Allocate fantasy sums ($500k+) for arcade-like thrill and speculative leverage.", color = TextSubtle, fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { profilerStep = 0 }) {
                                        Text("Cancel", color = TextSubtle, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { if (pGoal.isNotEmpty()) profilerStep = 2 },
                                        enabled = pGoal.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Next Step", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column {
                                Text(
                                    text = if (pGoal == "Learning") "If you were to invest real money tomorrow, what is your realistic starting budget?" else "Choose your fantasy starting purse:",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val budgets = if (pGoal == "Learning") {
                                    listOf(
                                        10000.0 to "Micro Retail Budget (Ideal for pure discipline)",
                                        25000.0 to "Intermediate Budget (Our highest recommended entry)",
                                        50000.0 to "Retail Professional Scale (Requires high focus)",
                                        100000.0 to "Advanced Allocation (For seasoned paper testing)"
                                    )
                                } else {
                                    listOf(
                                        100000.0 to "Elite Paper Class ($100k Speculator)",
                                        500000.0 to "Standard High Roller ($500k Balance)",
                                        1000000.0 to "Millionaire Simulation ($1M Balance)",
                                        2500000.0 to "Sovereign Wealth Class (Unlimited Playground)"
                                    )
                                }

                                budgets.forEach { (amt, desc) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (pCapital == amt) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                            .border(1.dp, if (pCapital == amt) BrandViolet.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .clickable { pCapital = amt }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = pCapital == amt,
                                            onClick = { pCapital = amt },
                                            colors = RadioButtonDefaults.colors(selectedColor = BrandViolet, unselectedColor = TextSubtle)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(formatCurrencyNoDecimals(amt, stats.currency), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(desc, color = TextSubtle, fontSize = 10.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { profilerStep = 1 }) {
                                        Text("Back", color = TextSubtle, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { profilerStep = 3 },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Next Step", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        3 -> {
                            Column {
                                Text(
                                    text = "How comfortable are you with seeing a position fluctuate -15% in a single day?",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val riskStyles = listOf(
                                    "Conservative" to "Highly anxious. I prefer stable blue chips with minimal variance.",
                                    "Moderate" to "Neutral. I expect moderate volatility for long-term index beats.",
                                    "Aggressive" to "Excited. I seek volatile swings and digital/crypto indices."
                                )

                                riskStyles.forEach { (style, desc) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (pRisk == style) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                            .border(1.dp, if (pRisk == style) BrandViolet.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .clickable { pRisk = style }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = pRisk == style,
                                            onClick = { pRisk = style },
                                            colors = RadioButtonDefaults.colors(selectedColor = BrandViolet, unselectedColor = TextSubtle)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(style, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(desc, color = TextSubtle, fontSize = 10.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { profilerStep = 2 }) {
                                        Text("Back", color = TextSubtle, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { profilerStep = 4 },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Calculate Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        4 -> {
                            Column {
                                Text(
                                    text = "Calibration Verdict Received",
                                    color = BrandViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (pGoal == "Learning") "Student Investor Profile" else "High-Roller Speculator Profile",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("RECOMMENDED CAPITAL", color = TextSubtle, fontSize = 9.sp)
                                        Text(formatCurrencyNoDecimals(pCapital, stats.currency), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("RECOMMENDED RISK", color = TextSubtle, fontSize = 9.sp)
                                        Text(pRisk, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (pGoal == "Learning") {
                                            "Excellent discipline. Starting with virtual millions leads to a psychological trap where position-sizing and fear of losses are lost. By matching your virtual capital to your real-world target (${formatCurrencyNoDecimals(pCapital, stats.currency)}), you learn authentic risk allocation, emotional resilience, and true market metrics."
                                        } else {
                                            "You've selected a Speculative profile. While high virtual funds are excellent for testing leveraged-style ideas without raw risk, remember that real water is deep. Use this high-roller playground to observe price swings, but stay grounded!"
                                        },
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { profilerStep = 0 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("Restart Profiler", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.resetPortfolio(pCapital, pRisk)
                                            profilerStep = 0
                                        },
                                        modifier = Modifier.weight(1.3f).testTag("apply_calibration_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet)
                                    ) {
                                        Text("Apply & Reset Wallet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset starting funds box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "RE-FUND PAPER WALLET",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Mission Academy info
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandViolet.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = "Academy Mission", tint = BrandViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Earn Free Refunds in Academy 🎓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You can easily earn rewards by completing learning missions and quizzes in the Academy. Completing these challenges easily replenishes your paper wallet budget with virtual cash!",
                            color = TextMuted,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Complete Portfolio Reset (Fixed ${if (stats.currency == "INR") "₹25,000" else "$25,000"})",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reset your starting cash completely to a fixed 25,000 ${if (stats.currency == "INR") "Rupees" else "Dollars"}. Warning: Everything will be lost! You will start from a fresh state.\n" +
                            if (stats.isPremium) "✓ Pro Active: Unlimited resets available." else "Unpaid Version: Limited to 3 resets. Resets used: ${stats.portfolioResetsCount}/3.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (stats.isPremium) {
                            viewModel.resetPortfolio(25000.0, stats.riskLevel)
                            viewModel.showFeedback("Pro Advantage: Portfolio reset to 25,000 successfully!")
                        } else if (stats.portfolioResetsCount >= 3) {
                            viewModel.showFeedback("Unpaid version is limited to 3 resets. Go Pro for unlimited resets!")
                        } else {
                            isAdLoading = true
                            adLoadFailedMessage = null
                            adFailedType = "REFUND_25000"
                            if (mainActivity != null) {
                                mainActivity.loadAndShowRewardedAd(
                                    adType = MainActivity.AdType.PORTFOLIO_RESET,
                                    onAdLoaded = { isAdLoading = false },
                                    onAdFailed = { err ->
                                        isAdLoading = false
                                        adLoadFailedMessage = err
                                        // Offline fallback
                                        viewModel.resetPortfolio(25000.0, stats.riskLevel)
                                    },
                                    onUserEarnedReward = {
                                        viewModel.resetPortfolio(25000.0, stats.riskLevel)
                                    }
                                )
                            } else {
                                isAdLoading = false
                                viewModel.resetPortfolio(25000.0, stats.riskLevel)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("portfolio_reset_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stats.isPremium) AccentYellow else if (stats.portfolioResetsCount >= 3) Color.White.copy(alpha = 0.05f) else BrandViolet
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = stats.isPremium || stats.portfolioResetsCount < 3
                ) {
                    Icon(
                        imageVector = if (stats.isPremium) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = "Reset Portfolio",
                        tint = if (stats.isPremium) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (stats.isPremium) "PRO: Reset Portfolio Instantly ⚡" else if (stats.portfolioResetsCount >= 3) "RESETS EXHAUSTED (Limit 3)" else "Watch Ad and Reset Portfolio",
                        color = if (stats.isPremium) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Developer note at the bottom of the profile screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Copyright Ashwat AI 2026",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Made with ❤️ by Ashwat AI",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // AI Coach Insight Dialog
    if (showAiCoachDialog) {
        AlertDialog(
            onDismissRequest = { showAiCoachDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = "AI Coach", tint = BrandViolet)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Strategy Briefing")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Analyzing your profile: Starting Balance is ${formatCurrencyNoDecimals(stats.startingCash, stats.currency)}, current cash is ${formatCurrency(stats.cash, stats.currency)}, with ${holdings.size} active holdings under a ${stats.riskLevel} risk tolerance.",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val coachingAdvice = remember(stats.riskLevel, holdings.isEmpty()) {
                        when {
                            holdings.isEmpty() -> {
                                "You haven't bought any mock stocks yet! Under your ${stats.riskLevel} profile, consider starting with a highly stable ticker like AAPL or MSFT to build basic investing confidence, or NVDA to capture growth momentum without risking real capital."
                            }
                            stats.riskLevel == "Conservative" -> {
                                "Excellent conservative footing. Capital preservation is key. Ensure you don't over-concentrate in high-volatility assets like BTC or TSLA. Focus your virtual cash on blue chips with solid history indices."
                            }
                            stats.riskLevel == "Aggressive" -> {
                                "Your profile is set to Aggressive. This lets you practice high-growth tactics with digital currencies like BTC and ETH. Since this is paper money, use this environment to learn position sizing and watch high volatility swings without fear of permanent losses."
                            }
                            else -> {
                                "Your balanced Moderate strategy is ideal for overall investing growth. Keep diversifying your paper portfolio. Consider allocating 60% to foundational technology (AAPL, GOOG) and 40% to growth or digital assets to measure volatility differences."
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = coachingAdvice,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    Text(
                        text = "Tip: Refreshes the market tick on the dashboard (Home tab) periodically to observe how these allocations react to simulated market swings!",
                        color = BrandViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiCoachDialog = false }) {
                    Text("Acknowledged", color = BrandViolet, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated,
            titleContentColor = Color.White,
            textContentColor = TextSecondary
        )
    }

    // Universal Ad Dialog Overlay (Clean design matching web sandbox)
    if (isAdLoading) {
        Dialog(onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BrandViolet, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading Video Sponsor Network...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Connecting to high-speed media stream, please wait",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else if (activeAdRewardType != null) {
        Dialog(onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BrandViolet, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Streaming Sponsored Message...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Thank you for supporting Trade Lab! Your reward unlocks in ${adTimerSec}s.",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        LaunchedEffect(activeAdRewardType) {
            adTimerSec = 3
            while (adTimerSec > 0) {
                kotlinx.coroutines.delay(1000)
                adTimerSec--
            }
            if (activeAdRewardType?.startsWith("REFUND_") == true) {
                val amt = activeAdRewardType?.removePrefix("REFUND_")?.toDoubleOrNull() ?: 25000.0
                viewModel.resetPortfolio(amt, stats.riskLevel)
            } else {
                when (activeAdRewardType) {
                    "CASH" -> viewModel.earnEmergencyCash(1000.0)
                    "REPLENISH_CASH" -> viewModel.earnEmergencyCash(1000.0)
                    "SHIELD" -> viewModel.earnBrokerageCredits(100)
                    "AI" -> viewModel.earnAiAuditCredit()
                    "INDICATORS" -> viewModel.unlockPremiumIndicators(24)
                }
            }
            activeAdRewardType = null
        }
    } else if (adLoadFailedMessage != null) {
        AlertDialog(
            onDismissRequest = { adLoadFailedMessage = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = AccentRose)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sponsor Stream Failed", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "We couldn't connect to our ad network partner ($adLoadFailedMessage).",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Would you like to unlock this premium reward for free via our sponsor fallback channel instead?",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reward = adFailedType
                        adLoadFailedMessage = null
                        if (reward != null) {
                            activeAdRewardType = reward
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandViolet)
                ) {
                    Text("Unlock Free", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { adLoadFailedMessage = null }) {
                    Text("Cancel", color = TextSubtle, fontSize = 11.sp)
                }
            },
            containerColor = DarkSurfaceElevated,
            titleContentColor = Color.White,
            textContentColor = TextSubtle
        )
    }
}


