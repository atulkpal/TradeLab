package com.ashwathai.tradelab.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ashwathai.tradelab.data.*
import com.ashwathai.tradelab.BuildConfig
import com.ashwathai.tradelab.shared.TradingHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = TradingRepository(database)

    // Dynamic Academy & Missions data from JSON
    private val _quizModules = MutableStateFlow<List<QuizModule>>(emptyList())
    val quizModules: StateFlow<List<QuizModule>> = _quizModules.asStateFlow()

    private val _missionsList = MutableStateFlow<List<Mission>>(emptyList())
    val missionsList: StateFlow<List<Mission>> = _missionsList.asStateFlow()

    // Dynamic Theme Mode
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

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

    // UI Interactive States
    private val _currentTab = MutableStateFlow("Portfolio") // Portfolio, Watchlist, Academy, Profile
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _commoditiesUnlocked = MutableStateFlow(false)
    val commoditiesUnlocked: StateFlow<Boolean> = _commoditiesUnlocked.asStateFlow()
    private var commoditiesUnlockTime: Long? = null

    private val _hasDismissedAuthScreen = MutableStateFlow(false)
    val hasDismissedAuthScreen: StateFlow<Boolean> = _hasDismissedAuthScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _autocompleteResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val autocompleteResults: StateFlow<List<SearchResult>> = _autocompleteResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

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

    // Order flow custom states
    private val _orderType = MutableStateFlow("Market") // Market, Limit, GTT
    val orderType: StateFlow<String> = _orderType.asStateFlow()

    private val _triggerPriceInput = MutableStateFlow("")
    val triggerPriceInput: StateFlow<String> = _triggerPriceInput.asStateFlow()

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

    // Derived State: The selected StockPrice details
    val selectedStock: StateFlow<StockPrice?> = combine(stockPrices, _selectedStockSymbol) { prices, symbol ->
        prices.find { it.symbol == symbol } ?: prices.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Derived State: Live combined portfolio values (Cash + Holding market values)
    val portfolioStats = combine(userProfile, holdings, stockPrices) { profile, activeHoldings, prices ->
        if (profile == null) return@combine PortfolioStats()

        var holdingsValue = 0.0
        var totalCostBasis = 0.0

        for (holding in activeHoldings) {
            val liveStock = prices.find { it.symbol == holding.symbol }
            val livePrice = liveStock?.currentPrice ?: holding.averagePrice
            val convertedLivePrice = getConvertedStockPrice(livePrice, holding.symbol, profile.currency)
            holdingsValue += (holding.shares * convertedLivePrice)
            totalCostBasis += (holding.shares * holding.averagePrice)
        }

        val totalValue = profile.cash + holdingsValue
        val totalProfitLoss = totalValue - profile.startingCash
        val profitLossPct = if (profile.startingCash > 0) (totalProfitLoss / profile.startingCash) * 100.0 else 0.0
        val openProfitLoss = holdingsValue - totalCostBasis

        PortfolioStats(
            totalValue = totalValue,
            cash = profile.cash,
            startingCash = profile.startingCash,
            holdingsValue = holdingsValue,
            totalPnL = totalProfitLoss,
            totalPnLPct = profitLossPct,
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
            portfolioResetsCount = profile.portfolioResetsCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioStats())

    init {
        // Check commodities unlock expiration periodically
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000)
                commoditiesUnlockTime?.let { unlockTime ->
                    if (System.currentTimeMillis() - unlockTime >= 3600 * 1000L) {
                        _commoditiesUnlocked.value = false
                        commoditiesUnlockTime = null
                        showFeedback("Your 1-hour Commodities Desk access has expired. Watch another ad to unlock!")
                    }
                }
            }
        }

        // Synchronize repository mode state with ViewModel state
        repository.isSimulatedMode = _isSimulatedMode.value

        // Initialize values on app launch
        viewModelScope.launch {
            repository.initializeDataIfEmpty()
            repository.updateCurrency("INR")
            
            // Immediate initial fetch of live delayed prices
            try {
                repository.updateAllPricesFromYahoo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Coroutine for live delayed price updates (Yahoo Finance API) every 15 seconds
        viewModelScope.launch {
            while (true) {
                if (!_isSimulatedMode.value) {
                    try {
                        repository.updateAllPricesFromYahoo()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                kotlinx.coroutines.delay(15000)
            }
        }

        // Coroutine for simulated price tick generation (prices fluctuate dynamically) every 2 seconds
        viewModelScope.launch {
            while (true) {
                if (_isSimulatedMode.value) {
                    try {
                        repository.simulateMarketTick()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                kotlinx.coroutines.delay(2000)
            }
        }

        loadAcademyAndMissionsData()
    }

    private fun loadAcademyAndMissionsData() {
        try {
            val assetManager = getApplication<Application>().assets
            val moshiInstance = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            // Load Academy JSON
            val academyJson = assetManager.open("academy_data.json").bufferedReader().use { it.readText() }
            val academyType = Types.newParameterizedType(List::class.java, QuizModule::class.java)
            val academyAdapter = moshiInstance.adapter<List<QuizModule>>(academyType)
            _quizModules.value = academyAdapter.fromJson(academyJson) ?: emptyList()

            // Load Missions JSON
            val missionsJson = assetManager.open("missions_data.json").bufferedReader().use { it.readText() }
            val missionsType = Types.newParameterizedType(List::class.java, Mission::class.java)
            val missionsAdapter = moshiInstance.adapter<List<Mission>>(missionsType)
            _missionsList.value = missionsAdapter.fromJson(missionsJson) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        if (next) {
            showFeedback("Ashwathey recommends sophisticated dark mode to shield your eyes.")
        } else {
            showFeedback("Light mode activated for visual clarity.")
        }
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
        viewModelScope.launch {
            try {
                if (enabled) {
                    repository.simulateMarketTick()
                } else {
                    repository.updateAllPricesFromYahoo()
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

    fun setTriggerPrice(price: String) {
        _triggerPriceInput.value = price
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
                val result = repository.buyStock(symbol, shares)
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
                    triggerPrice = triggerPrice
                )
                repository.insertPendingOrder(order)
                val statsVal = portfolioStats.value
                val sym = if (statsVal.currency == "INR") "₹" else "$"
                repository.addNotification("Placed ${orderTypeVal} BUY Order: $shares shares of $symbol at $sym${String.format("%.2f", triggerPrice)}")
                showFeedback("Pending ${orderTypeVal} BUY order placed successfully!")
                _tradeSharesInput.value = ""
                _triggerPriceInput.value = ""
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
                val result = repository.sellStock(symbol, shares)
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
                    triggerPrice = triggerPrice
                )
                repository.insertPendingOrder(order)
                val statsVal = portfolioStats.value
                val sym = if (statsVal.currency == "INR") "₹" else "$"
                repository.addNotification("Placed ${orderTypeVal} SELL Order: $shares shares of $symbol at $sym${String.format("%.2f", triggerPrice)}")
                showFeedback("Pending ${orderTypeVal} SELL order placed successfully!")
                _tradeSharesInput.value = ""
                _triggerPriceInput.value = ""
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

    fun simulateRegister(name: String, email: String) {
        viewModelScope.launch {
            repository.registerOrLogin(name, email)
            _showRegistrationGate.value = false
            _hasDismissedAuthScreen.value = true
            showFeedback("Welcome, $name! Trial limits unlocked.")
        }
    }

    fun registerOrLogin(name: String, email: String) {
        viewModelScope.launch {
            repository.registerOrLogin(name, email)
            _showRegistrationGate.value = false
            _hasDismissedAuthScreen.value = true
            showFeedback("Welcome to TradeLab, $name!")
        }
    }

    fun continueAsGuest() {
        _hasDismissedAuthScreen.value = true
        showFeedback("Exploring TradeLab as Guest.")
    }

    fun openProBenefits() {
        _showProBenefits.value = true
    }

    fun closeProBenefits() {
        _showProBenefits.value = false
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
            showFeedback("Google Play: Subscription activated! 15 days free, then ₹99/mo.")
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

                val client = okhttp3.OkHttpClient()
                val jsonBody = """
                    {
                        "contents": [{
                            "parts": [{"text": "$systemPrompt\n\nUser Question: $message"}]
                        }]
                    }
                """.trimIndent()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = okhttp3.RequestBody.create(
                    mediaType,
                    jsonBody
                )

                val request = okhttp3.Request.Builder()
                    .url("https://generativemodelsv1beta.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()

                val responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
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
        showFeedback("Commodities Desk successfully unlocked via sponsorship ad for 1 hour!")
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
            repository.insertOrUpdateOptionPrice(optionSymbol, strike, strike, isCall)
            
            val result = if (isBuy) {
                repository.buyStock(optionSymbol, shares)
            } else {
                repository.sellStock(optionSymbol, shares)
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
}

// Data holder for live portfolio aggregates
data class PortfolioStats(
    val totalValue: Double = 25000.0,
    val cash: Double = 25000.0,
    val startingCash: Double = 25000.0,
    val holdingsValue: Double = 0.0,
    val totalPnL: Double = 0.0,
    val totalPnLPct: Double = 0.0,
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
    val portfolioResetsCount: Int = 0
)

data class Lecture(
    val title: String,
    val content: String
)

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
)

data class Mission(
    val id: Int,
    val title: String,
    val desc: String,
    val reward: String,
    val identifier: String,
    val rewardAmt: Double
)
