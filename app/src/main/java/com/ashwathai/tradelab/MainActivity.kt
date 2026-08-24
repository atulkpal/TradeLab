package com.ashwathai.tradelab

import androidx.lifecycle.lifecycleScope
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.pager.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import com.ashwathai.tradelab.billing.SubscriptionConfig
import kotlinx.coroutines.launch
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.QuizModule
import com.ashwathai.tradelab.ui.QuizQuestion
import com.ashwathai.tradelab.ui.AcademyScoring
import com.ashwathai.tradelab.ui.Lecture
import com.ashwathai.tradelab.ui.Mission
import com.ashwathai.tradelab.ui.theme.*
import com.ashwathai.tradelab.ui.AuthScreen
import com.ashwathai.tradelab.billing.BillingManager
import com.ashwathai.tradelab.BuildConfig

// Import modular layouts and functions
import com.ashwathai.tradelab.ui.common.*
import com.ashwathai.tradelab.ui.charts.*
import com.ashwathai.tradelab.ui.portfolio.*
import com.ashwathai.tradelab.ui.watchlist.*
import com.ashwathai.tradelab.ui.watchlist.BuySellBottomSheet
import com.ashwathai.tradelab.ui.academy.*
import com.ashwathai.tradelab.ui.derivatives.*
import com.ashwathai.tradelab.ui.commodities.*
import com.ashwathai.tradelab.ui.profile.*

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TradingViewModel by viewModels()
    private lateinit var billingManager: BillingManager

    enum class AdType {
        ACADEMY_DOUBLE,
        PORTFOLIO_SHIELD,
        PROFILE_AI_ADVISOR,
        PROFILE_EMERGENCY_CASH,
        PROFILE_INDICATORS,
        PROFILE_SHIELD_MAX,
        WATCHLIST_CREATE,
        PORTFOLIO_RESET,
        PROFILE_LEVERAGE,
        APP_OPEN,
        NATIVE_WATCHLIST,
        PROFILE_BANNER
    }

    fun showRewardedFor(placement: String, onEarned: () -> Unit) {
        LevelPlayAdManager.loadAndShowRewardedAd(
            adType = placement,
            activity = this,
            onAdLoaded = {},
            onAdFailed = { viewModel.showFeedback("No ad available right now") },
            onUserEarnedReward = onEarned
        )
    }

    fun loadAndShowRewardedAd(
        adType: AdType,
        onAdLoaded: () -> Unit,
        onAdFailed: (String) -> Unit,
        onUserEarnedReward: () -> Unit
    ) {
        LevelPlayAdManager.loadAndShowRewardedAd(
            adType = adType.name,
            activity = this,
            onAdLoaded = onAdLoaded,
            onAdFailed = onAdFailed,
            onUserEarnedReward = onUserEarnedReward
        )
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Market Alerts"
            val descriptionText = "Notifications for market open/close"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MARKET_ALERTS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSystemNotification(message: String) {
        val builder = NotificationCompat.Builder(this, "MARKET_ALERTS")
            .setSmallIcon(R.drawable.app_logo_premium)
            .setContentTitle("TradeLab Market Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        billingManager = BillingManager(this) {
            viewModel.completePremiumPurchase()
        }

        // Initialize Unity LevelPlay (Epic 26) - replaces AdMob
        LevelPlayAdManager.init(application)
        LevelPlayAdManager.trackActivity(this)
        
        createNotificationChannel()

        // Observe system notifications
        lifecycleScope.launch {
            viewModel.systemNotificationFlow.collect { message ->
                showSystemNotification(message)
            }
        }
        
        // Start periodic price updates and simulations
        viewModel.startBackgroundTasks()

        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle()
            val isZenMode by viewModel.isZenMode.collectAsStateWithLifecycle()
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val hasDismissedAuthScreen by viewModel.hasDismissedAuthScreen.collectAsStateWithLifecycle()
            val isInitialized by viewModel.isInitialized.collectAsStateWithLifecycle()

            MyApplicationTheme(
                themeMode = themeMode,
                isStealthMode = isStealthMode,
                isZenMode = isZenMode
            ) {
                if (!isInitialized) {
                    TradeLabLoadingScreen()
                } else if (userProfile?.isLoggedIn != true && !hasDismissedAuthScreen) {
                    AuthScreen(viewModel = viewModel)
                } else {
                    MainContent(
                        viewModel = viewModel,
                        billingManager = billingManager
                    )
                }
            }
        }
    }
}


