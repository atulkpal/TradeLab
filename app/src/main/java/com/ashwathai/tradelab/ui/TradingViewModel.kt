package com.ashwathai.tradelab.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ashwathai.tradelab.data.*
import com.ashwathai.tradelab.BuildConfig
import com.ashwathai.tradelab.R
import com.ashwathai.tradelab.billing.SubscriptionConfig
import com.ashwathai.tradelab.ui.theme.ThemeMode
import com.ashwathai.tradelab.shared.TradingHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.google.firebase.auth.FirebaseAuth
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.ashwathai.tradelab.di.DefaultDispatcher
import com.ashwathai.tradelab.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TradingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TradingRepository,
    private val leaderboardManager: LeaderboardManager,
    private val videoManifestRepository: com.ashwathai.tradelab.data.VideoManifestRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // Dynamic Academy & Missions data from JSON
    private val _quizModules = MutableStateFlow<List<ChapterModule>>(emptyList())
    val quizModules: StateFlow<List<ChapterModule>> = _quizModules.asStateFlow()

    private val _academyCourses = MutableStateFlow<List<AcademyCourse>>(emptyList())
    val academyCourses: StateFlow<List<AcademyCourse>> = _academyCourses.asStateFlow()

    // Epic 27: remote video manifest + dynamic lecture language
    val videoManifest = videoManifestRepository.manifest
    val videoManifestReady: StateFlow<Boolean> = videoManifest
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _academyLanguage = MutableStateFlow(loadAcademyLanguagePref())
    val academyLanguage: StateFlow<String> = _academyLanguage.asStateFlow()

    /** Toggle EN ↔ HI for lecture videos (persisted; availability enforced in UI). */
    fun toggleAcademyLanguage() {
        val next = if (_academyLanguage.value == com.ashwathai.tradelab.data.VideoManifestRepository.LANG_HI) {
            com.ashwathai.tradelab.data.VideoManifestRepository.LANG_EN
        } else {
            com.ashwathai.tradelab.data.VideoManifestRepository.LANG_HI
        }
        _academyLanguage.value = next
        try {
            context.getSharedPreferences(ACADEMY_PREFS, Context.MODE_PRIVATE)
                .edit().putString(ACADEMY_LANG_KEY, next).apply()
        } catch (_: Exception) {
        }
    }

    /** Resolved playback info for a bundled lecture key in the current language. */
    fun lectureMedia(bundledVideoUrl: String): com.ashwathai.tradelab.data.LectureMedia =
        videoManifestRepository.lectureMedia(bundledVideoUrl, _academyLanguage.value)

    private fun loadAcademyLanguagePref(): String = try {
        context.getSharedPreferences(ACADEMY_PREFS, Context.MODE_PRIVATE)
            .getString(ACADEMY_LANG_KEY, com.ashwathai.tradelab.data.VideoManifestRepository.LANG_EN)
            ?: com.ashwathai.tradelab.data.VideoManifestRepository.LANG_EN
    } catch (_: Exception) {
        com.ashwathai.tradelab.data.VideoManifestRepository.LANG_EN
    }

    private val _missionsList = MutableStateFlow<List<Mission>>(emptyList())
    val missionsList: StateFlow<List<Mission>> = _missionsList.asStateFlow()

    val claimedMissions: StateFlow<Set<String>> = repository.userProfile
        .map { profile ->
            profile?.claimedMissions?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Dynamic Theme Mode
    private val _themeMode = MutableStateFlow(ThemeMode.SERIOUS)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    val isDarkTheme: StateFlow<Boolean> = _themeMode.map { it != ThemeMode.LIGHT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _isStealthMode = MutableStateFlow(false)
    val isStealthMode: StateFlow<Boolean> = _isStealthMode.asStateFlow()

    private val _isZenMode = MutableStateFlow(false)
    val isZenMode: StateFlow<Boolean> = _isZenMode.asStateFlow()

    // Raw database state flows
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val holdings: StateFlow<List<Holding>> = repository.holdings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist: StateFlow<List<WatchlistItem>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockPrices: StateFlow<List<StockPrice>> = repository.stockPrices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlistNames: StateFlow<List<WatchlistName>> = repository.watchlistNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingOrders: StateFlow<List<PendingOrder>> = repository.pendingOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePendingOrders: StateFlow<List<PendingOrder>> = repository.activePendingOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appNotifications: StateFlow<List<AppNotification>> = repository.appNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestNews: StateFlow<List<MarketNews>> = repository.latestNews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountSnapshots: StateFlow<List<AccountSnapshot>> = repository.accountSnapshots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerEntries: StateFlow<List<LedgerEntry>> = repository.ledgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getNewsForSymbol(symbol: String): Flow<List<MarketNews>> {
        return repository.getNewsBySymbolFlow(symbol)
    }

    // Multi-Watchlist Selected ID
    private val _selectedWatchlistId = MutableStateFlow(1)
    val selectedWatchlistId: StateFlow<Int> = _selectedWatchlistId.asStateFlow()

    // Derived State: Selected Watchlist's items
    val selectedWatchlistItems: StateFlow<List<WatchlistItemV2>> = _selectedWatchlistId
        .flatMapLatest { repository.getWatchlistItemsFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Registration Gate & Paywall UI states
    private val _showRegistrationGate = MutableStateFlow(false)
    val showRegistrationGate: StateFlow<Boolean> = _showRegistrationGate.asStateFlow()

    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    private val _showProBenefits = MutableStateFlow(false)
    val showProBenefits: StateFlow<Boolean> = _showProBenefits.asStateFlow()

    private val _showGoogleBilling = MutableStateFlow(false)
    val showGoogleBilling: StateFlow<Boolean> = _showGoogleBilling.asStateFlow()

    private val _showPremiumHub = MutableStateFlow(false)
    val showPremiumHub: StateFlow<Boolean> = _showPremiumHub.asStateFlow()

    // Gemini AI Chat log
    private val _aiChatLog = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiChatLog: StateFlow<List<Pair<String, String>>> = _aiChatLog.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Simulation Mode State (Default to true in debug environment)
    private val _isSimulatedMode = MutableStateFlow(BuildConfig.DEBUG)
    val isSimulatedMode: StateFlow<Boolean> = _isSimulatedMode.asStateFlow()

    fun isIndianStockSymbol(symbol: String): Boolean {
        return TradingHelper.isIndianStockSymbol(symbol)
    }

    fun getConvertedStockPrice(priceInNativeCurrency: Double, symbol: String, targetCurrency: String): Double {
        return TradingHelper.getConvertedStockPrice(priceInNativeCurrency, symbol, targetCurrency)
    }

    // Top Movers (Track B)
    val topGainers: StateFlow<List<StockPrice>> = stockPrices
        .map { list -> list.filter { it.symbol !in listOf("NIFTY50", "BANKNIFTY", "NIFTYIT") }.sortedByDescending { it.dailyChangePct }.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topLosers: StateFlow<List<StockPrice>> = stockPrices
        .map { list -> list.filter { it.symbol !in listOf("NIFTY50", "BANKNIFTY", "NIFTYIT") }.sortedBy { it.dailyChangePct }.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val indices: StateFlow<List<StockPrice>> = stockPrices
        .map { list -> list.filter { it.symbol in listOf("NIFTY50", "BANKNIFTY", "NIFTYIT") }.sortedBy { it.symbol } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _currentTab = MutableStateFlow("Portfolio") // Portfolio, Watchlist, Academy, Profile
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _commoditiesUnlocked = MutableStateFlow(false)
    val commoditiesUnlocked: StateFlow<Boolean> = _commoditiesUnlocked.asStateFlow()
    @Volatile private var commoditiesUnlockTime: Long? = null
    @Volatile private var backgroundTasksStarted = false

    private companion object {
        const val ACADEMY_PREFS = "academy_prefs"
        const val ACADEMY_LANG_KEY = "language"
    }

    private val _hasDismissedAuthScreen = MutableStateFlow(false)
    val hasDismissedAuthScreen: StateFlow<Boolean> = _hasDismissedAuthScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _autocompleteResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val autocompleteResults: StateFlow<List<SearchResult>> = _autocompleteResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _watchlistSearchQuery = MutableStateFlow("")
    val watchlistSearchQuery: StateFlow<String> = _watchlistSearchQuery.asStateFlow()

    private val _watchlistAutocompleteResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val watchlistAutocompleteResults: StateFlow<List<SearchResult>> = _watchlistAutocompleteResults.asStateFlow()

    private val _isWatchlistSearching = MutableStateFlow(false)
    val isWatchlistSearching: StateFlow<Boolean> = _isWatchlistSearching.asStateFlow()

    private val _selectedStockSymbol = MutableStateFlow<String?>("AAPL") // Start with AAPL
    val selectedStockSymbol: StateFlow<String?> = _selectedStockSymbol.asStateFlow()

    private val _tradeSharesInput = MutableStateFlow("")
    val tradeSharesInput: StateFlow<String> = _tradeSharesInput.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _confettiTrigger = MutableStateFlow<Long>(0L)
    val confettiTrigger: StateFlow<Long> = _confettiTrigger.asStateFlow()

    val isWatchlistCompactMode: StateFlow<Boolean> = userProfile
        .map { it?.isWatchlistCompactMode ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isWatchlistSearchVisible = MutableStateFlow(false)
    val isWatchlistSearchVisible: StateFlow<Boolean> = _isWatchlistSearchVisible.asStateFlow()

    // Order flow custom states
    private val _orderType = MutableStateFlow("Market") // Market, Limit, GTT, Bracket
    val orderType: StateFlow<String> = _orderType.asStateFlow()

    private val _isDelivery = MutableStateFlow(true)
    val isDelivery: StateFlow<Boolean> = _isDelivery.asStateFlow()

    private val _triggerPriceInput = MutableStateFlow("")
    val triggerPriceInput: StateFlow<String> = _triggerPriceInput.asStateFlow()

    private val _dismissBottomSheetTrigger = MutableSharedFlow<Unit>(replay = 0)
    val dismissBottomSheetTrigger: SharedFlow<Unit> = _dismissBottomSheetTrigger.asSharedFlow()

    fun navigateToChart(symbol: String) {
        selectStock(symbol)
        selectTab("Charts")
        viewModelScope.launch {
            _dismissBottomSheetTrigger.emit(Unit)
        }
    }

    private val _targetPriceInput = MutableStateFlow("")
    val targetPriceInput: StateFlow<String> = _targetPriceInput.asStateFlow()

    private val _stopLossPriceInput = MutableStateFlow("")
    val stopLossPriceInput: StateFlow<String> = _stopLossPriceInput.asStateFlow()

    private val _isTrailingInput = MutableStateFlow(false)
    val isTrailingInput: StateFlow<Boolean> = _isTrailingInput.asStateFlow()

    private val _trailingGapInput = MutableStateFlow("")
    val trailingGapInput: StateFlow<String> = _trailingGapInput.asStateFlow()

    // Post-trade ratings
    data class TradeRating(
        val symbol: String,
        val type: String,
        val shares: Double,
        val price: Double,
        val totalCost: Double,
        val sizeScore: Int,
        val typeScore: Int,
        val overallScore: Int,
        val sizeAdvice: String,
        val typeAdvice: String,
        val ratingText: String
    )

    private val _postTradeRating = MutableStateFlow<TradeRating?>(null)
    val postTradeRating: StateFlow<TradeRating?> = _postTradeRating.asStateFlow()

    // Global Leaderboard Sort Mode
    private val _leaderboardSortMode = MutableStateFlow("XP") // "XP" or "Discipline"
    val leaderboardSortMode: StateFlow<String> = _leaderboardSortMode.asStateFlow()

    // Global Leaderboard Flow - Reactive to sort mode
    val globalLeaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboardSortMode
        .flatMapLatest { mode ->
            val sortByField = if (mode == "XP") "xp" else "disciplineScore"
            leaderboardManager.getTopUsersFlow(sortByField)
        }
        .catch { e -> 
            android.util.Log.e("TradingViewModel", "Error in globalLeaderboard flow", e)
            emit(emptyList()) 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLeaderboardSort(mode: String) {
        _leaderboardSortMode.value = mode
    }

    fun shareDisciplineChallenge() {
        val statsVal = portfolioStats.value
        val score = statsVal.disciplineScore
        val text = "I just reached a $score/100 Discipline Score on Trade Lab! 🎯 Can you match my risk management skills? Join me in the arena: https://play.google.com/store/apps/details?id=com.ashwathai.tradelab #TradeLab #InvestSmart"
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = android.content.Intent.createChooser(intent, "Challenge a Friend")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareAppInvite() {
        val text = "🚀 Join me on Trade Lab! It's a realistic paper trading arena where we learn position sizing and risk management with virtual budgets. Download now and compete on the global leaderboard! https://play.google.com/store/apps/details?id=com.ashwathai.tradelab"
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = android.content.Intent.createChooser(intent, "Invite Friend to Trade Lab")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    // Derived State: The selected StockPrice details
    val selectedStock: StateFlow<StockPrice?> = combine(stockPrices, _selectedStockSymbol) { prices, symbol ->
        prices.find { it.symbol == symbol } ?: prices.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Derived State: Live combined portfolio values (Cash + Holding market values)
    val portfolioStats = combine(userProfile, holdings, stockPrices) { profile, activeHoldings, prices ->
        if (profile == null) return@combine PortfolioStats()

        var holdingsValue = 0.0
        var totalCostBasis = 0.0
        var todayPnL = 0.0
        var usedMargin = 0.0
        var phantomMarginPnL = 0.0
        val isLeverageUnlocked = profile.isPremium || profile.leverageUnlockedUntil > System.currentTimeMillis()

        for (holding in activeHoldings) {
            val liveStock = prices.find { it.symbol == holding.symbol }
            val livePrice = liveStock?.currentPrice ?: holding.averagePrice
            val prevClose = liveStock?.previousClose ?: livePrice
            
            val totalShares = holding.shares + holding.sharesT1
            val totalSharesAbs = kotlin.math.abs(totalShares)
            
            val convertedLivePrice = getConvertedStockPrice(livePrice, holding.symbol, profile.currency)
            val convertedPrevClose = getConvertedStockPrice(prevClose, holding.symbol, profile.currency)
            val convertedAvgPrice = getConvertedStockPrice(holding.averagePrice, holding.symbol, profile.currency)
            
            if (!holding.isDelivery) {
                val liveNotional = totalSharesAbs * convertedLivePrice
                val entryNotional = totalSharesAbs * convertedAvgPrice
                val blockedMargin = if (isLeverageUnlocked) entryNotional / 5.0 else entryNotional
                usedMargin += if (isLeverageUnlocked) liveNotional / 5.0 else liveNotional
                if (totalShares >= 0 && isLeverageUnlocked) {
                    phantomMarginPnL += entryNotional - blockedMargin
                }
            }

            if (totalShares >= 0) {
                // LONG POSITION
                holdingsValue += (totalShares * convertedLivePrice)
                totalCostBasis += (totalShares * convertedAvgPrice)
                
                val settledPnL = holding.shares * (convertedLivePrice - convertedPrevClose)
                val t1PnL = holding.sharesT1 * (convertedLivePrice - convertedAvgPrice)
                todayPnL += (settledPnL + t1PnL)
            } else {
                // SHORT POSITION (Shares are negative)
                // P/L = (Entry - Live) * |Shares|
                val openShortPnL = (convertedAvgPrice - convertedLivePrice) * totalSharesAbs
                val entryNotional = totalSharesAbs * convertedAvgPrice
                val blockedMargin = if (isLeverageUnlocked) entryNotional / 5.0 else entryNotional
                holdingsValue += blockedMargin + openShortPnL
                
                // For shorts, the "cost basis" is effectively the margin blocked, but let's keep it consistent
                // totalCostBasis += 0 // Margin is already in profile.cash as reduced amount
                
                // Today's P/L for Short = (YesterdayClose - Live) * |Shares|
                val dailyShortPnL = (convertedPrevClose - convertedLivePrice) * totalSharesAbs
                todayPnL += dailyShortPnL
            }
        }

        val totalValue = profile.cash + holdingsValue
        val totalProfitLoss = totalValue - profile.startingCash - phantomMarginPnL
        val profitLossPct = if (profile.startingCash > 0) (totalProfitLoss / profile.startingCash) * 100.0 else 0.0
        val openProfitLoss = holdingsValue - totalCostBasis
        
        // Today's percentage is relative to total value at start of day
        val valueAtStartOfDay = totalValue - todayPnL
        val todayPnLPct = if (valueAtStartOfDay > 0) (todayPnL / valueAtStartOfDay) * 100.0 else 0.0

        val buyingPower = if (isLeverageUnlocked) profile.cash * 5.0 else profile.cash

        PortfolioStats(
            totalValue = totalValue,
            cash = profile.cash,
            startingCash = profile.startingCash,
            holdingsValue = holdingsValue,
            totalPnL = totalProfitLoss,
            totalPnLPct = profitLossPct,
            todayPnL = todayPnL,
            todayPnLPct = todayPnLPct,
            openPnL = openProfitLoss,
            riskLevel = profile.riskPreference,
            currency = profile.currency,
            completedLevels = profile.completedLevels,
            isArcadeMode = profile.isArcadeMode,
            brokerageCredits = profile.brokerageCredits,
            indicatorsUnlockedUntil = profile.indicatorsUnlockedUntil,
            aiAuditCredits = profile.aiAuditCredits,
            isPremium = profile.isPremium,
            fnoTokens = profile.fnoTokens,
            portfolioResetsCount = profile.portfolioResetsCount,
            shouldShowShieldDialog = profile.shouldShowShieldDialog,
            usedMargin = usedMargin,
            buyingPower = buyingPower,
            disciplineScore = profile.disciplineScore,
            activeBadges = profile.activeBadges
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioStats())

    private val _systemNotificationFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val systemNotificationFlow: SharedFlow<String> = _systemNotificationFlow.asSharedFlow()

    init {
        // Synchronize repository mode state with ViewModel state
        repository.isSimulatedMode = _isSimulatedMode.value

        // Initialize values on app launch
        viewModelScope.launch(ioDispatcher) {
            repository.initializeDataIfEmpty()
            _isInitialized.value = true
            
            // Hyper-Gamification: Update Streak
            updateUserStreak()

            // Immediate initial fetch of live delayed prices
            try {
                repository.updateAllPricesFromYahoo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        loadAcademyAndMissionsData()

        // Epic 27: one-shot remote video manifest fetch (cache-first, no polling)
        videoManifestRepository.fetchIfNeeded()

        // Load theme from profile
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                profile?.let {
                    val mode = try { ThemeMode.valueOf(it.themeMode) } catch (_: Exception) { ThemeMode.SERIOUS }
                    if (_themeMode.value != mode) {
                        _themeMode.value = mode
                    }
                    if (_isStealthMode.value != it.isStealthMode) {
                        _isStealthMode.value = it.isStealthMode
                    }
                    if (_isZenMode.value != it.isZenMode) {
                        _isZenMode.value = it.isZenMode
                    }
                }
            }
        }

        // Sync stats to Firestore whenever they change significantly
        viewModelScope.launch(ioDispatcher) {
            var lastSyncedAt = 0L
            portfolioStats.collect { stats ->
                val now = System.currentTimeMillis()
                if (now - lastSyncedAt >= 60_000L) {
                    lastSyncedAt = now
                    syncStatsToFirestore(stats)
                }
            }
        }
    }

    fun startBackgroundTasks() {
        if (backgroundTasksStarted) return
        backgroundTasksStarted = true

        // Check commodities unlock expiration periodically
        viewModelScope.launch(defaultDispatcher) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                commoditiesUnlockTime?.let { unlockTime ->
                    // Updated to 3-hour expiry
                    if (System.currentTimeMillis() - unlockTime >= 3 * 3600 * 1000L) {
                        _commoditiesUnlocked.value = false
                        commoditiesUnlockTime = null
                        showFeedback("Your 3-hour Commodities Desk access has expired. Watch another ad to unlock!")
                    }
                }
            }
        }

        // Coroutine for live delayed price updates (Yahoo Finance API) every 15 seconds
        viewModelScope.launch(defaultDispatcher) {
            var marketOpenAlertSent = false
            var marketCloseAlertSent = false

            while (true) {
                // Local Market Alerts Logic (IST) using Calendar for API 24 compatibility
                val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = cal.get(java.util.Calendar.MINUTE)

                // 9:15 AM Market Open
                if (hour == 9 && minute == 15 && !marketOpenAlertSent) {
                    repository.addNotification("🔔 Market is OPEN! NSE/BSE active. Happy trading!")
                    _systemNotificationFlow.tryEmit("🔔 Market is OPEN! NSE/BSE active. Happy trading!")
                    marketOpenAlertSent = true
                } else if (hour != 9) {
                    marketOpenAlertSent = false
                }

                // 3:30 PM Market Close
                if (hour == 15 && minute == 30 && !marketCloseAlertSent) {
                    repository.addNotification("😴 Market is CLOSED. NSE/BSE session ended.")
                    _systemNotificationFlow.tryEmit("😴 Market is CLOSED. NSE/BSE session ended.")
                    marketCloseAlertSent = true
                } else if (hour != 15) {
                    marketCloseAlertSent = false
                }

                if (!_isSimulatedMode.value) {
                    try {
                        repository.updateAllPricesFromYahoo()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Intraday Square-off check
                try {
                    repository.checkAutoSquareOff()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                kotlinx.coroutines.delay(15000)
            }
        }

        // Coroutine for simulated price tick generation (prices fluctuate dynamically) every 2 seconds
        // Decoupled: Always run the simulation loop to provide live "wiggles"
        // In Live mode, it steers towards the real-world Anchor (targetPrice).
        viewModelScope.launch(defaultDispatcher) {
            while (true) {
                try {
                    repository.simulateMarketTick()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(2000)
            }
        }

        // Coroutine for real-world news sync every 60 seconds
        viewModelScope.launch(defaultDispatcher) {
            while (true) {
                if (!_isSimulatedMode.value) {
                    try {
                        val activeSymbol = _selectedStockSymbol.value
                        if (activeSymbol != null) {
                            repository.syncNewsFromYahoo(activeSymbol)
                        }
                        
                        // Rotate through top Indian indices news
                        val rotating = listOf("RELIANCE.NS", "TCS.NS", "HDFCBANK.NS", "AAPL", "TSLA").random()
                        repository.syncNewsFromYahoo(rotating)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                kotlinx.coroutines.delay(60000)
            }
        }
    }

    private fun syncStatsToFirestore(stats: PortfolioStats) {
        val profile = userProfile.value ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val name = profile.userName

        val xp = profile.xp
        
        viewModelScope.launch {
            leaderboardManager.syncUserStats(userId, name, xp, stats.totalValue, stats.disciplineScore)
        }
    }

    private fun loadAcademyAndMissionsData() {
        try {
            val assetManager = context.assets
            val moshiInstance = Moshi.Builder()
                // Removed KotlinJsonAdapterFactory to use Codegen for stability in Release/R8 builds
                .build()

            // Load Academy JSON (v2 Varsity-style curriculum with legacy fallback)
            val v2Courses = try {
                val v2Json = assetManager.open("academy_data_v2.json").bufferedReader().use { it.readText() }
                val v2Adapter = moshiInstance.adapter(AcademyContentV2::class.java)
                val parsed = v2Adapter.fromJson(v2Json)?.courses?.filter { it.chapters.isNotEmpty() } ?: emptyList()
                if (parsed.isEmpty()) {
                    android.util.Log.w("TradingViewModel", "Academy v2 JSON parsed but returned 0 courses.")
                }
                parsed
            } catch (e: Exception) {
                android.util.Log.e("TradingViewModel", "Failed to parse Academy v2 JSON: ${e.message}", e)
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e)
                emptyList()
            }

            if (v2Courses.isNotEmpty()) {
                _academyCourses.value = v2Courses.sortedBy { it.order }
                _quizModules.value = v2Courses.flatMap { course ->
                    course.chapters.map { chapter -> chapter.copy(courseId = course.id) }
                }
            } else {
                // Legacy fallback: map single-question modules into a synthetic "Stock Market Basics" course
                try {
                    val academyJson = assetManager.open("academy_data.json").bufferedReader().use { it.readText() }
                    val academyType = Types.newParameterizedType(List::class.java, QuizModule::class.java)
                    val academyAdapter = moshiInstance.adapter<List<QuizModule>>(academyType)
                    val legacyModules = academyAdapter.fromJson(academyJson) ?: emptyList()
                    val legacyChapters = legacyModules.map { it.toChapterModule(courseId = 1) }
                    _academyCourses.value = listOf(
                        AcademyCourse(
                            id = 1,
                            title = "Stock Market Basics",
                            tagline = "How markets work, what a stock is, and how trades settle.",
                            iconEmoji = "📈",
                            tier = "BEGINNER",
                            order = 1,
                            chapters = legacyChapters
                        )
                    )
                    _quizModules.value = legacyChapters
                } catch (e: Exception) {
                    android.util.Log.e("TradingViewModel", "Failed to parse legacy Academy JSON: ${e.message}", e)
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

            // Load Missions JSON
            try {
                val missionsJson = assetManager.open("missions_data.json").bufferedReader().use { it.readText() }
                val missionsType = Types.newParameterizedType(List::class.java, Mission::class.java)
                val missionsAdapter = moshiInstance.adapter<List<Mission>>(missionsType)
                _missionsList.value = missionsAdapter.fromJson(missionsJson) ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("TradingViewModel", "Failed to parse Missions JSON: ${e.message}", e)
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e)
            }
        } catch (e: Exception) {
            android.util.Log.e("TradingViewModel", "Fatal error in loadAcademyAndMissionsData: ${e.message}", e)
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun toggleTheme() {
        val next = when (_themeMode.value) {
            ThemeMode.SERIOUS -> ThemeMode.VIBRANT
            ThemeMode.VIBRANT -> ThemeMode.TERMINAL
            ThemeMode.TERMINAL -> ThemeMode.ARCADE
            ThemeMode.ARCADE -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SERIOUS
        }
        updateThemeMode(next)
    }

    fun updateThemeMode(next: ThemeMode) {
        _themeMode.value = next
        viewModelScope.launch {
            repository.updateThemeMode(next.name)
        }
        when (next) {
            ThemeMode.SERIOUS -> showFeedback("Serious Dark mode activated.")
            ThemeMode.VIBRANT -> showFeedback("Vibrant GenZ mode activated! 🚀")
            ThemeMode.LIGHT -> showFeedback("Light mode activated for clarity.")
            ThemeMode.TERMINAL -> showFeedback("Terminal Mode: System Override active. 📟")
            ThemeMode.ARCADE -> showFeedback("Arcade Mode: Insert Coin. 🕹️")
        }
    }

    fun toggleStealthMode() {
        val next = !_isStealthMode.value
        _isStealthMode.value = next
        viewModelScope.launch {
            repository.updateStealthMode(next)
        }
        if (next) showFeedback("Stealth Mode: Privacy Blur active.")
        else showFeedback("Stealth Mode: Privacy Blur disabled.")
    }

    fun toggleZenMode() {
        val next = !_isZenMode.value
        _isZenMode.value = next
        viewModelScope.launch {
            repository.updateZenMode(next)
        }
        if (next) showFeedback("Zen Mode: Focus active.")
        else showFeedback("Zen Mode: Focus disabled.")
    }

    // Tab switcher
    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun isMarketOpen(symbol: String): Boolean {
        return repository.isMarketOpen(symbol)
    }

        // Toggle between Live and Simulated modes
    fun toggleSimulationMode(enabled: Boolean) {
        _isSimulatedMode.value = enabled
        repository.isSimulatedMode = enabled
        viewModelScope.launch(ioDispatcher) {
            try {
                if (!enabled) {
                    // Fetch fresh Anchors immediately when switching to Live
                    repository.updateAllPricesFromYahoo()
                } else {
                    // Trigger a simulation tick immediately when switching to Simulated
                    repository.simulateMarketTick()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Stock selection
    fun selectStock(symbol: String?) {
        _selectedStockSymbol.value = symbol
        _tradeSharesInput.value = "" // clear input on switch
        _triggerPriceInput.value = ""
        _orderType.value = "Market"
    }

    // Set search query and trigger Live Autocomplete from Yahoo Finance API
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            viewModelScope.launch {
                _isSearching.value = true
                try {
                    // 1. Fetch autocompleted NSE/BSE and other tickers
                    val results = repository.searchYahooFinanceAutocomplete(trimmed)
                    _autocompleteResults.value = results

                    // 2. If user typed an exact single word ticker, try fetching live quote directly in background to pre-seed
                    if (!trimmed.contains(" ") && trimmed.all { it.isLetterOrDigit() || it == '.' || it == '-' }) {
                        val stock = repository.fetchLiveDelayedPrice(trimmed)
                        if (stock != null) {
                            repository.insertStockPrices(listOf(stock))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isSearching.value = false
                }
            }
        } else {
            _autocompleteResults.value = emptyList()
        }
    }

    // Set watchlist search query and trigger Live Autocomplete from Yahoo Finance API
    fun setWatchlistSearchQuery(query: String) {
        _watchlistSearchQuery.value = query
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            viewModelScope.launch {
                _isWatchlistSearching.value = true
                try {
                    val results = repository.searchYahooFinanceAutocomplete(trimmed)
                    _watchlistAutocompleteResults.value = results
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isWatchlistSearching.value = false
                }
            }
        } else {
            _watchlistAutocompleteResults.value = emptyList()
        }
    }

    // Dynamic SQLite/Room injection flow: fetch latest core quote from Yahoo, write it on the fly, and select it
    fun injectLiveStock(symbol: String, addToWatchlistId: Int? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                showFeedback("Fetching live market data for $symbol...")
                val stock = repository.fetchLiveDelayedPrice(symbol)
                if (stock != null) {
                    // Inject directly into SQLite
                    repository.insertStockPrices(listOf(stock))
                    
                    // Add to multi-watchlist if requested
                    if (addToWatchlistId != null) {
                        repository.addWatchlistItemV2(addToWatchlistId, stock.symbol)
                        // Trigger Flow refresh by re-selecting watchlist
                        selectWatchlist(addToWatchlistId)
                        showFeedback("${stock.symbol} added to watchlist and loaded live!")
                    } else {
                        showFeedback("${stock.companyName} (${stock.symbol}) loaded live!")
                    }
                    
                    // Instantly select this stock for trading/chart view
                    selectStock(stock.symbol)
                    onSuccess()
                } else {
                    showFeedback("Could not fetch live price for $symbol. Mode might be simulated.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showFeedback("Error loading live data: ${e.message}")
            }
        }
    }

    // Set shares input text
    fun setTradeShares(shares: String) {
        _tradeSharesInput.value = shares
    }

    fun setOrderType(type: String) {
        _orderType.value = type
    }

    fun setDeliveryMode(isDelivery: Boolean) {
        _isDelivery.value = isDelivery
    }

    fun setTriggerPrice(price: String) {
        _triggerPriceInput.value = price
    }

    fun setTargetPrice(price: String) {
        _targetPriceInput.value = price
    }

    fun setStopLossPrice(price: String) {
        _stopLossPriceInput.value = price
    }

    fun setIsTrailing(isTrailing: Boolean) {
        _isTrailingInput.value = isTrailing
    }

    fun setTrailingGap(gap: String) {
        _trailingGapInput.value = gap
    }

    fun clearTradeRating() {
        _postTradeRating.value = null
    }

    // Set Currency
    fun setCurrency(currency: String) {
        viewModelScope.launch {
            repository.updateCurrency(currency)
            showFeedback("Currency set to $currency")
        }
    }

    // Set Arcade Mode
    fun setArcadeMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setArcadeMode(enabled)
            val msg = if (enabled) "Fast-Forward Arcade Mode enabled!" else "Pure Realism Mode enabled!"
            showFeedback(msg)
        }
    }

    // Complete tutorial level and earn capital
    fun completeTutorial(levelId: Int, reward: Double) {
        viewModelScope.launch {
            repository.completeTutorialLevel(levelId, reward)
            val sym = if (portfolioStats.value.currency == "INR") "₹" else "$"
            showFeedback("Mission Completed! Earned $sym${String.format("%.0f", reward)}!")
            triggerConfetti()
        }
    }

    // Claim a completed mission's virtual cash reward (idempotent)
    fun claimMission(mission: Mission) {
        viewModelScope.launch {
            repository.claimMissionReward(mission.id, mission.title, mission.rewardAmt)
            val sym = if (portfolioStats.value.currency == "INR") "₹" else "$"
            showFeedback("Mission Reward Claimed! +$sym${String.format("%.0f", mission.rewardAmt)}")
            triggerConfetti()
        }
    }

    // Execute Buy
    fun executeBuy() {
        val symbol = _selectedStockSymbol.value ?: return
        val shares = _tradeSharesInput.value.toDoubleOrNull()
        if (shares == null || shares <= 0) {
            showFeedback("Please enter a valid positive number of shares")
            return
        }

        viewModelScope.launch {
            val shouldBlock = repository.incrementTrialActions()
            if (shouldBlock) {
                _showRegistrationGate.value = true
                return@launch
            }

            val stock = selectedStock.value ?: return@launch
            val orderTypeVal = _orderType.value

            if (orderTypeVal == "Market") {
                val result = repository.buyStock(symbol, shares, _isDelivery.value)
                result.onSuccess {
                    val statsVal = portfolioStats.value
                    val convertedPrice = getConvertedStockPrice(stock.currentPrice, stock.symbol, statsVal.currency)
                    val totalCost = shares * convertedPrice
                    val sizePct = if (statsVal.cash > 0) (totalCost / statsVal.cash) * 100 else 100.0

                    val sizeScore = when {
                        sizePct <= 12.0 -> 95
                        sizePct <= 25.0 -> 75
                        else -> 40
                    }
                    val typeScore = 65
                    val overallScore = (sizeScore + typeScore) / 2

                    val sizeAdvice = when {
                        sizePct <= 12.0 -> "Brilliant! Keeping individual trades under 10-15% of your wallet protects you from sudden drawdowns."
                        sizePct <= 25.0 -> "Caution: Allocating ${String.format("%.1f", sizePct)}% of your cash in a single asset increases concentration risk."
                        else -> "High Risk! You've allocated ${String.format("%.1f", sizePct)}% of your wallet in a single stock. A drop of 10% on this stock will wipe out a massive chunk of your total account."
                    }

                    val typeAdvice = "Tip: Using Market orders means you chase current spikes. Next time, try setting a Limit or GTT order to wait for the stock to drop to a discount support price!"

                    val ratingText = when {
                        overallScore >= 85 -> "A+ Disciplined Investor — Exceptional risk management!"
                        overallScore >= 70 -> "B- Developing Trader — Solid order selection, but watch your sizing carefully."
                        else -> "F High-Risk Speculator — Over-allocating on market momentum. This habit often leads to rapid real-world losses."
                    }

                    _postTradeRating.value = TradeRating(
                        symbol = symbol,
                        type = "BUY",
                        shares = shares,
                        price = convertedPrice,
                        totalCost = totalCost,
                        sizeScore = sizeScore,
                        typeScore = typeScore,
                        overallScore = overallScore,
                        sizeAdvice = sizeAdvice,
                        typeAdvice = typeAdvice,
                        ratingText = ratingText
                    )

                    val sym = if (statsVal.currency == "INR") "₹" else "$"
                    repository.addNotification("Bought $shares shares of $symbol at $sym${String.format("%.2f", convertedPrice)}")
                    showFeedback("Successfully bought $shares shares of $symbol!")
                    _tradeSharesInput.value = ""
                }.onFailure { error ->
                    showFeedback(error.message ?: "Transaction failed")
                }
            } else {
                val triggerPrice = _triggerPriceInput.value.toDoubleOrNull()
                if (triggerPrice == null || triggerPrice <= 0) {
                    showFeedback("Please enter a valid trigger price")
                    return@launch
                }

                val order = PendingOrder(
                    symbol = symbol,
                    type = "BUY",
                    orderType = orderTypeVal,
                    shares = shares,
                    triggerPrice = triggerPrice,
                    isDelivery = _isDelivery.value,
                    targetPrice = _targetPriceInput.value.toDoubleOrNull(),
                    stopLossPrice = _stopLossPriceInput.value.toDoubleOrNull(),
                    isTrailing = _isTrailingInput.value,
                    trailingGap = _trailingGapInput.value.toDoubleOrNull() ?: 0.0
                )
                repository.insertPendingOrder(order)
                val statsVal = portfolioStats.value
                val sym = if (statsVal.currency == "INR") "₹" else "$"
                repository.addNotification("Placed ${orderTypeVal} BUY Order: $shares shares of $symbol at $sym${String.format("%.2f", triggerPrice)}")
                showFeedback("Pending ${orderTypeVal} BUY order placed successfully!")
                _tradeSharesInput.value = ""
                _triggerPriceInput.value = ""
                _targetPriceInput.value = ""
                _stopLossPriceInput.value = ""
            }
        }
    }

    // Execute Sell
    fun executeSell() {
        val symbol = _selectedStockSymbol.value ?: return
        val shares = _tradeSharesInput.value.toDoubleOrNull()
        if (shares == null || shares <= 0) {
            showFeedback("Please enter a valid positive number of shares")
            return
        }

        viewModelScope.launch {
            val shouldBlock = repository.incrementTrialActions()
            if (shouldBlock) {
                _showRegistrationGate.value = true
                return@launch
            }

            val stock = selectedStock.value ?: return@launch
            val orderTypeVal = _orderType.value

            if (orderTypeVal == "Market") {
                val result = repository.sellStock(symbol, shares, _isDelivery.value)
                result.onSuccess {
                    val statsVal = portfolioStats.value
                    val convertedPrice = getConvertedStockPrice(stock.currentPrice, stock.symbol, statsVal.currency)
                    val totalCost = shares * convertedPrice
                    val sizePct = if (statsVal.cash > 0) (totalCost / statsVal.cash) * 100 else 100.0

                    val sizeScore = when {
                        sizePct <= 12.0 -> 95
                        sizePct <= 25.0 -> 75
                        else -> 40
                    }
                    val typeScore = 65
                    val overallScore = (sizeScore + typeScore) / 2

                    val sizeAdvice = when {
                        sizePct <= 12.0 -> "Brilliant position sizing! You maintain excellent liquid cash balance."
                        sizePct <= 25.0 -> "Moderate position scale. Be careful not to hold too few assets."
                        else -> "Large position sale. Glad you are locking in profits or cutting sizes safely!"
                    }

                    val typeAdvice = "Tip: Using Market orders might cost extra slippage on exit. Limit or GTT orders can lock in better target levels."

                    val ratingText = when {
                        overallScore >= 85 -> "A+ Disciplined Investor — Outstanding execution discipline!"
                        overallScore >= 70 -> "B- Developing Trader — Solid order, keep refining your limits."
                        else -> "C Standard Trader — Market selling is fine, but explore target GTT orders!"
                    }

                    _postTradeRating.value = TradeRating(
                        symbol = symbol,
                        type = "SELL",
                        shares = shares,
                        price = convertedPrice,
                        totalCost = totalCost,
                        sizeScore = sizeScore,
                        typeScore = typeScore,
                        overallScore = overallScore,
                        sizeAdvice = sizeAdvice,
                        typeAdvice = typeAdvice,
                        ratingText = ratingText
                    )

                    val sym = if (statsVal.currency == "INR") "₹" else "$"
                    repository.addNotification("Sold $shares shares of $symbol at $sym${String.format("%.2f", convertedPrice)}")
                    showFeedback("Successfully sold $shares shares of $symbol!")
                    _tradeSharesInput.value = ""
                }.onFailure { error ->
                    showFeedback(error.message ?: "Transaction failed")
                }
            } else {
                val triggerPrice = _triggerPriceInput.value.toDoubleOrNull()
                if (triggerPrice == null || triggerPrice <= 0) {
                    showFeedback("Please enter a valid trigger price")
                    return@launch
                }

                val order = PendingOrder(
                    symbol = symbol,
                    type = "SELL",
                    orderType = orderTypeVal,
                    shares = shares,
                    triggerPrice = triggerPrice,
                    isDelivery = _isDelivery.value,
                    targetPrice = _targetPriceInput.value.toDoubleOrNull(),
                    stopLossPrice = _stopLossPriceInput.value.toDoubleOrNull(),
                    isTrailing = _isTrailingInput.value,
                    trailingGap = _trailingGapInput.value.toDoubleOrNull() ?: 0.0
                )
                repository.insertPendingOrder(order)
                val statsVal = portfolioStats.value
                val sym = if (statsVal.currency == "INR") "₹" else "$"
                repository.addNotification("Placed ${orderTypeVal} SELL Order: $shares shares of $symbol at $sym${String.format("%.2f", triggerPrice)}")
                showFeedback("Pending ${orderTypeVal} SELL order placed successfully!")
                _tradeSharesInput.value = ""
                _triggerPriceInput.value = ""
                _targetPriceInput.value = ""
                _stopLossPriceInput.value = ""
            }
        }
    }

    // Toggle Ticker in Watchlist
    fun toggleWatchlist(symbol: String) {
        viewModelScope.launch {
            val added = repository.toggleWatchlist(symbol)
            if (added) {
                showFeedback("$symbol added to watchlist")
            } else {
                showFeedback("$symbol removed from watchlist")
            }
        }
    }

    // Toggle Watchlist V2
    fun toggleWatchlistV2(symbol: String) {
        viewModelScope.launch {
            val wid = _selectedWatchlistId.value
            val isPresent = repository.isWatchlistedV2(wid, symbol)
            if (isPresent) {
                repository.removeWatchlistItemV2(wid, symbol)
                showFeedback("$symbol removed from watchlist")
            } else {
                repository.addWatchlistItemV2(wid, symbol)
                showFeedback("$symbol added to watchlist")
            }
        }
    }

    fun selectWatchlist(id: Int) {
        _selectedWatchlistId.value = id
    }

    fun renameWatchlist(id: Int, name: String) {
        viewModelScope.launch {
            repository.renameWatchlist(id, name)
            showFeedback("Watchlist renamed to '$name'")
        }
    }

    fun addNewWatchlist(name: String) {
        viewModelScope.launch {
            val result = repository.addNewWatchlist(name)
            result.onSuccess { newId ->
                _selectedWatchlistId.value = newId
                showFeedback("Watchlist '$name' created!")
            }.onFailure { error ->
                showFeedback(error.message ?: "Failed to create watchlist")
            }
        }
    }

    fun deleteWatchlist(id: Int) {
        viewModelScope.launch {
            repository.deleteWatchlist(id)
            showFeedback("Watchlist deleted")
            val remaining = watchlistNames.value.filter { it.id != id }
            if (remaining.isNotEmpty()) {
                _selectedWatchlistId.value = remaining.first().id
            } else {
                _selectedWatchlistId.value = 1
            }
        }
    }

    // Delete Pending Order
    fun deletePendingOrder(id: Int) {
        viewModelScope.launch {
            repository.deletePendingOrder(id)
            showFeedback("Pending order cancelled successfully.")
        }
    }

    // Notification operations
    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
            showFeedback("Notifications cleared.")
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // Registration and Premium
    fun dismissRegistrationGate() {
        _showRegistrationGate.value = false
    }

    fun dismissPaywall() {
        _showPaywall.value = false
    }

    fun triggerPaywall() {
        _showPaywall.value = true
    }

    fun simulateRegister(name: String, email: String, phone: String = "") {
        viewModelScope.launch {
            repository.registerOrLogin(name, email, phone)
            _showRegistrationGate.value = false
            _hasDismissedAuthScreen.value = true
            showFeedback("Welcome, $name! Trial limits unlocked.")
        }
    }

    fun registerOrLogin(name: String, email: String, phone: String = "") {
        viewModelScope.launch {
            repository.registerOrLogin(name, email, phone)
            _showRegistrationGate.value = false
            _hasDismissedAuthScreen.value = true
            showFeedback("Welcome to TradeLab, $name!")
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, email, phone)
            showFeedback("Profile updated successfully!")
        }
    }

    fun continueAsGuest() {
        _hasDismissedAuthScreen.value = true
        showFeedback("Exploring TradeLab as Guest.")
    }

    fun openProBenefits() {
        _showProBenefits.value = true
    }

    fun acceptSimulationDisclaimer() {
        viewModelScope.launch {
            repository.acceptSimDisclaimer()
        }
    }

    private fun updateUserStreak() {
        viewModelScope.launch(ioDispatcher) {
            repository.updateUserStreak()
        }
    }

    // League System Logic
    val userLeague = userProfile
        .map { profile ->
            val xp = profile?.xp ?: 0
            when {
                xp >= 150000 -> "Diamond"
                xp >= 50000 -> "Platinum"
                xp >= 15000 -> "Gold"
                xp >= 5000 -> "Silver"
                else -> "Bronze"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Bronze")

    val xpToNextLeague = userProfile
        .map { profile ->
            val xp = profile?.xp ?: 0
            when {
                xp < 5000 -> 5000 - xp
                xp < 15000 -> 15000 - xp
                xp < 50000 -> 50000 - xp
                xp < 150000 -> 150000 - xp
                else -> 0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleWatchlistCompactMode() {
        viewModelScope.launch {
            repository.setWatchlistCompactMode(!isWatchlistCompactMode.value)
        }
    }

    fun toggleWatchlistSearch() {
        _isWatchlistSearchVisible.value = !_isWatchlistSearchVisible.value
        if (!_isWatchlistSearchVisible.value) {
            _watchlistSearchQuery.value = ""
        }
    }

    fun closeProBenefits() {
        _showProBenefits.value = false
    }

    fun openPremiumHub() {
        _showPremiumHub.value = true
    }

    fun closePremiumHub() {
        _showPremiumHub.value = false
    }

    fun openBillingFlow() {
        _showGoogleBilling.value = true
    }

    fun closeBillingFlow() {
        _showGoogleBilling.value = false
    }

    fun completePremiumPurchase() {
        viewModelScope.launch {
            repository.purchasePremium()
            _showGoogleBilling.value = false
            _showPaywall.value = false
            _showProBenefits.value = false
            showFeedback("Google Play: Subscription activated! ${SubscriptionConfig.FREE_TRIAL_DAYS} days free, then ${SubscriptionConfig.displayPrice()}/mo.")
            repository.addNotification("TradeLab Pro subscription activated! Enjoy zero-brokerage, unlimited watchlist sheets, and double quiz rewards.")
        }
    }

    fun simulatePremiumPurchase() {
        openBillingFlow()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _hasDismissedAuthScreen.value = false
            showFeedback("Logged out successfully.")
        }
    }

    // Gemini AI Advisor
    fun clearChat() {
        _aiChatLog.value = emptyList()
    }

    fun sendMessageToAi(message: String) {
        if (message.isBlank()) return
        
        _aiChatLog.value = _aiChatLog.value + Pair("User", message)
        _isAiLoading.value = true

        viewModelScope.launch {
            try {
                val statsVal = portfolioStats.value
                val isUserPremium = statsVal.isPremium
                
                if (!isUserPremium) {
                    // Check if they have credits
                    if (statsVal.aiAuditCredits <= 0) {
                        kotlinx.coroutines.delay(800)
                        val errorText = "⚠️ **AI ADVISOR CREDITS EXHAUSTED**\n\nYou have run out of AI consultation credits on the Free Tier.\n\nTo continue consulting your advisor, you can:\n1. 📺 **Watch a Sponsor Ad** on the Profile tab to claim +3 credits instantly.\n2. 👑 **Upgrade to Trade Lab Pro** for unlimited consultations and pro tools!"
                        _aiChatLog.value = _aiChatLog.value + Pair("AI Advisor", errorText)
                        _isAiLoading.value = false
                        return@launch
                    }
                    
                    // Consume 1 credit
                    repository.useAiAuditCredit()
                }

                val apiKey = BuildConfig.GEMINI_API_KEY
                
                // If not premium, OR premium without an API key -> run the Offline Simulated Advisor!
                if (!isUserPremium || apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                    kotlinx.coroutines.delay(1200) // Realistic simulated loading
                    
                    val risk = statsVal.riskLevel
                    val activeHoldings = holdings.value
                    
                    val responseText = buildString {
                        if (isUserPremium) {
                            append("✨ **PRO UNLIMITED AUDIT (Simulation Mode)**\n")
                            append("Welcome back to Trade Lab Pro! Since no custom Gemini API Key is configured in AI Studio Secrets, we are running in Unlimited Simulation Mode.\n\n")
                        } else {
                            append("📊 **OFFLINE PORTFOLIO DIAGNOSTIC AUDIT**\n")
                            append("Free Tier • Spent 1 AI Credit (${statsVal.aiAuditCredits - 1} remaining)\n\n")
                        }
                        
                        append("Your query: *\"$message\"*\n\n")
                        append("Here is your automated position-sizing and portfolio risk analysis:\n")
                        append("• **Risk Profile Setting:** $risk risk preference. Sizing discipline rules restrict any single trade size to 12% of total virtual capital.\n")
                        
                        if (activeHoldings.isEmpty()) {
                            append("• **Active Holdings:** None. Your account is 100% Cash. Under your $risk profile, we recommend starting small. Add top equities (such as TCS, RELIANCE, or INFOSYS) to your Watchlist and deploy defensive Limit orders at support zones.\n")
                        } else {
                            append("• **Active Exposures:** Detected ${activeHoldings.size} active positions:\n")
                            var hasOverSized = false
                            for (h in activeHoldings) {
                                val stockValue = h.shares * h.averagePrice
                                val pct = if (statsVal.totalValue > 0) (stockValue / statsVal.totalValue) * 100.0 else 0.0
                                if (pct > 12.0) {
                                    append("  - ⚠️ *Concentration risk on ${h.symbol}:* Sized at ${String.format("%.1f", pct)}% of total capital. This is over our recommended 12% limit! Trimming is highly advised to avoid outsized portfolio drops.\n")
                                    hasOverSized = true
                                } else {
                                    append("  - *Discipline on ${h.symbol}:* Nicely sized at ${String.format("%.1f", pct)}%. Excellent position control.\n")
                                }
                            }
                            if (hasOverSized) {
                                append("• **Actionable Advice:** Trim the oversized positions back to the 12% limit. Reallocate that freed-up capital into watchlist opportunities using Stop-Loss or Limit triggers.\n")
                            } else {
                                append("• **Actionable Advice:** Your active allocations are impeccably sized! Keep maintaining this standard to protect your capital against sudden market swings.\n")
                            }
                        }
                        
                        if (!isUserPremium) {
                            append("\n👑 **Want Real Live Custom AI?** Upgrade to Trade Lab Pro to query Google Gemini directly with custom API keys and access advanced institutional GTT order desking!")
                        } else {
                            append("\n💡 *Pro-Tip: You can add your personal Gemini API Key in AI Studio Secrets to enable real-time live web audits for your custom questions!*")
                        }
                    }
                    
                    _aiChatLog.value = _aiChatLog.value + Pair("AI Advisor", responseText)
                    _isAiLoading.value = false
                    return@launch
                }

                // If premium AND API Key is present -> Call Real Gemini API!
                val activeHoldings = holdings.value
                val holdingsStr = activeHoldings.joinToString { "${it.shares} shares of ${it.symbol} at avg $${it.averagePrice}" }
                
                val systemPrompt = "You are the Trade Lab AI Financial Advisor. Provide highly strategic, educational position sizing, risk management, and trading advice in Indian Rupees (INR) or US Dollars. Keep answers concise, direct, professional, and limited to 2-3 short paragraphs. User's Portfolio: Total Value: ${statsVal.totalValue}, Cash: ${statsVal.cash}, Risk Preference: ${statsVal.riskLevel}. Holdings: $holdingsStr."

                val promptText = "$systemPrompt\n\nUser Question: $message"
                val jsonBody = JSONObject()
                    .put(
                        "contents",
                        JSONArray().put(
                            JSONObject().put(
                                "parts",
                                JSONArray().put(JSONObject().put("text", promptText))
                            )
                        )
                    )
                    .toString()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toRequestBody(mediaType)

                val request = okhttp3.Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent")
                    .header("x-goog-api-key", apiKey)
                    .post(requestBody)
                    .build()

                val responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            "Error: ${response.code} ${response.message}"
                        } else {
                            val bodyString = response.body?.string() ?: ""
                            val matchResult = "\"text\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(bodyString)
                            val rawText = matchResult?.groupValues?.get(1) ?: "I analyzed your request. Please maintain solid risk limits and proper sizing."
                            rawText.replace("\\n", "\n").replace("\\\"", "\"")
                        }
                    }
                }

                _aiChatLog.value = _aiChatLog.value + Pair("AI Advisor", responseText)
            } catch (e: Exception) {
                _aiChatLog.value = _aiChatLog.value + Pair("AI Advisor", "Sorry, I encountered an issue connecting to Gemini: ${e.localizedMessage}. Please verify your API Key.")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Trigger simulation tick (Random market volatility fluctuation)
    fun simulateMarketMove() {
        viewModelScope.launch {
            repository.simulateMarketTick()
            showFeedback("Market prices updated!")
        }
    }

    // Reset Portfolio
    fun resetPortfolio(balance: Double, risk: String) {
        viewModelScope.launch {
            try {
                repository.resetPortfolio(balance, risk)
                showFeedback("Portfolio reset successfully!")
                triggerConfetti()
            } catch (e: Exception) {
                showFeedback(e.message ?: "Failed to reset portfolio.")
            }
        }
    }

    fun earnBrokerageCredits(amount: Int) {
        viewModelScope.launch {
            repository.earnBrokerageCredits(amount)
            showFeedback("Received $amount Brokerage Credits!")
            triggerConfetti()
        }
    }

    fun earnEmergencyCash(amount: Double) {
        viewModelScope.launch {
            repository.earnEmergencyCash(amount)
            val sym = if (portfolioStats.value.currency == "INR") "₹" else "$"
            showFeedback("Emergency fund received: $sym${String.format("%.2f", amount)}")
            triggerConfetti()
        }
    }

    fun earnAiAuditCredit() {
        viewModelScope.launch {
            repository.earnAiAuditCredit()
            showFeedback("Received 1 AI Audit Credit!")
            triggerConfetti()
        }
    }

    fun earnAiAuditCredits(amount: Int) {
        viewModelScope.launch {
            repeat(amount) {
                repository.earnAiAuditCredit()
            }
            showFeedback("Received $amount AI Advisor Credits!")
            triggerConfetti()
        }
    }

    fun unlockIntradaySession() {
        viewModelScope.launch {
            repository.unlockIntradaySession()
            showFeedback("Intraday Session Pass unlocked! Practice with 5x leverage.")
            triggerConfetti()
        }
    }

    fun setShouldShowShieldDialog(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateShieldDialogPreference(enabled)
        }
    }

    fun unlockPremiumIndicators(durationHours: Int) {
        viewModelScope.launch {
            repository.unlockPremiumIndicators(durationHours)
            showFeedback("Premium Technical Indicators unlocked for $durationHours hours!")
            triggerConfetti()
        }
    }

    fun unlockCommodities() {
        _commoditiesUnlocked.value = true
        commoditiesUnlockTime = System.currentTimeMillis()
        showFeedback("Commodities Desk successfully unlocked via sponsorship ad for 3 hours!")
        triggerConfetti()
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun showFeedback(msg: String) {
        _feedbackMessage.value = msg
    }

    fun triggerConfetti() {
        _confettiTrigger.value = System.currentTimeMillis()
    }

    fun earnFnoTokens(amount: Int) {
        viewModelScope.launch {
            repository.earnFnoTokens(amount)
            showFeedback("Received $amount F&O Free Trade Tokens! 🎫")
            triggerConfetti()
        }
    }

    fun useFnoToken(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.useFnoToken()
            onComplete(success)
        }
    }

    fun insertOrUpdateOptionPrice(optionSymbol: String, underlyingPrice: Double, strike: Double, isCall: Boolean) {
        viewModelScope.launch {
            repository.insertOrUpdateOptionPrice(optionSymbol, underlyingPrice, strike, isCall)
        }
    }

    fun executeOptionOrder(
        optionSymbol: String,
        isBuy: Boolean,
        shares: Double,
        premium: Double,
        strike: Double,
        isCall: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Ensure stock price entry exists so buy/sell works
            val underlyingLive = stockPrices.value.find { it.symbol == optionSymbol.substringBefore("_") }?.currentPrice
            repository.insertOrUpdateOptionPrice(optionSymbol, underlyingLive ?: strike, strike, isCall)

            val orderTypeVal = _orderType.value
            if (orderTypeVal != "Market") {
                val triggerPrice = _triggerPriceInput.value.toDoubleOrNull()
                if (triggerPrice == null || triggerPrice <= 0) {
                    showFeedback("Please enter a valid trigger price")
                    return@launch
                }
                val order = PendingOrder(
                    symbol = optionSymbol,
                    type = if (isBuy) "BUY" else "SELL",
                    orderType = orderTypeVal,
                    shares = shares,
                    triggerPrice = triggerPrice,
                    isDelivery = _isDelivery.value,
                    targetPrice = _targetPriceInput.value.toDoubleOrNull(),
                    stopLossPrice = _stopLossPriceInput.value.toDoubleOrNull(),
                    isTrailing = _isTrailingInput.value,
                    trailingGap = _trailingGapInput.value.toDoubleOrNull() ?: 0.0
                )
                repository.insertPendingOrder(order)
                showFeedback("Pending ${orderTypeVal} F&O ${if (isBuy) "BUY" else "SELL"} order placed for $optionSymbol @ ${String.format("%.2f", triggerPrice)}")
                onSuccess()
                return@launch
            }

            val result = if (isBuy) {
                repository.buyStock(optionSymbol, shares, _isDelivery.value)
            } else {
                repository.sellStock(optionSymbol, shares, _isDelivery.value)
            }
            
            result.onSuccess {
                showFeedback("${if (isBuy) "Bought" else "Sold"} ${shares.toInt()} contract shares of ${optionSymbol} @ ${premium} successfully!")
                repository.addNotification("Successfully executed option trade: ${optionSymbol}. Track your P/L on the Portfolio tab.")
                onSuccess()
            }.onFailure {
                showFeedback(it.message ?: "Failed to execute option trade")
            }
        }
    }

    private val _selectedTimeframe = MutableStateFlow("15m")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    fun setTimeframe(timeframe: String) {
        _selectedTimeframe.value = timeframe
    }

    private val _holdingsSortMode = MutableStateFlow("default")
    val holdingsSortMode: StateFlow<String> = _holdingsSortMode.asStateFlow()

    fun setHoldingsSortMode(mode: String) {
        _holdingsSortMode.value = mode
    }

    val adaptiveGuidanceText: StateFlow<String> = combine(holdings, stockPrices, userProfile) { h, p, u ->
        val totalValue = (u?.cash ?: 0.0) + h.sumOf { holding ->
            val price = p.find { it.symbol == holding.symbol }?.currentPrice ?: holding.averagePrice
            (holding.shares + holding.sharesT1) * price
        }
        val oversizedTrades = h.filter { holding ->
            val price = p.find { it.symbol == holding.symbol }?.currentPrice ?: 0.0
            val posValue = (holding.shares + holding.sharesT1) * price
            posValue > 0 && totalValue > 0 && posValue / totalValue > 0.12
        }
        when {
            oversizedTrades.isNotEmpty() -> "You've oversized positions in ${oversizedTrades.take(3).joinToString { it.symbol }}. Consider reducing to under 12% each."
            (u?.disciplineScore ?: 0) >= 90 -> "Excellent discipline! You're in the top tier of traders."
            h.isEmpty() -> "Start trading! Build a diversified portfolio across sectors."
            h.size <= 2 -> "Your portfolio is concentrated in ${h.size} position(s). Diversify across sectors."
            else -> "Practice disciplined sizing: Never allocate more than 12% of your account to a single ticker."
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Practice disciplined sizing.")

    fun exitPosition(symbol: String) {
        viewModelScope.launch {
            val holdingsList = holdings.value
            val holding = holdingsList.find { it.symbol == symbol }
            if (holding != null) {
                val totalShares = holding.shares + holding.sharesT1
                if (totalShares > 0) {
                    val result = repository.sellStock(symbol, totalShares, holding.isDelivery)
                    result.onSuccess {
                        showFeedback("Position squared off successfully!")
                        repository.addNotification("Squared off $totalShares shares of $symbol successfully.")
                    }.onFailure {
                        showFeedback(it.message ?: "Failed to square off position")
                    }
                }
            }
        }
    }

    fun exitAllFnoPositions() {
        viewModelScope.launch {
            val result = repository.exitAllFnoPositions()
            result.onSuccess {
                showFeedback(it)
            }.onFailure {
                showFeedback(it.message ?: "Failed to close F&O positions")
            }
        }
    }

    fun sellStock(symbol: String, shares: Double) {
        viewModelScope.launch {
            val result = repository.sellStock(symbol, shares)
            result.onSuccess {
                showFeedback("Position squared off successfully!")
                repository.addNotification("Squared off $shares shares of $symbol successfully.")
            }.onFailure {
                showFeedback(it.message ?: "Failed to square off position")
            }
        }
    }

    // Debug Actions (Track A Testing)
    fun debugSimulateAccountSnapshot() {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch {
                val current = portfolioStats.value.totalValue
                repository.recordAccountSnapshot(current)
                showFeedback("Debug: Snapshot recorded at $current")
            }
        }
    }
}

// Data holder for live portfolio aggregates
@JsonClass(generateAdapter = true)
data class PortfolioStats(
    val totalValue: Double = 25000.0,
    val cash: Double = 25000.0,
    val startingCash: Double = 25000.0,
    val holdingsValue: Double = 0.0,
    val totalPnL: Double = 0.0,
    val totalPnLPct: Double = 0.0,
    val todayPnL: Double = 0.0,
    val todayPnLPct: Double = 0.0,
    val openPnL: Double = 0.0,
    val riskLevel: String = "Moderate",
    val currency: String = "INR",
    val completedLevels: String = "",
    val isArcadeMode: Boolean = false,
    val brokerageCredits: Int = 300,
    val indicatorsUnlockedUntil: Long = 0L,
    val aiAuditCredits: Int = 3,
    val isPremium: Boolean = false,
    val fnoTokens: Int = 0,
    val portfolioResetsCount: Int = 0,
    val shouldShowShieldDialog: Boolean = true,
    val usedMargin: Double = 0.0,
    val buyingPower: Double = 0.0,
    val disciplineScore: Int = 75,
    val activeBadges: String = ""
)

@JsonClass(generateAdapter = true)
data class Lecture(
    val title: String,
    val content: String,
    val videoUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = ""
)

@JsonClass(generateAdapter = true)
data class ChapterModule(
    val id: Int,
    val courseId: Int = 0,
    val title: String,
    val topic: String,
    val rewardAmt: Double,
    val concept: String,
    val lectures: List<Lecture> = emptyList(),
    val quizzes: List<QuizQuestion> = emptyList(),
    val riskDisclosure: String = ""
)

@JsonClass(generateAdapter = true)
data class AcademyCourse(
    val id: Int,
    val title: String,
    val tagline: String = "",
    val iconEmoji: String = "",
    val tier: String = "BEGINNER",
    val order: Int = 0,
    val chapters: List<ChapterModule> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AcademyContentV2(
    val version: Int = 1,
    val courses: List<AcademyCourse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuizModule(
    val id: Int,
    val title: String,
    val topic: String,
    val rewardAmt: Double,
    val concept: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val lectures: List<Lecture> = emptyList()
) {
    fun toChapterModule(courseId: Int = 1): ChapterModule = ChapterModule(
        id = id,
        courseId = courseId,
        title = title,
        topic = topic,
        rewardAmt = rewardAmt,
        concept = concept,
        lectures = lectures,
        quizzes = listOf(QuizQuestion(question, options, correctIndex))
    )
}

@JsonClass(generateAdapter = true)
data class Mission(
    val id: Int,
    val title: String,
    val desc: String,
    val reward: String,
    val identifier: String,
    val rewardAmt: Double,
    val targetCount: Int? = null,
    val targetCourseId: Int? = null
)

object AcademyScoring {
    const val PASS_RATIO = 0.6f

    fun isCorrect(question: QuizQuestion, selected: Int): Boolean = selected == question.correctIndex

    fun score(quizzes: List<QuizQuestion>, answers: Map<Int, Int>): Pair<Int, Int> {
        val total = quizzes.size
        val correct = quizzes.indices.count { index ->
            val answer = answers[index]
            answer != null && answer == quizzes[index].correctIndex
        }
        return correct to total
    }

    fun passes(quizzes: List<QuizQuestion>, answers: Map<Int, Int>): Boolean {
        val (correct, total) = score(quizzes, answers)
        if (total == 0) return false
        return correct.toFloat() / total >= PASS_RATIO
    }

    fun tierFor(tier: String): String = when (tier.uppercase()) {
        "BEGINNER" -> "BEGINNER"
        "INTERMEDIATE" -> "INTERMEDIATE"
        "ADVANCED" -> "ADVANCED"
        else -> "BEGINNER"
    }

    fun tierReward(tier: String): Double = when (tier.uppercase()) {
        "BEGINNER" -> 500.0
        "INTERMEDIATE" -> 750.0
        "ADVANCED" -> 1000.0
        else -> 500.0
    }

    fun courseIcon(courseId: Int): Int = when (courseId) {
        1 -> R.drawable.ic_course_markets
        2 -> R.drawable.ic_course_technical
        3 -> R.drawable.ic_course_fundamental
        4 -> R.drawable.ic_course_derivatives
        5 -> R.drawable.ic_course_psychology
        6 -> R.drawable.ic_course_taxation
        else -> R.drawable.ic_course_markets
    }

    fun biasIcon(totalDelta: Double): Int = when {
        totalDelta >= 5.0 -> R.drawable.ic_status_trend_up
        totalDelta <= -5.0 -> R.drawable.ic_status_trend_down
        else -> R.drawable.ic_status_balance
    }

    // F&O Academic Gate: requires the first 3 beginner-curriculum chapters.
    // v2 academy awards chapter ids 101, 102, 103...; legacy ids 1, 2, 3 kept for backward compatibility.
    fun fnoAcademicUnlocked(completedSet: Set<String>): Boolean {
        val requiredV2 = setOf("101", "102", "103")
        val requiredLegacy = setOf("1", "2", "3")
        return completedSet.containsAll(requiredV2) || completedSet.containsAll(requiredLegacy)
    }

    fun unlockedCourseIds(courses: List<AcademyCourse>, completedIds: Set<String>): Set<Int> {
        if (courses.isEmpty()) return emptySet()
        val unlocked = mutableSetOf<Int>()
        courses.sortedBy { it.order }.forEachIndexed { index, course ->
            if (index == 0) {
                unlocked.add(course.id)
            } else {
                val previous = courses.sortedBy { it.order }[index - 1]
                if (previous.chapters.all { completedIds.contains(it.id.toString()) }) {
                    unlocked.add(course.id)
                }
            }
        }
        return unlocked
    }

    data class ValidationResult(val isValid: Boolean, val errors: List<String>)

    fun validateCourses(courses: List<AcademyCourse>): ValidationResult {
        val errors = mutableListOf<String>()
        val seenIds = mutableSetOf<Int>()

        courses.forEach { course ->
            if (course.chapters.isEmpty()) {
                errors.add("Course '${course.title}' (id ${course.id}) has zero chapters")
            }
            val requiresDisclosure = course.id == 4 || course.id == 6
            course.chapters.forEach { chapter ->
                if (!seenIds.add(chapter.id)) {
                    errors.add("Duplicate chapter id ${chapter.id}")
                }
                if (requiresDisclosure && chapter.riskDisclosure.isBlank()) {
                    errors.add("Chapter ${chapter.id} (course ${course.id}) is missing riskDisclosure")
                }
                if (chapter.lectures.isEmpty()) {
                    errors.add("Chapter ${chapter.id} has zero lectures")
                }
                if (chapter.lectures.size > 4) {
                    errors.add("Chapter ${chapter.id} has ${chapter.lectures.size} lectures (max 4)")
                }
                chapter.lectures.forEachIndexed { i, lecture ->
                    val words = lecture.content.trim().split(Regex("\\s+")).size
                    if (lecture.content.isBlank()) {
                        errors.add("Chapter ${chapter.id} lecture ${i + 1} is blank")
                    }
                }
                if (chapter.quizzes.isEmpty()) {
                    errors.add("Chapter ${chapter.id} has zero quiz questions")
                }
                if (chapter.quizzes.size !in 3..5) {
                    errors.add("Chapter ${chapter.id} has ${chapter.quizzes.size} questions (expected 3-5)")
                }
                chapter.quizzes.forEachIndexed { i, q ->
                    if (q.correctIndex < 0 || q.correctIndex >= q.options.size) {
                        errors.add("Chapter ${chapter.id} question ${i + 1} has invalid correctIndex ${q.correctIndex}")
                    }
                    if (q.options.size < 2) {
                        errors.add("Chapter ${chapter.id} question ${i + 1} has fewer than 2 options")
                    }
                    if (q.explanation.isBlank()) {
                        errors.add("Chapter ${chapter.id} question ${i + 1} is missing explanation")
                    }
                }
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    data class MissionEvaluation(
        val progress: Int,
        val target: Int,
        val isCompleted: Boolean
    )

    fun evaluateMission(
        mission: Mission,
        completedSet: Set<String>,
        academyCourses: List<AcademyCourse>,
        unlockedIds: Set<Int>,
        stats: PortfolioStats
    ): MissionEvaluation {
        return when (mission.identifier) {
            "has_traded" -> {
                val done = stats.startingCash != stats.cash || stats.holdingsValue > 0
                MissionEvaluation(if (done) 1 else 0, 1, done)
            }
            "completed_3_modules" -> {
                val progress = completedSet.size
                val target = mission.targetCount ?: 3
                MissionEvaluation(progress, target, progress >= target)
            }
            "has_calibrated" -> {
                val done = stats.riskLevel != "Moderate" || stats.startingCash != 25000.0
                MissionEvaluation(if (done) 1 else 0, 1, done)
            }
            "completed_course_1", "completed_fno_course" -> {
                val course = academyCourses.firstOrNull { it.id == mission.targetCourseId }
                if (course == null) {
                    MissionEvaluation(0, mission.targetCount ?: 1, false)
                } else {
                    val done = course.chapters.count { completedSet.contains(it.id.toString()) }
                    val target = course.chapters.size
                    MissionEvaluation(done, target, done >= target)
                }
            }
            "unlocked_advanced_course" -> {
                val done = unlockedIds.size > 1
                MissionEvaluation(if (done) 1 else 0, 1, done)
            }
            "earned_certificate" -> {
                val totalChapters = academyCourses.sumOf { it.chapters.size }
                val target = if (totalChapters > 0) totalChapters else (mission.targetCount ?: 1)
                val progress = completedSet.size
                MissionEvaluation(progress, target, progress >= target)
            }
            else -> MissionEvaluation(0, 1, false)
        }
    }
}