@Composable
private fun TradeLabLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Trade Lab",
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Preparing your market simulator...",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            ShimmerProgress(
                progress = 0.68f,
                color = BrandViolet,
                modifier = Modifier
                    .width(220.dp)
                    .height(8.dp)
            )
            Text(
                text = "Loading watchlists, ledgers, and live paper prices",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun MainContent(
    viewModel: TradingViewModel,
    billingManager: BillingManager
) {
    val mainActivity = androidx.compose.ui.platform.LocalContext.current as? MainActivity
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val stats by viewModel.portfolioStats.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val postTradeRating by viewModel.postTradeRating.collectAsStateWithLifecycle()
    val showRegistrationGate by viewModel.showRegistrationGate.collectAsStateWithLifecycle()
    val showPaywall by viewModel.showPaywall.collectAsStateWithLifecycle()
    val showProBenefits by viewModel.showProBenefits.collectAsStateWithLifecycle()
    val showPremiumHub by viewModel.showPremiumHub.collectAsStateWithLifecycle()
    val showGoogleBilling by viewModel.showGoogleBilling.collectAsStateWithLifecycle()
    val isSimulatedMode by viewModel.isSimulatedMode.collectAsStateWithLifecycle()
    val latestNews by viewModel.latestNews.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showTradeSheet by remember { mutableStateOf(false) }
    var tradeSheetIsBuy by remember { mutableStateOf(true) }
    var tradeSheetIsExpanded by remember { mutableStateOf(false) }

    // Active Quiz dialogue anchor (for Learn-to-Earn Academy or deep link triggers)
    var activeQuizLevelId by remember { mutableStateOf<Int?>(null) }

    // Trigger sheet dismissal from VM
    LaunchedEffect(Unit) {
        viewModel.dismissBottomSheetTrigger.collect {
            showTradeSheet = false
        }
    }

    // Trigger snackbar on message update
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedback()
        }
    }

    val focusManager = LocalFocusManager.current
    val pagerState = rememberPagerState(
        initialPage = MainTabs.indexOfFirst { it.name == currentTab }.coerceAtLeast(0),
        pageCount = { MainTabs.size }
    )

    // Sync ViewModel tab -> Pager (when tab changed via BottomNav or code)
    LaunchedEffect(currentTab) {
        val targetPage = MainTabs.indexOfFirst { it.name == currentTab }
        if (targetPage != -1 && pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync Pager -> ViewModel tab (when tab changed via Swipe)
    LaunchedEffect(pagerState.settledPage) {
        val tabName = MainTabs[pagerState.settledPage].name
        if (currentTab != tabName) {
            viewModel.selectTab(tabName)
        }
    }

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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBg,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                BottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header
                HeaderBar(
                    title = when (currentTab) {
                        "Portfolio" -> "Portfolio"
                        "Watchlist" -> "Watchlist"
                        "Commodities" -> "Commodities"
                        "F&O" -> "F&O"
                        "Academy" -> "Academy"
                        "Profile" -> "Profile"
                        else -> "Trade Lab"
                    },
                    riskLevel = stats.riskLevel,
                    isSimulatedMode = isSimulatedMode,
                    onToggleSimulated = { viewModel.toggleSimulationMode(it) },
                    actions = {
                        if (currentTab == "Watchlist") {
                            val isSearchVisible by viewModel.isWatchlistSearchVisible.collectAsStateWithLifecycle()
                            IconButton(
                                onClick = { viewModel.toggleWatchlistSearch() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchVisible) BrandViolet else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Dynamic view based on active tab with Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true 
                ) { page ->
                    when (MainTabs[page].name) {
                        "Portfolio" -> PortfolioScreen(
                            viewModel = viewModel,
                            stats = stats,
                            latestNews = latestNews,
                            onTickerClick = { symbol ->
                                viewModel.selectStock(symbol)
                                tradeSheetIsBuy = true
                                tradeSheetIsExpanded = true
                                showTradeSheet = true
                            }
                        )
                        "Watchlist" -> WatchlistScreen(
                            viewModel = viewModel,
                            stats = stats,
                            latestNews = latestNews,
                            onTickerClick = { symbol, isBuy, isExpanded ->
                                viewModel.selectStock(symbol)
                                tradeSheetIsBuy = isBuy
                                tradeSheetIsExpanded = isExpanded
                                showTradeSheet = true
                            }
                        )
                        "Charts" -> ChartScreen(
                            viewModel = viewModel
                        )
                        "Commodities" -> CommoditiesScreen(
                            viewModel = viewModel,
                            stats = stats,
                            onTickerClick = { symbol ->
                                viewModel.selectStock(symbol)
                                tradeSheetIsBuy = true
                                tradeSheetIsExpanded = true
                                showTradeSheet = true
                            },
                            onShowRewardedAd = { onEarned, onFailed ->
                                val act = mainActivity
                                if (act != null) act.showRewardedFor("commodities_unlock", onEarned)
                                else onFailed("unavailable")
                            }
                        )
                        "F&O" -> FoDeskScreen(
                            viewModel = viewModel,
                            stats = stats,
                            onShowRewardedAd = { onEarned, onFailed ->
                                val act = mainActivity
                                if (act != null) act.showRewardedFor("fno_tokens", onEarned)
                                else onFailed("unavailable")
                            }
                        )
                        "Academy" -> AcademyScreen(
                            viewModel = viewModel,
                            stats = stats,
                            onOpenQuiz = { activeQuizLevelId = it },
                            onShowRewardedAd = { onEarned, onFailed ->
                                val act = mainActivity
                                if (act != null) act.showRewardedFor("ai_credits", onEarned)
                                else onFailed("unavailable")
                            }
                        )
                        "Profile" -> ProfileScreen(
                            viewModel = viewModel,
                            stats = stats,
                                            )
                    }
                }

                // Premium Hub Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = showPremiumHub,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it }),
                    modifier = Modifier.fillMaxSize().zIndex(100f)
                ) {
                    PremiumHubScreen(
                        viewModel = viewModel,
                        stats = stats,
                        onBack = { viewModel.closePremiumHub() }
                    )
                }
            }
        }

            // Post-Trade Diagnostic and Rating Feedback Dialog
            postTradeRating?.let { rating ->
                Dialog(onDismissRequest = { viewModel.clearTradeRating() }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("post_trade_rating_dialog"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (rating.overallScore >= 70) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Trade Assessment",
                                tint = if (rating.overallScore >= 85) AccentGreen else if (rating.overallScore >= 70) AccentYellow else AccentRose,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Trade Quality Score",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${rating.overallScore}/100",
                                color = TextPrimary,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = rating.ratingText,
                                color = if (rating.overallScore >= 85) AccentGreen else if (rating.overallScore >= 70) AccentYellow else AccentRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = DarkBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Sizing breakdown
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Position Sizing:", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("${rating.sizeScore}/100", color = if (rating.sizeScore >= 75) AccentGreen else AccentRose, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = rating.sizeScore / 100f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (rating.sizeScore >= 75) AccentGreen else AccentRose,
                                    trackColor = Color.White.copy(alpha = 0.05f)
                                )
                                Text(rating.sizeAdvice, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Order type breakdown
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Order Type Selection:", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("${rating.typeScore}/100", color = if (rating.typeScore >= 75) AccentGreen else AccentYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = rating.typeScore / 100f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (rating.typeScore >= 75) AccentGreen else AccentYellow,
                                    trackColor = Color.White.copy(alpha = 0.05f)
                                )
                                Text(rating.typeAdvice, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { viewModel.clearTradeRating() },
                                colors = ButtonDefaults.buttonColors(containerColor = DynamicPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Acknowledge & Continue", color = TextOnAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Registration Gate Dialog
            if (showRegistrationGate) {
                var regName by remember { mutableStateOf("") }
                var regEmail by remember { mutableStateOf("") }
                var regError by remember { mutableStateOf<String?>(null) }

                Dialog(onDismissRequest = { viewModel.dismissRegistrationGate() }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("registration_gate_dialog"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Registration required",
                                tint = BrandViolet,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unlock Unlimited Trades",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You have reached the trial action limit. Register your free Trade Lab account to unlock unlimited realistic paper trading!",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it; regError = null },
                                modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                                placeholder = { Text("Enter your full name", color = TextSubtle) },
                                label = { Text("Name", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = DynamicPrimary,
                                    unfocusedBorderColor = DarkBorder
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it; regError = null },
                                modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                                placeholder = { Text("Enter your email address", color = TextSubtle) },
                                label = { Text("Email Address", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = DynamicPrimary,
                                    unfocusedBorderColor = DarkBorder
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (regError != null) {
                                Text(
                                    text = regError!!,
                                    color = AccentRose,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank()) {
                                        regError = "All fields are required"
                                    } else if (!regEmail.contains("@")) {
                                        regError = "Please enter a valid email"
                                    } else {
                                        viewModel.simulateRegister(regName, regEmail)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DynamicPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().testTag("register_submit_button")
                            ) {
                                Text("Register & Continue", color = TextOnAccent, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { viewModel.dismissRegistrationGate() }
                            ) {
                                Text("Cancel", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Paywall Dialog
            if (showPaywall) {
                val promo = rememberLaunchPromo()
                Dialog(onDismissRequest = { viewModel.dismissPaywall() }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("premium_paywall_dialog"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Premium Paywall",
                                    tint = AccentYellow,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Upgrade to Trade Lab Pro",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unlock premium institutional-grade trading tools and expert consultation.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Features list
                            listOf(
                                Icons.Default.Lightbulb to "Unlimited Deep AI Consultations",
                                Icons.Default.ShowChart to "Precision Technical Indicators (RSI, SMA)",
                                Icons.Default.Bolt to "Instant Execution GTT Triggering Desk",
                                Icons.Default.Star to "Exclusive Youth Academy Level 5-10"
                            ).forEach { (icon, text) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = text,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            LaunchPromoBanner(promo)

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.simulatePremiumPurchase() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().testTag("upgrade_pro_button")
                            ) {
                                Text(promo.ctaLabel, color = DarkBg, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { viewModel.dismissPaywall() }
                            ) {
                                Text("Maybe Later", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Animated overlay sliding buy/sell sheet!
        AnimatedVisibility(
            visible = showTradeSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(99f)
        ) {
            val stock by viewModel.selectedStock.collectAsStateWithLifecycle()
            stock?.let { currentStock ->
                BuySellBottomSheet(
                    stock = currentStock,
                    viewModel = viewModel,
                    stats = stats,
                    initialIsBuy = tradeSheetIsBuy,
                    initialIsExpanded = tradeSheetIsExpanded,
                    onDismiss = { showTradeSheet = false }
                )
            }
        }

        // Active Academy Lecture and Quiz — full-screen destination (Epic 25.3)
        activeQuizLevelId?.let { quizId ->
            val quizModules by viewModel.quizModules.collectAsStateWithLifecycle()
            val quiz = quizModules.find { it.id == quizId }
            val academyCourses by viewModel.academyCourses.collectAsStateWithLifecycle()
            val completedSet = remember(stats.completedLevels) {
                stats.completedLevels.split(",").filter { it.isNotBlank() }.toSet()
            }
            val unlockedIds = remember(academyCourses, completedSet) {
                AcademyScoring.unlockedCourseIds(academyCourses, completedSet)
            }
            val isChapterLocked = quiz != null && quiz.courseId !in unlockedIds

            if (quiz != null) {
                val mainActivity = androidx.compose.ui.platform.LocalContext.current as? MainActivity
                LectureScreen(
                    quiz = quiz,
                    isChapterLocked = isChapterLocked,
                    isAlreadyCompleted = completedSet.contains(quizId.toString()),
                    stats = stats,
                    onDismiss = { activeQuizLevelId = null },
                    onCompleteStandard = { viewModel.completeTutorial(quiz.id, quiz.rewardAmt) },
                    onCompleteDouble = { viewModel.completeTutorial(quiz.id, quiz.rewardAmt * 2.0) },
                    onFeedback = { viewModel.showFeedback(it) },
                    onLaunchDoubleRewardAd = { onAdLoaded, onAdFailed, onUserEarnedReward ->
                        mainActivity?.loadAndShowRewardedAd(
                            adType = MainActivity.AdType.ACADEMY_DOUBLE,
                            onAdLoaded = onAdLoaded,
                            onAdFailed = onAdFailed,
                            onUserEarnedReward = onUserEarnedReward
                        ) ?: onAdFailed("Ad host unavailable")
                    }
                )
            }
        }
        ConfettiOverlay(trigger = confettiTrigger)
        
        TradeLabProBenefitsDialog(
            show = showProBenefits,
            onDismiss = { viewModel.closeProBenefits() },
            onUpgradeClick = {
                viewModel.closeProBenefits()
                viewModel.openBillingFlow()
            },
            currency = stats.currency
        )
        
        GoogleBillingDialog(
            show = showGoogleBilling,
            onDismiss = { viewModel.closeBillingFlow() },
            onPurchaseSuccess = { viewModel.completePremiumPurchase() },
            billingManager = billingManager
        )

        SimulationDisclaimerDialog(
            show = userProfile != null && !userProfile!!.hasAcceptedSimDisclaimer,
            onAccept = { viewModel.acceptSimulationDisclaimer() }
        )
    }
}

data class LaunchPromoState(
    val isActive: Boolean,
    val countdown: String,
    val ctaLabel: String,
    val offerLine: String,
    val renewalPrice: String,
    val trialDays: Int,
    val priceLabel: String
)

@Composable
fun rememberLaunchPromo(): LaunchPromoState {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }
    val isActive = SubscriptionConfig.isLaunchPromoActive(now)
    val renewalPrice = SubscriptionConfig.monthlyPrice(now)
    val ctaLabel = if (isActive) {
        "7-Day Free Trial • ₹${SubscriptionConfig.LAUNCH_PRICE_INR}/mo (50% OFF, was ₹${SubscriptionConfig.REGULAR_PRICE_INR})"
    } else {
        "7-Day Free Trial • ₹${SubscriptionConfig.REGULAR_PRICE_INR}/mo"
    }
    return LaunchPromoState(
        isActive = isActive,
        countdown = SubscriptionConfig.countdownLabel(now),
        ctaLabel = ctaLabel,
        offerLine = "7-day free trial • Renews at $renewalPrice/month",
        renewalPrice = renewalPrice,
        trialDays = SubscriptionConfig.FREE_TRIAL_DAYS,
        priceLabel = "₹${if (isActive) SubscriptionConfig.LAUNCH_PRICE_INR else SubscriptionConfig.REGULAR_PRICE_INR}/mo"
    )
}

@Composable
fun LaunchPromoBanner(promo: LaunchPromoState, modifier: Modifier = Modifier) {
    if (!promo.isActive) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentYellow.copy(alpha = 0.12f))
            .border(1.dp, AccentYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "₹49 launch offer ends in ${promo.countdown}",
                color = AccentYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GoogleBillingDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    billingManager: BillingManager? = null
) {
    if (!show) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? Activity
    
    val isFromPlayStore = remember {
        if (BuildConfig.DEBUG) {
            true // Always allow simulated billing in debug mode for development and testing
        } else {
            try {
                val installer = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getInstallerPackageName(context.packageName)
                }
                installer == "com.android.vending"
            } catch (e: Exception) {
                false
            }
        }
    }

    if (!isFromPlayStore) {
        // ... (existing sideload warning)
        Dialog(onDismissRequest = onDismiss) {
            // ... (sideload warning content)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("sideload_warning_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GoogleDarkGray),
                border = BorderStroke(1.5.dp, WarningRed.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(WarningRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Billing Locked",
                            tint = WarningRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Google Play Store Required",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "To run secure billing, please download this app from the Google Play Store.\n\nSide-loaded versions cannot process official Google Play transactions.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=com.ashwathai.tradelab")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.ashwathai.tradelab")
                                    )
                                    context.startActivity(intent)
                                } catch (ex: Exception) {
                                    // Fallback
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Download from Play Store", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got it", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                }
            }
        }
        return
    }

    // NEW: If we are in Release mode AND installed from Play Store, 
    // we bypass our simulated dialog and launch the real Google Play Billing sheet.
    if (!BuildConfig.DEBUG && isFromPlayStore) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            activity?.let {
                billingManager?.startBillingFlow(it, SubscriptionConfig.activeProductId())
            }
            onDismiss()
        }
        return
    }

    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
// "UPI" or "CARD"
    var upiId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf(0) } // 0 = Idle, 1 = Connecting, 2 = Authenticating, 3 = Success
    
    val promo = rememberLaunchPromo()

    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.DAY_OF_YEAR, promo.trialDays)
    val renewalDateStr = java.text.SimpleDateFormat("dd MMM yyyy", LocalConfiguration.current.locales[0]).format(calendar.time)

    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("google_billing_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GoogleDarkGray), // Google Play Dark Gray
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header: Google Play Brand Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Play Multi-color Play Icon representation
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Google Play",
                            tint = GoogleBlue, // Google blue
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Google Play",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = GoogleGreen, // Google green
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (processingStep < 3) {
                    // Item Detail
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TradeLab Pro (Ashwath AI)",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = promo.offerLine,
                                color = GoogleTextGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isProcessing) {
                        // Trial Breakdown
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Today – Day ${promo.trialDays}", color = GoogleTextGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₹0.00", color = GoogleGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Starting $renewalDateStr", color = Color.White, fontSize = 11.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("${promo.renewalPrice} / month", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Choose Payment Method
                        Text(
                            text = "SELECT PAYMENT METHOD",
                            color = GoogleTextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // UPI option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedPaymentMethod == "UPI") BrandViolet.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, if (selectedPaymentMethod == "UPI") BrandViolet else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .clickable { selectedPaymentMethod = "UPI" }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "UPI",
                                        tint = if (selectedPaymentMethod == "UPI") BrandViolet else Color(0xFF9AA0A6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("UPI / GPay", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Card Option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedPaymentMethod == "CARD") BrandViolet.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, if (selectedPaymentMethod == "CARD") BrandViolet else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .clickable { selectedPaymentMethod = "CARD" }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = "Credit Card",
                                        tint = if (selectedPaymentMethod == "CARD") BrandViolet else Color(0xFF9AA0A6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Credit/Debit Card", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Form Inputs
                        if (selectedPaymentMethod == "UPI") {
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("Enter UPI ID (e.g. name@okhdfcbank)", color = GoogleTextGray, fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DynamicPrimary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("billing_upi_input")
                            )
                        } else {
                            Column {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { if (it.length <= 16) cardNumber = it },
                                    label = { Text("Card Number (16 Digits)", color = GoogleTextGray, fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DynamicPrimary,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("billing_card_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = { if (it.length <= 5) cardExpiry = it },
                                        label = { Text("Expiry (MM/YY)", color = GoogleTextGray, fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = DynamicPrimary,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).testTag("billing_expiry_input")
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = { if (it.length <= 3) cardCvv = it },
                                        label = { Text("CVV (3 Digits)", color = GoogleTextGray, fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = DynamicPrimary,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).testTag("billing_cvv_input")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Terms
                        Text(
                            text = "By clicking 'SUBSCRIBE', you authorize Google Play to charge ${promo.renewalPrice}/mo automatically after your ${promo.trialDays}-day free trial. Cancel anytime in subscription settings.",
                            color = GoogleTextGray,
                            fontSize = 9.sp,
                            lineHeight = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Subscribe CTA Buttons
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL", color = GoogleTextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    isProcessing = true
                                    processingStep = 1
                                    scope.launch {
                                        kotlinx.coroutines.delay(1200)
                                        processingStep = 2
                                        kotlinx.coroutines.delay(1200)
                                        processingStep = 3
                                        kotlinx.coroutines.delay(1000)
                                        onPurchaseSuccess()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue), // Google Blue
                                shape = RoundedCornerShape(12.dp),
                                enabled = if (selectedPaymentMethod == "UPI") upiId.isNotBlank() else (cardNumber.length >= 15 && cardExpiry.length >= 4 && cardCvv.length >= 3),
                                modifier = Modifier.weight(1.5f).testTag("billing_subscribe_button")
                            ) {
                                Text("SUBSCRIBE", color = TextOnAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Processing state loader
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = GoogleBlue, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (processingStep == 1) "Securing bank connection..." else "Processing subscription authorization...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Success Screen!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = GoogleGreen, // Google Green
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Approved!",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Subscription Activated successfully.",
                            color = GoogleTextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TradeLabProBenefitsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit,
    currency: String
) {
    if (!show) return

    val promo = rememberLaunchPromo()

    val symbol = if (currency == "INR") "₹" else "$"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("pro_benefits_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GoogleDarkGray),
            border = BorderStroke(1.5.dp, AccentYellow.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Top header with golden badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AccentYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Pro Icon",
                                tint = AccentYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TradeLab Pro Benefits",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSubtle,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "Accelerate your investing journey with our premium training tools. No limits, no distractions, pure learning flow.",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable container for features
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Feature 1: Zero Brokerage
                    ProBenefitRow(
                        icon = Icons.Default.CheckCircle,
                        iconColor = AccentGreen,
                        title = "Zero Brokerage Charges",
                        description = "Fully waive the 20 credits/trade transaction cost. Keep 100% of your gains."
                    )

                    // Feature 2: Technical Chart Indicators
                    ProBenefitRow(
                        icon = Icons.Default.ShowChart,
                        iconColor = AccentRose,
                        title = "Premium Technical Indicators",
                        description = "Unlock Simple Moving Average (SMA), Exponential Moving Average (EMA), and RSI indicators permanently to master technical analysis trends."
                    )

                    // Feature 3: Double Quiz Rewards
                    ProBenefitRow(
                        icon = Icons.Default.School,
                        iconColor = BrandViolet,
                        title = "Double Quiz Rewards Instantly",
                        description = "Claim double virtual cash (${symbol}2,000 instead of ${symbol}1,000) on completing academy lessons without watching video ads."
                    )

                    // Feature 4: Unlimited Portfolio Resets
                    ProBenefitRow(
                        icon = Icons.Default.Refresh,
                        iconColor = GoogleBlue,
                        title = "Unlimited Portfolio Resets",
                        description = "Erase your history and start over with ${symbol}25,000 cash instantly whenever you want. No daily limits or wait times."
                    )

                    // Feature 5: Emergency Wallet Cash
                    ProBenefitRow(
                        icon = Icons.Default.AttachMoney,
                        iconColor = AccentGreen,
                        title = "Instant Emergency Wallet Cash",
                        description = "Replenish your balance with +${symbol}1,000 cash instantly anytime with one click. No video ads required."
                    )

                    // Feature 6: Unlimited Watchlists
                    ProBenefitRow(
                        icon = Icons.Default.List,
                        iconColor = AccentYellow,
                        title = "Unlimited Watchlist Space",
                        description = "Organize multiple groups of Indian and US stock trackers across custom sheets seamlessly."
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Buttons
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = promo.ctaLabel,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cancel anytime in Google Play Store. Reverts to Free Tier after trial.",
                    color = TextMuted,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun ProBenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSubtle,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}


